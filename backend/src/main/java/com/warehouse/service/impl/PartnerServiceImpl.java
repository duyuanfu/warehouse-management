package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.Partner;
import com.warehouse.mapper.PartnerMapper;
import com.warehouse.service.PartnerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartnerServiceImpl extends ServiceImpl<PartnerMapper, Partner> implements PartnerService {

    @Override
    public List<Partner> listSuppliers() {
        return this.list(new LambdaQueryWrapper<Partner>()
                .eq(Partner::getType, "SUPPLIER")
                .orderByAsc(Partner::getId));
    }

    @Override
    public List<Partner> listCustomers() {
        return this.list(new LambdaQueryWrapper<Partner>()
                .eq(Partner::getType, "CUSTOMER")
                .orderByAsc(Partner::getId));
    }
}
