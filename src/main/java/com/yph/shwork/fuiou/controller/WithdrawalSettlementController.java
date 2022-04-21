package com.yph.shwork.fuiou.controller;

import com.alibaba.fastjson.JSON;
import com.oigbuy.common.http.JsonResult;
import com.oigbuy.common.http.ResultCode;
import com.oigbuy.common.pojo.check.orderwithdrawal.dto.WithdrawalSettlementParamReqDTO;
import com.oigbuy.finance.fuiou.service.SrmWithdrawalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther zhuo.lv
 * @Date 2022-01-11
 */

@RestController
@RequestMapping("/withdrawal")
@Api(tags="提现订单结汇")
public class WithdrawalSettlementController {

    @Autowired
    private SrmWithdrawalService srmWithdrawalService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/settlement", method = RequestMethod.POST)
    @ApiOperation(value = "结汇")
    public JsonResult transferBalanceQuery(@RequestBody WithdrawalSettlementParamReqDTO condition) {
        logger.info("结汇数据查询，请求参数：{} ", JSON.toJSONString(condition));
        JsonResult result = new JsonResult<>(ResultCode.SUCCESS);
        try {
            srmWithdrawalService.settlementExchangeProcess(condition);
        }catch (Exception e){
            result.setCode(ResultCode.EXCEPTION);
            result.setMessage(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}
