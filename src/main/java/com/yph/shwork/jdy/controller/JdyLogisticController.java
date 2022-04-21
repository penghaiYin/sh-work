package com.yph.shwork.jdy.controller;

import com.oigbuy.check.jdy.dto.JdyLogisticFeeDTO;
import com.oigbuy.check.jdy.dto.JdyLogisticFeeExportDTO;
import com.oigbuy.check.jdy.service.JdyLogisticService;
import com.oigbuy.check.jdy.vo.JdyLogisticFeeVO;
import com.oigbuy.common.http.JsonResult;
import com.oigbuy.common.http.ResultCode;
import com.oigbuy.common.pojo.check.export.entity.CheckExportRecord;
import com.oigbuy.common.pojo.check.export.enums.DataTemplateEnum;
import com.oigbuy.jeesite.common.persistence.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jdy/logistic")
@Api(tags = "简道云物流")
public class JdyLogisticController {

    @Autowired
    private JdyLogisticService jdyLogisticService;

    @RequestMapping(value = "/findList", method = RequestMethod.GET)
    @ApiOperation(value = "分页查询")
    public JsonResult<Page<JdyLogisticFeeVO>> findList(JdyLogisticFeeDTO jdyLogisticFeeDTO) {
        JsonResult<Page<JdyLogisticFeeVO>> result = new JsonResult<>(ResultCode.SUCCESS);
        try {
            Page<JdyLogisticFeeVO> list = jdyLogisticService.findListByPage(jdyLogisticFeeDTO);
            result.setData(list);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ResultCode.EXCEPTION);
            result.setMessage("简道云物流查询异常：" + e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/export", method = RequestMethod.POST)
    @ApiOperation(value = "导出")
    public JsonResult<String> exportCert(@RequestBody JdyLogisticFeeExportDTO dto) {
        JsonResult<String> result = new JsonResult<>(ResultCode.SUCCESS);
        try {
            CheckExportRecord checkExportRecord = jdyLogisticService.exportExcel(dto, DataTemplateEnum.JDY_LOGISTIC_FEE);
            result.setData(checkExportRecord.getOssUrl());
        } catch (Exception e) {
            result.setCode(ResultCode.EXCEPTION);
            result.setMessage("勾选导出！" + e.getMessage());
        }
        return result;
    }

}
