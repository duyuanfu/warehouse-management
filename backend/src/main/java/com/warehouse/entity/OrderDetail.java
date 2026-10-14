package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_detail")
public class OrderDetail implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 单据类型: INBOUND(入库) / OUTBOUND(出库)
     */
    private String orderType;

    /**
     * 关联入库单/出库单ID
     */
    private Long orderId;

    /**
     * 货品ID
     */
    private Long goodsId;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 单价快照
     */
    private BigDecimal price;

    private LocalDateTime createTime;
}
