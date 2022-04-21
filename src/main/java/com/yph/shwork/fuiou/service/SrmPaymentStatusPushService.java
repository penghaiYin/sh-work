package com.yph.shwork.fuiou.service;

import com.alibaba.fastjson.JSON;
import com.oigbuy.common.constant.FyConstants;
import com.oigbuy.common.pojo.check.orderwithdrawal.entity.SrmWithdrawalDetailSplit;
import com.oigbuy.common.utils.DateUtils;
import com.oigbuy.common.utils.StringUtils;
import com.oigbuy.finance.common.service.TokenService;
import com.oigbuy.finance.dao.finance.orderwithdrawal.SrmPaymentStatusPushDao;
import com.oigbuy.finance.dao.finance.orderwithdrawal.SrmTransferDetailDao;
import com.oigbuy.finance.dao.finance.orderwithdrawal.SrmWithdrawalDetailSplitDao;
import com.oigbuy.finance.dao.finance.orderwithdrawal.SrmWithdrawalMainDao;
import com.oigbuy.finance.feignService.SrmFeignService;
import com.oigbuy.finance.fuiou.result.SrmJsonResult;
import com.oigbuy.finance.orderwithdrawal.entity.WithdrawalPayCompleted;
import com.oigbuy.finance.orderwithdrawal.entity.WithdrawalPayment;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @Auther zhuo.lv
 * @Date 2022-01-18
 */
