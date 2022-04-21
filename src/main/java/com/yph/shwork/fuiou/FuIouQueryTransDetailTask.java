package com.yph.shwork.fuiou;

import com.oigbuy.common.constant.FyConstants;
import com.oigbuy.common.utils.DateUtils;
import com.oigbuy.finance.fuiou.service.FyTransferCallService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class FuIouQueryTransDetailTask {

    @Autowired
    private FyTransferCallService fyTransferCallService;

    /**
     * 计划1个小时执行一次
     * @param param
     * @return
     */
    @XxlJob(value = "queryTransDetailJobHandler")
    public ReturnT queryTransDetailJobHandler(String param) {
        XxlJobLogger.log("---------交易明细查询开始处理-------------");
        try {
            fyTransferCallService.queryTransferProgress();
        } catch (Exception e) {
            e.printStackTrace();
            fyTransferCallService.dingDingWarning(FyConstants.DingWarningTitle.FY_CRON_JOB_TITLE, new ArrayList<String>() {
                {
                    add("#### 定时任务名称: 交易明细查询" + " \n  ");
                    add("> 异常日志：" + e.getMessage() + "  \n  ");
                    add("> 异常发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                }
            });
        }

        XxlJobLogger.log("---------交易明细查询处理结束-------------");
        return ReturnT.SUCCESS;
    }
}
