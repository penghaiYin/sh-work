package com.yph.shwork.fuiou;

import com.oigbuy.common.constant.FyConstants;
import com.oigbuy.common.utils.DateUtils;
import com.oigbuy.finance.common.utils.OigDateUtils;
import com.oigbuy.finance.fuiou.entity.res.ResultVO;
import com.oigbuy.finance.fuiou.service.FyTransFlowDownloadService;
import com.oigbuy.finance.fuiou.service.FyTransferCallService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class FuIouTransFlowDownloadTask {

    @Autowired
    private FyTransFlowDownloadService service;

    @Autowired
    private FyTransferCallService fyTransferCallService;

    /**
     * 每天拉取
     *
     * @param param
     * @return
     */
    @XxlJob(value = "transRecordPullJobHandler")
    public ReturnT transRecordPullJobHandler(String param) {
        XxlJobLogger.log("---------拉取富友流水 START-------------");
        String startTime;
        String endTime;
        if (StringUtils.isNotBlank(param)) {
            String[] dateArray = param.split(",");
            if (dateArray.length != 2) return ReturnT.FAIL;
            startTime = dateArray[0];
            endTime = dateArray[1];
        } else {
            startTime = OigDateUtils.formatDate(OigDateUtils.nextDay(-1));
            endTime = startTime;
        }
        try {
            // 富友交易流水拉取
            Map<String, List<ResultVO>> updateMap = service.transFlowPull(startTime, endTime);
            service.transFlowDownload(updateMap);
        } catch (Exception e) {
            e.printStackTrace();
            fyTransferCallService.dingDingWarning(FyConstants.DingWarningTitle.FY_CRON_JOB_TITLE, new ArrayList<String>() {
                {
                    add("#### 定时任务名称: 富友流水下载" + " \n  ");
                    add("> 异常日志：" + e.getMessage() + "  \n  ");
                    add("> 异常发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                }
            });
        }
        XxlJobLogger.log("---------拉取富友流水 END-------------");
        return ReturnT.SUCCESS;
    }

}