@Service
public class SrmPaymentStatusPushService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SrmWithdrawalDetailSplitDao srmWithdrawalDetailSplitDao;

    @Autowired
    private SrmPaymentStatusPushDao srmPaymentStatusPushDao;

    @Autowired
    private SrmFeignService srmFeignService;

    @Autowired
    private TokenService tokenService;

    private static final String METHOD = "withdrawalpaycompleted";

    private static final String STABLETEXT = "UimAWL89ropzj&4";

    @Autowired
    private SrmWithdrawalMainDao srmWithdrawalMainDao;

    @Autowired
    private SrmTransferDetailDao srmTransferDetailDao;

    @Autowired
    private FyTransferCallService fyTransferCallService;

    public void findSrmPaymentStatus() {
        String token = tokenService.getToken();
        Set<String> hasTransferCodeSet = new HashSet<>();
        // 查询已转账的提现单
        List<SrmWithdrawalDetailSplit> list = srmWithdrawalDetailSplitDao.findSrmWithdrawalDetail();
        for (SrmWithdrawalDetailSplit srmWithdrawalDetailSplit : list) {
            // 收集主单号
            hasTransferCodeSet.add(srmWithdrawalDetailSplit.getWithdrawalCode());
        }
        // 排除已经通知||无需通知的主单号
        List<String> hasTransferNoticeSet = srmWithdrawalMainDao.findHasTransferNotice();
        hasTransferCodeSet.removeAll(hasTransferNoticeSet);

        for (String withdrawalCode : hasTransferCodeSet) {
            boolean withdrawalFlag = true; // 全部提现到账成功标志
            Set<String> billDetailCodeSet = new HashSet<>();
            // 校验这个主单下是否全部完成提现   -- 根据更新时间倒序排列
            List<SrmWithdrawalDetailSplit> detailSplits = srmWithdrawalDetailSplitDao.findSrmWithdrawalDetailWithdrawalCode(withdrawalCode);
            for (SrmWithdrawalDetailSplit split : detailSplits) {
                billDetailCodeSet.add(split.getBillDetailCode());
                if (split.getDocumentStatus() != 7) {
                    withdrawalFlag = false;
                    continue;
                }
            }
            // 如果全部完成提现，对接SRM，确认提现金额是否有误

            if (withdrawalFlag) {
                // 通知SRM
                WithdrawalPayCompleted param = new WithdrawalPayCompleted();
                try {
                    // 全部完成的打款时间
                    Date arriveTime = srmTransferDetailDao.getTransferFinishTime(withdrawalCode);
                    // 批量更新全部完成的打款时间
                    srmPaymentStatusPushDao.updateArrivalTimeByWithdrawalCode(withdrawalCode, arriveTime);
                    // 批量更新打款状态
                    srmPaymentStatusPushDao.updatePaymentStatusByWithdrawalCode(withdrawalCode);

                    String plaintext = new StringBuilder().append(DateUtils.formatDateTime(arriveTime)).append(METHOD).append(STABLETEXT).toString();
                    // MD5加密后字符串
                    String textAfterMD5 = md5(plaintext);
                    // 在MD5加密的基础上，再进行Base64加密
                    String textAfterBase64 = encodeBase64(textAfterMD5);
                    // 提现主单号
                    param.setBillCode(withdrawalCode);
                    // 加密后的密文
                    param.setSign(textAfterBase64);
                    // 非稳利宝提现金额
                    param.setSpAmount(detailSplits.stream().filter(d -> (d.getWithdrawalType() == 1)).map(SrmWithdrawalDetailSplit::getSplitAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
                    // 稳利宝提现金额
                    param.setWlbAmount(detailSplits.stream().filter(d -> (d.getWithdrawalType() == 2)).map(SrmWithdrawalDetailSplit::getSplitAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
                    // 总提现金额
                    param.setGcTotalAmount(param.getSpAmount().add(param.getWlbAmount()));
                    // 提现完成时间
                    param.setPayCompletedTime(DateUtils.formatDateTime(arriveTime));
                    // 时间
                    param.setTimeStamp(DateUtils.formatDateTime(arriveTime));
                    // 付款标志
                    param.setPaymentSign("1"); // 1已全部完成 0未完成
                } catch (Exception e){
                    e.printStackTrace();
                    fyTransferCallService.dingDingWarning(FyConstants.DingWarningTitle.NOTICE_SRM_CHECK_TRANSFER, new ArrayList<String>() {
                        {
                            add("#### 当前处理流程: 打款完成通知SRM" + " \n  ");
                            add("#### 异常原因: " + e.getMessage() + " \n  ");
                            add("> 发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                        }
                    });
                }

                int transferNoticeFlag = FyConstants.TransferNoticeFlag.NOTICE_FAILURE; // 通知失败
                SrmJsonResult result = null;
                try {
                    WithdrawalPayment withdrawalPayment = new WithdrawalPayment();
                    withdrawalPayment.setParam(param);
                    logger.info(JSON.toJSONString(withdrawalPayment));
                    result = srmFeignService.pushWithdrawalPaymentStatus(token, withdrawalPayment);
                    if ("200".equals(result.getCode())) {
                        if (result.getSuccess()) {
                            transferNoticeFlag = FyConstants.TransferNoticeFlag.NOTICE_AND_CONFIRM_SUCCESS;
                        }
                    }else {
                        transferNoticeFlag = FyConstants.TransferNoticeFlag.NOTICE_AND_CONFIRM_FAILURE;
                    }
                    logger.info(JSON.toJSONString(result));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (transferNoticeFlag != FyConstants.TransferNoticeFlag.NOTICE_AND_CONFIRM_SUCCESS) {
                        // 预警
                        int finalTransferNoticeFlag = transferNoticeFlag;
                        SrmJsonResult finalResult = result;
                        fyTransferCallService.dingDingWarning(FyConstants.DingWarningTitle.NOTICE_SRM_CHECK_TRANSFER, new ArrayList<String>() {
                            {
                                add("#### 当前处理流程: 打款完成通知SRM" + " \n  ");
                                add("#### 提现单主单编号: " + withdrawalCode + " \n  ");
                                add("> transfer_notice_flag=" + finalTransferNoticeFlag + "  \n  ");
                                add("> 响应报文: " + JSON.toJSONString(finalResult) + "  \n  ");
                                add("> 发生时间: " + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                            }
                        });
                    }
                    srmWithdrawalMainDao.updateTransferNoticeFlag(withdrawalCode, transferNoticeFlag);
                }
            }
        }
    }

    public void test() {
        String token = tokenService.getToken();
        List<SrmWithdrawalDetailSplit> detailSplits = new ArrayList<>();
        SrmWithdrawalDetailSplit split = new SrmWithdrawalDetailSplit();
        split.setSplitAmount(new BigDecimal("101.00"));
        split.setWithdrawalType(1);
        SrmWithdrawalDetailSplit split2 = new SrmWithdrawalDetailSplit();
        split2.setSplitAmount(new BigDecimal("102.00"));
        split2.setWithdrawalType(1);
        SrmWithdrawalDetailSplit split3 = new SrmWithdrawalDetailSplit();
        split3.setSplitAmount(new BigDecimal("103.00"));
        split3.setWithdrawalType(0);
        SrmWithdrawalDetailSplit split4 = new SrmWithdrawalDetailSplit();
        split4.setSplitAmount(new BigDecimal("1043.00"));
        split4.setWithdrawalType(0);
        detailSplits.add(split);
        detailSplits.add(split2);
        detailSplits.add(split3);
        detailSplits.add(split4);
        // 全部完成的打款时间
        Date arriveTime = new Date();
        String plaintext = new StringBuilder().append(DateUtils.formatDateTime(arriveTime)).append(METHOD).append(STABLETEXT).toString();
        // MD5加密后字符串
        String textAfterMD5 = md5(plaintext);
        // 在MD5加密的基础上，再进行Base64加密
        String textAfterBase64 = encodeBase64(textAfterMD5);

        // 通知SRM
        WithdrawalPayCompleted param = new WithdrawalPayCompleted();
        // 提现主单号
        param.setBillCode("T120604130");
        // 加密后的密文
        param.setSign(textAfterBase64);
        // 非稳利宝提现金额
        param.setSpAmount(detailSplits.stream().filter(d -> (d.getWithdrawalType() == 1)).map(SrmWithdrawalDetailSplit::getSplitAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        // 稳利宝提现金额
        param.setWlbAmount(detailSplits.stream().filter(d -> (d.getWithdrawalType() == 0)).map(SrmWithdrawalDetailSplit::getSplitAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        // 总提现金额
        param.setGcTotalAmount(param.getSpAmount().add(param.getWlbAmount()));
        // 提现完成时间
        param.setPayCompletedTime(DateUtils.formatDateTime(arriveTime));
        // 时间
        param.setTimeStamp(DateUtils.formatDateTime(arriveTime));
        // 付款标志
        param.setPaymentSign("1"); // 1已全部完成 0未完成
        int transferNoticeFlag = FyConstants.TransferNoticeFlag.NOTICE_FAILURE; // 通知失败
        try {
            WithdrawalPayment withdrawalPayment = new WithdrawalPayment();
            withdrawalPayment.setParam(param);
            logger.info(JSON.toJSONString(withdrawalPayment));
            SrmJsonResult result = srmFeignService.pushWithdrawalPaymentStatus(token, withdrawalPayment);
            if ("200".equals(result.getCode())) {
                if (result.getSuccess()) {
                    transferNoticeFlag = FyConstants.TransferNoticeFlag.NOTICE_AND_CONFIRM_SUCCESS;
                } else {
                    transferNoticeFlag = FyConstants.TransferNoticeFlag.NOTICE_AND_CONFIRM_FAILURE;
                }
            }
            logger.info(JSON.toJSONString(result));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (transferNoticeFlag != FyConstants.TransferNoticeFlag.NOTICE_AND_CONFIRM_SUCCESS) {
                // 预警
                int finalTransferNoticeFlag = transferNoticeFlag;
                fyTransferCallService.dingDingWarning(FyConstants.DingWarningTitle.NOTICE_SRM_CHECK_TRANSFER, new ArrayList<String>() {
                    {
                        add("#### 当前处理流程: 转账通知SRM" + " \n  ");
                        add("> transfer_notice_flag: " + finalTransferNoticeFlag + "  \n  ");
                        add("> 预警时间: " + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                    }
                });
            }
            srmWithdrawalMainDao.updateTransferNoticeFlag("T120604130", transferNoticeFlag);
        }
    }

    //MD5加密方法
    public static final String md5(String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        }
        String md5str = DigestUtils.md5DigestAsHex(str.getBytes());
        return md5str;
    }

    //Base64加密
    public static String encodeBase64(String input) {
        if (StringUtils.isBlank(input)) {
            return "";
        }
        try {
            return new String(Base64.encodeBase64(input.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static void main(String[] args) {
        String arriveTime = DateUtils.formatDateTime(new Date());
        WithdrawalPayment withdrawalPayment = new WithdrawalPayment();
        WithdrawalPayCompleted requestParam = new WithdrawalPayCompleted();

        String plaintext = new StringBuilder().append(arriveTime).append(METHOD).append(STABLETEXT).toString();
        // MD5加密后字符串
        String textAfterMD5 = md5(plaintext);
        // 在MD5加密的基础上，再进行Base64加密
        String textAfterBase64 = encodeBase64(textAfterMD5);
        requestParam.setSign(textAfterBase64);
        requestParam.setTimeStamp(arriveTime);
        requestParam.setBillCode("T032401");
        requestParam.setSpAmount(new BigDecimal("100.00"));
        requestParam.setWlbAmount(new BigDecimal("200.000"));
        requestParam.setGcTotalAmount(new BigDecimal("300.000"));
        withdrawalPayment.setParam(requestParam);
        System.out.println(JSON.toJSONString(withdrawalPayment));
    }
}
