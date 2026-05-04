package com.itranswarp.exchange.message.event;

import java.math.BigDecimal;

import com.itranswarp.exchange.enums.Direction;

/**
 * 订单请求事件
 */
public class OrderRequestEvent extends AbstractEvent {

    /**
     * 用户ID
     */
    public Long userId;

    /**
     * 方向
     */
    public Direction direction;

    /**
     * 价格
     */
    public BigDecimal price;

    /**
     * 数量
     */
    public BigDecimal quantity;

    @Override
    public String toString() {
        return "OrderRequestEvent [sequenceId=" + sequenceId + ", previousId=" + previousId + ", uniqueId=" + uniqueId
                + ", refId=" + refId + ", createdAt=" + createdAt + ", userId=" + userId + ", direction=" + direction
                + ", price=" + price + ", quantity=" + quantity + "]";
    }
}
