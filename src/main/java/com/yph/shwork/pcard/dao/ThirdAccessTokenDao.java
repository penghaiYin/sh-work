package com.yph.shwork.pcard.dao;

import com.yph.shwork.pingpong.entity.ThirdAccessToken;
import org.apache.ibatis.annotations.Param;
public interface ThirdAccessTokenDao {
    int insert(ThirdAccessToken record);

    ThirdAccessToken queryAccessToken(@Param("source") int source);

    int updateById(ThirdAccessToken entity);
}
