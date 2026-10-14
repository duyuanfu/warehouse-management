package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.common.BusinessException;
import com.warehouse.dto.GoodsDTO;
import com.warehouse.entity.Goods;
import com.warehouse.entity.Warehouse;
import com.warehouse.mapper.GoodsMapper;
import com.warehouse.mapper.WarehouseMapper;
import com.warehouse.service.GoodsService;
import com.warehouse.vo.GoodsVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public Page<GoodsVO> queryGoodsPage(Integer pageNo, Integer pageSize, Long warehouseId, String keyword, Boolean onlyWarning) {
        Page<Goods> page = new Page<>(pageNo == null ? 1 : pageNo, pageSize == null ? 10 : pageSize);
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();

        if (warehouseId != null) {
            wrapper.eq(Goods::getWarehouseId, warehouseId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Goods::getName, keyword).or().like(Goods::getSkuCode, keyword));
        }
        if (Boolean.TRUE.equals(onlyWarning)) {
            wrapper.eq(Goods::getStatus, "WARNING");
        }
        wrapper.orderByDesc(Goods::getId);

        Page<Goods> goodsPage = this.page(page, wrapper);

        Map<Long, String> warehouseMap = warehouseMapper.selectList(null).stream()
                .collect(Collectors.toMap(Warehouse::getId, Warehouse::getName, (k1, k2) -> k1));

        Page<GoodsVO> voPage = new Page<>(goodsPage.getCurrent(), goodsPage.getSize(), goodsPage.getTotal());
        List<GoodsVO> voList = goodsPage.getRecords().stream().map(g -> convertToVO(g, warehouseMap)).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<GoodsVO> listGoodsByWarehouse(Long warehouseId) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            wrapper.eq(Goods::getWarehouseId, warehouseId);
        }
        wrapper.orderByAsc(Goods::getName);
        List<Goods> list = this.list(wrapper);

        Map<Long, String> warehouseMap = warehouseMapper.selectList(null).stream()
                .collect(Collectors.toMap(Warehouse::getId, Warehouse::getName, (k1, k2) -> k1));

        return list.stream().map(g -> convertToVO(g, warehouseMap)).collect(Collectors.toList());
    }

    @Override
    public GoodsVO getGoodsDetail(Long id) {
        Goods goods = this.getById(id);
        if (goods == null) {
            throw new BusinessException("货品不存在");
        }
        Warehouse warehouse = warehouseMapper.selectById(goods.getWarehouseId());
        GoodsVO vo = convertToVO(goods, null);
        if (warehouse != null) {
            vo.setWarehouseName(warehouse.getName());
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrUpdateGoods(GoodsDTO goodsDTO) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<Goods>()
                .eq(Goods::getSkuCode, goodsDTO.getSkuCode());
        if (goodsDTO.getId() != null) {
            wrapper.ne(Goods::getId, goodsDTO.getId());
        }
        if (this.count(wrapper) > 0) {
            throw new BusinessException("SKU编码 [" + goodsDTO.getSkuCode() + "] 已存在");
        }

        Warehouse warehouse = warehouseMapper.selectById(goodsDTO.getWarehouseId());
        if (warehouse == null) {
            throw new BusinessException("所属仓库不存在");
        }

        Goods goods;
        if (goodsDTO.getId() != null) {
            goods = this.getById(goodsDTO.getId());
            if (goods == null) {
                throw new BusinessException("货品不存在");
            }
        } else {
            goods = new Goods();
            goods.setStockQuantity(goodsDTO.getStockQuantity() == null ? 0 : goodsDTO.getStockQuantity());
            goods.setVersion(0);
        }

        goods.setSkuCode(goodsDTO.getSkuCode());
        goods.setName(goodsDTO.getName());
        goods.setSpec(goodsDTO.getSpec());
        goods.setSafetyStock(goodsDTO.getSafetyStock() == null ? 10 : goodsDTO.getSafetyStock());
        goods.setWarehouseId(goodsDTO.getWarehouseId());
        goods.setPrice(goodsDTO.getPrice());

        int currentStock = goods.getStockQuantity() != null ? goods.getStockQuantity() : 0;
        int safety = goods.getSafetyStock() != null ? goods.getSafetyStock() : 0;
        goods.setStatus(currentStock <= safety ? "WARNING" : "NORMAL");

        this.saveOrUpdate(goods);
        return goods.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGoods(Long id) {
        Goods goods = this.getById(id);
        if (goods == null) {
            return;
        }
        if (goods.getStockQuantity() != null && goods.getStockQuantity() > 0) {
            throw new BusinessException("当前货品库存大于0，无法删除");
        }
        this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustStock(Long goodsId, int delta, Integer expectedVersion) {
        Goods goods = this.getById(goodsId);
        if (goods == null) {
            throw new BusinessException("货品不存在");
        }

        int targetStock = goods.getStockQuantity() + delta;
        if (targetStock < 0) {
            throw new BusinessException("货品 [" + goods.getName() + "] 库存不足");
        }

        if (expectedVersion != null && !expectedVersion.equals(goods.getVersion())) {
            throw new BusinessException(409, "货品 [" + goods.getName() + "] 库存已被其他员工修改，请刷新后重试");
        }

        int version = expectedVersion != null ? expectedVersion : goods.getVersion();
        int affected = goodsMapper.updateStockWithVersion(goodsId, delta, version);
        if (affected == 0) {
            throw new BusinessException(409, "货品 [" + goods.getName() + "] 并发修改失败，请刷新后重试");
        }

        Goods updatedGoods = this.getById(goodsId);
        int safety = updatedGoods.getSafetyStock() == null ? 0 : updatedGoods.getSafetyStock();
        String newStatus = updatedGoods.getStockQuantity() <= safety ? "WARNING" : "NORMAL";
        if (!newStatus.equals(updatedGoods.getStatus())) {
            updatedGoods.setStatus(newStatus);
            this.updateById(updatedGoods);
        }
    }

    private GoodsVO convertToVO(Goods goods, Map<Long, String> warehouseMap) {
        GoodsVO vo = new GoodsVO();
        BeanUtils.copyProperties(goods, vo);
        if (warehouseMap != null) {
            vo.setWarehouseName(warehouseMap.getOrDefault(goods.getWarehouseId(), "未知仓库"));
        }
        int stock = goods.getStockQuantity() != null ? goods.getStockQuantity() : 0;
        int safety = goods.getSafetyStock() != null ? goods.getSafetyStock() : 0;
        vo.setIsLowStock(stock <= safety);
        return vo;
    }
}
