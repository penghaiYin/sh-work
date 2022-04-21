package com.yph.shwork.pingpong.dto;

import java.io.Serializable;

public class StoreWater implements Serializable {
    private String client_id;
    private String account_id;
    private String tx_id;
    private String transfer_time;
    private String transfer_type;
    private String amount;
    private String currency;
    private String platform;
    private String closing_balance;

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

    public String getTx_id() {
        return tx_id;
    }

    public void setTx_id(String tx_id) {
        this.tx_id = tx_id;
    }

    public String getTransfer_time() {
        return transfer_time;
    }

    public void setTransfer_time(String transfer_time) {
        this.transfer_time = transfer_time;
    }

    public String getTransfer_type() {
        return transfer_type;
    }

    public void setTransfer_type(String transfer_type) {
        this.transfer_type = transfer_type;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getClosing_balance() {
        return closing_balance;
    }

    public void setClosing_balance(String closing_balance) {
        this.closing_balance = closing_balance;
    }
}
