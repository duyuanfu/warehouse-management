package com.warehouse.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OutboundDTO {
    @NotNull(message = "客户不能为空")
    private Long partnerId;

    @NotNull(message = "出库仓库不能为空")
    private Long warehouseId;

    private Long employeeId;

    private String remark;

    @NotEmpty(message = "出库明细不能为空")
    private List<OutboundItemDTO> items;
}
