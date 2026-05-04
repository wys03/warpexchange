package com.itranswarp.exchange.enums;

/**
 * 买卖方向
 */
public enum Direction {

    /**
     * 买入
     */
    BUY(1),

    /**
     * 卖出
     */
    SELL(0);

    /**
     * Direction的int值
     */
    public final int value;

    /**
     * 反向
     */
    public Direction negate() {
        return this == BUY ? SELL : BUY;
    }

    /**
     * 构造函数
     * @param value Direction的int值
     */
    Direction(int value) {
        this.value = value;
    }

    /**
     * 根据int值获取Direction
     * @param intValue  int值
     * @return
     */
    public static Direction of(int intValue) {
        if (intValue == 1) {
            return BUY;
        }
        if (intValue == 0) {
            return SELL;
        }
        throw new IllegalArgumentException("Invalid Direction value.");
    }
}
