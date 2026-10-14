package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("inbound_order")
public class InboundOrder implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 入库单号
     */
    private String orderNo;

    /**
     * 供货单位(供应商)ID
     */
    private Long partnerId;

    /**
     * 接收仓库ID
     */
    private Long warehouseId;

    /**
     * 创建员工ID
     */
    private Long employeeId;

    /**
     * 入库时间
     */
    private LocalDateTime inboundTime;

    /**
     * 备注
     */
    private String remark;

    private LocalDateTime createTime;
}
