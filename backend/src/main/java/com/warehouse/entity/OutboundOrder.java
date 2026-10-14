package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("outbound_order")
public class OutboundOrder implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 出库单号
     */
    private String orderNo;

    /**
     * 客户单位ID
     */
    private Long partnerId;

    /**
     * 发出仓库ID
     */
    private Long warehouseId;

    /**
     * 创建员工ID
     */
    private Long employeeId;

    /**
     * 出库时间
     */
    private LocalDateTime outboundTime;

    /**
     * 备注
     */
    private String remark;

    private LocalDateTime createTime;
}
