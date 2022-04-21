package com.yph.shwork.fuiou.controller;

import com.oigbuy.common.constant.FyConstants;
import com.oigbuy.finance.fuiou.entity.callback.FileNoticeCallBackEntity;
import com.oigbuy.finance.fuiou.service.FyCallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(FyConstants.BASE_PATH + "/callback")
public class FyCallBackController {

    @Autowired
    private FyCallService fyCallService;

    @RequestMapping(value = "/fileNotice", method = RequestMethod.POST)
    public void fileHandleNotice(FileNoticeCallBackEntity entity){
        // TODO 接收文件处理通知
        fyCallService.callBackHandleFileNotice(entity);
    }

    // 富友银行转账接口回调，暂时不需要
//    @RequestMapping(value = "/transferNotice", method = RequestMethod.POST)
//    public void fileHandleNotice(@RequestParam String reqData) {
//
//    }
}
