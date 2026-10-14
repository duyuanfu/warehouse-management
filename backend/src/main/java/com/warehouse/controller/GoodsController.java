package com.warehouse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.Result;
import com.warehouse.dto.GoodsDTO;
import com.warehouse.service.GoodsService;
import com.warehouse.vo.GoodsVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("/page")
    public Result<Page<GoodsVO>> page(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "warehouseId", required = false) Long warehouseId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "onlyWarning", required = false) Boolean onlyWarning) {
        return Result.success(goodsService.queryGoodsPage(pageNo, pageSize, warehouseId, keyword, onlyWarning));
    }

    @GetMapping("/by-warehouse")
    public Result<List<GoodsVO>> listByWarehouse(@RequestParam("warehouseId") Long warehouseId) {
        return Result.success(goodsService.listGoodsByWarehouse(warehouseId));
    }

    @GetMapping("/{id}")
    public Result<GoodsVO> getDetail(@PathVariable("id") Long id) {
        return Result.success(goodsService.getGoodsDetail(id));
    }

    @PostMapping("/save")
    public Result<Long> saveOrUpdate(@RequestBody @Valid GoodsDTO goodsDTO) {
        Long id = goodsService.saveOrUpdateGoods(goodsDTO);
        return Result.success("保存成功", id);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        goodsService.deleteGoods(id);
        return Result.success("删除成功", null);
    }
}
