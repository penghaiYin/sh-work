package com.yph.shwork.pingpong.entity;

import com.oigbuy.pingpong.dto.BankInfo;

import java.io.Serializable;

public class GetStoreInfoDetailData implements Serializable {
    private String client_id;
    private String account_id;
    private String nation;
    private BankInfo bank_info;

    private static final long serialVersionUID = 1L;

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getAccount_id() {
        return account_id;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public BankInfo getBank_info() {
        return bank_info;
    }

    public void setBank_info(BankInfo bank_info) {
        this.bank_info = bank_info;
    }
}
