package com.yph.shwork.pcard.dao;

import com.oigbuy.common.annotation.MyBatisDao;
import com.oigbuy.common.pojo.third.entity.pcard.PaycardBillDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@MyBatisDao
public interface PaycardBillDetailDao {
    int insert(PaycardBillDetail entity);

    int insertBatch(@Param("list") List<PaycardBillDetail> list);
}