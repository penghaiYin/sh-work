package com.yph.shwork.pingpong.dao;


import com.oigbuy.common.annotation.MyBatisDao;
import com.oigbuy.common.pojo.third.entity.pingpong.PingpongWithdrawDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@MyBatisDao
public interface PingpongWithdrawDetailDao {
    int insert(PingpongWithdrawDetail record);

    int insertBatch(@Param("list") List<PingpongWithdrawDetail> list);
}