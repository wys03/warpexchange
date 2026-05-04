package com.itranswarp.exchange.bean;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.itranswarp.exchange.ApiError;
import com.itranswarp.exchange.ApiException;
import com.itranswarp.exchange.enums.Direction;

/**
 * Order Request Bean
 * 订单请求Bean
 */
public class OrderRequestBean implements ValidatableBean {

    /**
     * 买卖方向
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
    public void validate() {
        /**
         * 验证方向不能为空
         */
        if (this.direction == null) {
            throw new ApiException(ApiError.PARAMETER_INVALID, "direction", "direction is required.");
        }
        /**
         * 验证价格必须为正数
         */
        if (this.price == null) {
            throw new ApiException(ApiError.PARAMETER_INVALID, "price", "price is required.");
        }
        this.price = this.price.setScale(2, RoundingMode.DOWN);
        if (this.price.signum() <= 0) {
            throw new ApiException(ApiError.PARAMETER_INVALID, "price", "price must be positive.");
        }
        /**
         * 验证数量必须为正数
         */
        if (this.quantity == null) {
            throw new ApiException(ApiError.PARAMETER_INVALID, "quantity", "quantity is required.");
        }
        this.quantity = this.quantity.setScale(2, RoundingMode.DOWN);
        if (this.quantity.signum() <= 0) {
            throw new ApiException(ApiError.PARAMETER_INVALID, "quantity", "quantity must be positive.");
        }
    }
}
