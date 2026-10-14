package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("check_order")
public class CheckOrder implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 盘点单号
     */
    private String checkNo;

    /**
     * 盘点仓库ID
     */
    private Long warehouseId;

    /**
     * 发起员工ID
     */
    private Long employeeId;

    /**
     * 盘点时间
     */
    private LocalDateTime checkTime;

    /**
     * 状态: IN_PROGRESS(盘点中) / COMPLETED(已完成)
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    private LocalDateTime createTime;
}
