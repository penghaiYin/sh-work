package com.yph.shwork.pcard.dao;

import com.oigbuy.common.annotation.MyBatisDao;
import com.oigbuy.pcard.entity.ItemEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@MyBatisDao
public interface PaycardBillDownloadDao {
    int insert(ItemEntity entity);

    int insertBatch(@Param("list") List<ItemEntity> list);
}
