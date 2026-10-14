package com.warehouse.vo;

import lombok.Data;

@Data
public class CheckDetailVO {
    private Long id;
    private Long checkId;
    private Long goodsId;
    private String goodsName;
    private String goodsSku;
    private String goodsSpec;
    private Integer systemStock;
    private Integer actualStock;
    private Integer differenceStock;
    private String remark;
}
