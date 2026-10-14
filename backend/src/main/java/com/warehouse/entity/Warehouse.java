package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("warehouse")
public class Warehouse implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 仓库名称
     */
    private String name;

    /**
     * 地址
     */
    private String address;

    /**
     * 状态: ACTIVE(启用) / INACTIVE(停用)
     */
    private String status;

    private LocalDateTime createTime;
}
