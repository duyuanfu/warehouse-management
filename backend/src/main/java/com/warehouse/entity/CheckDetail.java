package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("check_detail")
public class CheckDetail implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 盘点单ID
     */
    private Long checkId;

    /**
     * 货品ID
     */
    private Long goodsId;

    /**
     * 系统库存
     */
    private Integer systemStock;

    /**
     * 实际库存
     */
    private Integer actualStock;

    /**
     * 盈亏数量 (actualStock - systemStock)
     */
    private Integer differenceStock;

    /**
     * 盈亏原因/备注
     */
    private String remark;

    private LocalDateTime createTime;
}
