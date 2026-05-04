package com.itranswarp.exchange.match;

import java.math.BigDecimal;

import com.itranswarp.exchange.model.trade.OrderEntity;

/**
 * 成交详情记录
 * @param price         价格
 * @param quantity      数量
 * @param takerOrder    买方订单
 * @param makerOrder    卖方订单
 */
public record MatchDetailRecord(BigDecimal price, BigDecimal quantity, OrderEntity takerOrder, OrderEntity makerOrder) {
}
