package com.yph.shwork.fuiou.service;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.SftpException;
import com.oigbuy.common.constant.FyConstants;
import com.oigbuy.common.dto.dingding.DingDingWorkMessageDto;
import com.oigbuy.common.dto.dingding.OigMarkdownMessageDTO;
import com.oigbuy.common.exception.BusiException;
import com.oigbuy.common.exception.DefaultException;
import com.oigbuy.common.feign.auth.token.TokenFeignService;
import com.oigbuy.common.feign.dingding.DingdingFeignService;
import com.oigbuy.common.http.JsonResult;
import com.oigbuy.common.pojo.check.orderwithdrawal.entity.FyAccount;
import com.oigbuy.common.pojo.check.orderwithdrawal.entity.FyOrder;
import com.oigbuy.common.pojo.check.orderwithdrawal.entity.FyOrderDetail;
import com.oigbuy.common.pojo.check.orderwithdrawal.entity.SrmWithdrawalBatch;
import com.oigbuy.common.utils.DateUtils;
import com.oigbuy.common.utils.cache.RedisUtils;
import com.oigbuy.common.utils.oss.OssClientUtils;
import com.oigbuy.finance.common.utils.SFTPUtil;
import com.oigbuy.finance.fuiou.entity.callback.FileNoticeCallBackEntity;
import com.oigbuy.finance.fuiou.entity.req.FyFileNotifyReqEntity;
import com.oigbuy.finance.fuiou.entity.req.FyQueryOrderReqEntity;
import com.oigbuy.finance.fuiou.entity.res.FyFileNotifyResEntity;
import com.oigbuy.finance.fuiou.entity.res.FyOrderQueryResEntity;
import com.oigbuy.finance.fuiou.entity.res.FyOrderSubmitResEntity;
import com.oigbuy.finance.orderwithdrawal.service.FyOrderDetailService;
import com.oigbuy.finance.orderwithdrawal.service.FyOrderService;
import com.oigbuy.finance.orderwithdrawal.service.SrmWithdrawalBatchService;
import com.oigbuy.finance.orderwithdrawal.service.SrmWithdrawalDetailSplitService;
import com.xxl.job.core.log.XxlJobLogger;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class FyCallService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private TokenFeignService tokenFeignService;

    @Value("${fy.receipt-order-submit.url}")
    private String receiptOrderSubmitUrl;

    @Value("${fy.file-upload-notify.url}")
    private String fileUploadNotifyUrl;

    @Value("${fy.order-query-page.url}")
    private String orderQueryPageUrl;

    @Value("${fy.file-upload-notify.req-param.back-url}")
    private String fileNotifyBackUrl;

    @Value("${bank-file.local-generate-path}")
    private String localGeneratePath;

//    @Value("${ftp.host}")
//    private String host;
//
//    @Value("${ftp.port}")
//    private Integer port;
//
//    @Value("${ftp.username}")
//    private String username;
//
//    @Value("${ftp.password}")
//    private String password;
//
//    @Value("${ftp.receipt-storage-path}")
//    private String ftpReceiptStoragePath;

    @Value("${auth.username}")
    private String authUsername;

    @Value("${auth.password}")
    private String authPassword;

    @Value("${dingding.fy-warning.client-id}")
    private String clientId;

