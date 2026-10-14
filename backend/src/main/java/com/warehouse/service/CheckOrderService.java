package com.warehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.dto.CheckDTO;
import com.warehouse.entity.CheckOrder;
import com.warehouse.vo.CheckOrderVO;

public interface CheckOrderService extends IService<CheckOrder> {
    Long createAndCompleteCheckOrder(CheckDTO dto);
    Page<CheckOrderVO> queryCheckPage(Integer pageNo, Integer pageSize, String checkNo);
    CheckOrderVO getCheckDetail(Long id);
}
