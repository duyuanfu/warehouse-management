package com.warehouse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.Result;
import com.warehouse.dto.InboundDTO;
import com.warehouse.service.InboundOrderService;
import com.warehouse.vo.InboundOrderVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inbound")
public class InboundController {

    @Autowired
    private InboundOrderService inboundOrderService;

    @PostMapping("/create")
    public Result<Long> create(@RequestBody @Valid InboundDTO dto) {
        Long id = inboundOrderService.createInboundOrder(dto);
        return Result.success("入库单创建成功，库存已更新", id);
    }

    @GetMapping("/page")
    public Result<Page<InboundOrderVO>> page(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "orderNo", required = false) String orderNo) {
        return Result.success(inboundOrderService.queryInboundPage(pageNo, pageSize, orderNo));
    }

    @GetMapping("/{id}")
    public Result<InboundOrderVO> getDetail(@PathVariable("id") Long id) {
        return Result.success(inboundOrderService.getInboundDetail(id));
    }
}
