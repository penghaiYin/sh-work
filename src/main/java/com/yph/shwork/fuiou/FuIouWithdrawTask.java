package com.yph.shwork.fuiou;

import com.oigbuy.common.constant.FyConstants;
import com.oigbuy.common.pojo.check.orderwithdrawal.entity.SrmWithdrawalBatch;
import com.oigbuy.finance.fuiou.service.FyCallService;
import com.oigbuy.finance.orderwithdrawal.service.SrmWithdrawalBatchService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FuIouWithdrawTask {

    @Autowired
    private FyCallService fyCallService;

    @Autowired
    private SrmWithdrawalBatchService srmWithdrawalBatchService;

    @XxlJob(value = "pollHandleWithdrawalJobHandler")
    public ReturnT pollHandleWithdrawalJobHandler(String param) {
        XxlJobLogger.log("---------富友轮询开始处理提现单-------------");
        // 查询 t_srm_withdrawal_batch (结汇生成的数据)
        List<SrmWithdrawalBatch> srmWithdrawalBatches = srmWithdrawalBatchService.queryFlowNotCompleted();
        for (SrmWithdrawalBatch srmWithdrawalBatch : srmWithdrawalBatches) {
            if (srmWithdrawalBatch.getProcessStatus() == null) {
                fyCallService.fyProcessWarning(FyConstants.ProcessName.ONE_POLL, "商户订单号【" + srmWithdrawalBatch.getMchntOrderId() + "】，流转状态【" + srmWithdrawalBatch.getProcessStatus() + "】", "流转状态数据异常");
                continue;
            }
            switch (srmWithdrawalBatch.getProcessStatus()) {
                case FyConstants.ProcessStatus.TO_SUBMIT:
                    // SRM提现变更涉及修改 2022年3月28日19:18:28
                    fyCallService.callReceiptOrderSubmit(srmWithdrawalBatch.getMchntOrderId(), srmWithdrawalBatch.getFyAccountCode());
                    break;
                case FyConstants.ProcessStatus.TO_SEND:
//                    fyCallService.uploadFileDetail(srmWithdrawalBatch.getFyOrderNo(), srmWithdrawalBatch.getMchntOrderId());
                    fyCallService.uploadFileDetail(srmWithdrawalBatch);
                    break;
                case FyConstants.ProcessStatus.TO_NOTIFY:
//                    fyCallService.fileNotify(srmWithdrawalBatch.getFyOrderNo(), srmWithdrawalBatch.getMchntOrderId(), srmWithdrawalBatch.getOrderFileName());
                    fyCallService.fileNotify(srmWithdrawalBatch);
                    break;
                case FyConstants.ProcessStatus.TO_PULL:
//                    fyCallService.pullOrderStatus(srmWithdrawalBatch.getFyOrderNo(), srmWithdrawalBatch.getMchntOrderId(), srmWithdrawalBatch.getOrderState());
                    fyCallService.pullOrderStatus(srmWithdrawalBatch);
                    break;
                case FyConstants.ProcessStatus.TO_TRANSFER:
                case FyConstants.ProcessStatus.FY_DEAL_FAILURE:
                    break;
                default:
                    fyCallService.fyProcessWarning(FyConstants.ProcessName.ONE_POLL, "商户订单号【" + srmWithdrawalBatch.getMchntOrderId() + "】，流转状态【" + srmWithdrawalBatch.getProcessStatus() + "】", "流转状态异常");
            }
        }
        XxlJobLogger.log("---------富友轮询处理结束-------------");
        return ReturnT.SUCCESS;
    }

}
