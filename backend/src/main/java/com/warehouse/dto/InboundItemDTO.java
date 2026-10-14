package com.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InboundItemDTO {
    @NotNull(message = "货品不能为空")
    private Long goodsId;

    @NotNull(message = "入库数量不能为空")
    @Min(value = 1, message = "入库数量必须大于0")
    private Integer quantity;

    private BigDecimal price;

    /**
     * 前端读取到的货品版本号，用于乐观锁防并发冲突
     */
    private Integer version;
}
