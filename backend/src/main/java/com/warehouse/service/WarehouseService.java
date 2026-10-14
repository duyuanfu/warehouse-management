package com.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.entity.Warehouse;

import java.util.List;

public interface WarehouseService extends IService<Warehouse> {
    List<Warehouse> listActive();
}
