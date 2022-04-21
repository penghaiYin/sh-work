package com.yph.shwork.pingpong.constant;

public class PingPongConstant {

    public static final String GET_STORE_URL = "/store/info";
    public static final String GET_STORE_WATER_URL = "/account/statement";
    public static final String GET_WITHDRAW_DETAIL_URL = "/withdraw/detail";
    public static final String GET_STORE_INFO_URL = "/account/info";
    public static final String API_SUCCESS_CODE = "0000";

    public static class DingWarningTitle {
        public static final String WITHDRAW_DETAIL_PULL_EXCEPTION = "PingPong 提现明细拉取";
        public static final String STORE_WATER_PULL_EXCEPTION = "PingPong 店铺流水拉取";
        public static final String STORE_INFO_PULL_EXCEPTION = "PingPong 店铺信息拉取";
        public static final String STORE_INFO_DETAIL_PULL_EXCEPTION = "PingPong 店铺详情拉取";
    }
}
