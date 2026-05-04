package com.itranswarp.exchange.assets;

/**
 * 转账类型
 */
public enum Transfer {

    /**
     * 可用->可用
     */
    AVAILABLE_TO_AVAILABLE,

    /**
     * 可用->冻结
     */
    AVAILABLE_TO_FROZEN,

    /**
     * 冻结->可用
     */
    FROZEN_TO_AVAILABLE;
}
