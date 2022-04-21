package com.yph.shwork.pingpong.dao;


import com.oigbuy.common.annotation.MyBatisDao;
import com.oigbuy.common.pojo.third.entity.pingpong.PingpongAccount;
import com.oigbuy.common.pojo.third.entity.pingpong.PingpongAccountInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@MyBatisDao
public interface PingpongAccountInfoDao {
    int insert(PingpongAccount record);

    int insertBatch(@Param("list") List<PingpongAccountInfo> list);

    List<PingpongAccountInfo> getList();

}