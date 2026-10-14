package com.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OutboundItemDTO {
    @NotNull(message = "货品不能为空")
    private Long goodsId;

    @NotNull(message = "出库数量不能为空")
    @Min(value = 1, message = "出库数量必须大于0")
    private Integer quantity;

    private BigDecimal price;

    /**
     * 前端读取到的货品版本号，用于乐观锁校验
     */
    private Integer version;
}
