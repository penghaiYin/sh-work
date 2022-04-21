package com.yph.shwork.pingpong.dao;




import com.yph.shwork.pingpong.entity.PingpongAccount;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PingpongAccountDao {
    int insert(PingpongAccount record);

    int insertBatch(@Param("list") List<PingpongAccount> list);

    List<PingpongAccount> getList();
}