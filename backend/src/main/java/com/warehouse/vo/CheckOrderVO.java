package com.warehouse.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CheckOrderVO {
    private Long id;
    private String checkNo;
    private Long warehouseId;
    private String warehouseName;
    private Long employeeId;
    private String employeeName;
    private LocalDateTime checkTime;
    private String status;
    private String remark;
    private Integer totalItems;
    private Integer profitCount; // 盘盈项数
    private Integer lossCount;   // 盘亏项数
    private List<CheckDetailVO> items;
}
