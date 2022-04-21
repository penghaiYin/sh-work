package com.yph.shwork.pcard.entity;

import com.oigbuy.common.pojo.third.entity.pcard.PaycardBillDetail;
import com.oigbuy.common.pojo.third.entity.pcard.PaycardBillDownload;

public class ItemEntity extends PaycardBillDownload {
    private PaycardBillDetail details;

    public PaycardBillDetail getDetails() {
        return details;
    }

    public void setDetails(PaycardBillDetail details) {
        this.details = details;
    }
}
