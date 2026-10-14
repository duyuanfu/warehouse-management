package com.warehouse.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GoodsVO {
    private Long id;
    private String skuCode;
    private String name;
    private String spec;
    private Integer safetyStock;
    private Integer stockQuantity;
    private Long warehouseId;
    private String warehouseName;
    private BigDecimal price;
    private String status;
    private Boolean isLowStock; // 是否低于安全库存预警
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
