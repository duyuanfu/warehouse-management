package com.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckItemDTO {
    @NotNull(message = "货品不能为空")
    private Long goodsId;

    @NotNull(message = "实际盘点库存不能为空")
    @Min(value = 0, message = "实际盘点库存不能小于0")
    private Integer actualStock;

    private String remark;
}
