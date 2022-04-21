package com.yph.shwork.fuiou.controller;

import com.alibaba.fastjson.JSON;
import com.oigbuy.common.http.JsonResult;
import com.oigbuy.common.http.ResultCode;
import com.oigbuy.common.pojo.check.orderwithdrawal.dto.TransferConfirmDTO;
import com.oigbuy.common.pojo.check.orderwithdrawal.dto.TransferQueryBalanceDTO;
import com.oigbuy.common.pojo.check.orderwithdrawal.vo.TransferQueryBalanceVO;
import com.oigbuy.common.pojo.check.orderwithdrawal.vo.TransferWarningVO;
import com.oigbuy.finance.fuiou.service.FyTransferCallService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/fy")
@Api(tags="富友转账")
public class FyTransferController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private FyTransferCallService fyTransferCallService;

    @RequestMapping(value = "/transferQueryBalance", method = RequestMethod.POST)
    @ApiOperation(value = "余额查询")
    public JsonResult<TransferQueryBalanceVO> transferBalanceQuery(@RequestBody TransferQueryBalanceDTO condition) {
        logger.info("转账余额查询，请求参数：{} ", JSON.toJSONString(condition));
        JsonResult result = new JsonResult<>(ResultCode.SUCCESS);
        try {
            TransferQueryBalanceVO balanceQueryVO = fyTransferCallService.transferBalanceQuery(condition);
            result.setData(balanceQueryVO);
        }catch (Exception e){
            result.setCode(ResultCode.EXCEPTION);
            result.setMessage(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "/transferConfirm", method = RequestMethod.POST)
    @ApiOperation(value = "转账确认")
    public JsonResult transferConfirm(@RequestBody @Valid TransferConfirmDTO dto) {
        logger.info("转账确认，请求参数：{} ", JSON.toJSONString(dto));
        JsonResult result = new JsonResult<>(ResultCode.SUCCESS);
        try {
            fyTransferCallService.transferConfirm(dto);
        }catch (Exception e){
            result.setCode(ResultCode.EXCEPTION);
            result.setMessage(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "/transferWarning", method = RequestMethod.POST)
    @ApiOperation(value = "转账预警")
    public JsonResult<TransferWarningVO> transferWarning(@RequestBody TransferQueryBalanceDTO condition) {
        logger.info("转账预警，请求参数：{} ", JSON.toJSONString(condition));
        JsonResult result = new JsonResult<>(ResultCode.SUCCESS);
        try {
            TransferWarningVO transferWarningVO = fyTransferCallService.transferWarning(condition);
            result.setData(transferWarningVO);
        }catch (Exception e){
            result.setCode(ResultCode.EXCEPTION);
            result.setMessage(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

}
