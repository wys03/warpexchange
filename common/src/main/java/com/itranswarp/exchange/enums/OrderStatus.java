package com.itranswarp.exchange.enums;

/**
 * 订单状态
 */
public enum OrderStatus {

    /**
     * 等待成交 (unfilledQuantity == quantity)
     */
    PENDING(false),

    /**
     * 完全成交 (unfilledQuantity = 0)
     */
    FULLY_FILLED(true),

    /**
     * 部分成交 (quantity > unfilledQuantity > 0)
     */
    PARTIAL_FILLED(false),

    /**
     * 部分成交后取消 (quantity > unfilledQuantity > 0)
     */
    PARTIAL_CANCELLED(true),

    /**
     * 完全取消 (unfilledQuantity == quantity)
     */
    FULLY_CANCELLED(true);

    /**
     * 是否终态
     * 终态订单会被送去 异步落库（TradingEngineService 里 closedOrders 等逻辑）
     */
    public final boolean isFinalStatus;

    OrderStatus(boolean isFinalStatus) {
        this.isFinalStatus = isFinalStatus;
    }
}
