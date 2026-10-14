package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("goods")
public class Goods implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * 货品名称
     */
    private String name;

    /**
     * 规格型号
     */
    private String spec;

    /**
     * 安全库存
     */
    private Integer safetyStock;

    /**
     * 当前库存量
     */
    private Integer stockQuantity;

    /**
     * 存放仓库ID
     */
    private Long warehouseId;

    /**
     * 参考单价
     */
    private BigDecimal price;

    /**
     * 状态: NORMAL(正常) / WARNING(库存预警)
     */
    private String status;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
