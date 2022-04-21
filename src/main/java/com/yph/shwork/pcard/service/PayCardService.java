package com.yph.shwork.pcard.service;


import com.yph.shwork.common.constant.OigFinanceConstant;
import com.yph.shwork.common.service.DingDingWarningService;
import com.yph.shwork.common.utils.ThreadPoolTool;
import com.yph.shwork.pcard.constant.PayCardConstant;
import com.yph.shwork.pcard.dao.PaycardBillDetailDao;
import com.yph.shwork.pcard.dao.PaycardBillDownloadDao;
import com.yph.shwork.pcard.dao.ThirdAccessTokenDao;
import com.yph.shwork.pcard.entity.ItemEntity;
import com.yph.shwork.pingpong.entity.ThirdAccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@Service
public class PayCardService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("paycard.client_id")
    private String ApiClientId;

    @Value("paycard.client_secret")
    private String clientSecret;

    @Value("paycard.redirect_uri")
    private String redirectUri;

    @Value("paycard.base_authorize_url")
    private String baseAuthorizeUrl;

    @Value("paycard.base_open_url")
    private String baseOpenUrl;

    private final RestTemplate restTemplate;

    private static final int SINGLE_COUNT = 5000;

    private static final int BATCH_COUNT = 1000;

    private static final String THREAD_NAME = "P卡交易流水处理thread";

    @Autowired
    private ThirdAccessTokenDao thirdAccessTokenDao;

    @Autowired
    private PaycardBillDownloadDao paycardBillDownloadDao;

    @Autowired
    private PaycardBillDetailDao paycardBillDetailDao;

    @Autowired
    private DingDingWarningService dingDingWarningService;

    public PayCardService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }


    public void sendAuthorize() {
        StringBuffer authorizeUrl = new StringBuffer();
        authorizeUrl.append(baseAuthorizeUrl).append(PayCardConstant.AUTHORIZE_URL_PATH).append("?client_id=").append(ApiClientId).append("&redirect_uri=")
                .append(redirectUri).append("&scope=read write openid personal-details&response_type=code");
        ResponseEntity<String> result = restTemplate.getForEntity(authorizeUrl.toString(), String.class);
        if (200 != result.getStatusCodeValue()) {
            dingDingWarningService.sendDingDing(PayCardConstant.DingWarningTitle.PKA_CALL_FAILURE, new ArrayList<String>() {
                {
                    add("#### 接口名称: 用户授权" + " \n  ");
                    add("> 接口地址：" + authorizeUrl.toString() + "  \n  ");
                    add("> 接口响应状态码：" + result.getStatusCodeValue() + "  \n  ");
                    add("> 发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                }
            }, OigFinanceConstant.NOTICE_YPH);
            throw new BusiException("用户授权失败");
        }
    }

    public void sendAccessToken(String code, String error) {
        StringBuffer accessTokenUrl = new StringBuffer();
        accessTokenUrl.append(baseAuthorizeUrl).append(PayCardConstant.ACCESS_TOKEN_URL_PATH);
        try {
            checkParam(code, error);
            Map<Object, Object> param = new HashMap<>();
            param.put("grant_type", "authorization_code");
            param.put("code", code);
            param.put("redirect_uri", redirectUri);
            param.put("client_id", ApiClientId);
            param.put("client_secret", clientSecret);
            ResponseEntity<ThirdAccessToken> result = restTemplate.postForEntity(accessTokenUrl.toString(), HttpMethod.POST, ThirdAccessToken.class, param);
            if (200 != result.getStatusCodeValue()) {
                throw new RuntimeException("P卡token请求失败！");
            }

            ThirdAccessToken thirdAccessToken = result.getBody();
            if (thirdAccessToken == null) {
                throw new BusiException("接口返回结果为空");
            }
            if (thirdAccessTokenDao.insert(thirdAccessToken) != 1) {
                throw new BusiException("Body: " + JSON.toJSONString(thirdAccessToken) + "。数据库保存，校验失败");
            }
        } catch (Exception e) {
            dingDingWarningService.sendDingDing(PayCardConstant.DingWarningTitle.PKA_CALL_FAILURE, new ArrayList<String>() {
                {
                    add("#### 异常流程: 请求访问令牌" + " \n  ");
                    add("> 接口地址：" + accessTokenUrl.toString() + "  \n  ");
                    add("> 异常可能原因：" + e.getMessage() + "  \n  ");
                    add("> 发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                }
            }, OigFinanceConstant.NOTICE_YPH);
            throw new BusiException("授权回调失败");
        }
    }

    public void checkParam(String code, String error) {
        if (StringUtils.isNotEmpty(error)) {
            throw new BusiException("授权回调失败通知: " + error);
        }
        if (StringUtils.isEmpty(code)) {
            throw new BusiException("授权回调code不存在");
        }
    }


    public void sendRefreshToken() {
        StringBuffer accessTokenUrl = new StringBuffer();
        accessTokenUrl.append(baseAuthorizeUrl).append(PayCardConstant.ACCESS_TOKEN_URL_PATH);
        try {
            ThirdAccessToken thirdAccessToken = thirdAccessTokenDao.queryAccessToken(1);
            if (thirdAccessToken == null) {
                throw new BusiException("P卡token不存在，无法刷新");
            }
            Date futureExpireDate = DateUtils.addDays(thirdAccessToken.getUpdateTime(), (thirdAccessToken.getExpiresIn() / (60 * 60 * 24)) + 1);
            // 在即将过期的前一天刷新token
            if (futureExpireDate.after(new Date())) {
                Map<Object, Object> param = new HashMap<>();
                param.put("grant_type", "refresh_token");
                param.put("refresh_token", thirdAccessToken.getRefreshToken());
                ResponseEntity<ThirdAccessToken> result = restTemplate.postForEntity(accessTokenUrl.toString(), HttpMethod.POST, ThirdAccessToken.class, param);

                if (200 != result.getStatusCodeValue()) {
                    throw new RuntimeException("P卡token请求失败！");
                }

                if (result.getBody() == null) {
                    throw new BusiException("接口返回结果为空");
                }

                if (thirdAccessTokenDao.updateById(result.getBody()) < 1) {
                    throw new BusiException("Body: +" + JSON.toJSONString(result.getBody()) + "。数据库更新，校验失败");
                }
            }
        } catch (Exception e) {
            dingDingWarningService.sendDingDing(PayCardConstant.DingWarningTitle.PKA_CALL_FAILURE, new ArrayList<String>() {
                {
                    add("#### 异常流程: 刷新访问令牌" + " \n  ");
                    add("> 接口地址：" + accessTokenUrl.toString() + "  \n  ");
                    add("> 异常可能原因：" + e.getMessage() + "  \n  ");
                    add("> 发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                }
            }, OigFinanceConstant.NOTICE_YPH);
            throw new BusiException("刷新访问令牌失败");
        }

    }

    /**
     * @param updateTimeStart 2022-03-01
     * @param updateTimeEnd   2022-03-10
     */
    public void sendGetTransaction(String updateTimeStart, String updateTimeEnd) {
        ThirdAccessToken thirdAccessToken = thirdAccessTokenDao.queryAccessToken(1);
        String accountId = decodeAccountId(thirdAccessToken.getIdToken());
        callGetTransaction(thirdAccessToken, updateTimeStart, updateTimeEnd, accountId, 10000);
    }

    private void callGetTransaction(ThirdAccessToken thirdAccessToken, String updateTimeStart, String updateTimeEnd, String accountId, int pageSize) {
        /**
         * 按天拉取，分页大小2万，尽量减少系统交互。
         * 汇总一天的数据。多线程插入
         * 如果期间出现异常，提供补偿机制，重新拉取（判断是否存在）
         */
        String currentPullDate = updateTimeStart;
        boolean flag = true;
        List<ItemEntity> taskList = null;
        int pullNum = 0;
        ThreadPoolTool<ItemEntity> tool = null;
        boolean open = true;//控制线程池创建的开关
        try {
            while (flag) {
                if (Long.valueOf(currentPullDate) <= Long.valueOf(updateTimeEnd)) {
                    String pullTimeFormat = currentPullDate.substring(0, 4) + "-" + currentPullDate.substring(4, 6) + "-" + currentPullDate.substring(6, 8);
                    String reqUrl = buildReqUrl(pullTimeFormat, accountId, pageSize);
                    List<ItemEntity> dataList = new ArrayList<>();
                    sendReq(thirdAccessToken.getAccessToken(), reqUrl, dataList);
                    int size = dataList.size();
                    logger.info("P卡当前拉取时间：{}。查询到的交易流水数量：{}", pullTimeFormat, size);
                    // 将创建时间+1, 进行下一次的拉取
                    currentPullDate = DateUtils.formatDate(DateUtils.addDays(DateUtils.strToDate(pullTimeFormat), 1), "yyyyMMdd");
                    if (size > 0) {
//                    transform(taskList);// 数据转换
//                    logger.info("数据过滤之后的数量：{}", taskList.size());
                        pullNum += taskList.size();
                        if (taskList.size() > SINGLE_COUNT) {
                            if (open) {
                                tool = new ThreadPoolTool(SINGLE_COUNT, THREAD_NAME);
                                tool.setCallBack((ThreadPoolTool.CallBack<ItemEntity>) list -> {
                                    processBatch(list);
                                });
                                logger.info("----------初始化线程池--------");
                                open = false;
                            }
                            tool.setList(taskList);
                            CountDownLatch countDownLatch = new CountDownLatch(1);
                            tool.setOuter(countDownLatch);
                            try {
                                tool.execute();
                                countDownLatch.await();
                            } catch (Exception e) {
                                e.printStackTrace();
                                shutdown(tool);
                                return;
                            } finally {
                                taskList = clear(taskList);
                            }
                        } else {
                            processBatch(taskList);
                        }
                    }
                } else {
                    flag = false;
                    logger.info("P卡交易流水执行结束：{}-{}, 已完成。累计拉取：{}", updateTimeStart, updateTimeEnd, pullNum);
                }
            }
        } finally {
            shutdown(tool);
        }
    }

//    private void transform(List<ItemEntity> taskList) {
//        for (ItemEntity itemEntity : taskList) {
//
//        }
//    }

    private void sendReq(String accessToken, String reqUrl, List<ItemEntity> dataList) {
        HashMap headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        String callResult = null;
        try {
            callResult = HttpUtil.get(reqUrl, null, headers);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("调用P卡交易API发生异常");
        }
        GetTransactionsResponse response = JSONObject.parseObject(callResult, GetTransactionsResponse.class);

        if (response == null) {
            throw new RuntimeException("P卡交易API返回结果，解析失败");
        }

        dataList.addAll(response.getResult().getTransactions().getItems());
        String next = response.getResult().getTransactions().getNext();
        // 请求下一页
        if (StringUtils.isNotEmpty(next)) {
            sendReq(accessToken, baseOpenUrl + next, dataList);
        }
    }

    private String buildReqUrl(String pullTime, String accountId, int pageSize) {
        StringBuffer url = new StringBuffer();
        StringBuilder pathBuilder = new StringBuilder(PayCardConstant.GET_TRANSACTION_URL_PATH);
        url.append(baseOpenUrl).append(pathBuilder.replace(10, 22, accountId))
                .append("?page_size=").append(pageSize).append("&from=").append(pullTime).append("T00:00:00.000Z")
                .append("&to=").append(pullTime).append("T23:59:59.999Z").append("&include_details=true");
        return url.toString();
    }

    private String decodeAccountId(String idToken) {
        //TODO 密钥待提供
        Claims claims = Jwts.parser()
                .setSigningKey("")
                .parseClaimsJws(idToken).getBody();

        if (!claims.containsKey("account_id")) {
            throw new BusiException("账户ID不存在");
        }
        String accountId = claims.get("account_id", String.class);
        return accountId;
    }



//    public static void main(String[] args) {
//        GetTransactionsResponse response = JSONObject.parseObject(null, GetTransactionsResponse.class);
//        if (response == null) {
//            throw new RuntimeException("查询P卡交易响应解析失败");
//        }
//    }

    /**
     * 处理odoo出库单信息
     */
    public void processBatch(List<ItemEntity> list) {
        // 这里考虑到性能，不做事务，特殊情况异常发预警走补偿
        try {
            logger.info(Thread.currentThread().getName() + "，正在处理...");
            // 性能考虑：分批次，批量更新，每批次处理1000
            if (list.size() < BATCH_COUNT) {
                saveBatch(list);
                return;
            }
            int time = list.size() % BATCH_COUNT == 0 ? list.size() / BATCH_COUNT : (list.size() / BATCH_COUNT) + 1;
            for (int i = 0; i < time; i++) {
                List<ItemEntity> itemEntities = list.subList(i * BATCH_COUNT, (i + 1) * BATCH_COUNT > list.size() ? list.size() : (i + 1) * BATCH_COUNT);
                saveBatch(itemEntities);
            }
        }catch (Exception e){
            dingDingWarningService.sendDingDing(PayCardConstant.DingWarningTitle.PKA_CALL_FAILURE, new ArrayList<String>() {
                {
                    add("#### 异常流程: P卡交易流水入库" + " \n  ");
                    add("> 异常可能原因：" + e.getMessage() + "  \n  ");
                    add("> 发生时间：" + DateUtils.getDate("yyyy-MM-dd HH:mm:ss") + "  \n  ");
                }
            }, OigFinanceConstant.NOTICE_YPH);
            throw new BusiException("P卡交易流水入库失败");
        }
    }

    public void saveBatch(List<ItemEntity> list) {
        List<PaycardBillDetail> details = new ArrayList<>();
        for (ItemEntity itemEntity : list) {
            itemEntity.getDetails().setTransactionId(itemEntity.getId());
            details.add(itemEntity.getDetails());
        }
        paycardBillDownloadDao.insertBatch(list);
        paycardBillDetailDao.insertBatch(details);
    }

    private void shutdown(ThreadPoolTool<ItemEntity> tool) {
        if (tool != null) {
            tool.shutdown();
            logger.info("关闭资源");
        }
    }

    private List<ItemEntity> clear(List<ItemEntity> taskList) {
        if (taskList != null) {
            if (taskList.size() > 0) {
                taskList.clear();
            }
            taskList = null;
        }
        return taskList;
    }
}
