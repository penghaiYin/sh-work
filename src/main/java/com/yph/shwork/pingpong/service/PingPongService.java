package com.yph.shwork.pingpong.service;

import com.alibaba.fastjson.JSONObject;
import com.oigbuy.common.constant.OigFinanceConstant;
import com.oigbuy.common.exception.BusiException;
import com.oigbuy.common.http.HttpUtil;
import com.oigbuy.common.pojo.third.entity.pingpong.PingpongAccount;
import com.oigbuy.common.pojo.third.entity.pingpong.PingpongAccountInfo;
import com.oigbuy.common.pojo.third.entity.pingpong.PingpongBillDownload;
import com.oigbuy.common.pojo.third.entity.pingpong.PingpongWithdrawDetail;
import com.oigbuy.common.service.DingDingWarningService;
import com.oigbuy.common.utils.DateUtils;
import com.oigbuy.common.utils.StringUtils;
import com.oigbuy.pingpong.constant.PingPongConstant;
import com.oigbuy.pingpong.dao.PingpongAccountDao;
import com.oigbuy.pingpong.dao.PingpongAccountInfoDao;
import com.oigbuy.pingpong.dao.PingpongBillDownloadDao;
import com.oigbuy.pingpong.dao.PingpongWithdrawDetailDao;
import com.oigbuy.pingpong.dto.StoreInfo;
import com.oigbuy.pingpong.dto.StoreWater;
import com.oigbuy.pingpong.entity.*;
import com.oigbuy.pingpong.utils.PingPongSignUtils;
import org.apache.shiro.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PingPongService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private PingpongAccountDao pingpongAccountDao;

    @Autowired
    private PingpongBillDownloadDao pingpongBillDownloadDao;

    @Autowired
    private PingpongWithdrawDetailDao withdrawDetailDao;

    @Value("${pingpong.api_prefix_path}")
    private String apiPrefixPath;

    @Value("${pingpong.app_id}")
    private String appId;

    @Value("${pingpong.app_secret}")
    private String appSecret;

    @Value("${pingpong.client_id}")
    private String clientId;

    private static final int BATCH_COUNT = 1000;

    @Autowired
    private DingDingWarningService dingDingWarningService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private PingpongAccountInfoDao pingpongAccountInfoDao;

    public void sendGetStoreInfo(List<StoreInfo> dataList, int pageNo) {
        GetStoreInfoResponse getStoreInfoResponse = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("app_id", appId);
            params.put("app_secret", appSecret);
            params.put("client_id", clientId);
            params.put("pg_no", pageNo);
            String url = buildUrl(params, PingPongConstant.GET_STORE_URL);
            logger.info("店铺拉取请求地址: " + url);
            String response = HttpUtil.post(url, null);
            getStoreInfoResponse = JSONObject.parseObject(response, GetStoreInfoResponse.class);

            checkResponse(getStoreInfoResponse);

            GetStoreInfoData data = getStoreInfoResponse.getData();
            if (data == null) {
                return;
            }
            dataList.addAll(data.getStoreInfoList());
            if (data.getTotal_pg() > pageNo) {
                sendGetStoreInfo(dataList, ++pageNo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String exception = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            dingDingWarningService.sendDingDing(PingPongConstant.DingWarningTitle.STORE_INFO_PULL_EXCEPTION, new ArrayList<String>() {
                {
                    add("#### 当前流程: PingPong 店铺信息拉取异常" + " \n  ");
                    add("> 异常可能原因：" + exception + "  \n  ");
                    add("> 发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                }
            }, OigFinanceConstant.NOTICE_YPH);
            throw new BusiException("PingPong 店铺信息拉取异常");
        }
    }

    private String buildUrl(Map<String, Object> params, String requestPath) throws NoSuchAlgorithmException {
        PingPongSignUtils.generateSign(params, appSecret);
        StringBuffer url = new StringBuffer();
        url.append(apiPrefixPath).append(requestPath).append("?");
        for (String key : params.keySet()) {
            if (!url.toString().endsWith("?")) {
                url.append(OigFinanceConstant.DELIMITER);
            }
            url.append(key).append("=").append(params.get(key));
        }
        return url.toString();
    }

    public void saveAccounts(List<StoreInfo> storeInfos) {
        List<PingpongAccount> list = storeInfoTransForm(storeInfos);
        List<PingpongAccount> existList = pingpongAccountDao.getList();
        if (!CollectionUtils.isEmpty(existList)) {
            // 过滤已存在的，防止重复插入
            Set<String> collect = existList.stream().map(PingpongAccount::getAccountIdList).collect(Collectors.toSet());
            list = list.stream().filter(a -> collect.add(a.getAccountIdList())).collect(Collectors.toList());
        }

        // 编程式事务
        TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            if (list.size() < BATCH_COUNT) {
                saveBatch(list);
            } else {
                int time = list.size() % BATCH_COUNT == 0 ? list.size() / BATCH_COUNT : (list.size() / BATCH_COUNT) + 1;
                for (int i = 0; i < time; i++) {
                    List<PingpongAccount> childList = list.subList(i * BATCH_COUNT, (i + 1) * BATCH_COUNT > list.size() ? list.size() : (i + 1) * BATCH_COUNT);
                    saveBatch(childList);
                }
            }
            transactionManager.commit(transaction);
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
            String exception = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            dingDingWarningService.sendDingDing(PingPongConstant.DingWarningTitle.STORE_INFO_PULL_EXCEPTION, new ArrayList<String>() {
                {
                    add("#### 当前流程: PingPong 店铺信息入库异常" + " \n  ");
                    add("> 异常可能原因：" + exception + "  \n  ");
                    add("> 发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                }
            }, OigFinanceConstant.NOTICE_YPH);
            throw new BusiException("PingPong 店铺信息入库异常");
        }
    }

    public void saveBatch(List<PingpongAccount> list) {
        pingpongAccountDao.insertBatch(list);
    }

    private List<PingpongAccount> storeInfoTransForm(List<StoreInfo> list) {
        List<PingpongAccount> accounts = new ArrayList<>();
        for (StoreInfo storeInfo : list) {
            PingpongAccount account = new PingpongAccount();
            account.setAccountIdList(StringUtils.join(storeInfo.getAccount_id_list(), ","));
            account.setAliasName(storeInfo.getAlias_name());
            account.setPlatform(storeInfo.getPlatform());
            account.setSellerId(storeInfo.getSeller_id());
            account.setStoreName(storeInfo.getStore_name());
            accounts.add(account);
        }
        return accounts;
    }

    public void sendGetStoreWater(String updateTimeStart, String updateTimeEnd) {
        Set<String> accountIds = getAllAccountIds();
        // 查询所有店铺流水，如果某个店铺拉取异常，后续走补偿
        for (String accountId : accountIds) {
            callGetStoreWater(updateTimeStart, updateTimeEnd, accountId);
        }
    }

    private Set<String> getAllAccountIds() {
        List<PingpongAccount> accounts = pingpongAccountDao.getList();
        Set<String> accountIds = getAccountIds(accounts);
        return accountIds;
    }

    private void callGetStoreWater(String updateTimeStart, String updateTimeEnd, String accountId) {
        // 编程式事务
        TransactionStatus transaction = null;
        GetStoreWaterResponse getStoreWaterResponse = null;
        String url = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("app_id", appId);
            params.put("app_secret", appSecret);
            params.put("client_id", clientId);
            params.put("account_id", accountId);
            params.put("start_time", updateTimeStart + "000000");
            params.put("end_time", updateTimeEnd + "000000");
            url = buildUrl(params, PingPongConstant.GET_STORE_WATER_URL);
            logger.info("PingPong 店铺流水请求接口: " + url);
            String response = HttpUtil.post(url, null);
            getStoreWaterResponse = JSONObject.parseObject(response, GetStoreWaterResponse.class);

            checkResponse(getStoreWaterResponse);

            GetStoreWaterData data = getStoreWaterResponse.getData();
            if (!CollectionUtils.isEmpty(data.getFlowList())) {
                transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
                List<StoreWater> flowList = data.getFlowList();
                List<PingpongBillDownload> billDownloads = transForm(flowList);
                saveBillDownloads(billDownloads);
            }
            if (transaction != null) {
                transactionManager.commit(transaction);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (transaction != null) {
                transactionManager.rollback(transaction);
            }
            String exception = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            dingDingWarningService.sendDingDing(PingPongConstant.DingWarningTitle.STORE_WATER_PULL_EXCEPTION, new ArrayList<String>() {
                {
                    add("#### 当前流程: PingPong 店铺流水拉取异常" + " \n  ");
                    add("> 店铺ID：" + accountId + "  \n  ");
                    add("> 开始日期：" + updateTimeStart + "  \n  ");
                    add("> 结束日期：" + updateTimeEnd + "  \n  ");
                    add("> 异常可能原因：" + exception + "  \n  ");
                    add("> 发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                }
            }, OigFinanceConstant.NOTICE_YPH);
        }

    }

    public void saveBillDownloads(List<PingpongBillDownload> list) {
        if (list.size() < BATCH_COUNT) {
            pingpongBillDownloadDao.insertBatch(list);
            return;
        }
        int time = list.size() % BATCH_COUNT == 0 ? list.size() / BATCH_COUNT : (list.size() / BATCH_COUNT) + 1;
        for (int i = 0; i < time; i++) {
            List<PingpongBillDownload> childList = list.subList(i * BATCH_COUNT, (i + 1) * BATCH_COUNT > list.size() ? list.size() : (i + 1) * BATCH_COUNT);
            pingpongBillDownloadDao.insertBatch(childList);
        }
    }

    private List<PingpongBillDownload> transForm(List<StoreWater> flowList) {
        List<PingpongBillDownload> list = new ArrayList<>();
        for (StoreWater storeWater : flowList) {
            PingpongBillDownload billDownload = new PingpongBillDownload();
            billDownload.setAmount(new BigDecimal(storeWater.getAmount()));
            billDownload.setAccountId(storeWater.getAccount_id());
            billDownload.setClientId(storeWater.getClient_id());
            billDownload.setClosingBalance(new BigDecimal(storeWater.getClosing_balance()));
            billDownload.setCurrency(storeWater.getCurrency());
            billDownload.setPlatform(storeWater.getPlatform());
            billDownload.setTransferTime(storeWater.getTransfer_time());
            billDownload.setTransferType(storeWater.getTransfer_type());
            billDownload.setTxId(storeWater.getTx_id());
            list.add(billDownload);
        }
        return list;
    }


    private void checkResponse(CommonResponse response) {
        if (response == null) {
            throw new BusiException("接口响应报文解析为空");
        }
        if (!PingPongConstant.API_SUCCESS_CODE.equals(response.getCode())) {
            throw new BusiException("接口响应报文返回失败，错误码：" + response.getCode() + "，错误信息：" + response.getMessage());
        }
    }

    private Set<String> getAccountIds(List<PingpongAccount> accounts) {
        Set<String> accountIds = new HashSet<>();
        for (PingpongAccount account : accounts) {
            if (StringUtils.isEmpty(account.getAccountIdList())) {
                continue;
            }
            if (account.getAccountIdList().contains(",")) {
                accountIds.addAll(Arrays.asList(account.getAccountIdList().split(",")));
            } else {
                accountIds.add(account.getAccountIdList());
            }
        }
        return accountIds;

    }

    public void sendGetWithdrawDetail(String updateTimeStart, String updateTimeEnd) {
        Set<String> set = pingpongBillDownloadDao.getWithdrawTxIds(updateTimeStart + "000000", updateTimeEnd + "000000");
        List<String> txIds = new ArrayList<>(set);
        if (CollectionUtils.isEmpty(txIds)) {
            logger.info("PingPong 未查询到提现明细");
            return;
        }
        // 编程式事务
        TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            if (txIds.size() < BATCH_COUNT) {
                callWithdrawDetail(txIds);
            } else {
                int time = txIds.size() % BATCH_COUNT == 0 ? txIds.size() / BATCH_COUNT : (txIds.size() / BATCH_COUNT) + 1;
                for (int i = 0; i < time; i++) {
                    logger.info("批次数: " + time);
                    List<String> childList = txIds.subList(i * BATCH_COUNT, (i + 1) * BATCH_COUNT > txIds.size() ? txIds.size() : (i + 1) * BATCH_COUNT);
                    callWithdrawDetail(childList);
                }
            }
            transactionManager.commit(transaction);
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
            String exception = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            dingDingWarningService.sendDingDing(PingPongConstant.DingWarningTitle.WITHDRAW_DETAIL_PULL_EXCEPTION, new ArrayList<String>() {
                {
                    add("#### 当前流程: PingPong 提现明细拉取异常" + " \n  ");
                    add("> 开始日期：" + updateTimeStart + "  \n  ");
                    add("> 结束日期：" + updateTimeEnd + "  \n  ");
                    add("> 异常可能原因：" + exception + "  \n  ");
                    add("> 发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                }
            }, OigFinanceConstant.NOTICE_YPH);
        }
    }

    private void callWithdrawDetail(List<String> txIds) throws Exception {
        List<GetWithdrawDetailData> result = new ArrayList<>();
        for (String txId : txIds) {
            callGetWithdrawDetail(result, txId);
        }
        if (!CollectionUtils.isEmpty(result)) {
            List<PingpongWithdrawDetail> details = new ArrayList<>();
            for (GetWithdrawDetailData data : result) {
                PingpongWithdrawDetail entity = new PingpongWithdrawDetail();
                entity.setBank(data.getBank());
                entity.setCard(data.getCard());
                entity.setWithdrawTime(data.getWithdraw_time());
                entity.setWithdrawType(data.getWithdraw_type());
                entity.setWithdrawAmount(new BigDecimal(data.getWithdraw_amount()));
                entity.setPlatform(data.getPlatform());
                entity.setFee(new BigDecimal(data.getFee()));
                entity.setFeeRate(data.getFee_rate());
                entity.setWithdrawCurrency(data.getWithdraw_currency());
                entity.setPaidAmount(new BigDecimal(data.getPaid_amount()));
                entity.setPaidCurrency(data.getPaid_currency());
                entity.setTxId(data.getTx_id());
                details.add(entity);
            }
            // 批量处理
            withdrawDetailDao.insertBatch(details);
        }
    }

    private void callGetWithdrawDetail(List<GetWithdrawDetailData> result, String txId) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("app_id", appId);
        params.put("app_secret", appSecret);
        params.put("tx_id", txId);
        String url = buildUrl(params, PingPongConstant.GET_WITHDRAW_DETAIL_URL);
        logger.info("PingPong 提现明细请求报文: " + url);
        String response = HttpUtil.post(url, null);
        GetWithdrawDetailResponse responseEntity = JSONObject.parseObject(response, GetWithdrawDetailResponse.class);

        checkResponse(responseEntity);

        GetWithdrawDetailData data = responseEntity.getData();
        if (data != null) {
            result.add(data);
        }
    }

    public void storeWaterOffset(String updateTimeStart, String updateTimeEnd, String accountId) {
        callGetStoreWater(updateTimeStart, updateTimeEnd, accountId);
    }

    public void getStoreInfoDetail() throws Exception {
        // 查询所有店铺
        Set<String> allAccountIds = getAllAccountIds();
        // 循环调用pingpong接口查询所有店铺详情
        List<GetStoreInfoDetailData> result = new ArrayList<>();
        for (String accountId : allAccountIds) {
            callGeStoreInfoDetail(result, accountId);
        }
        List<PingpongAccountInfo> list = storeInfoDetailTransForm(result);
        List<PingpongAccountInfo> existList = pingpongAccountInfoDao.getList();
        if (!CollectionUtils.isEmpty(existList)) {
            // 如果库里有存在的，要过滤防止重复插入
            Set<String> collect = existList.stream().map(PingpongAccountInfo::getAccountId).collect(Collectors.toSet());
            list = list.stream().filter(a -> collect.add(a.getAccountId())).collect(Collectors.toList());
        }
        saveAccountInfoDetailBatch(list);
    }

    private void saveAccountInfoDetailBatch(List<PingpongAccountInfo> list) {
        // 编程式事务 + 拆分
        TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            if (list.size() < BATCH_COUNT) {
                pingpongAccountInfoDao.insertBatch(list);
            } else {
                int time = list.size() % BATCH_COUNT == 0 ? list.size() / BATCH_COUNT : (list.size() / BATCH_COUNT) + 1;
                for (int i = 0; i < time; i++) {
                    logger.info("批次数: " + time);
                    List<PingpongAccountInfo> childList = list.subList(i * BATCH_COUNT, (i + 1) * BATCH_COUNT > list.size() ? list.size() : (i + 1) * BATCH_COUNT);
                    pingpongAccountInfoDao.insertBatch(childList);
                }
            }
            transactionManager.commit(transaction);
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            e.printStackTrace();
            String exception = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            dingDingWarningService.sendDingDing(PingPongConstant.DingWarningTitle.STORE_INFO_DETAIL_PULL_EXCEPTION, new ArrayList<String>() {
                {
                    add("#### 当前流程: PingPong 店铺详情拉取" + " \n  ");
                    add("> 异常可能原因：" + exception + "  \n  ");
                    add("> 发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                }
            }, OigFinanceConstant.NOTICE_YPH);
        }
    }


    private List<PingpongAccountInfo> storeInfoDetailTransForm(List<GetStoreInfoDetailData> result) {
        List<PingpongAccountInfo> list = new ArrayList<>();
        for (GetStoreInfoDetailData data : result) {
            PingpongAccountInfo entity = new PingpongAccountInfo();
            entity.setClientId(data.getClient_id());
            entity.setAccountId(data.getAccount_id());
            entity.setNation(data.getNation());
            if (data.getBank_info() != null) {
                entity.setCard(data.getBank_info().getCard());
                entity.setIban(data.getBank_info().getIban());
                entity.setBankName(data.getBank_info().getBank_name());
                entity.setBankCode(data.getBank_info().getBank_code());
                entity.setBranchNumber(data.getBank_info().getBranch_number());
                entity.setSortCode(data.getBank_info().getSort_code());
                entity.setSwiftCode(data.getBank_info().getSwift_code());
                entity.setRoutingNumber(data.getBank_info().getRouting_number());
                entity.setAccountType(data.getBank_info().getAccount_type());
            }
            list.add(entity);
        }
        return list;
    }

    private void callGeStoreInfoDetail(List<GetStoreInfoDetailData> result, String accountId) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("app_id", appId);
        params.put("app_secret", appSecret);
        params.put("client_id", clientId);
        params.put("account_id", accountId);
        String url = buildUrl(params, PingPongConstant.GET_STORE_INFO_URL);
        logger.info("PingPong 拉取店铺详情请求报文: " + url);
        String response = HttpUtil.post(url, null);
        GetStoreInfoDetailResponse responseEntity = JSONObject.parseObject(response, GetStoreInfoDetailResponse.class);

        checkResponse(responseEntity);

        GetStoreInfoDetailData data = responseEntity.getData();
        if (data != null) {
            result.add(data);
        }
    }
}
