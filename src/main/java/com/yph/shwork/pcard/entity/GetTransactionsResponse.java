package com.yph.shwork.pcard.entity;

import java.io.Serializable;

public class GetTransactionsResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private ResultEntity result;

    public ResultEntity getResult() {
        return result;
    }

    public void setResult(ResultEntity result) {
        this.result = result;
    }
}
