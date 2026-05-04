package com.itranswarp.exchange.model.trade;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.itranswarp.exchange.enums.Direction;
import com.itranswarp.exchange.enums.OrderStatus;
import com.itranswarp.exchange.model.support.EntitySupport;

/**
 * 订单实体类
 */
@Entity
@Table(name = "orders")
public class OrderEntity implements EntitySupport, Comparable<OrderEntity> {

    /**
     * 订单唯一ID
     */
    @Id
    @Column(nullable = false, updatable = false)
    public Long id;

    /**
     * 序列号（用于保证交易顺序）
     */
    @Column(nullable = false, updatable = false)
    public long sequenceId;

    /**
     * 买卖方向 (BUY/SELL)
     */
    @Column(nullable = false, updatable = false, length = VAR_ENUM)
    public Direction direction;

    /**
     * 下单用户ID
     */
    @Column(nullable = false, updatable = false)
    public Long userId;

    /**
     * 当前状态
     * 反映当前成交情况
     */
    @Column(nullable = false, updatable = false, length = VAR_ENUM)
    public OrderStatus status;

    /**
     * 更新订单状态
     * @param unfilledQuantity  未成交数量
     * @param status            状态
     * @param updatedAt         更新时间
     */
    public void updateOrder(BigDecimal unfilledQuantity, OrderStatus status, long updatedAt) {
        this.version++;
        this.unfilledQuantity = unfilledQuantity;
        this.status = status;
        this.updatedAt = updatedAt;
        this.version++;
    }

    /**
     * 限价（想成交的价格）
     *  下单后不能修改
     */
    @Column(nullable = false, updatable = false, precision = PRECISION, scale = SCALE)
    public BigDecimal price;

    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    public long createdAt;

    /**
     * 更新时间
     */
    @Column(nullable = false, updatable = false)
    public long updatedAt;

    private int version;

    /**
     * 获取版本号
     * @return
     */
    @Transient
    @JsonIgnore
    public int getVersion() {
        return this.version;
    }

    /**
     * 原始数量
     *  下单后不能修改
     */
    @Column(nullable = false, updatable = false, precision = PRECISION, scale = SCALE)
    public BigDecimal quantity;

    /**
     * 未成交数量
     * 会随着成交逐渐减少
     */
    @Column(nullable = false, updatable = false, precision = PRECISION, scale = SCALE)
    public BigDecimal unfilledQuantity;

    /**
     * 复制订单
     * @return
     */
    @Nullable
    public OrderEntity copy() {
        OrderEntity entity = new OrderEntity();
        int ver = this.version;
        entity.status = this.status;
        entity.unfilledQuantity = this.unfilledQuantity;
        entity.updatedAt = this.updatedAt;
        if (ver != this.version) {
            return null;
        }

        entity.createdAt = this.createdAt;
        entity.direction = this.direction;
        entity.id = this.id;
        entity.price = this.price;
        entity.quantity = this.quantity;
        entity.sequenceId = this.sequenceId;
        entity.userId = this.userId;
        return entity;
    }

    /**
     * 判断两个订单是否相等
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof OrderEntity) {
            OrderEntity e = (OrderEntity) o;
            return this.id.longValue() == e.id.longValue();
        }
        return false;
    }

    /**
     * 获取订单的hash值
     * @return
     */
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    /**
     * 获取订单的字符串表示
     * @return
     */
    @Override
    public String toString() {
        return "OrderEntity [id=" + id + ", sequenceId=" + sequenceId + ", direction=" + direction + ", userId="
                + userId + ", status=" + status + ", price=" + price + ", createdAt=" + createdAt + ", updatedAt="
                + updatedAt + ", version=" + version + ", quantity=" + quantity + ", unfilledQuantity="
                + unfilledQuantity + "]";
    }

    /**
     * 按OrderID排序
     */
    @Override
    public int compareTo(OrderEntity o) {
        return Long.compare(this.id.longValue(), o.id.longValue());
    }
}
