package com.yph.shwork.jdy.dao;

import com.oigbuy.check.jdy.dto.JdyLogisticFeeDTO;
import com.oigbuy.common.annotation.MyBatisDao;
import com.oigbuy.common.pojo.jdy.JdyLogisticFee;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@MyBatisDao
public interface JdyLogisticFeeDao {

    List<JdyLogisticFee> findListByPage(JdyLogisticFeeDTO dto);

    List<JdyLogisticFee> findListByIds(@Param("list") List<Integer> ids);

}
