package com.yph.shwork.pcard.controller;

import com.oigbuy.common.http.JsonResult;
import com.oigbuy.common.http.ResultCode;
import com.oigbuy.common.utils.DateUtils;
import com.oigbuy.common.utils.StringUtils;
import com.oigbuy.pcard.constant.PayCardConstant;
import com.oigbuy.pcard.service.PayCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PayCardConstant.CALL_BASE)
public class PayCardController {
    @Autowired
    private PayCardService payCardService;
    /**
     * 初始化，用户授权
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/userAuthorize", method = RequestMethod.POST)
    public JsonResult userAuthorizeJobHandler() {
        JsonResult result = new JsonResult<>(ResultCode.SUCCESS);
        try {
            payCardService.sendAuthorize();
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ResultCode.EXCEPTION);
        }
        return result;
    }

    /**
     * 判断即将过期时，才刷新token
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/refreshToken", method = RequestMethod.POST)
    public JsonResult refreshTokenJobHandler() {
        JsonResult result = new JsonResult<>(ResultCode.SUCCESS);
        try {
            payCardService.sendRefreshToken();
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ResultCode.EXCEPTION);
        }
        return result;
    }


    /**
     * 每日开启，定时拉取P卡交易流水
     *
     * @param param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getTransactions", method = RequestMethod.POST)
    public JsonResult getTransactionsJobHandler(@RequestParam(required = false) String param) {
        JsonResult result = new JsonResult<>(ResultCode.SUCCESS);
        try {
            String updateTimeStart = DateUtils.addDayToString(-1);
            String updateTimeEnd = updateTimeStart;

            if (StringUtils.isNotBlank(param)) {
                String[] dateArray = param.split(",");
                updateTimeStart = dateArray[0];
                if (dateArray.length == 2) {
                    updateTimeEnd = dateArray[1];
                }
            }
            payCardService.sendGetTransaction(updateTimeStart, updateTimeEnd);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ResultCode.EXCEPTION);
        }
        return result;
    }
}
