package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.common.BusinessException;
import com.warehouse.dto.CheckDTO;
import com.warehouse.dto.CheckItemDTO;
import com.warehouse.entity.*;
import com.warehouse.mapper.*;
import com.warehouse.service.CheckOrderService;
import com.warehouse.service.GoodsService;
import com.warehouse.vo.CheckDetailVO;
import com.warehouse.vo.CheckOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CheckOrderServiceImpl extends ServiceImpl<CheckOrderMapper, CheckOrder> implements CheckOrderService {

    @Autowired
    private CheckDetailMapper checkDetailMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAndCompleteCheckOrder(CheckDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("盘点货品明细不能为空");
        }

        Warehouse warehouse = warehouseMapper.selectById(dto.getWarehouseId());
        if (warehouse == null) {
            throw new BusinessException("盘点仓库不存在");
        }

        // 生成盘点单号: PD + yyyyMMddHHmmss + 3位随机数
        String checkNo = "PD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", new Random().nextInt(1000));

        CheckOrder checkOrder = new CheckOrder();
        checkOrder.setCheckNo(checkNo);
        checkOrder.setWarehouseId(dto.getWarehouseId());
        checkOrder.setEmployeeId(dto.getEmployeeId() == null ? 1L : dto.getEmployeeId());
        checkOrder.setCheckTime(LocalDateTime.now());
        checkOrder.setStatus("COMPLETED");
        checkOrder.setRemark(dto.getRemark());
        this.save(checkOrder);

        Long checkId = checkOrder.getId();

        // 处理明细并更新货品库存
        for (CheckItemDTO item : dto.getItems()) {
            Goods goods = goodsMapper.selectById(item.getGoodsId());
            if (goods == null) {
                throw new BusinessException("货品ID " + item.getGoodsId() + " 不存在");
            }

            int systemStock = goods.getStockQuantity() != null ? goods.getStockQuantity() : 0;
            int actualStock = item.getActualStock();
            int diff = actualStock - systemStock;

            CheckDetail detail = new CheckDetail();
            detail.setCheckId(checkId);
            detail.setGoodsId(goods.getId());
            detail.setSystemStock(systemStock);
            detail.setActualStock(actualStock);
            detail.setDifferenceStock(diff);
            detail.setRemark(item.getRemark());
            checkDetailMapper.insert(detail);

            // 更新库存至实际库存，并递增版本号
            goods.setStockQuantity(actualStock);
            int safety = goods.getSafetyStock() != null ? goods.getSafetyStock() : 0;
            goods.setStatus(actualStock <= safety ? "WARNING" : "NORMAL");
            // 通过 updateById 会自动触发乐观锁 @Version version 递增
            goodsMapper.updateById(goods);
        }

        return checkId;
    }

    @Override
    public Page<CheckOrderVO> queryCheckPage(Integer pageNo, Integer pageSize, String checkNo) {
        Page<CheckOrder> page = new Page<>(pageNo == null ? 1 : pageNo, pageSize == null ? 10 : pageSize);
        LambdaQueryWrapper<CheckOrder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(checkNo)) {
            wrapper.like(CheckOrder::getCheckNo, checkNo);
        }
        wrapper.orderByDesc(CheckOrder::getId);

        Page<CheckOrder> orderPage = this.page(page, wrapper);

        Map<Long, String> warehouseMap = warehouseMapper.selectList(null).stream()
                .collect(Collectors.toMap(Warehouse::getId, Warehouse::getName, (k1, k2) -> k1));
        Map<Long, String> employeeMap = employeeMapper.selectList(null).stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName, (k1, k2) -> k1));

        Page<CheckOrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        List<CheckOrderVO> voList = orderPage.getRecords().stream().map(order -> {
            CheckOrderVO vo = new CheckOrderVO();
            vo.setId(order.getId());
            vo.setCheckNo(order.getCheckNo());
            vo.setWarehouseId(order.getWarehouseId());
            vo.setWarehouseName(warehouseMap.getOrDefault(order.getWarehouseId(), "未知仓库"));
            vo.setEmployeeId(order.getEmployeeId());
            vo.setEmployeeName(employeeMap.getOrDefault(order.getEmployeeId(), "系统管理员"));
            vo.setCheckTime(order.getCheckTime());
            vo.setStatus(order.getStatus());
            vo.setRemark(order.getRemark());

            List<CheckDetail> details = checkDetailMapper.selectList(new LambdaQueryWrapper<CheckDetail>()
                    .eq(CheckDetail::getCheckId, order.getId()));
            vo.setTotalItems(details.size());
            vo.setProfitCount((int) details.stream().filter(d -> d.getDifferenceStock() > 0).count());
            vo.setLossCount((int) details.stream().filter(d -> d.getDifferenceStock() < 0).count());
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public CheckOrderVO getCheckDetail(Long id) {
        CheckOrder order = this.getById(id);
        if (order == null) {
            throw new BusinessException("盘点单不存在");
        }

        Warehouse warehouse = warehouseMapper.selectById(order.getWarehouseId());
        Employee employee = employeeMapper.selectById(order.getEmployeeId());

        CheckOrderVO vo = new CheckOrderVO();
        vo.setId(order.getId());
        vo.setCheckNo(order.getCheckNo());
        vo.setWarehouseId(order.getWarehouseId());
        vo.setWarehouseName(warehouse != null ? warehouse.getName() : "");
        vo.setEmployeeId(order.getEmployeeId());
        vo.setEmployeeName(employee != null ? employee.getName() : "");
        vo.setCheckTime(order.getCheckTime());
        vo.setStatus(order.getStatus());
        vo.setRemark(order.getRemark());

        List<CheckDetail> details = checkDetailMapper.selectList(new LambdaQueryWrapper<CheckDetail>()
                .eq(CheckDetail::getCheckId, order.getId()));

        int profitCount = 0;
        int lossCount = 0;
        List<CheckDetailVO> itemVOList = new ArrayList<>();
        for (CheckDetail d : details) {
            Goods goods = goodsMapper.selectById(d.getGoodsId());
            CheckDetailVO itemVO = new CheckDetailVO();
            itemVO.setId(d.getId());
            itemVO.setCheckId(d.getCheckId());
            itemVO.setGoodsId(d.getGoodsId());
            itemVO.setSystemStock(d.getSystemStock());
            itemVO.setActualStock(d.getActualStock());
            itemVO.setDifferenceStock(d.getDifferenceStock());
            itemVO.setRemark(d.getRemark());

            if (goods != null) {
                itemVO.setGoodsName(goods.getName());
                itemVO.setGoodsSku(goods.getSkuCode());
                itemVO.setGoodsSpec(goods.getSpec());
            }
            if (d.getDifferenceStock() > 0) {
                profitCount++;
            } else if (d.getDifferenceStock() < 0) {
                lossCount++;
            }
            itemVOList.add(itemVO);
        }

        vo.setTotalItems(details.size());
        vo.setProfitCount(profitCount);
        vo.setLossCount(lossCount);
        vo.setItems(itemVOList);
        return vo;
    }
}
