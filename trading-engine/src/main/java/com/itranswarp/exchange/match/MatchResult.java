package com.itranswarp.exchange.match;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.itranswarp.exchange.model.trade.OrderEntity;

/**
 * 成交结果
 */
public class MatchResult {

    /**
     * 买方订单
     */
    public final OrderEntity takerOrder;
    /**
     * 成交详情列表
     */
    public final List<MatchDetailRecord> matchDetails = new ArrayList<>();

    public MatchResult(OrderEntity takerOrder) {
        this.takerOrder = takerOrder;
    }

    public void add(BigDecimal price, BigDecimal matchedQuantity, OrderEntity makerOrder) {
        matchDetails.add(new MatchDetailRecord(price, matchedQuantity, this.takerOrder, makerOrder));
    }

    @Override
    public String toString() {
        if (matchDetails.isEmpty()) {
            return "no matched.";
        }
        return matchDetails.size() + " matched: "
                + String.join(", ", this.matchDetails.stream().map(MatchDetailRecord::toString).toArray(String[]::new));
    }
}
