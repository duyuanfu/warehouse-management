package com.warehouse.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OutboundOrderVO {
    private Long id;
    private String orderNo;
    private Long partnerId;
    private String partnerName;
    private Long warehouseId;
    private String warehouseName;
    private Long employeeId;
    private String employeeName;
    private LocalDateTime outboundTime;
    private String remark;
    private Integer totalQuantity;
    private List<OrderDetailVO> items;
}
