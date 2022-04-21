package com.yph.shwork.pingpong.controller;

import com.oigbuy.common.exception.BusiException;
import com.oigbuy.common.http.JsonResult;
import com.oigbuy.common.http.ResultCode;
import com.oigbuy.common.utils.OigDateUtils;
import com.oigbuy.common.utils.StringUtils;
import com.oigbuy.pingpong.dto.StoreInfo;
import com.oigbuy.pingpong.service.PingPongService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/pp")
@Api(tags = "PingPong")
public class PingPongController {
    @Autowired
    private PingPongService pingPongService;

    /**
     * 拉取店铺信息，一次性操作
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getStoreInfo", method = RequestMethod.GET)
    public JsonResult getStoreInfo() {
        JsonResult result = new JsonResult<>(ResultCode.SUCCESS);
        try {
            List<StoreInfo> dataList = new ArrayList<>();
            pingPongService.sendGetStoreInfo(dataList, 1);
            pingPongService.saveAccounts(dataList);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ResultCode.EXCEPTION);
        }

        return result;
    }

    /**
     * 拉取店铺详细信息
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getStoreInfoDetail", method = RequestMethod.GET)
    public JsonResult getStoreInfoDetail() {
        JsonResult result = new JsonResult<>(ResultCode.SUCCESS);
        try {
            pingPongService.getStoreInfoDetail();
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ResultCode.EXCEPTION);
        }
        return result;
    }


    /**
     * 拉取所有店铺的流水
     * 根据参数查询一段时间内的账户变化流水（时间范围不超过1个月）
     *
     * @param param
     * @throws Exception
     */
    @RequestMapping(value = "/getStoreWater", method = RequestMethod.GET)
    public JsonResult getStoreWater(@RequestParam(required = false) String param) {
        JsonResult result = new JsonResult<>(ResultCode.SUCCESS);
        try {
            String updateTimeStart = OigDateUtils.formatDate(OigDateUtils.nextDay(-1), "yyyyMMdd");
            String updateTimeEnd = updateTimeStart;

            if (StringUtils.isNotBlank(param)) {
                String[] dateArray = param.split(",");
                updateTimeStart = dateArray[0];
                if (dateArray.length == 2) {
                    updateTimeEnd = dateArray[1];
                }
            }
            pingPongService.sendGetStoreWater(updateTimeStart, updateTimeEnd);

        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ResultCode.EXCEPTION);
        }
        return result;
    }

    /**
     * 根据提现流水号查询提现明细 (目前只能查询提现到自己所属银⾏卡的信息)
     * 在店铺流水拉取之后执行
     *
     * @param param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getWithdrawDetail", method = RequestMethod.GET)
    public JsonResult getWithdrawDetail(@RequestParam(required = false) String param) {
        JsonResult result = new JsonResult<>(ResultCode.SUCCESS);
        try {
            String updateTimeStart = OigDateUtils.formatDate(OigDateUtils.nextDay(-1), "yyyyMMdd");
            String updateTimeEnd = updateTimeStart;

            if (StringUtils.isNotBlank(param)) {
                String[] dateArray = param.split(",");
                updateTimeStart = dateArray[0];
                if (dateArray.length == 2) {
                    updateTimeEnd = dateArray[1];
                }
            }
            pingPongService.sendGetWithdrawDetail(updateTimeStart, updateTimeEnd);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ResultCode.EXCEPTION);
        }
        return result;
    }

    /**
     * 补偿任务：某个店铺拉取流水失败
     * @param param 三个参数，开始时间、结束时间、accountId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/storeWaterOffset", method = RequestMethod.GET)
    public JsonResult storeWaterOffset(@RequestParam(required = false) String param) {
        JsonResult result = new JsonResult<>(ResultCode.SUCCESS);
        try {
            if (StringUtils.isEmpty(param) || param.split(",").length != 3) {
                throw new BusiException("参数不正确");
            }
            String[] dateArray = param.split(",");
            String updateTimeStart = dateArray[0];
            String updateTimeEnd = dateArray[1];
            String accountId = dateArray[2];
            pingPongService.storeWaterOffset(updateTimeStart, updateTimeEnd, accountId);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ResultCode.EXCEPTION);
        }
        return result;
    }

}
