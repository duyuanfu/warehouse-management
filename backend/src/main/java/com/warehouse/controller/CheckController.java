package com.warehouse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.Result;
import com.warehouse.dto.CheckDTO;
import com.warehouse.service.CheckOrderService;
import com.warehouse.vo.CheckOrderVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/check")
public class CheckController {

    @Autowired
    private CheckOrderService checkOrderService;

    @PostMapping("/create")
    public Result<Long> createAndComplete(@RequestBody @Valid CheckDTO dto) {
        Long id = checkOrderService.createAndCompleteCheckOrder(dto);
        return Result.success("盘点已完成，盘点记录已保存", id);
    }

    @GetMapping("/page")
    public Result<Page<CheckOrderVO>> page(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "checkNo", required = false) String checkNo) {
        return Result.success(checkOrderService.queryCheckPage(pageNo, pageSize, checkNo));
    }

    @GetMapping("/{id}")
    public Result<CheckOrderVO> getDetail(@PathVariable("id") Long id) {
        return Result.success(checkOrderService.getCheckDetail(id));
    }
}
