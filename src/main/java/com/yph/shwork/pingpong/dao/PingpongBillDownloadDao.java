package com.yph.shwork.pingpong.dao;


import com.oigbuy.common.annotation.MyBatisDao;
import com.oigbuy.common.pojo.third.entity.pingpong.PingpongBillDownload;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@MyBatisDao
public interface PingpongBillDownloadDao {
    int insert(PingpongBillDownload record);

    int insertBatch(@Param("list") List<PingpongBillDownload> list);

    Set<String> getWithdrawTxIds(@Param("startTime") String updateTimeStart, @Param("endTime") String updateTimeEnd);
}