package com.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.warehouse.entity.OutboundOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OutboundOrderMapper extends BaseMapper<OutboundOrder> {
}
