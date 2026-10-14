package com.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.entity.Partner;

import java.util.List;

public interface PartnerService extends IService<Partner> {
    List<Partner> listSuppliers();
    List<Partner> listCustomers();
}
