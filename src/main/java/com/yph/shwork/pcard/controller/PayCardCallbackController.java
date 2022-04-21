package com.yph.shwork.pcard.controller;

import com.oigbuy.pcard.constant.PayCardConstant;
import com.oigbuy.pcard.service.PayCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PayCardConstant.CALL_BACK_BASE)
public class PayCardCallbackController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PayCardService payCardService;

    /**
     * 授权回调
     *
     * @param code
     * @param error
     * @param state
     */
    @GetMapping("/callback")
    public void callback(@RequestParam(value = "code", required = false) String code,
                         @RequestParam(value = "error", required = false) String error,
                         @RequestParam(value = "state", required = false) String state) {
        logger.info("code: {}，error: {}", code, error);
        payCardService.sendAccessToken(code, error);
    }
}
