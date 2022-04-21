package com.yph.shwork.fuiou;

import com.oigbuy.common.constant.FyConstants;
import com.oigbuy.common.pojo.check.orderwithdrawal.entity.SrmWithdrawalBatch;
import com.oigbuy.common.utils.cache.RedisUtils;
import com.oigbuy.finance.dao.finance.orderwithdrawal.SrmWithdrawalBatchDao;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FySynchronizeCacheTask {

    @Autowired
    private SrmWithdrawalBatchDao srmWithdrawalBatchDao;

    @XxlJob(value = "fySynchronizeCacheTaskJobHandler")
    public ReturnT fySynchronizeCacheTaskJobHandler(String param) {
        XxlJobLogger.log("---------富友提现流程逆转需同步缓存-------------");

        if (StringUtils.isBlank(param)) {
            return ReturnT.FAIL;
        }

        String[] params = param.split(",");
        if (params.length != 2) {
            return ReturnT.FAIL;
        }
        SrmWithdrawalBatch check = srmWithdrawalBatchDao.getByOrderId(params[0]);
        if (check == null) {
            return ReturnT.FAIL;
        }

        RedisUtils.hset(FyConstants.SRM_BATCH_STATUS_REDIS_KEY, params[0], params[1]);
        XxlJobLogger.log("---------同步成功-------------");
        return ReturnT.SUCCESS;
    }
}
