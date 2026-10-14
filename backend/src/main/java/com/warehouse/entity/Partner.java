package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("partner")
public class Partner implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 单位名称
     */
    private String name;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 单位类型: SUPPLIER(供应商) / CUSTOMER(客户)
     */
    private String type;

    /**
     * 地址
     */
    private String address;

    private LocalDateTime createTime;
}
