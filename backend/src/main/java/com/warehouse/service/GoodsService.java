package com.warehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.dto.GoodsDTO;
import com.warehouse.entity.Goods;
import com.warehouse.vo.GoodsVO;

import java.util.List;

public interface GoodsService extends IService<Goods> {

    Page<GoodsVO> queryGoodsPage(Integer pageNo, Integer pageSize, Long warehouseId, String keyword, Boolean onlyWarning);

    List<GoodsVO> listGoodsByWarehouse(Long warehouseId);

    GoodsVO getGoodsDetail(Long id);

    Long saveOrUpdateGoods(GoodsDTO goodsDTO);

    void deleteGoods(Long id);

    void adjustStock(Long goodsId, int delta, Integer expectedVersion);
}
