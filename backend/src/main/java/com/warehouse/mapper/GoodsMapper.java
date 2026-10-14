package com.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.warehouse.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * 手动带有版本号的比对更新库存，用于直观体验乐观锁冲突
     */
    @Update("UPDATE goods SET stock_quantity = stock_quantity + #{delta}, version = version + 1 WHERE id = #{id} AND version = #{version}")
    int updateStockWithVersion(@Param("id") Long id, @Param("delta") Integer delta, @Param("version") Integer version);
}
