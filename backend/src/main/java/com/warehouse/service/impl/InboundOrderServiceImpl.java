package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.common.BusinessException;
import com.warehouse.dto.InboundDTO;
import com.warehouse.dto.InboundItemDTO;
import com.warehouse.entity.*;
import com.warehouse.mapper.*;
import com.warehouse.service.GoodsService;
import com.warehouse.service.InboundOrderService;
import com.warehouse.vo.InboundOrderVO;
import com.warehouse.vo.OrderDetailVO;
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
public class InboundOrderServiceImpl extends ServiceImpl<InboundOrderMapper, InboundOrder> implements InboundOrderService {

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
    public Long createInboundOrder(InboundDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("入库明细不能为空");
        }

        Partner partner = partnerMapper.selectById(dto.getPartnerId());
        if (partner == null) {
            throw new BusinessException("供应商不存在");
        }

        Warehouse warehouse = warehouseMapper.selectById(dto.getWarehouseId());
        if (warehouse == null) {
            throw new BusinessException("目标仓库不存在");
        }

        // 生成入库单号: RK + yyyyMMddHHmmss + 3位随机数
        String orderNo = "RK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", new Random().nextInt(1000));

        InboundOrder inboundOrder = new InboundOrder();
        inboundOrder.setOrderNo(orderNo);
        inboundOrder.setPartnerId(dto.getPartnerId());
        inboundOrder.setWarehouseId(dto.getWarehouseId());
        inboundOrder.setEmployeeId(dto.getEmployeeId() == null ? 1L : dto.getEmployeeId());
        inboundOrder.setInboundTime(LocalDateTime.now());
        inboundOrder.setRemark(dto.getRemark());
        this.save(inboundOrder);

        Long orderId = inboundOrder.getId();

        // 遍历明细并增加库存（包含乐观锁校验）
        for (InboundItemDTO item : dto.getItems()) {
            Goods goods = goodsMapper.selectById(item.getGoodsId());
            if (goods == null) {
                throw new BusinessException("货品ID " + item.getGoodsId() + " 不存在");
            }

            BigDecimal price = item.getPrice() != null ? item.getPrice() : (goods.getPrice() != null ? goods.getPrice() : BigDecimal.ZERO);

            OrderDetail detail = new OrderDetail();
            detail.setOrderType("INBOUND");
            detail.setOrderId(orderId);
            detail.setGoodsId(goods.getId());
            detail.setQuantity(item.getQuantity());
            detail.setPrice(price);
            orderDetailMapper.insert(detail);

            // 增库存，带乐观锁
            goodsService.adjustStock(goods.getId(), item.getQuantity(), item.getVersion());
        }

        return orderId;
    }

    @Override
    public Page<InboundOrderVO> queryInboundPage(Integer pageNo, Integer pageSize, String orderNo) {
        Page<InboundOrder> page = new Page<>(pageNo == null ? 1 : pageNo, pageSize == null ? 10 : pageSize);
        LambdaQueryWrapper<InboundOrder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(InboundOrder::getOrderNo, orderNo);
        }
        wrapper.orderByDesc(InboundOrder::getId);

        Page<InboundOrder> orderPage = this.page(page, wrapper);

        Map<Long, String> partnerMap = partnerMapper.selectList(null).stream()
                .collect(Collectors.toMap(Partner::getId, Partner::getName, (k1, k2) -> k1));
        Map<Long, String> warehouseMap = warehouseMapper.selectList(null).stream()
                .collect(Collectors.toMap(Warehouse::getId, Warehouse::getName, (k1, k2) -> k1));
        Map<Long, String> employeeMap = employeeMapper.selectList(null).stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName, (k1, k2) -> k1));

        Page<InboundOrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        List<InboundOrderVO> voList = orderPage.getRecords().stream().map(order -> {
            InboundOrderVO vo = new InboundOrderVO();
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setPartnerId(order.getPartnerId());
            vo.setPartnerName(partnerMap.getOrDefault(order.getPartnerId(), "未知供应商"));
            vo.setWarehouseId(order.getWarehouseId());
            vo.setWarehouseName(warehouseMap.getOrDefault(order.getWarehouseId(), "未知仓库"));
            vo.setEmployeeId(order.getEmployeeId());
            vo.setEmployeeName(employeeMap.getOrDefault(order.getEmployeeId(), "系统管理员"));
            vo.setInboundTime(order.getInboundTime());
            vo.setRemark(order.getRemark());

            // 统计总数量
            List<OrderDetail> details = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                    .eq(OrderDetail::getOrderType, "INBOUND")
                    .eq(OrderDetail::getOrderId, order.getId()));
            int totalQty = details.stream().mapToInt(OrderDetail::getQuantity).sum();
            vo.setTotalQuantity(totalQty);
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public InboundOrderVO getInboundDetail(Long id) {
        InboundOrder order = this.getById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }

        Partner partner = partnerMapper.selectById(order.getPartnerId());
        Warehouse warehouse = warehouseMapper.selectById(order.getWarehouseId());
        Employee employee = employeeMapper.selectById(order.getEmployeeId());

        InboundOrderVO vo = new InboundOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setPartnerId(order.getPartnerId());
        vo.setPartnerName(partner != null ? partner.getName() : "");
        vo.setWarehouseId(order.getWarehouseId());
        vo.setWarehouseName(warehouse != null ? warehouse.getName() : "");
        vo.setEmployeeId(order.getEmployeeId());
        vo.setEmployeeName(employee != null ? employee.getName() : "");
        vo.setInboundTime(order.getInboundTime());
        vo.setRemark(order.getRemark());

        List<OrderDetail> details = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getOrderType, "INBOUND")
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
