package com.itranswarp.exchange.web.api;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itranswarp.exchange.ApiError;
import com.itranswarp.exchange.ApiException;
import com.itranswarp.exchange.ctx.UserContext;
import com.itranswarp.exchange.enums.AssetEnum;
import com.itranswarp.exchange.enums.UserType;
import com.itranswarp.exchange.message.event.TransferEvent;
import com.itranswarp.exchange.service.SendEventService;
import com.itranswarp.exchange.service.TradingEngineApiProxyService;
import com.itranswarp.exchange.support.AbstractApiController;
import com.itranswarp.exchange.util.IdUtil;

/**
 * Wallet APIs for demo deposit/withdraw.
 * 仅开放 USD 充提，BTC 一律禁止。
 */
@RestController
@RequestMapping("/api/wallet")
public class WalletApiController extends AbstractApiController {

    @Autowired
    private SendEventService sendEventService;

    @Autowired
    private TradingEngineApiProxyService tradingEngineApiProxyService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/deposit")
    public Map<String, Object> deposit(@RequestBody WalletRequest request) throws IOException {
        request.validate();
        Long userId = UserContext.getRequiredUserId();
        String transferId = IdUtil.generateUniqueId();
        enqueueTransfer(transferId, UserType.DEBT.getInternalUserId(), userId.longValue(), request.amount, false);
        return Map.of("result", Boolean.TRUE, "transferId", transferId);
    }

    @PostMapping("/withdraw")
    public Map<String, Object> withdraw(@RequestBody WalletRequest request) throws IOException {
        request.validate();
        Long userId = UserContext.getRequiredUserId();
        BigDecimal available = readAvailableUsd(userId);
        if (request.amount.compareTo(available) > 0) {
            throw new ApiException(ApiError.NO_ENOUGH_ASSET, "USD", "Insufficient available USD.");
        }
        String transferId = IdUtil.generateUniqueId();
        enqueueTransfer(transferId, userId.longValue(), UserType.DEBT.getInternalUserId(), request.amount, true);
        return Map.of("result", Boolean.TRUE, "transferId", transferId);
    }

    private void enqueueTransfer(String transferId, long fromUserId, long toUserId, BigDecimal amount, boolean sufficient) {
        TransferEvent event = new TransferEvent();
        event.uniqueId = transferId;
        event.fromUserId = Long.valueOf(fromUserId);
        event.toUserId = Long.valueOf(toUserId);
        event.asset = AssetEnum.USD;
        event.amount = amount;
        event.sufficient = sufficient;
        event.createdAt = System.currentTimeMillis();
        this.sendEventService.sendMessage(event);
    }

    private BigDecimal readAvailableUsd(Long userId) throws IOException {
        String json = tradingEngineApiProxyService.get("/internal/" + userId + "/assets");
        JsonNode root = objectMapper.readTree(json);
        JsonNode usd = root.get(AssetEnum.USD.name());
        if (usd == null || usd.isNull()) {
            return BigDecimal.ZERO.setScale(AssetEnum.SCALE);
        }
        JsonNode av = usd.get("available");
        if (av == null || av.isNull()) {
            return BigDecimal.ZERO.setScale(AssetEnum.SCALE);
        }
        return new BigDecimal(av.asText()).setScale(AssetEnum.SCALE);
    }

    static class WalletRequest {
        public AssetEnum asset;
        public BigDecimal amount;

        void validate() {
            if (asset == null) {
                throw new ApiException(ApiError.PARAMETER_INVALID, "asset", "Must specify asset.");
            }
            if (asset != AssetEnum.USD) {
                throw new ApiException(ApiError.PARAMETER_INVALID, "asset", "Only USD is allowed.");
            }
            if (amount == null) {
                throw new ApiException(ApiError.PARAMETER_INVALID, "amount", "Must specify amount.");
            }
            amount = amount.setScale(AssetEnum.SCALE);
            if (amount.signum() <= 0) {
                throw new ApiException(ApiError.PARAMETER_INVALID, "amount", "Must specify positive amount.");
            }
        }
    }
}
