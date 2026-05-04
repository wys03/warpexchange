package com.itranswarp.exchange.assets;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 资产
 */
public class Asset {

    /**
     * 可用
     */
    BigDecimal available;

    /**
     * 冻结
     */
    BigDecimal frozen;

    public Asset() {
        this(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public Asset(BigDecimal available, BigDecimal frozen) {
        this.available = available;
        this.frozen = frozen;
    }

    public BigDecimal getAvailable() {
        return available;
    }

    public BigDecimal getFrozen() {
        return frozen;
    }

    /**
     * 总额   = 可用 + 冻结
     * @return
     */
    @JsonIgnore
    public BigDecimal getTotal() {
        return available.add(frozen);
    }

    @Override
    public String toString() {
        return String.format("[available=%04.2f, frozen=%02.2f]", available, frozen);
    }
}
