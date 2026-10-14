package com.warehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.dto.OutboundDTO;
import com.warehouse.entity.OutboundOrder;
import com.warehouse.vo.OutboundOrderVO;

public interface OutboundOrderService extends IService<OutboundOrder> {
    Long createOutboundOrder(OutboundDTO dto);
    Page<OutboundOrderVO> queryOutboundPage(Integer pageNo, Integer pageSize, String orderNo);
    OutboundOrderVO getOutboundDetail(Long id);
}
