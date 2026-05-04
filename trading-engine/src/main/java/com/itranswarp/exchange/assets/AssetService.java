package com.itranswarp.exchange.assets;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.itranswarp.exchange.enums.AssetEnum;
import com.itranswarp.exchange.support.LoggerSupport;

/**
 * 资产服务
 *
 * 用户各币种可用/冻结余额
 */
@Component
public class AssetService extends LoggerSupport {

    /**
     * 用户ID -> 资产ID -> 资产[可用/冻结]
     */
    final ConcurrentMap<Long, ConcurrentMap<AssetEnum, Asset>> userAssets = new ConcurrentHashMap<>();

    /**
     * 获取资产
     * @param userId    用户ID
     * @param assetId   资产ID
     * @return
     */
    public Asset getAsset(Long userId, AssetEnum assetId) {
        ConcurrentMap<AssetEnum, Asset> assets = userAssets.get(userId);
        if (assets == null) {
            return null;
        }
        return assets.get(assetId);
    }

    /**
     * 获取所有资产
     * @param userId    用户ID
     * @return
     */
    public Map<AssetEnum, Asset> getAssets(Long userId) {
        Map<AssetEnum, Asset> assets = userAssets.get(userId);
        if (assets == null) {
            return Map.of();
        }
        return assets;
    }

    /**
     * 获取所有用户资产
     * @return
     */
    public ConcurrentMap<Long, ConcurrentMap<AssetEnum, Asset>> getUserAssets() {
        return this.userAssets;
    }

    /**
     * 冻结资产
     * @param userId    用户ID
     * @param assetId   资产ID
     * @param amount    冻结金额
     * @return
     */
    public boolean tryFreeze(Long userId, AssetEnum assetId, BigDecimal amount) {
        boolean ok = tryTransfer(Transfer.AVAILABLE_TO_FROZEN, userId, userId, assetId, amount, true);
        if (ok && logger.isDebugEnabled()) {
            logger.debug("freezed user {}, asset {}, amount {}", userId, assetId, amount);
        }
        return ok;
    }

    /**
     * 解冻资产
     * @param userId    用户ID
     * @param assetId   资产ID
     * @param amount    解冻金额
     */
    public void unfreeze(Long userId, AssetEnum assetId, BigDecimal amount) {
        if (!tryTransfer(Transfer.FROZEN_TO_AVAILABLE, userId, userId, assetId, amount, true)) {
            throw new RuntimeException(
                    "Unfreeze failed for user " + userId + ", asset = " + assetId + ", amount = " + amount);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("unfreezed user {}, asset {}, amount {}", userId, assetId, amount);
        }
    }

    /**
     * 转账操作（不检查余额）
     * @param type      转账类型
     * @param fromUser  转出用户
     * @param toUser    转入用户
     * @param assetId   资产ID
     * @param amount    转账金额
     */
    public void transfer(Transfer type, Long fromUser, Long toUser, AssetEnum assetId, BigDecimal amount) {
        if (!tryTransfer(type, fromUser, toUser, assetId, amount, true)) {
            throw new RuntimeException("Transfer failed for " + type + ", from user " + fromUser + " to user " + toUser
                    + ", asset = " + assetId + ", amount = " + amount);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("transfer asset {}, from {} => {}, amount {}", assetId, fromUser, toUser, amount);
        }
    }

    /**
     * 转账操作（检查余额）
     * @param type          转账类型
     * @param fromUser      转出用户
     * @param toUser        转入用户
     * @param assetId       资产ID
     * @param amount        转账金额
     * @param checkBalance  是否检查余额
     * @return
     */
    public boolean tryTransfer(Transfer type, Long fromUser, Long toUser, AssetEnum assetId, BigDecimal amount,
            boolean checkBalance) {
        // 金额为0时，直接返回true
        if (amount.signum() == 0) {
            return true;
        }
        // 
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Negative amount");
        }
        /**
         * 获取转出用户资产，如果用户资产不存在，则初始化一个空的资产
         * @param fromUser    转出用户
         * @param assetId     资产ID
         * @return 转出用户资产
         */
        Asset fromAsset = getAsset(fromUser, assetId);
        if (fromAsset == null) {
            fromAsset = initAssets(fromUser, assetId);
        }   
        /**
         * 获取转入用户资产，如果用户资产不存在，则初始化一个空的资产
         * @param toUser    转入用户
         * @param assetId     资产ID
         * @return 转入用户资产
         */
        Asset toAsset = getAsset(toUser, assetId);
        if (toAsset == null) {
            toAsset = initAssets(toUser, assetId);
        }
        return switch (type) {
            // 可用->可用
            case AVAILABLE_TO_AVAILABLE -> {
                // 需要检查余额且余额不足:
                if (checkBalance && fromAsset.available.compareTo(amount) < 0) {
                    // 余额不足，返回false
                    yield false;
                }
                // 源用户的可用资产减少:
                fromAsset.available = fromAsset.available.subtract(amount);
                // 目标用户的可用资产增加:
                toAsset.available = toAsset.available.add(amount);
                // 返回true
                yield true;
            }
            // 可用->冻结
            case AVAILABLE_TO_FROZEN -> {
                if (checkBalance && fromAsset.available.compareTo(amount) < 0) {
                    yield false;
                }
                fromAsset.available = fromAsset.available.subtract(amount);
                toAsset.frozen = toAsset.frozen.add(amount);
                yield true;
            }
            // 冻结->可用
            case FROZEN_TO_AVAILABLE -> {
                if (checkBalance && fromAsset.frozen.compareTo(amount) < 0) {
                    yield false;
                }
                fromAsset.frozen = fromAsset.frozen.subtract(amount);
                toAsset.available = toAsset.available.add(amount);
                yield true;
            }
            default -> {
                throw new IllegalArgumentException("invalid type: " + type);
            }
        };
    }

    /**
     * 初始化资产
     * 懒创建，当用户资产不存在时，初始化一个空的资产
     * @param userId    用户ID
     * @param assetId   资产ID
     * @return
     */
    private Asset initAssets(Long userId, AssetEnum assetId) {
        ConcurrentMap<AssetEnum, Asset> map = userAssets.get(userId);
        if (map == null) {
            map = new ConcurrentHashMap<>();
            userAssets.put(userId, map);
        }
        Asset zeroAsset = new Asset();
        map.put(assetId, zeroAsset);
        return zeroAsset;
    }

    /**
     * 调试输出
     */
    public void debug() {
        System.out.println("---------- assets ----------");
        List<Long> userIds = new ArrayList<>(userAssets.keySet());
        Collections.sort(userIds);
        for (Long userId : userIds) {
            System.out.println("  user " + userId + " ----------");
            Map<AssetEnum, Asset> assets = userAssets.get(userId);
            List<AssetEnum> assetIds = new ArrayList<>(assets.keySet());
            Collections.sort(assetIds);
            for (AssetEnum assetId : assetIds) {
                System.out.println("    " + assetId + ": " + assets.get(assetId));
            }
        }
        System.out.println("---------- // assets ----------");
    }
}
