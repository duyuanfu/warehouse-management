package com.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsDTO {
    private Long id;

    @NotBlank(message = "SKU编码不能为空")
    private String skuCode;

    @NotBlank(message = "货品名称不能为空")
    private String name;

    private String spec;

    private Integer safetyStock;

    private Integer stockQuantity;

    @NotNull(message = "所属仓库不能为空")
    private Long warehouseId;

    private BigDecimal price;

    private Integer version;
}
