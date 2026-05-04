package com.itranswarp.exchange.message.event;

import java.math.BigDecimal;

import com.itranswarp.exchange.enums.AssetEnum;

/**
 * Transfer between users.
 * 
 * 转账事件
 */
public class  TransferEvent extends AbstractEvent {

    /**
     * 转出用户ID
     */
    public Long fromUserId;
    /**
     * 转入用户ID
     */
    public Long toUserId;   
    /**
     * 资产类型
     */
    public AssetEnum asset;
    /**
     * 转账金额
     */
    public BigDecimal amount;
    /**
     * 是否足够
     */
    public boolean sufficient;

    @Override
    public String toString() {
        return "TransferEvent [sequenceId=" + sequenceId + ", previousId=" + previousId + ", uniqueId=" + uniqueId
                + ", refId=" + refId + ", createdAt=" + createdAt + ", fromUserId=" + fromUserId + ", toUserId="
                + toUserId + ", asset=" + asset + ", amount=" + amount + ", sufficient=" + sufficient + "]";
    }
}
