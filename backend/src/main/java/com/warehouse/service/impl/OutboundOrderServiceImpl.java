package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.common.BusinessException;
import com.warehouse.dto.OutboundDTO;
import com.warehouse.dto.OutboundItemDTO;
import com.warehouse.entity.*;
import com.warehouse.mapper.*;
import com.warehouse.service.GoodsService;
import com.warehouse.service.OutboundOrderService;
import com.warehouse.vo.OrderDetailVO;
import com.warehouse.vo.OutboundOrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OutboundOrderServiceImpl extends ServiceImpl<OutboundOrderMapper, OutboundOrder> implements OutboundOrderService {

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private PartnerMapper partnerMapper;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOutboundOrder(OutboundDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("出库明细不能为空");
        }

        Partner partner = partnerMapper.selectById(dto.getPartnerId());
        if (partner == null) {
            throw new BusinessException("客户不存在");
        }

        Warehouse warehouse = warehouseMapper.selectById(dto.getWarehouseId());
        if (warehouse == null) {
            throw new BusinessException("发出仓库不存在");
        }

        // 预检查库存
        for (OutboundItemDTO item : dto.getItems()) {
            Goods goods = goodsMapper.selectById(item.getGoodsId());
            if (goods == null) {
                throw new BusinessException("货品ID " + item.getGoodsId() + " 不存在");
            }
            if (goods.getStockQuantity() < item.getQuantity()) {
                throw new BusinessException("货品 [" + goods.getName() + "] 当前库存(" + goods.getStockQuantity() + ")不足以出库 " + item.getQuantity() + " 件！");
            }
        }

        // 生成出库单号: CK + yyyyMMddHHmmss + 3位随机数
        String orderNo = "CK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", new Random().nextInt(1000));

        OutboundOrder outboundOrder = new OutboundOrder();
        outboundOrder.setOrderNo(orderNo);
        outboundOrder.setPartnerId(dto.getPartnerId());
        outboundOrder.setWarehouseId(dto.getWarehouseId());
        outboundOrder.setEmployeeId(dto.getEmployeeId() == null ? 1L : dto.getEmployeeId());
        outboundOrder.setOutboundTime(LocalDateTime.now());
        outboundOrder.setRemark(dto.getRemark());
        this.save(outboundOrder);

        Long orderId = outboundOrder.getId();

        // 遍历明细并扣减库存（包含乐观锁校验）
        for (OutboundItemDTO item : dto.getItems()) {
            Goods goods = goodsMapper.selectById(item.getGoodsId());
            BigDecimal price = item.getPrice() != null ? item.getPrice() : (goods.getPrice() != null ? goods.getPrice() : BigDecimal.ZERO);

            OrderDetail detail = new OrderDetail();
            detail.setOrderType("OUTBOUND");
            detail.setOrderId(orderId);
            detail.setGoodsId(goods.getId());
            detail.setQuantity(item.getQuantity());
            detail.setPrice(price);
            orderDetailMapper.insert(detail);

            // 扣减库存，带乐观锁
            goodsService.adjustStock(goods.getId(), -item.getQuantity(), item.getVersion());
        }

        return orderId;
    }

    @Override
    public Page<OutboundOrderVO> queryOutboundPage(Integer pageNo, Integer pageSize, String orderNo) {
        Page<OutboundOrder> page = new Page<>(pageNo == null ? 1 : pageNo, pageSize == null ? 10 : pageSize);
        LambdaQueryWrapper<OutboundOrder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(OutboundOrder::getOrderNo, orderNo);
        }
        wrapper.orderByDesc(OutboundOrder::getId);

        Page<OutboundOrder> orderPage = this.page(page, wrapper);

        Map<Long, String> partnerMap = partnerMapper.selectList(null).stream()
                .collect(Collectors.toMap(Partner::getId, Partner::getName, (k1, k2) -> k1));
        Map<Long, String> warehouseMap = warehouseMapper.selectList(null).stream()
                .collect(Collectors.toMap(Warehouse::getId, Warehouse::getName, (k1, k2) -> k1));
        Map<Long, String> employeeMap = employeeMapper.selectList(null).stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName, (k1, k2) -> k1));

        Page<OutboundOrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        List<OutboundOrderVO> voList = orderPage.getRecords().stream().map(order -> {
            OutboundOrderVO vo = new OutboundOrderVO();
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setPartnerId(order.getPartnerId());
            vo.setPartnerName(partnerMap.getOrDefault(order.getPartnerId(), "未知客户"));
            vo.setWarehouseId(order.getWarehouseId());
            vo.setWarehouseName(warehouseMap.getOrDefault(order.getWarehouseId(), "未知仓库"));
            vo.setEmployeeId(order.getEmployeeId());
            vo.setEmployeeName(employeeMap.getOrDefault(order.getEmployeeId(), "系统管理员"));
            vo.setOutboundTime(order.getOutboundTime());
            vo.setRemark(order.getRemark());

            List<OrderDetail> details = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                    .eq(OrderDetail::getOrderType, "OUTBOUND")
                    .eq(OrderDetail::getOrderId, order.getId()));
            int totalQty = details.stream().mapToInt(OrderDetail::getQuantity).sum();
            vo.setTotalQuantity(totalQty);
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public OutboundOrderVO getOutboundDetail(Long id) {
        OutboundOrder order = this.getById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }

        Partner partner = partnerMapper.selectById(order.getPartnerId());
        Warehouse warehouse = warehouseMapper.selectById(order.getWarehouseId());
        Employee employee = employeeMapper.selectById(order.getEmployeeId());

        OutboundOrderVO vo = new OutboundOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setPartnerId(order.getPartnerId());
        vo.setPartnerName(partner != null ? partner.getName() : "");
        vo.setWarehouseId(order.getWarehouseId());
        vo.setWarehouseName(warehouse != null ? warehouse.getName() : "");
        vo.setEmployeeId(order.getEmployeeId());
        vo.setEmployeeName(employee != null ? employee.getName() : "");
        vo.setOutboundTime(order.getOutboundTime());
        vo.setRemark(order.getRemark());

        List<OrderDetail> details = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getOrderType, "OUTBOUND")
                .eq(OrderDetail::getOrderId, order.getId()));

        int totalQty = 0;
        List<OrderDetailVO> itemVOList = new ArrayList<>();
        for (OrderDetail d : details) {
            Goods goods = goodsMapper.selectById(d.getGoodsId());
            OrderDetailVO itemVO = new OrderDetailVO();
            itemVO.setId(d.getId());
            itemVO.setOrderType(d.getOrderType());
            itemVO.setOrderId(d.getOrderId());
            itemVO.setGoodsId(d.getGoodsId());
            itemVO.setQuantity(d.getQuantity());
            itemVO.setPrice(d.getPrice());
            itemVO.setTotalAmount(d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())));
            if (goods != null) {
                itemVO.setGoodsName(goods.getName());
                itemVO.setGoodsSku(goods.getSkuCode());
                itemVO.setGoodsSpec(goods.getSpec());
            }
            itemVOList.add(itemVO);
            totalQty += d.getQuantity();
        }

        vo.setTotalQuantity(totalQty);
        vo.setItems(itemVOList);
        return vo;
    }
}
