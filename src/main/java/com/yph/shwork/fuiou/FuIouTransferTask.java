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
public class FuIouTransferTask {

    @Autowired
    private FyTransferCallService fyTransferCallService;

    /**
     * 计划每分钟执行一次，待本次处理结束，才会执行下一次
     * @param param
     * @return
     */
    @XxlJob(value = "handleBankTransferJobHandler")
    public ReturnT handleBankTransferJobHandler(String param) {
        XxlJobLogger.log("---------银行转账开始处理-------------");
        try {
            fyTransferCallService.handleBankTransfer();
        } catch (Exception e) {
            e.printStackTrace();
            fyTransferCallService.dingDingWarning(FyConstants.DingWarningTitle.FY_CRON_JOB_TITLE, new ArrayList<String>() {
                {
                    add("#### 定时任务名称: 银行转账" + " \n  ");
                    add("> 异常日志：" + e.getMessage() + "  \n  ");
                    add("> 异常发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                }
            });
        }

        XxlJobLogger.log("---------银行转账处理结束-------------");
        return ReturnT.SUCCESS;
    }
}
