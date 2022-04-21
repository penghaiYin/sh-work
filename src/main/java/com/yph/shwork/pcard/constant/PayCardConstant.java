package com.yph.shwork.pcard.constant;

public class PayCardConstant {
    public static final String CALL_BACK_BASE = "/no-filter/paycard";
    public static final String CALL_BASE = "/paycard";
    public static final String AUTHORIZE_URL_PATH = "/api/v2/oauth2/authorize";
    public static final String ACCESS_TOKEN_URL_PATH = "/api/v2/oauth2/token";
    public static final String GET_TRANSACTION_URL_PATH = "/accounts/{account_id}/transactions";

    public static class DingWarningTitle {
        public static final String PKA_CALL_FAILURE = "P卡接口调用失败";
        public static final String PKA_CALL_EXCEPTION = "P卡接口调用异常";

    }
}
