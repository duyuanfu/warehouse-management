package com.warehouse.controller;

import com.warehouse.common.Result;
import com.warehouse.entity.Partner;
import com.warehouse.service.PartnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/partner")
public class PartnerController {

    @Autowired
    private PartnerService partnerService;

    @GetMapping("/suppliers")
    public Result<List<Partner>> listSuppliers() {
        return Result.success(partnerService.listSuppliers());
    }

    @GetMapping("/customers")
    public Result<List<Partner>> listCustomers() {
        return Result.success(partnerService.listCustomers());
    }
}
