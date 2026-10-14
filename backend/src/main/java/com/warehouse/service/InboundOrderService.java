package com.warehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.dto.InboundDTO;
import com.warehouse.entity.InboundOrder;
import com.warehouse.vo.InboundOrderVO;

public interface InboundOrderService extends IService<InboundOrder> {
    Long createInboundOrder(InboundDTO dto);
    Page<InboundOrderVO> queryInboundPage(Integer pageNo, Integer pageSize, String orderNo);
    InboundOrderVO getInboundDetail(Long id);
}