//    @Value("${fy.mchntCd}")
//    private String mchntCd;     //商户代码
//
//    @Value("${fy.secret-key}")
//    private String secretKey;   //商户秘钥

    @Value("${fy.work-notify.user-id.remit}")
    private String userIdList;   //通知

    @Value("${fy.work-notify.user-id.settlement}")
    private String settlementUserIdList;   //通知

    @Autowired
    private DingdingFeignService dingdingFeignService;

    @Autowired
    private FyOrderDetailService fyOrderDetailService;

    @Autowired
    private SrmWithdrawalBatchService srmWithdrawalBatchService;

    @Autowired
    private SrmWithdrawalDetailSplitService srmWithdrawalDetailSplitService;

    @Autowired
    private FyOrderService fyOrderService;

    private final RestTemplate restTemplate;

    @Autowired
    private FyAccountService fyAccountService;

    public FyCallService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public void callReceiptOrderSubmit(String mchntOrderId, String fyAccountCode) {
        logger.info("-----------收汇订单提交执行开始：{}------------", mchntOrderId);
        final String currentProcessName = FyConstants.ProcessName.TWO_SUBMIT;
        if (StringUtils.isEmpty(mchntOrderId)) {
            fyProcessWarning(currentProcessName, "商户订单号【" + mchntOrderId + "】", "数据错误");
            return;
        }
        // 避免接口重复调用
        if (cacheCompareDatabase(currentProcessName, mchntOrderId)) {
            /**
             * 调用富友订单提交接口
             */
            ResponseEntity<FyOrderSubmitResEntity> result;
            try {
                HttpEntity<MultiValueMap<String, String>> requestEntity;
                FyAccount fyAccount = fyAccountService.checkFyAccount(fyAccountCode);
                try {
                    MultiValueMap<String, String> params = buildOrderSubmitReqParam(mchntOrderId, fyAccount);
                    requestEntity = buildRequestEntity(params);
                } catch (Exception e) {
                    throw new BusiException("请求参数构建失败，异常日志【" + e.getMessage() + "】");
                }
//                res = HttpUtil.post(receiptOrderSubmitUrl, paramMap);
//                res = HttpUtils.sendPost(receiptOrderSubmitUrl, JSON.toJSONString(fyOrderSubmitReqEntity));

                result = restTemplate.exchange(receiptOrderSubmitUrl, HttpMethod.POST, requestEntity, FyOrderSubmitResEntity.class);
                logger.info(JSON.toJSONString(result.getBody()));
//                logger.info("res: " + res);
//                result = JSON.toJavaObject((JSON) JSONObject.parse(res), FyOrderSubmitResEntity.class);
            } catch (Exception e) {
                fyCallExceptionWarning(currentProcessName, receiptOrderSubmitUrl, mchntOrderId, e.getMessage());
                e.printStackTrace();
                return;
            }
            if (result == null || result.getBody() == null) {
                fyCallExceptionWarning(currentProcessName, receiptOrderSubmitUrl, mchntOrderId, "接口响应失败");
                return;
            }

            /**
             * 接口返回响应处理
             */
            String rspCd = result.getBody().getRspCd();
            String rspDesc = result.getBody().getRspDesc();
            logger.info("返回状态码：{}，返回信息：{}", rspCd, rspDesc);
            if (FyConstants.RESPONSE_SUCCESS_CODE.equals(rspCd)) {

                if (StringUtils.isEmpty(result.getBody().getOrderNo())) {
                    fyCallFailWarning(currentProcessName, receiptOrderSubmitUrl, rspCd, rspDesc, mchntOrderId, "接口响应成功，但未解析到富友订单号");
                    return;
                }

                // 数据进行缓存。这里实际已成功推送至富友，缓存的结果不影响流转状态的更新
                dataCache(FyConstants.ProcessName.TWO_SUBMIT, mchntOrderId, result.getBody());
                // 更新流转状态
                updateProcessStatusByOrderId(FyConstants.ProcessName.TWO_SUBMIT, mchntOrderId, FyConstants.ProcessStatus.TO_SEND, false);
                logger.info("-----------处理结束---------------");

            } else {
                // 提示商户订单号已存在，说明已经成功推送到富友，尝试同步流转状态
                if (FyConstants.RESPONSE_ERROR_CODE.equals(rspCd) && result.getBody().getErrorList() != null && result.getBody().getErrorList().size() == 1
                        && FyConstants.ERROR_LIST_ORDER_ID_NOT_EXIST.equals(result.getBody().getErrorList().get(0))) {

                    updateProcessStatusByOrderId(currentProcessName, mchntOrderId, FyConstants.ProcessStatus.TO_SEND, false);
                    return;
                }
                fyCallFailWarning(currentProcessName, receiptOrderSubmitUrl, rspCd, rspDesc, mchntOrderId, result.getBody().getErrorList() != null ? result.getBody().getErrorList().toString() : "");
            }

        }
        logger.info("-----------收汇订单提交执行结束------------");
    }

    private void dataCache(String processName, String mchntOrderId, FyOrderSubmitResEntity result) {
        try {
            int orderUpNum = fyOrderService.updateOrderNoByOrderId(result.getOrderNo(), mchntOrderId);
            if (orderUpNum != 1) {
                throw new BusiException("更新行数=" + orderUpNum);
            }
        } catch (Exception e) {
            dingDingTableWarning(processName, "t_fy_order", "富友订单号缓存失败", mchntOrderId, e.getMessage());
            e.printStackTrace();
        }

        try {
            int detailUpNum = fyOrderDetailService.updateByMchntOrderId(mchntOrderId, result.getOrderNo());
            if (detailUpNum < 1) {
                throw new BusiException("更新行数=" + detailUpNum);
            }
        } catch (Exception e) {
            dingDingTableWarning(processName, "t_fy_order_detail", "富友订单号缓存失败", mchntOrderId, e.getMessage());
            e.printStackTrace();
        }

        try {
            int srmUpNum = srmWithdrawalBatchService.updateOrderNoByMchntOrderId(mchntOrderId, result.getOrderNo());
            if (srmUpNum != 1) {
                throw new BusiException("更新行数=" + srmUpNum);
            }
        } catch (Exception e) {
            dingDingTableWarning(processName, "t_srm_withdrawal_batch", "富友订单号缓存失败", mchntOrderId, e.getMessage());
        }
        // 更新单据状态待富友审核
        updateDocumentStatusByOrderId(processName, mchntOrderId, FyConstants.DocumentStatus.TO_FY_APPROVED);
    }

    private void updateDocumentStatusByOrderId(String processName, String mchntOrderId, int documentStatus) {
        try {
            int num = srmWithdrawalDetailSplitService.updateDocumentStatusByOrderId(mchntOrderId, documentStatus);
            if (num < 1) {
                throw new BusiException("更新行数=" + num);
            }
        } catch (Exception e) {
            dingDingTableWarning(processName, "t_srm_withdrawal_detail_split", "单据状态【" + documentStatus + "】，更新失败", mchntOrderId, e.getMessage());
        }
    }

    public void uploadFileDetail(SrmWithdrawalBatch srmWithdrawalBatch) {
        String fyOrderNo = srmWithdrawalBatch.getFyOrderNo();
        String mchntOrderId = srmWithdrawalBatch.getMchntOrderId();
        XxlJobLogger.log("-----------上传明细开始：{}------------", mchntOrderId);
        final String currentProcessName = FyConstants.ProcessName.THREE_SEND;
        if (StringUtils.isEmpty(fyOrderNo) || StringUtils.isEmpty(mchntOrderId)) {
            fyProcessWarning(currentProcessName, "商户订单号【" + mchntOrderId + "】，富友订单号【" + fyOrderNo + "】", "数据错误");
            return;
        }
        if (cacheCompareDatabase(currentProcessName, mchntOrderId)) {
            // 收汇明细拼接生成txt
            String fileName = DateUtils.getDate("yyyyMMdd") + mchntOrderId + FyConstants.BANK_FILE_TYPE;
            try {
                FyAccount fyAccount = fyAccountService.checkFyAccount(srmWithdrawalBatch.getFyAccountCode());
                receiptOrderDetailWriteToTxt(fyOrderDetailService.queryOrderDetail(mchntOrderId), localGeneratePath, fileName, fyAccount);
            } catch (Exception e) {
                fyProcessWarning(currentProcessName, "收汇明细生成失败，商户订单号【" + mchntOrderId + "】", e.getMessage());
                e.printStackTrace();
                return;
            }
            XxlJobLogger.log("收汇明细拼接，生成txt：{}", fileName);

            // 上传至FTP
            try {
                fileUploadToFTP(fileName, srmWithdrawalBatch.getFyAccountCode());
            } catch (Exception e) {
                fyProcessWarning(currentProcessName, "FTP上传失败，商户订单号【" + mchntOrderId + "】", e.getMessage());
                e.printStackTrace();
                return;
            }
            XxlJobLogger.log("---------文件已上传至FTP------------");
            RedisUtils.hset(FyConstants.SRM_BATCH_STATUS_REDIS_KEY, mchntOrderId, FyConstants.ProcessStatus.TO_NOTIFY);

            // 上传OSS
            String ossFileUrl = null;
            try {
                ossFileUrl = OssClientUtils.uploadFile(fileName, new FileInputStream(localGeneratePath + fileName));
            } catch (Exception e) {
                e.printStackTrace();
            }
            XxlJobLogger.log("OSS文件地址：{}", ossFileUrl);

            /**
             * 优化，临时文件删除，避免系统资源的浪费
             */
            File file = new File(localGeneratePath + fileName);
            if (file.exists()) {
                file.delete();
            }

            // 待通知
            SrmWithdrawalBatch entity = new SrmWithdrawalBatch();
            entity.setMchntOrderId(mchntOrderId);
            entity.setOrderFileOssUrl(ossFileUrl);
            entity.setOrderFileName(fileName);
            entity.setProcessStatus(FyConstants.ProcessStatus.TO_NOTIFY);
            entity.setSubmitTime(new Date());
            try {
                int i = srmWithdrawalBatchService.updateByMchntOrderId(entity);
                if (i != 1) {
                    throw new BusiException("更新行数=【" + i + "】");
                }
            } catch (Exception e) {
                dingDingTableWarning(currentProcessName, "t_srm_withdrawal_batch", "明细缓存失败", mchntOrderId, e.getMessage());
            }

        }
        logger.info("-----------上传明细结束------------");

    }

    public void fileNotify(SrmWithdrawalBatch srmWithdrawalBatch) {
        String fyOrderNo = srmWithdrawalBatch.getFyOrderNo();
        String mchntOrderId = srmWithdrawalBatch.getMchntOrderId();
        String fileName = srmWithdrawalBatch.getOrderFileName();
        logger.info("-----------明细上传通知开始：{}------------", mchntOrderId);
        final String currentProcessName = FyConstants.ProcessName.FOUR_NOTIFY;
        if (StringUtils.isEmpty(fyOrderNo) || StringUtils.isEmpty(mchntOrderId) || StringUtils.isEmpty(fileName)) {
            fyProcessWarning(currentProcessName, "富友订单号【" + fyOrderNo + "】，商户订单号【" + mchntOrderId + "】，文件名称【" + fileName + "】", "数据错误");
            return;
        }
        if (cacheCompareDatabase(currentProcessName, mchntOrderId)) {
            ResponseEntity<FyFileNotifyResEntity> result;
            try {
                FyAccount fyAccount = fyAccountService.checkFyAccount(srmWithdrawalBatch.getFyAccountCode());
                MultiValueMap<String, String> params = buildFileNoticeReqParam(fyOrderNo, fileName, fyAccount);
                HttpEntity<MultiValueMap<String, String>> requestEntity = buildRequestEntity(params);
                result = restTemplate.exchange(fileUploadNotifyUrl, HttpMethod.POST, requestEntity, FyFileNotifyResEntity.class);
            } catch (Exception e) {
                fyCallExceptionWarning(currentProcessName, fileUploadNotifyUrl, mchntOrderId, e.getMessage());
                e.printStackTrace();
                return;
            }
            if (result == null || result.getBody() == null) {
                fyCallExceptionWarning(currentProcessName, fileUploadNotifyUrl, mchntOrderId, "接口响应失败");
                return;
            }

            String rspCd = result.getBody().getRspCd();
            String rspDesc = result.getBody().getRspDesc();
            logger.info("返回状态码：{}，返回信息：{}", rspCd, rspDesc);

            if (FyConstants.RESPONSE_SUCCESS_CODE.equals(rspCd)) {

                updateProcessStatusByOrderId(currentProcessName, mchntOrderId, FyConstants.ProcessStatus.TO_PULL, false);
                logger.info("-------通知成功--------");
            } else {
                fyCallFailWarning(currentProcessName, fileUploadNotifyUrl, rspCd, rspDesc, mchntOrderId, result.getBody().getErrorList() != null ? result.getBody().getErrorList().toString() : "");
            }
        }
        logger.info("-----------明细上传通知结束------------");
    }

    public void pullOrderStatus(SrmWithdrawalBatch srmWithdrawalBatch) {
        String fyOrderNo = srmWithdrawalBatch.getFyOrderNo();
        String mchntOrderId = srmWithdrawalBatch.getMchntOrderId();
        String orderState = srmWithdrawalBatch.getOrderState();
        logger.info("-----------查询订单状态执行开始：{}------------", mchntOrderId);
        final String currentProcessName = FyConstants.ProcessName.FIVE_PULL;

        if (StringUtils.isEmpty(fyOrderNo) || StringUtils.isEmpty(mchntOrderId)) {
            fyProcessWarning(currentProcessName, "商户订单号【" + mchntOrderId + "】，富友订单号【" + fyOrderNo + "】", "数据错误");
            return;
        }

//        String res;
        ResponseEntity<JSONObject> result;
        HttpEntity<MultiValueMap<String, String>> requestEntity;
        try {
            FyAccount fyAccount = fyAccountService.checkFyAccount(srmWithdrawalBatch.getFyAccountCode());
            try {
                MultiValueMap<String, String> params = buildOrderQueryReqParam(fyOrderNo, fyAccount);
                requestEntity = buildRequestEntity(params);
            } catch (Exception e) {
                throw new BusiException("请求参数构建失败，异常日志【" + e.getMessage() + "】");
            }
//            res = HttpUtil.post(orderQueryPageUrl, fyQueryOrderReqEntity);
//            res = HttpUtils.sendPost(orderQueryPageUrl, JSON.toJSONString(fyQueryOrderReqEntity));
            result = restTemplate.exchange(orderQueryPageUrl, HttpMethod.POST, requestEntity, JSONObject.class);
//            result = JSONObject.parseObject(res);
        } catch (Exception e) {
            fyCallExceptionWarning(currentProcessName, orderQueryPageUrl, mchntOrderId, e.getMessage());
            e.printStackTrace();
            return;
        }
        if (result == null || result.getBody() == null) {
            fyCallExceptionWarning(currentProcessName, orderQueryPageUrl, mchntOrderId, "接口响应失败");
            return;
        }
        JSONObject jsonObject = result.getBody();
        String rspCd = (String) jsonObject.get("rspCd");
        String rspDesc = (String) jsonObject.get("rspDesc");
        logger.info("返回状态码：{}，返回信息：{}", rspCd, rspDesc);

        List<FyOrderQueryResEntity> resultList = JSONArray.parseArray(JSON.toJSONString(jsonObject.get("orderLogList")), FyOrderQueryResEntity.class);
        if (resultList == null || resultList.size() != 1) {
            fyCallExceptionWarning(currentProcessName, orderQueryPageUrl, mchntOrderId, "orderLogList 解析失败: " + (CollectionUtil.isNotEmpty(resultList) ? JSON.toJSONString(resultList) : null));
            return;
        }

        FyOrderQueryResEntity param = resultList.get(0);
        if (FyConstants.RESPONSE_SUCCESS_CODE.equals(rspCd)) {
            if (StringUtils.isEmpty(param.getOrderState())) {
                fyProcessWarning(currentProcessName, "交易状态【" + param.getOrderState() + "】", "未解析到交易状态");
                return;
            }
            logger.info("交易状态：" + param.getOrderState());

            // 富友交易状态缓存
            if (!param.getOrderState().equals(orderState)) {
                updateOrderStateByOrderId(currentProcessName, mchntOrderId, param.getOrderState());
            }
            switch (param.getOrderState()) {
                case FyConstants.OrderState.INCOMPLETE:
                    fyProcessWarning(currentProcessName, "交易状态【明细未到】，商户单号【" + mchntOrderId + "】", "明细格式不对");
                    break;
                case FyConstants.OrderState.PENDING:
                    break;
                case FyConstants.OrderState.APPROVED:
                    notifyRemit(fyOrderNo, mchntOrderId, param);
                    break;
                case FyConstants.OrderState.SUCCESS:
                    updateProcessStatusByOrderId(FyConstants.ProcessName.FIVE_PULL_SUCCESS, mchntOrderId, FyConstants.ProcessStatus.TO_TRANSFER, true);
                    updateDocumentStatusByOrderId(FyConstants.ProcessName.FIVE_PULL_SUCCESS, mchntOrderId, FyConstants.DocumentStatus.COMPLETED);
                    // 2021年12月22日14:18:48 增加结汇通知
                    try {
                        sendWorkMessage(FyConstants.SendWorkMessage.FY_WORK_TITLE, new ArrayList<String>() {
                            {
                                add(FyConstants.SendWorkMessage.SETTLEMENT_TITLE);
                                add("> 关联的富友订单号：" + fyOrderNo + "  \n  ");
                            }
                        }, settlementUserIdList);
                    } catch (Exception e) {
                        fyProcessWarning(FyConstants.SendWorkMessage.SETTLEMENT_TITLE, "结汇通知失败，商户订单号【" + mchntOrderId + "】", e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                case FyConstants.OrderState.NOT_APPROVED:
                case FyConstants.OrderState.FAILURE:
                case FyConstants.OrderState.CLOSED:
                    String approveNote = null;
                    if (param.getOrderState().equals(FyConstants.OrderState.NOT_APPROVED)) {
                        approveNote = (String) jsonObject.get("approveNote");
                    }
                    updateDocumentStatusByOrderId(FyConstants.ProcessName.FIVE_PULL_FAILURE, mchntOrderId, FyConstants.DocumentStatus.FAILURE);

                    updateFailureProcess(FyConstants.ProcessName.FIVE_PULL_FAILURE, mchntOrderId, approveNote, getStatus(param.getOrderState()));
                    break;
            }
            logger.info("-------处理结束--------");
        } else {

            fyCallFailWarning(currentProcessName, orderQueryPageUrl, rspCd, rspDesc, mchntOrderId, jsonObject.get("errorList") != null ? jsonObject.get("errorList").toString() : "");
        }
        logger.info("-----------查询订单状态执行结束------------");
    }

    private int getStatus(String orderState) {
        int failStatus;
        if (orderState.equals(FyConstants.OrderState.NOT_APPROVED)) {
            failStatus = FyConstants.FailureStatus.NOT_APPROVED;
        } else if (orderState.equals(FyConstants.OrderState.FAILURE)) {
            failStatus = FyConstants.FailureStatus.FAILURE;
        } else {
            failStatus = FyConstants.FailureStatus.CLOSED;
        }
        return failStatus;
    }

    private void notifyRemit(String fyOrderNo, String mchntOrderId, FyOrderQueryResEntity result) {
        // 工作通知祝萍给富友打款
        try {
            SrmWithdrawalBatch check = srmWithdrawalBatchService.getByFyOrderNo(fyOrderNo);
            if (check != null && check.getNotifyStatus()) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            /*sendWorkMessage(BigDecimal.valueOf(result.getOutTxnAmt()).divide(new BigDecimal(100)).toString(),
                    BigDecimal.valueOf(result.getInTxnAmt()).divide(new BigDecimal(100)).toString(),
                    fyOrderNo, userIdList);*/
            sendWorkMessage(FyConstants.SendWorkMessage.FY_WORK_TITLE, new ArrayList<String>() {
                {
                    add(FyConstants.SendWorkMessage.REMIT_TITLE);
                    add("> 应付总金额：" + BigDecimal.valueOf(result.getOutTxnAmt()).divide(new BigDecimal(100)).toString() + " \n  ");
                    add("> 富友汇出金额：" + BigDecimal.valueOf(result.getInTxnAmt()).divide(new BigDecimal(100)).toString() + "  \n  ");
                    add("> 关联的富友订单号：" + fyOrderNo + "  \n  ");
                }
            }, userIdList);

        } catch (Exception e) {
            fyProcessWarning(FyConstants.ProcessName.FIVE_PULL_NOTIFY_REMIT, "祝萍打款通知失败，商户订单号【" + mchntOrderId + "】", e.getMessage());
            e.printStackTrace();
        }

        try {
            SrmWithdrawalBatch srmWithdrawalBatch = new SrmWithdrawalBatch();
            srmWithdrawalBatch.setMchntOrderId(mchntOrderId);
            srmWithdrawalBatch.setNotifyStatus(true);
            srmWithdrawalBatchService.updateByMchntOrderId(srmWithdrawalBatch);
        } catch (Exception e) {
            dingDingTableWarning(FyConstants.ProcessName.FIVE_PULL_NOTIFY_REMIT, "t_srm_withdrawal_batch", "状态更新为已通知", mchntOrderId, e.getMessage());
            e.printStackTrace();
        }

        updateProcessStatusByOrderId(FyConstants.ProcessName.FIVE_PULL_NOTIFY_REMIT, mchntOrderId, FyConstants.ProcessStatus.TO_TRANSFER, true);
        updateDocumentStatusByOrderId(FyConstants.ProcessName.FIVE_PULL_NOTIFY_REMIT, mchntOrderId, FyConstants.DocumentStatus.TO_FY_PAYMENT);
    }

    private void updateFailureProcess(String processName, String mchntOrderId, String approveNote, int failStatus) {
        SrmWithdrawalBatch srmWithdrawalBatch = new SrmWithdrawalBatch();
        srmWithdrawalBatch.setMchntOrderId(mchntOrderId);
        srmWithdrawalBatch.setProcessStatus(FyConstants.ProcessStatus.FY_DEAL_FAILURE);
        srmWithdrawalBatch.setFailStatus(failStatus);
        srmWithdrawalBatch.setAuditFailCause(approveNote);
        try {
            int num = srmWithdrawalBatchService.updateByMchntOrderId(srmWithdrawalBatch);
            if (num != 1) {
                throw new BusiException("更新行数=" + num);
            }
        } catch (Exception e) {
            dingDingTableWarning(processName, "t_srm_withdrawal_batch", "更新交易失败相关信息", mchntOrderId, e.getMessage());
        }
    }

    private MultiValueMap<String, String> buildOrderQueryReqParam(String fyOrderNo, FyAccount fyAccount) {
        FyQueryOrderReqEntity entity = new FyQueryOrderReqEntity();
        entity.setMchntCd(fyAccount.getFyMchntCd());
        entity.setOrderNo(fyOrderNo);
        entity.setPageNo(1);
        entity.setPageSize(10);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("mchntCd", fyAccount.getFyMchntCd());
        params.add("orderNo", fyOrderNo);
        params.add("pageNo", "1");
        params.add("pageSize", "10");
        params.add("md5", generateMD5(entity, fyAccount.getFySecretKey()));
        return params;
    }

    /**
     * 构建富友订单提交接口请求参数
     *
     * @param mchntOrderId
     * @param fyAccount
     * @return
     */
    private MultiValueMap<String, String> buildOrderSubmitReqParam(String mchntOrderId, FyAccount fyAccount) {
        FyOrder fyOrder = fyOrderService.queryByOrderId(mchntOrderId);
        // 封装参数，千万不要替换为Map与HashMap，否则参数无法传递
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("mchntCd", fyAccount.getFyMchntCd());
        params.add("subCustNo", fyAccount.getFyMchntCd());
        params.add("orderId", fyOrder.getOrderId());
        params.add("backNotifyUrl", fyOrder.getBackNotifyUrl());
        params.add("txnTp", fyOrder.getTxnTp());
        params.add("settleAccountsTp", fyOrder.getSettleAccountsTp());
        params.add("outAcntNo", fyOrder.getOutAcntNo());
        params.add("outAcntNm", fyOrder.getOutAcntNm());
        params.add("outAcntBankNm", fyOrder.getOutAcntBankNm());
        params.add("countryCd", fyOrder.getCountryCd());
        params.add("bankCountryCd", fyOrder.getBankCountryCd());
        params.add("outCurCd", fyOrder.getOutCurCd());
        params.add("inAcntNo", fyOrder.getInAcntNo() != null ? fyOrder.getInAcntNo().toString() : "");
        params.add("inAcntNm", fyOrder.getInAcntNm());
        params.add("inAcntBankNm", fyOrder.getInAcntBankNm());
        params.add("bankNo", fyOrder.getBankNo());
        params.add("cityNo", fyOrder.getCityNo());
        params.add("orderTp", fyOrder.getOrderTp());
        params.add("orderType", fyOrder.getOrderType());
        params.add("orderAmt", fyOrder.getOrderAmt() != null ? fyOrder.getOrderAmt().toString() : "");
        params.add("ver", fyOrder.getVer());
        params.add("md5", generateMD5(fyOrder, fyAccount));
        return params;
    }

    private MultiValueMap<String, String> buildFileNoticeReqParam(String fyOrderNo, String fileName, FyAccount fyAccount) {
        FyFileNotifyReqEntity entity = new FyFileNotifyReqEntity();
        entity.setMchntCd(fyAccount.getFyMchntCd());
        entity.setOrderNo(fyOrderNo);
        entity.setBackNotifyUrl(fileNotifyBackUrl);
        entity.setFileNm(fileName);
        entity.setFileTp(FyConstants.FILE_TP_RECEIPT);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("mchntCd", fyAccount.getFyMchntCd());
        params.add("backNotifyUrl", fileNotifyBackUrl);
        params.add("fileTp", FyConstants.FILE_TP_RECEIPT);
        params.add("fileNm", fileName);
        params.add("orderNo", fyOrderNo);
        params.add("md5", generateMD5(entity, fyAccount.getFySecretKey()));
        return params;
    }

    private HttpEntity<MultiValueMap<String, String>> buildRequestEntity(MultiValueMap<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(params, headers);
        return requestEntity;
    }

    private String generateMD5(FyOrder param, FyAccount fyAccount) {
        final String delimiter = FyConstants.MD5_DIGEST_DELIMITER;
        String md5Digest = (fyAccount.getFyMchntCd() == null ? "" : fyAccount.getFyMchntCd()) + delimiter
                + (fyAccount.getFyMchntCd() == null ? "" : fyAccount.getFyMchntCd()) + delimiter
                + (param.getOrderId() == null ? "" : param.getOrderId()) + delimiter
                + (param.getBackNotifyUrl() == null ? "" : param.getBackNotifyUrl()) + delimiter
                + (param.getTxnTp() == null ? "" : param.getTxnTp()) + delimiter
                + (param.getSettleAccountsTp() == null ? "" : param.getSettleAccountsTp()) + delimiter
                + (param.getOutAcntNo() == null ? "" : param.getOutAcntNo()) + delimiter
                + (param.getOutAcntNm() == null ? "" : param.getOutAcntNm()) + delimiter
                + (param.getOutAcntBankNm() == null ? "" : param.getOutAcntBankNm()) + delimiter
                + (param.getOutCurCd() == null ? "" : param.getOutCurCd()) + delimiter
                + (param.getInAcntNo() == null ? "" : param.getInAcntNo()) + delimiter
                + (param.getInAcntNm() == null ? "" : param.getInAcntNm()) + delimiter
                + (param.getInAcntBankNm() == null ? "" : param.getInAcntBankNm()) + delimiter
                + (param.getBankNo() == null ? "" : param.getBankNo()) + delimiter
                + (param.getCityNo() == null ? "" : param.getCityNo()) + delimiter
                + (param.getCountryCd() == null ? "" : param.getCountryCd()) + delimiter
                + (param.getOrderTp() == null ? "" : param.getOrderTp()) + delimiter
                + (param.getOrderAmt() == null ? "" : param.getOrderAmt()) + delimiter
                + (param.getVer() == null ? "" : param.getVer()) + delimiter
                + fyAccount.getFySecretKey();
        return DigestUtils.md5Hex(md5Digest);
    }

    private String receiptOrderDetailWriteToTxt(List<FyOrderDetail> list, String storagePath, String fileName, FyAccount fyAccount) throws IOException {
        final String delimiter = FyConstants.BANK_FILE_DELIMITER;
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(storagePath + fileName));
//            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(storagePath + fileName)),"utf-8"));
            for (FyOrderDetail detail : list) {
                // 商户代码
                bw.write((fyAccount.getFyMchntCd() == null ? "" : fyAccount.getFyMchntCd()) + delimiter);
                // 子客户号
                bw.write((fyAccount.getFyMchntCd() == null ? "" : fyAccount.getFyMchntCd()) + delimiter);
                // 富友订单号
                bw.write((detail.getOrderNo() == null ? "" : detail.getOrderNo()) + delimiter);
                // 源订单编号
                bw.write((detail.getMchntOrderNo() == null ? "" : detail.getMchntOrderNo()) + delimiter);
                // 源交易日期
                bw.write((detail.getOrderDate() == null ? "" : detail.getOrderDate()) + delimiter);
                // 收款人名称
                bw.write((detail.getCustNm() == null ? "" : detail.getCustNm()) + delimiter);
                // 收款人证件号
                bw.write((detail.getCustId() == null ? "" : detail.getCustId()) + delimiter);
                // 订单币种
                bw.write((detail.getCurCd() == null ? "" : detail.getCurCd()) + delimiter);
                // 订单金额
                bw.write((detail.getOrderAmt() == null ? "" : detail.getOrderAmt().toString()) + delimiter);
                // 付款人常驻国家
                bw.write((detail.getCountryCd() == null ? "" : detail.getCountryCd()) + delimiter);
                // 境外转出账户账号
                bw.write((detail.getOutAcntNo() == null ? "" : detail.getOutAcntNo()) + delimiter);
                // 境外转出账户户名
                bw.write((detail.getOutAcntNm() == null ? "" : detail.getOutAcntNm()) + delimiter);
                // 贸易类型
                bw.write((detail.getOrderType() == null ? "" : detail.getOrderType()) + delimiter);
                // 商品名称
                bw.write((detail.getGoodsNm() == null ? "" : detail.getGoodsNm()) + delimiter);
                // 商品数量
                bw.write((detail.getGoodsNo() == null ? "" : detail.getGoodsNo()) + delimiter);
                // 商品单价
                bw.write((detail.getGoodsPrice() == null ? "" : detail.getGoodsPrice().toString()) + delimiter);
                // 收款人联系电话
                bw.write((detail.getCustContactNumber() == null ? "" : detail.getCustContactNumber()) + delimiter);
                // 商品种类
                bw.write(detail.getGoodsCatagory() == null ? "" : detail.getGoodsCatagory());
                bw.newLine();
                bw.flush();
            }
        } finally {
            if (bw != null) {
                bw.close();
            }
        }
        return fileName;
    }

    private void fileUploadToFTP(String fileName, String fyAccountCode) throws IOException, SftpException {
        FyAccount fyAccount = fyAccountService.checkFyAccount(fyAccountCode);
        SFTPUtil sftp = new SFTPUtil(fyAccount.getSftpUsername(), fyAccount.getSftpSecret(), fyAccount.getSftpHost(), fyAccount.getSftpPort());
        sftp.login();
        InputStream in = null;
        /**
         * 记得关闭流
         */
        try {
            in = new FileInputStream(localGeneratePath + fileName);
            sftp.upload(fyAccount.getSftpReceiptPath(), null, fileName, in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        sftp.logout();
    }

    private String generateMD5(FyFileNotifyReqEntity param, String fySecretKey) {
        String md5Digest = (param.getMchntCd() == null ? "" : param.getMchntCd()) + FyConstants.MD5_DIGEST_DELIMITER
                + (param.getBackNotifyUrl() == null ? "" : param.getBackNotifyUrl()) + FyConstants.MD5_DIGEST_DELIMITER
                + (param.getFileTp() == null ? "" : param.getFileTp()) + FyConstants.MD5_DIGEST_DELIMITER
                + (param.getFileNm() == null ? "" : param.getFileNm()) + FyConstants.MD5_DIGEST_DELIMITER
                + (param.getOrderNo() == null ? "" : param.getOrderNo()) + FyConstants.MD5_DIGEST_DELIMITER
                + fySecretKey;
        return DigestUtils.md5Hex(md5Digest);
    }

    private String generateMD5(FyQueryOrderReqEntity param, String fySecretKey) {
        String md5Digest = (param.getMchntCd() == null ? "" : param.getMchntCd()) + FyConstants.MD5_DIGEST_DELIMITER
                + (param.getBackNotifyUrl() == null ? "" : param.getBackNotifyUrl()) + FyConstants.MD5_DIGEST_DELIMITER
                + (param.getPageNo() == null ? "" : param.getPageNo()) + FyConstants.MD5_DIGEST_DELIMITER
                + (param.getStartTime() == null ? "" : param.getStartTime()) + FyConstants.MD5_DIGEST_DELIMITER
                + (param.getEndTime() == null ? "" : param.getEndTime()) + FyConstants.MD5_DIGEST_DELIMITER
                + (param.getOrderNo() == null ? "" : param.getOrderNo()) + FyConstants.MD5_DIGEST_DELIMITER
                + (param.getOrderId() == null ? "" : param.getOrderId()) + FyConstants.MD5_DIGEST_DELIMITER
                + (param.getPageSize() == null ? "" : param.getPageSize()) + FyConstants.MD5_DIGEST_DELIMITER
                + (param.getTxnTp() == null ? "" : param.getTxnTp()) + FyConstants.MD5_DIGEST_DELIMITER
                + fySecretKey;
        return DigestUtils.md5Hex(md5Digest);
    }


    private void updateProcessStatusByOrderId(String processName, String mchntOrderId, Integer processStatus, Boolean warn) {
        RedisUtils.hset(FyConstants.SRM_BATCH_STATUS_REDIS_KEY, mchntOrderId, processStatus);
        try {
            srmWithdrawalBatchService.updateProcessStatusByOrderId(mchntOrderId, processStatus);
        } catch (Exception e) {
            e.printStackTrace();
            // 可能会自动修复
            if (warn) {
                dingDingTableWarning(processName, "srm_withdrawal_batch", "流转状态=" + processStatus + "缓存失败，请及时修复", mchntOrderId, e.getMessage());
            }
        }
    }

    private void updateOrderStateByOrderId(String processName, String mchntOrderId, String orderState) {
        try {
            int i = srmWithdrawalBatchService.updateOrderStateByOrderId(mchntOrderId, orderState);
            if (1 != i) {
                throw new BusiException("更新行数【" + i + "】");
            }
        } catch (Exception e) {
            dingDingTableWarning(processName, "t_srm_withdrawal_batch", "富友交易状态=【" + orderState + "】缓存失败", mchntOrderId, e.getMessage());
            e.printStackTrace();
        }
    }


    private Boolean cacheCompareDatabase(String processName, String mchntOrderId) {
        Object hGet = RedisUtils.hget(FyConstants.SRM_BATCH_STATUS_REDIS_KEY, mchntOrderId);
        int status = srmWithdrawalBatchService.queryFlowStatusByOrderId(mchntOrderId);
        if (hGet == null) {
            RedisUtils.hset(FyConstants.SRM_BATCH_STATUS_REDIS_KEY, mchntOrderId, status);
            return true;
        }
        if (Integer.valueOf(hGet.toString()) == status) {
            return true;
        } else if (Integer.valueOf(hGet.toString()) < status) {
            RedisUtils.hset(FyConstants.SRM_BATCH_STATUS_REDIS_KEY, mchntOrderId, status);
            return true;
        }
        try {
            srmWithdrawalBatchService.updateProcessStatusByOrderId(mchntOrderId, Integer.valueOf(hGet.toString()));
        } catch (Exception e) {
            dingDingTableWarning(processName, "srm_withdrawal_batch", "流转状态=" + hGet + "缓存失败，请及时修复", mchntOrderId, e.getMessage());
            e.printStackTrace();
        }
        logger.info("缓存与数据库不一致，防止接口重复调用。");
        return false;
    }

    /**
     * @param title
     * @param list
     */
    public void sendWorkMessage(String title, List<String> list, String userIdList) throws DefaultException {
        if (CollectionUtil.isNotEmpty(list)) {
            StringBuffer text = new StringBuffer();
            for (String content : list) {
                text.append(content);
            }
            JsonResult<String> tokenResult = tokenFeignService.token(null, authUsername, authPassword, true);
            if ("200".equals(tokenResult.getCode())) {
                String token = tokenResult.getData().toString();
                logger.info("发送钉钉工作通知：" + token);
                DingDingWorkMessageDto dingDingWorkMessageDto = new DingDingWorkMessageDto();
                dingDingWorkMessageDto.setClientId("4");
                dingDingWorkMessageDto.setDdUserIdList(userIdList);
                dingDingWorkMessageDto.setText(text.toString());
                dingDingWorkMessageDto.setTitle(title);
                dingDingWorkMessageDto.setMsgType("markdown");
                dingdingFeignService.sendWorkMessage(token, dingDingWorkMessageDto);
            }
        }

    }

    /**
     * @param outTxnAmt
     * @param inTxnAmt
     * @param fyOrderNo
     * @param userIdList
     * @throws DefaultException
     *//*
    private void sendWorkMessage(String outTxnAmt, String inTxnAmt, String fyOrderNo, String userIdList) throws DefaultException {
        String title = "富友账户打款通知";
        StringBuffer text = new StringBuffer();
        text.append("#### 应付总金额: " + outTxnAmt + " \n  " +
                "> 富友汇出金额：" + inTxnAmt + "  \n  " +
                "> 关联的富友订单号：" + fyOrderNo + "  \n  ");
        JsonResult<String> tokenResult = tokenFeignService.token(null, authUsername, authPassword, true);
        if ("200".equals(tokenResult.getCode())) {
            String token = tokenResult.getData().toString();
            logger.info("发送钉钉工作通知：" + token);
            DingDingWorkMessageDto dingDingWorkMessageDto = new DingDingWorkMessageDto();
            dingDingWorkMessageDto.setClientId("4");
            dingDingWorkMessageDto.setDdUserIdList(userIdList);
            dingDingWorkMessageDto.setText(text.toString());
            dingDingWorkMessageDto.setTitle(title);
            dingDingWorkMessageDto.setMsgType("markdown");
            dingdingFeignService.sendWorkMessage(token, dingDingWorkMessageDto);

        }
    }*/

    /**
     * @param process
     * @param content
     * @param reason
     */
    public void fyProcessWarning(String process, String content, String reason) {
        String title = "富友异常预警";
        StringBuffer text = new StringBuffer();
        text.append("#### 当前处理流程: " + process + "  \n  " +
                "> 异常数据: " + content + "  \n  " +
                "> 异常可能原因：" + reason + "  \n  " +
                "> 异常发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
        // 获取执行的token
        JsonResult<String> tokenResult = tokenFeignService.token(null, authUsername, authPassword, true);
        if ("200".equals(tokenResult.getCode())) {
            String token = tokenResult.getData().toString();
            logger.info("发送钉钉消息：" + token);
            OigMarkdownMessageDTO oigMarkdownMessageDTO = new OigMarkdownMessageDTO(clientId, title, text.toString(), null);
            try {
                dingdingFeignService.sendMarkdown(token, oigMarkdownMessageDTO);
            } catch (DefaultException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param name
     * @param url
     * @param rspCode
     * @param rspDesc
     * @param mchntOrderId
     * @param reason
     */
    public void fyCallFailWarning(String name, String url, String rspCode, String rspDesc, String mchntOrderId, String reason) {
        String title = "富友接口调用失败预警";
        StringBuffer text = new StringBuffer();

        text.append("#### 接口名称: " + name + " \n  " +
                "> 接口地址：" + url + "  \n  " +
                "> 接口响应状态码：" + rspCode + "  \n  " +
                "> 接口响应描述：" + rspDesc + "  \n  " +
                "> 商户订单号：" + mchntOrderId + "  \n  " +
                "> 失败可能原因：" + reason + "  \n  " +
                "> 失败发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
        JsonResult<String> tokenResult = tokenFeignService.token(null, authUsername, authPassword, true);
        if ("200".equals(tokenResult.getCode())) {
            String token = tokenResult.getData().toString();
            logger.info("发送钉钉消息：" + token);
            OigMarkdownMessageDTO oigMarkdownMessageDTO = new OigMarkdownMessageDTO(clientId, title, text.toString(), null);
            try {
                dingdingFeignService.sendMarkdown(token, oigMarkdownMessageDTO);
            } catch (DefaultException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param name
     * @param url
     * @param mchntOrderId
     * @param reason
     */
    public void fyCallExceptionWarning(String name, String url, String mchntOrderId, String reason) {
        String title = "富友接口调用异常预警";
        StringBuffer text = new StringBuffer();

        text.append("#### 接口名称: " + name + " \n  " +
                "> 接口地址：" + url + "  \n  " +
                "> 商户订单号：" + mchntOrderId + "  \n  " +
                "> 异常可能原因：" + reason + "  \n  " +
                "> 异常发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
        // 获取执行的token
        JsonResult<String> tokenResult = tokenFeignService.token(null, authUsername, authPassword, true);
        if ("200".equals(tokenResult.getCode())) {
            String token = tokenResult.getData().toString();
            logger.info("发送钉钉消息：" + token);
            OigMarkdownMessageDTO oigMarkdownMessageDTO = new OigMarkdownMessageDTO(clientId, title, text.toString(), null);
            try {
                dingdingFeignService.sendMarkdown(token, oigMarkdownMessageDTO);
            } catch (DefaultException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param tableName    表名
     * @param content      操作
     * @param mchntOrderId 商户订单号
     * @param message      异常信息
     */
    public void dingDingTableWarning(String processName, String tableName, String content, String mchntOrderId, String message) {
        String title = "富友数据库预警";
        StringBuffer text = new StringBuffer();

        text.append("#### 当前处理流程: " + processName + " \n  " +
                "> 操作的表名: " + tableName + " \n  " +
                "> 操作的内容：" + content + "  \n  " +
                "> 商户订单号：" + mchntOrderId + "  \n  " +
                "> 异常可能原因：" + message + "  \n  " +
                "> 异常发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
        JsonResult<String> tokenResult = tokenFeignService.token(null, authUsername, authPassword, true);
        if ("200".equals(tokenResult.getCode())) {
            String token = tokenResult.getData().toString();
            logger.info("发送钉钉消息：" + token);
            OigMarkdownMessageDTO oigMarkdownMessageDTO = new OigMarkdownMessageDTO(clientId, title, text.toString(), null);
            try {
                dingdingFeignService.sendMarkdown(token, oigMarkdownMessageDTO);
            } catch (DefaultException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param response
     */
    public void callBackHandleFileNotice(FileNoticeCallBackEntity response) {
        if (!FyConstants.RESPONSE_SUCCESS_CODE.equals(response.getReqCd())) {
            fyProcessWarning(FyConstants.ProcessName.CALL_BACK_FILE_NOTICE, response.toString(), "明细格式不对");
        }
    }

}
