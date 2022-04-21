package com.yph.shwork.common.service;

import cn.hutool.core.collection.CollectionUtil;
import com.oigbuy.common.dto.dingding.OigMarkdownMessageDTO;
import com.oigbuy.common.exception.DefaultException;
import com.oigbuy.common.feign.auth.token.TokenFeignService;
import com.oigbuy.common.feign.dingding.DingdingFeignService;
import com.oigbuy.common.http.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DingDingWarningService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private TokenFeignService tokenFeignService;

    @Value("${auth.username}")
    private String authUsername;

    @Value("${auth.password}")
    private String authPassword;

    @Value("${dingding.finance-warning.client-id}")
    private String clientId;

    @Autowired
    private DingdingFeignService dingdingFeignService;

    public void sendDingDing(String title, List<String> list, String atMobile) {
        if (CollectionUtil.isNotEmpty(list)) {
            StringBuffer text = new StringBuffer();
            for (String content : list) {
                text.append(content);
            }
            JsonResult<String> tokenResult = tokenFeignService.token(null, authUsername, authPassword, true);
            if ("200".equals(tokenResult.getCode())) {
                String token = tokenResult.getData().toString();
                logger.info("发送钉钉消息：" + token);
                OigMarkdownMessageDTO oigMarkdownMessageDTO = new OigMarkdownMessageDTO(clientId, title, text.toString(), atMobile);
                try {
                    dingdingFeignService.sendMarkdown(token, oigMarkdownMessageDTO);
                } catch (DefaultException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
