package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.Warehouse;
import com.warehouse.mapper.WarehouseMapper;
import com.warehouse.service.WarehouseService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {

    @Override
    public List<Warehouse> listActive() {
        return this.list(new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getStatus, "ACTIVE")
                .orderByAsc(Warehouse::getId));
    }
}
