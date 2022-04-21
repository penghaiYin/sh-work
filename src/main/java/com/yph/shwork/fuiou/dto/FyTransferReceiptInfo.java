package com.yph.shwork.fuiou.dto;

import com.oigbuy.common.pojo.check.orderwithdrawal.entity.FyTransferDetail;

public class FyTransferReceiptInfo extends FyTransferDetail {
    private String supplierId;//供应商ID

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }
}
