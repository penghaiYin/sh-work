package com.yph.shwork.fuiou.service;

import com.alibaba.fastjson.JSON;
import com.oigbuy.common.constant.FyConstants;
import com.oigbuy.common.exception.BusiException;
import com.oigbuy.common.pojo.check.orderwithdrawal.entity.FyAccount;
import com.oigbuy.common.pojo.check.orderwithdrawal.entity.FyTransFlowDownload;
import com.oigbuy.finance.dao.finance.orderwithdrawal.FyAccountDao;
import com.oigbuy.finance.dao.finance.orderwithdrawal.FyTransFlowDownloadDao;
import com.oigbuy.finance.fuiou.entity.req.FyQueryTransDetailReqEntity;
import com.oigbuy.finance.fuiou.entity.res.FyQueryTransDetailResEntity;
import com.oigbuy.finance.fuiou.entity.res.ResultVO;
import com.oigbuy.finance.fuiou.utils.FuIouSignatureUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
public class FyTransFlowDownloadService {
    @Autowired
    private FyTransFlowDownloadDao fyTransFlowDownloadDao;

    @Autowired
    private FyTransferCallService fyTransferCallService;

    @Autowired
    private FyAccountDao fyAccountDao;

    public int update(FyTransFlowDownload entity) {
        FyTransFlowDownload checkIfExist = fyTransFlowDownloadDao.queryIfExist(entity);
        if (checkIfExist != null) {
            return fyTransFlowDownloadDao.update(entity);
        }
        return fyTransFlowDownloadDao.insert(entity);
    }

    public void buildUpdateEntity(String fyAccountCode, ResultVO resultVO, FyTransFlowDownload fyTransFlowDownload) {
        fyTransFlowDownload.setFyAccountCode(fyAccountCode);
        fyTransFlowDownload.setFuiouTransNo(resultVO.getFuiouTransNo());
        fyTransFlowDownload.setTransTime(resultVO.getTransTime());
        fyTransFlowDownload.setTransType(resultVO.getTransType());
        fyTransFlowDownload.setBalanceDirection(resultVO.getBalanceDirection());
        fyTransFlowDownload.setTransState(resultVO.getTransState());
        fyTransFlowDownload.setTransCurrency("CNH");
        fyTransFlowDownload.setServiceFee(BigDecimal.valueOf(resultVO.getServiceFee()).divide(new BigDecimal("100")));
        fyTransFlowDownload.setOppAccountNm(resultVO.getOppAccountNm());
        fyTransFlowDownload.setOppBankCardNo(resultVO.getOppBankCardNo());
        fyTransFlowDownload.setOrderAmt(BigDecimal.valueOf(resultVO.getOrderAmt()).divide(new BigDecimal("100")));
        fyTransFlowDownload.setRemark(resultVO.getRemark());
        fyTransFlowDownload.setTotalAmt(BigDecimal.valueOf(resultVO.getTotalAmt()).divide(new BigDecimal("100")));
        fyTransFlowDownload.setMchntOrderId(resultVO.getMchntOrderId());
    }

    public Map<String, List<ResultVO>> transFlowPull(String startTime, String endTime) throws IOException {
        List<FyAccount> accounts = fyAccountDao.getList();
        Map<String, List<ResultVO>> updateMap = new HashMap<>();
        // 根据商户号、时间查询，汇总拉取的数据
        for (FyAccount account : accounts) {
            FyQueryTransDetailResEntity result;
            List<ResultVO> resultList = new ArrayList<>();
            int pageNo = 1;
            do {
                result = fyTransferCallService.callQueryTransDetail(buildReqEntity(account, pageNo, 1000, startTime, endTime));
                if (!FyConstants.Version20.SUCCESS_CODE.equals(result.getResultCode())) {
                    throw new BusiException(JSON.toJSONString(result.getErrorList()));
                }
                resultList.addAll(result.getResultList());
                pageNo++;
            } while ("T".equals(result.getHasNextPage()));
            updateMap.put(account.getFyAccountCode(), resultList);
        }
        return updateMap;
    }

    private FyQueryTransDetailReqEntity buildReqEntity(FyAccount fyAccount, int pageNo, int pageSize, String startTime, String endTime) {
        FyQueryTransDetailReqEntity reqEntity = new FyQueryTransDetailReqEntity();
        reqEntity.setMchntCd(fyAccount.getFyMchntCd());
        reqEntity.setRandomStr(UUID.randomUUID().toString().replaceAll("-", ""));
        reqEntity.setPageNo(pageNo);
        reqEntity.setPageSize(pageSize);
        reqEntity.setStartTime(startTime + " 00:00:00");
        reqEntity.setEndTime(endTime + " 23:59:59");
        reqEntity.setSign(FuIouSignatureUtils.generate20Sign(reqEntity, fyAccount.getFySecretKey()));
        return reqEntity;
    }

    public void transFlowDownload(Map<String, List<ResultVO>> updateMap) {
        FyTransFlowDownload fyTransFlowDownload = new FyTransFlowDownload();
        for (String fyAccountCode : updateMap.keySet()) {
            List<ResultVO> resultVOS = updateMap.get(fyAccountCode);
            for (ResultVO resultVO : resultVOS) {
                buildUpdateEntity(fyAccountCode, resultVO, fyTransFlowDownload);
                if (update(fyTransFlowDownload) != 1) {
                    throw new BusiException(JSON.toJSONString(fyTransFlowDownload) + "更新，校验失败");
                }
            }
        }
    }
}
