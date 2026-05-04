package com.itranswarp.exchange.match;

import java.math.BigDecimal;

/**
 * 订单键
 * 
 * 用于存储订单的键
 * 
 * 使用sequenceId和price作为键，保证订单按照价格和时间排序
 */
public record OrderKey(long sequenceId, BigDecimal price) {
}
