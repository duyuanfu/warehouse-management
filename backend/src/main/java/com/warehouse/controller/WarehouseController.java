package com.warehouse.controller;

import com.warehouse.common.Result;
import com.warehouse.entity.Warehouse;
import com.warehouse.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @GetMapping("/list")
    public Result<List<Warehouse>> list() {
        return Result.success(warehouseService.listActive());
    }
}
