package com.yph.shwork.fuiou.dto;

import com.oigbuy.common.pojo.jdy.JdyFlowRecord;

public class JdyFlowRecordDTO extends JdyFlowRecord {

    private String failCause;

    public String getFailCause() {
        return failCause;
    }

    public void setFailCause(String failCause) {
        this.failCause = failCause;
    }
}