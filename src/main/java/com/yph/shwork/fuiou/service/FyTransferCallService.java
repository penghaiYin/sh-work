package com.yph.shwork.fuiou.service;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.oigbuy.common.constant.FyConstants;
import com.oigbuy.common.dto.dingding.OigMarkdownMessageDTO;
import com.oigbuy.common.exception.BusiException;
import com.oigbuy.common.exception.DefaultException;
import com.oigbuy.common.feign.auth.token.TokenFeignService;
import com.oigbuy.common.feign.dingding.DingdingFeignService;
import com.oigbuy.common.http.HttpUtil;
import com.oigbuy.common.http.JsonResult;
import com.oigbuy.common.pojo.check.orderwithdrawal.dto.TransferConfirmDTO;
import com.oigbuy.common.pojo.check.orderwithdrawal.dto.TransferQueryBalanceDTO;
import com.oigbuy.common.pojo.check.orderwithdrawal.entity.FyAccount;
import com.oigbuy.common.pojo.check.orderwithdrawal.entity.FyTransferDetail;
import com.oigbuy.common.pojo.check.orderwithdrawal.entity.SrmTransferDetail;
import com.oigbuy.common.pojo.check.orderwithdrawal.entity.TransferWarningEntity;
import com.oigbuy.common.pojo.check.orderwithdrawal.vo.TransferQueryBalanceVO;
import com.oigbuy.common.pojo.check.orderwithdrawal.vo.TransferWarningVO;
import com.oigbuy.common.pojo.jdy.JdyFlowRecord;
import com.oigbuy.common.pojo.jdy.request.JdyCreateDataRequestDto;
import com.oigbuy.common.utils.DateUtils;
import com.oigbuy.common.utils.StringUtils;
import com.oigbuy.common.utils.cache.RedisJsonUtils;
import com.oigbuy.common.utils.cache.RedisUtils;
import com.oigbuy.finance.common.utils.OigDateUtils;
import com.oigbuy.finance.dao.finance.jdy.JdyFlowRecordDao;
import com.oigbuy.finance.dao.finance.orderwithdrawal.FyTransferDetailDao;
import com.oigbuy.finance.dao.finance.orderwithdrawal.SrmTransferDetailDao;
import com.oigbuy.finance.dao.finance.orderwithdrawal.SrmWithdrawalDetailSplitDao;
import com.oigbuy.finance.feignService.JdyFeignService;
import com.oigbuy.finance.fuiou.common.Common20Response;
import com.oigbuy.finance.fuiou.dto.FyTransferDetailDTO;
import com.oigbuy.finance.fuiou.dto.FyTransferReceiptInfo;
import com.oigbuy.finance.fuiou.dto.JdyFyTransferFailDto;
import com.oigbuy.finance.fuiou.dto.JdyValueDto;
import com.oigbuy.finance.fuiou.entity.req.FyBalanceQueryReqEntity;
import com.oigbuy.finance.fuiou.entity.req.FyBankTransferReqEntity;
import com.oigbuy.finance.fuiou.entity.req.FyQueryTransDetailReqEntity;
import com.oigbuy.finance.fuiou.entity.res.FyBalanceQueryResEntity;
import com.oigbuy.finance.fuiou.entity.res.FyBankTransferResEntity;
import com.oigbuy.finance.fuiou.entity.res.FyQueryTransDetailResEntity;
import com.oigbuy.finance.fuiou.entity.res.ResultVO;
import com.oigbuy.finance.fuiou.utils.FuIouSignatureUtils;
import com.oigbuy.finance.fuiou.vo.AccountBalanceVO;
import org.apache.shiro.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FyTransferCallService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private TokenFeignService tokenFeignService;

    @Value("${fy.bank-transfer.url}")
    private String bankTransferUrl;

    @Value("${fy.balance-query.url}")
    private String balanceQueryUrl;

    @Value("${fy.trans-detail-query.url}")
    private String transDetailQueryUrl;

    @Value("${auth.username}")
    private String authUsername;

    @Value("${auth.password}")
    private String authPassword;

    @Value("${dingding.fy-warning.client-id}")
    private String clientId;

    @Value("${fy.bank-transfer.req-param.isNotify}")
    private String isNotify;

    @Value("${fy.bank-transfer.req-param.isNeedReview}")
    private String isNeedReview;

    @Autowired
    private DingdingFeignService dingdingFeignService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private SrmTransferDetailDao srmTransferDetailDao;

    @Autowired
    private FyTransferDetailDao fyTransferDetailDao;

    @Autowired
    private SrmWithdrawalDetailSplitDao srmWithdrawalDetailSplitDao;

    @Autowired
    private FyAccountService fyAccountService;

    @Autowired
    private FyCallService fyCallService;

    @Value("${fy.work-notify.user-id.transfer-fail}")
    private String userIdList;   //通知

    @Value("${fy.jdy.transfail.appId}")
    private String appId;

    @Value("${fy.jdy.transfail.entryId}")
    private String entryId;

    @Autowired
    private JdyFeignService jdyFeignService;

    @Autowired
    private JdyFlowRecordDao jdyFlowRecordDao;

    public static final String TRANSFER_LOCK = FyConstants.LockKey.FY_TRANSFER_CONFIRM_LOCK;

    public TransferQueryBalanceVO transferBalanceQuery(TransferQueryBalanceDTO condition) {

        checkParam(condition);

        // 为确保余额精准，如果该商户下转账跑批未全部处理，友好提示不让操作转账
//        List<FyTransferDetailDTO> listByStatus = fyTransferDetailDao.getListByStatus(FyConstants.Transfer_Status.TO_TRANSFER);
//        if (!CollectionUtils.isEmpty(listByStatus)) {
//            throw new BusiException("存在" + listByStatus.size() + "笔付款清单后台在执行转账，请稍后再试！");
//        }
        // 查询待转账单
        List<SrmTransferDetail> list = getSrmTransferDetails(condition);

        // 校验并统计
        TransferQueryBalanceVO vo = checkAndStatistics(list);

        // 账户校验
        FyAccount fyAccount = fyAccountService.checkFyAccount(list.get(0).getFyAccountCode());

        // 调用富友余额查询
        FyBalanceQueryResEntity result;
        AccountBalanceVO account;
        try {
            result = callBalanceQuery(fyAccount);
            account = balanceQueryResultCheck(result);
        } catch (Exception e) {
            e.printStackTrace();
            dingDingWarning(FyConstants.DingWarningTitle.FY_CALL_EXCEPTION_TITLE, new ArrayList<String>() {
                {
                    add("#### 接口名称: 余额查询" + " \n  ");
                    add("> 接口地址：" + balanceQueryUrl + "  \n  ");
                    add("> 异常可能原因：" + e.getMessage() + "  \n  ");
                    add("> 商户号Code：" + condition.getFyAccountCode() + "  \n  ");
                    add("> 异常发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                }
            });
            throw new BusiException("余额查询失败");
        }
        int occupyAmount = fyTransferDetailDao.getCurrentOccupyAmount();
        vo.setBalance(BigDecimal.valueOf(account.getAvailableBalance()).divide(new BigDecimal(100)));
        vo.setOccupyAmount(BigDecimal.valueOf(occupyAmount).divide(new BigDecimal(100)));
        vo.setTheoryBalance(vo.getBalance().subtract(vo.getOccupyAmount()));
        vo.setSerCharge(new BigDecimal(1).multiply(BigDecimal.valueOf(vo.getTransCnt())));
        vo.setBalanceEnough((vo.getArrivalAmount().add(vo.getSerCharge())).compareTo(vo.getTheoryBalance()) == 1 ? false : true);
        vo.setPaymentAmount(vo.getArrivalAmount().add(vo.getSerCharge()));
        return vo;
    }

    private void checkParam(TransferQueryBalanceDTO condition) {
        if (condition == null) {
            throw new BusiException("请检查入参！");
        }

        if (CollectionUtils.isEmpty(condition.getIds())) {
            if (StringUtils.isEmpty(condition.getFyAccountCode())) {
                throw new BusiException("付款公司不能为空");
            }
            fyAccountService.checkFyAccount(condition.getFyAccountCode());
            if (condition.getDocumentStatus() == null || FyConstants.DocumentStatus.TO_TRANSFER != condition.getDocumentStatus()) {
                throw new BusiException("单据状态必须是待转账");
            }
        }
    }

    /**
     * 查询待转账单
     * @param condition
     * @return
     */
    private List<SrmTransferDetail> getSrmTransferDetails(TransferQueryBalanceDTO condition) {
        List<SrmTransferDetail> list;
        if (CollectionUtil.isNotEmpty(condition.getIds())) {
            list = srmTransferDetailDao.getListByIds(condition.getIds());
            if (list != null && list.size() != condition.getIds().size()) {
                throw new BusiException("请确认勾选的都是待转账，或者重新查询检查是否已操作转账");
            }
        } else {
            list = srmTransferDetailDao.getListByCondition(condition);
        }
        return list;
    }

    /**
     * 转账数据校验和统计
     *
     * @param list
     * @return
     */
    private TransferQueryBalanceVO checkAndStatistics(List<SrmTransferDetail> list) {
        checkTransList(list);
        TransferQueryBalanceVO vo = new TransferQueryBalanceVO();
        vo.setTransCnt(list.size());
        BigDecimal arrivalAmount = new BigDecimal(0);
        for (SrmTransferDetail srmTransferDetail : list) {
            arrivalAmount = arrivalAmount.add(srmTransferDetail.getArrivalAmount());
        }
        vo.setArrivalAmount(arrivalAmount);
        return vo;
    }

    private void checkTransList(List<SrmTransferDetail> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new BusiException("没有查到可转的付款清单");
        }
        /**
         * 产品说转账必须是同一家公司，防止出错。个人觉得。。。 2021年12月15日16:07:19
         */
        Set<String> set = list.stream().map(SrmTransferDetail::getFyAccountCode).collect(Collectors.toSet());
        if (set.size() != 1) {
            throw new BusiException("不支持多个付款公司同时转账！");
        }
    }

    private AccountBalanceVO balanceQueryResultCheck(FyBalanceQueryResEntity result) {
        if (result == null || !FyConstants.Version20.SUCCESS_CODE.equals(result.getRspCd())) {
            throw new BusiException("富友余额查询失败！响应状态码：" + result.getRspCd() + "，返回信息：" + result.getRspDesc() + "，错误描述信息：" + JSON.toJSONString(result.getErrorList()));
        }

        List<AccountBalanceVO> accountBalanceList = result.getAccountBalanceList();
        if (CollectionUtils.isEmpty(accountBalanceList)) {
            throw new BusiException("未查到富友系统账户");
        }

        AccountBalanceVO cnyAccount = null;
        for (AccountBalanceVO accountBalanceVO : accountBalanceList) {
            if (FyConstants.Version20.ACCOUNT_TYPE_CNY.equals(accountBalanceVO.getAccountType())) {
                cnyAccount = accountBalanceVO;
            }
        }

        if (cnyAccount == null) {
            throw new BusiException("未查到CNY账户");
        }
        return cnyAccount;
    }

    public FyBalanceQueryResEntity callBalanceQuery(FyAccount fyAccount) throws Exception {
        FyBalanceQueryReqEntity entity = new FyBalanceQueryReqEntity();
        entity.setMchntCd(fyAccount.getFyMchntCd());
        entity.setRandomStr(UUID.randomUUID().toString().replaceAll("-", ""));
        entity.setSign(FuIouSignatureUtils.generate20Sign(entity, fyAccount.getFySecretKey()));
        Map<String, String> param = new HashMap<>();
        param.put("reqData", JSON.toJSONString(entity));

        String post = HttpUtil.post(balanceQueryUrl, param);
        FyBalanceQueryResEntity result = JSONObject.parseObject(post, FyBalanceQueryResEntity.class);
        // 校验
        callResultCheck(result);
        return result;
    }

    public void dingDingWarning(String title, List<String> list) {
        if (CollectionUtil.isNotEmpty(list)) {
            StringBuffer text = new StringBuffer();
            for (String content : list) {
                text.append(content);
            }
            JsonResult<String> tokenResult = tokenFeignService.token(null, authUsername, authPassword, true);
            if ("200".equals(tokenResult.getCode())) {
                String token = tokenResult.getData().toString();
                logger.info("发送钉钉消息：" + token);
                OigMarkdownMessageDTO oigMarkdownMessageDTO = new OigMarkdownMessageDTO(clientId, title, text.toString(), FyConstants.NOTICE_YPH);
                try {
                    dingdingFeignService.sendMarkdown(token, oigMarkdownMessageDTO);
                } catch (DefaultException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void transferConfirm(TransferConfirmDTO dto) throws Exception {
        checkParam(dto);
        if (!RedisUtils.lock(TRANSFER_LOCK, 120 * 1000)) {
            throw new BusiException("服务器繁忙，请稍后重试");
        }

        try {
            List<SrmTransferDetail> srmTransferDetails = getSrmTransferDetails(dto);
            // 统计
            TransferQueryBalanceVO vo = checkAndStatistics(srmTransferDetails);

            // 校验付款单列表是否已发生改变
            checkIfChange(dto, vo);

            FyAccount fyAccount = fyAccountService.checkFyAccount(srmTransferDetails.get(0).getFyAccountCode());

            FyBalanceQueryResEntity result = null;
            Boolean retry = true;
            int retryCnt = 0;
            while (retry) {
                try {
                    result = callBalanceQuery(fyAccount);
                    retry = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    if (retryCnt == 1) {
                        dingDingWarning(FyConstants.DingWarningTitle.FY_CALL_EXCEPTION_TITLE, new ArrayList<String>() {
                            {
                                add("#### 接口名称: 余额查询" + " \n  ");
                                add("> 接口地址：" + balanceQueryUrl + "  \n  ");
                                add("> 商户号Code：" + dto.getFyAccountCode() + "  \n  ");
                                add("> 异常可能原因：" + e.getMessage() + "  \n  ");
                                add("> 异常发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                            }
                        });
                        throw new BusiException("余额校验失败");
                    }
                    ++retryCnt;
                    Thread.currentThread().sleep(1000);
                }
            }

            balanceTwiceCheck(result, dto);
            // 批量生成转账记录、批量更新付款单状态、批量更新单据状态
            createBankTransferRecord(srmTransferDetails, dto.getMchntMergeOrderId(), dto);
        } finally {
            RedisUtils.unLock(TRANSFER_LOCK);
        }
    }

    /**
     * 生成转账记录
     *
     * @param srmTransferDetails
     * @param mchntMergeOrderId
     * @param dto
     */
    private void createBankTransferRecord(List<SrmTransferDetail> srmTransferDetails, String mchntMergeOrderId, TransferConfirmDTO dto) {
        TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            // 批量生成转账记录
            List<FyTransferDetail> fyTransferDetails = new ArrayList<>();
            for (SrmTransferDetail srmTransferDetail : srmTransferDetails) {
                FyTransferDetail fyTransferDetail = new FyTransferDetail();
                fyTransferDetail.setMchntOrderId(srmTransferDetail.getMchntMergeOrderId());
                fyTransferDetail.setBankCardTp(srmTransferDetail.getBankCardType());
                fyTransferDetail.setBankCardNo(srmTransferDetail.getReceiptBankCardNo());
                fyTransferDetail.setOppName(srmTransferDetail.getReceiptName());
                fyTransferDetail.setOppIdNo(String.valueOf(srmTransferDetail.getReceiptIdcardNo()));
                fyTransferDetail.setAmt(srmTransferDetail.getArrivalAmount().multiply(new BigDecimal(100)).intValue());
                fyTransferDetail.setIsNotify(isNotify);
                fyTransferDetail.setOppMobile(srmTransferDetail.getReceiptTelNumber());
                if ("Y".equals(isNotify) && (StringUtils.isEmpty(fyTransferDetail.getOppMobile()) || fyTransferDetail.getOppMobile().length() != 11)) {
                    fyTransferDetail.setIsNotify("N");//手机格式有问题，就不通知
                }
                fyTransferDetail.setIsNeedReview(isNeedReview);
                fyTransferDetail.setStatus(FyConstants.TransferStatus.TO_TRANSFER);
                fyTransferDetail.setFyAccountCode(srmTransferDetail.getFyAccountCode());
                fyTransferDetails.add(fyTransferDetail);
            }
            int addCount = fyTransferDetailDao.insertBatch(fyTransferDetails);
            if (addCount != fyTransferDetails.size()) {
                throw new BusiException("转账单新增失败！新增数【" + addCount + "】，付款单笔数【" + fyTransferDetails.size() + "】");
            }

            // 批量更新付款单
            List<String> mergeOrderIds = srmTransferDetails.stream().map(SrmTransferDetail::getMchntMergeOrderId).collect(Collectors.toList());
            int payUpCount = srmTransferDetailDao.updateDocumentStatusBatch(FyConstants.DocumentStatus.TRANSFER_PROCESS, mergeOrderIds);
            if (payUpCount != mergeOrderIds.size()) {
                throw new BusiException("付款单更新失败！更新数【" + payUpCount + "】，付款单笔数【" + mergeOrderIds.size() + "】");
            }

            // 批量更新单据状态
            int splitUpCount = srmWithdrawalDetailSplitDao.updateDocumentStatusBatch(FyConstants.DocumentStatus.TRANSFER_PROCESS, mergeOrderIds);
            if (splitUpCount < 1) {
                throw new BusiException("单据更新失败！更新数【" + splitUpCount + "】");
            }

            transactionManager.commit(transaction);
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
            dingDingWarning(FyConstants.DingWarningTitle.FY_LOCAL_CACHE_TITLE, new ArrayList<String>() {{
                add("#### 接口地址: /fy/transferConfirm" + " \n  ");
                add("> 异常日志：" + e.getMessage() + "  \n  ");
                add("> 请求入参：" + JSON.toJSONString(dto) + "  \n  ");
                add("> 异常发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
            }});
            logger.error("----------异常原因：" + e.getMessage());
            throw new BusiException("操作失败，请联系管理员");
        }
    }

    /**
     * 富友余额二次校验
     *
     * @param result
     * @param dto
     */
    private void balanceTwiceCheck(FyBalanceQueryResEntity result, TransferConfirmDTO dto) {
        AccountBalanceVO account = balanceQueryResultCheck(result);
        BigDecimal availableBalance = BigDecimal.valueOf(account.getAvailableBalance()).divide(new BigDecimal(100));
        int occupyAmount = fyTransferDetailDao.getCurrentOccupyAmount();
        // 理论余额 = 富友余额 - 占用金额
        BigDecimal theoryBalance = availableBalance.subtract(BigDecimal.valueOf(occupyAmount).divide(new BigDecimal(100)));
        BigDecimal payAmount = dto.getArrivalAmount().add(BigDecimal.valueOf(dto.getTransCnt()).multiply(new BigDecimal(1)));
        if (payAmount.compareTo(theoryBalance) == 1) {
            throw new BusiException("余额不足，请重新操作确认");
        }
    }

    private void checkIfChange(TransferConfirmDTO dto, TransferQueryBalanceVO vo) {
        if (vo.getTransCnt().intValue() != dto.getTransCnt().intValue() || vo.getArrivalAmount().compareTo(dto.getArrivalAmount()) != 0) {
            throw new BusiException("付款单可能发生改变，请刷新重试");
        }
    }

    public void handleBankTransfer() {
        List<FyTransferDetailDTO> list = fyTransferDetailDao.getListByStatus(FyConstants.TransferStatus.TO_TRANSFER); // 查询待转账的转账单
        if (CollectionUtil.isNotEmpty(list)) {
            for (FyTransferDetailDTO dto : list) {
                FyBankTransferResEntity result;
                try {
//                    int i = 1 / 0; // 模拟转账异常
                    result = callBankTransfer(dto);
                } catch (Exception e) {
                    e.printStackTrace();
                    /**
                     * 网络原因或者富友接口异常，转账重试每达到10次，预警一次
                     */
                    long oldValue = RedisJsonUtils.getIncrValue("fy_trans_fail_" + dto.getMchntOrderId());
                    if (RedisJsonUtils.set("fy_trans_fail_" + dto.getMchntOrderId(), ++oldValue, 60 * 30)) {
                        if (oldValue % 10 == 1) {
                            dingDingWarning(FyConstants.DingWarningTitle.FY_CALL_EXCEPTION_TITLE, new ArrayList<String>() {
                                {
                                    add("#### 接口名称: 银行转账" + " \n  ");
                                    add("> 接口地址：" + bankTransferUrl + "  \n  ");
                                    add("> 转账编号：" + dto.getMchntOrderId() + "  \n  ");
                                    add("> 异常可能原因：" + e.getMessage() + "  \n  ");
                                    add("> 异常发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                                }
                            });
                        }

                    } else {
                        logger.error("redis 异常。。。。预警控制失效");
                        dingDingWarning(FyConstants.DingWarningTitle.FY_CALL_EXCEPTION_TITLE, new ArrayList<String>() {
                            {
                                add("#### 接口名称: 银行转账" + " \n  ");
                                add("> 接口地址：" + bankTransferUrl + "  \n  ");
                                add("> 转账编号：" + dto.getMchntOrderId() + "  \n  ");
                                add("> 异常可能原因：" + e.getMessage() + "  \n  ");
                                add("> 异常发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                            }
                        });
                    }
                    continue;
                }
                // 转账完成后处理
                handleAfterBankTransfer(result, dto);
            }

        }
    }

    /**
     * 银行转账之后处理
     *
     * @param result
     * @param dto
     */
    private void handleAfterBankTransfer(FyBankTransferResEntity result, FyTransferDetailDTO dto) {
        int status = FyConstants.Version20.SUCCESS_CODE.equals(result.getResultCode()) ? FyConstants.TransferStatus.TO_REVIEW : FyConstants.TransferStatus.TRANSFER_FAILURE;
        TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            // 转账失败增加工作通知 2021年12月23日14:42:37
            transferFailSendWorkMessage(status, dto.getMchntOrderId(), result.getErrCodeDes());

            // 更新转账单状态 = 转账中/失败
            updateTransStatus(status, result.getErrCodeDes(), dto.getMchntOrderId());

            // 待复核，单据表无需更新
            if (status == FyConstants.DocumentStatus.TRANSFER_FAILURE) {
                updateBillStatus(status, dto.getMchntOrderId(), null);
            }
            transactionManager.commit(transaction);
            if (status == FyConstants.TransferStatus.TRANSFER_FAILURE) {
                if ("INVALID_BANK_CARD_NO".equals(result.getErrCode()) || "INVALID_OPP_ID_NO".equals(result.getResultCode())
                        || "INVALID_OPP_NAME".equals(result.getResultCode())) {
                    createJdyFlow(dto.getMchntOrderId(), result.getErrCodeDes());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            transactionManager.rollback(transaction);
            dingDingWarning(FyConstants.DingWarningTitle.FY_LOCAL_CACHE_TITLE, new ArrayList<String>() {{
                add("#### 当前流程: 银行转账之后处理" + " \n  ");
                add("> 更新状态：" + status + "  \n  ");
                add("> 转账编号：" + dto.getMchntOrderId() + "  \n  ");
                add("> 异常日志：" + e.getMessage() + "  \n  ");
                add("> 异常发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
            }});
        }
    }

    private void transferFailSendWorkMessage(int status, String mchntOrderId, String failReason) {
        if (status == FyConstants.TransferStatus.TRANSFER_FAILURE) {
            try {
                fyCallService.sendWorkMessage(FyConstants.SendWorkMessage.FY_WORK_TITLE, new ArrayList<String>() {
                    {
                        add(FyConstants.SendWorkMessage.TRANSFER_FAILURE_TITLE);
                        add("> 转账编号：" + mchntOrderId + "  \n  ");
                        add("> 失败原因：" + failReason + "  \n  ");
                    }
                }, userIdList);
            } catch (Exception e) {
                dingDingWarning(FyConstants.DingWarningTitle.SEND_WORK_EXCEPTION_TITLE, new ArrayList<String>() {
                    {
                        add("#### 当前处理流程: " + FyConstants.SendWorkMessage.TRANSFER_FAILURE_TITLE + "  \n  ");
                        add("> 转账编号: " + mchntOrderId + "  \n  ");
                        add("> 异常可能原因：" + e.getMessage() + "  \n  ");
                        add("> 异常发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                    }
                });
                e.printStackTrace();
            }
        }
    }

    public void updateBillStatus(int status, String mchntOrderId, String paymentTime) {
        int stdUp = srmTransferDetailDao.updateDocumentStatusByMchntOrderId(status, mchntOrderId, paymentTime);
        if (stdUp != 1) {
            throw new BusiException("t_srm_transfer_detail更新校验失败！更新数【" + stdUp + "】");
        }

        int swdUp = srmWithdrawalDetailSplitDao.updateDocumentStatusByMergeId(status, mchntOrderId);
        if (swdUp < 1) {
            throw new BusiException("t_srm_withdrawal_detail_split更新校验失败！更新数【" + swdUp + "】");
        }
    }

    private void changeStatus(ResultVO resultVO, FyTransferDetailDTO dto) {
        int status = FyConstants.getTransStatus(resultVO.getTransState());
        if (status < 0) {
            throw new BusiException("不识别的交易状态：" + resultVO.getTransState());
        }
        /**
         * 交易成功或失败只会处理一次
         */
        if (dto.getStatus().intValue() != status) {
            TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                //转账失败工作通知 2021年12月23日14:47:53
                transferFailSendWorkMessage(status, resultVO.getMchntOrderId(), resultVO.getFailReason());

                updateTransStatus(status, resultVO.getFailReason(), resultVO.getMchntOrderId());
                if (status != FyConstants.TransferStatus.TRANSFER_PROCESS) {
                    updateBillStatus(status, resultVO.getMchntOrderId(), status == FyConstants.TransferStatus.TRANSFER_SUCCESS ? resultVO.getTransTime() : null);
                }
                transactionManager.commit(transaction);
            } catch (Exception e) {
                e.printStackTrace();
                transactionManager.rollback(transaction);
                dingDingWarning(FyConstants.DingWarningTitle.FY_LOCAL_CACHE_TITLE, new ArrayList<String>() {{
                    add("#### 当前流程: 交易明细查询后处理" + " \n  ");
                    add("> 更新状态：" + status + "  \n  ");
                    add("> 转账编号：" + resultVO.getMchntOrderId() + "  \n  ");
                    add("> 异常日志：" + e.getMessage() + "  \n  ");
                    add("> 异常发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                }});
            }
            /**
             * 转账失败推送简道云
             */
            if (FyConstants.TransferStatus.TRANSFER_FAILURE == status) {
                createJdyFlow(dto.getMchntOrderId(), resultVO.getFailReason());
            }
        }

    }

    private void updateTransStatus(int status, String failCause, String mchntOrderId) {
        int ftdUp = fyTransferDetailDao.updateStatus(status, failCause, mchntOrderId);
        if (ftdUp != 1) {
            throw new BusiException("t_fy_transfer_detail更新数=" + ftdUp + "，校验失败");
        }
    }

    private FyBankTransferResEntity callBankTransfer(FyTransferDetailDTO dto) throws Exception {
        FyBankTransferReqEntity reqEntity = new FyBankTransferReqEntity();
        reqEntity.setMchntCd(dto.getFyMchntCd());
        reqEntity.setRandomStr(UUID.randomUUID().toString().replaceAll("-", ""));
//        reqEntity.setBackNotifyUrl("http://23489u4u43.wicp.vip:46203/no-filter/fy/callback/fileNotice");
        reqEntity.setMchntOrderId(dto.getMchntOrderId());
        reqEntity.setBankCardTp(dto.getBankCardTp());
        reqEntity.setBankCardNo(dto.getBankCardNo());
        reqEntity.setOppName(dto.getOppName());
        reqEntity.setOppIdNo(dto.getOppIdNo());
        reqEntity.setAmt(dto.getAmt());
        reqEntity.setIsNotify(dto.getIsNotify());
        reqEntity.setOppMobile(dto.getOppMobile());
        reqEntity.setIsNeedReview(dto.getIsNeedReview());

        reqEntity.setSign(FuIouSignatureUtils.generate20Sign(reqEntity, dto.getFySecretKey()));
        Map<String, String> param = new HashMap<>();
        param.put("reqData", JSON.toJSONString(reqEntity));
        String post = HttpUtil.post(bankTransferUrl, param);
        FyBankTransferResEntity result = JSONObject.parseObject(post, FyBankTransferResEntity.class);
        callResultCheck(result);
        return result;
    }

    private void callResultCheck(Common20Response result) {
        if (result == null) {
            throw new BusiException("JSON解析失败");
        }
        if (!FyConstants.Version20.SUCCESS_CODE.equals(result.getRspCd())) {
            throw new BusiException("富友接口返回失败！响应状态码：" + result.getRspCd() + "，返回信息：" + result.getRspDesc() + "，错误描述信息：" + JSON.toJSONString(result.getErrorList()));
        }
    }

    public void queryTransferProgress() {
        List<FyTransferDetailDTO> list = fyTransferDetailDao.getTransferProgress();
        if (CollectionUtil.isNotEmpty(list)) {
            for (FyTransferDetailDTO dto : list) {
                FyQueryTransDetailResEntity result;
                try {
                    result = callQueryTransDetail(buildReqEntity(dto));
                } catch (IOException e) {
                    e.printStackTrace();
                    dingDingWarning(FyConstants.DingWarningTitle.FY_CALL_EXCEPTION_TITLE, new ArrayList<String>() {
                        {
                            add("#### 接口名称: 交易明细分页查询" + " \n  ");
                            add("> 接口地址：" + transDetailQueryUrl + "  \n  ");
                            add("> 转账编号：" + dto.getMchntOrderId() + "  \n  ");
                            add("> 异常日志：" + e.getMessage() + "  \n  ");
                            add("> 异常发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                        }
                    });
                    continue;
                }
                if (FyConstants.Version20.SUCCESS_CODE.equals(result.getResultCode())
                        && result.getResultList().size() == 1) {
                    // 业务处理
                    changeStatus(result.getResultList().get(0), dto);
                }

            }
        }
    }

    public FyQueryTransDetailResEntity callQueryTransDetail(FyQueryTransDetailReqEntity reqEntity) throws IOException {
        Map<String, String> param = new HashMap<>();
        param.put("reqData", JSON.toJSONString(reqEntity));
        String post = HttpUtil.post(transDetailQueryUrl, param);
        FyQueryTransDetailResEntity result = JSONObject.parseObject(post, FyQueryTransDetailResEntity.class);
        callResultCheck(result);
        return result;
    }

    private FyQueryTransDetailReqEntity buildReqEntity(FyTransferDetailDTO dto) {
        FyQueryTransDetailReqEntity reqEntity = new FyQueryTransDetailReqEntity();
        reqEntity.setMchntCd(dto.getFyMchntCd());
        reqEntity.setRandomStr(UUID.randomUUID().toString().replaceAll("-", ""));
        reqEntity.setPageNo(1);
        reqEntity.setMchntOrderId(dto.getMchntOrderId());
        reqEntity.setSign(FuIouSignatureUtils.generate20Sign(reqEntity, dto.getFySecretKey()));
        return reqEntity;
    }

    public TransferWarningVO transferWarning(TransferQueryBalanceDTO condition) {
        List<SrmTransferDetail> list = checkAndGet(condition);
        TransferWarningVO transferWarningVO = new TransferWarningVO();
        transferWarningVO.setTransAmountWarnList(list.stream().filter(s -> s.getArrivalAmount().compareTo(new BigDecimal("200000")) == 1)
                .sorted(Comparator.comparing(SrmTransferDetail::getReceiptIdcardNo)).collect(Collectors.toList()));
        transferWarningVO.setTransTimeWarnList(getTransTimeAndWarning(list));
        return transferWarningVO;
    }

    private List<TransferWarningEntity> getTransTimeAndWarning(List<SrmTransferDetail> transferDetails) {
        Set<String> idCards = transferDetails.stream().map(s -> s.getReceiptIdcardNo()).collect(Collectors.toSet());
        String endDate = OigDateUtils.formatDate(OigDateUtils.nextDay(1));
        String startDate = OigDateUtils.formatDate(OigDateUtils.nextDay(-6));
        return queryTransTimeWarnList(idCards, startDate, endDate);
    }

    private List<TransferWarningEntity> queryTransTimeWarnList(Set<String> idCards, String startDate, String endDate) {
        if (CollectionUtils.isEmpty(idCards)) {
            return null;
        }
        return fyTransferDetailDao.queryTransTimeWarnList(idCards, startDate, endDate);
    }

    private List<SrmTransferDetail> checkAndGet(TransferQueryBalanceDTO condition) {
        checkParam(condition);
        List<SrmTransferDetail> srmTransferDetails = getSrmTransferDetails(condition);
        checkTransList(srmTransferDetails);
        return srmTransferDetails;
    }

    /**
     * 转账失败发起简道云流程
     *
     * @param transferNo
     * @param failReason
     */
    public void createJdyFlow(String transferNo, String failReason) {
        FyTransferReceiptInfo receiptInfo = fyTransferDetailDao.getReceiptInfoByTransferNo(transferNo);
        JdyFlowRecord jdyRecord = new JdyFlowRecord();
        jdyRecord.setModuleId(transferNo);
        if (receiptInfo != null) {
            try {
                JsonResult<String> tokenResult = tokenFeignService.token(null, authUsername, authPassword, true);
                if (!"200".equals(tokenResult.getCode())) {
                    throw new BusiException("获取token异常");
                }
                JsonResult<String> jsonResult = jdyFeignService.createData(tokenResult.getData().toString(), buildJdyRequestDto(transferNo, failReason, receiptInfo));
                if (!"200".equals(jsonResult.getCode())) {
                    throw new BusiException("简道云流程创建，远程调用返回失败");
                }
                jdyRecord.setFlowStatus(FyConstants.flowStatus.CREATE_SUCCESS);
            } catch (Exception e) {
                e.printStackTrace();
                jdyRecord.setFlowStatus(FyConstants.flowStatus.CREATE_FAILURE);
                jdyRecord.setFailReason(e.getMessage().length() > 100 ? e.getMessage().substring(0, 100) : e.getMessage());
            }
            try {
                updateJdyRecord(jdyRecord);
            } catch (Exception e) {
                dingDingWarning(FyConstants.DingWarningTitle.CREATE_JDY_FLOW, new ArrayList<String>() {
                    {
                        add("#### 当前处理流程: " + FyConstants.WarningCurrentFlow.CREATE_JDY_RECORD + "  \n  ");
                        add("> jdyRecord实体: " + JSON.toJSONString(jdyRecord) + "  \n  ");
                        add("> 异常可能原因: " + e.getMessage() + "  \n  ");
                        add("> 异常发生时间: " + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                    }
                });
                e.printStackTrace();
            }
        }
    }

    private void updateJdyRecord(JdyFlowRecord jdyRecord) {
        JdyFlowRecord check = jdyFlowRecordDao.getByModuleId(jdyRecord.getModuleId(), FyConstants.flowStatus.CREATE_FAILURE, 1);
        int result;
        if (check != null) {
            jdyRecord.setId(check.getId());
            result = jdyFlowRecordDao.updateById(jdyRecord);
        } else {
            result = jdyFlowRecordDao.insert(jdyRecord);
        }
        if (result != 1) {
            throw new BusiException("简道云记录更新失败");
        }
    }

    private JdyCreateDataRequestDto buildJdyRequestDto(String transferNo, String failReason, FyTransferReceiptInfo receiptInfo) {
        JdyCreateDataRequestDto jdyCreateDataRequestDto = new JdyCreateDataRequestDto();
        jdyCreateDataRequestDto.setAppId(appId);
        jdyCreateDataRequestDto.setEntryId(entryId);
        JSONObject jsonObject = new JSONObject();
        JdyFyTransferFailDto data = new JdyFyTransferFailDto();
        data.setTransfer_no(new JdyValueDto(transferNo));
        data.setFail_reason(new JdyValueDto(failReason));
        data.setSupplier_name(new JdyValueDto(receiptInfo.getSupplierId()));
        data.setOriginal_payee(new JdyValueDto(receiptInfo.getOppName()));
        data.setOriginal_bank_card_no(new JdyValueDto(receiptInfo.getBankCardNo()));
        data.setOriginal_id_card(new JdyValueDto(receiptInfo.getOppIdNo()));
        jsonObject.put("data", data);
        jsonObject.put("is_start_workflow", true);
        jdyCreateDataRequestDto.setRequestJsonData(jsonObject);
        return jdyCreateDataRequestDto;
    }

}
