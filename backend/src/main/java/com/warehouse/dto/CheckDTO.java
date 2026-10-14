package com.warehouse.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CheckDTO {
    @NotNull(message = "盘点仓库不能为空")
    private Long warehouseId;

    private Long employeeId;

    private String remark;

    @NotEmpty(message = "盘点明细不能为空")
    private List<CheckItemDTO> items;
}
