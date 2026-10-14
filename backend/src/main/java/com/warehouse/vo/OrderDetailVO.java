package com.warehouse.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDetailVO {
    private Long id;
    private String orderType;
    private Long orderId;
    private Long goodsId;
    private String goodsName;
    private String goodsSku;
    private String goodsSpec;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
}
