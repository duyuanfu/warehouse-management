package com.warehouse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.Result;
import com.warehouse.dto.OutboundDTO;
import com.warehouse.service.OutboundOrderService;
import com.warehouse.vo.OutboundOrderVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/outbound")
public class OutboundController {

    @Autowired
    private OutboundOrderService outboundOrderService;

    @PostMapping("/create")
    public Result<Long> create(@RequestBody @Valid OutboundDTO dto) {
        Long id = outboundOrderService.createOutboundOrder(dto);
        return Result.success("出库单创建成功，库存已扣减", id);
    }

    @GetMapping("/page")
    public Result<Page<OutboundOrderVO>> page(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "orderNo", required = false) String orderNo) {
        return Result.success(outboundOrderService.queryOutboundPage(pageNo, pageSize, orderNo));
    }

    @GetMapping("/{id}")
    public Result<OutboundOrderVO> getDetail(@PathVariable("id") Long id) {
        return Result.success(outboundOrderService.getOutboundDetail(id));
    }
}
