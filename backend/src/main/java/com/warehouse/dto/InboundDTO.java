package com.warehouse.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class InboundDTO {
    @NotNull(message = "供应商不能为空")
    private Long partnerId;

    @NotNull(message = "入库仓库不能为空")
    private Long warehouseId;

    private Long employeeId;

    private String remark;

    @NotEmpty(message = "入库明细不能为空")
    private List<InboundItemDTO> items;
}
