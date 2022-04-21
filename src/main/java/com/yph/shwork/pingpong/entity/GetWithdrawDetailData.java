package com.yph.shwork.pingpong.entity;

import java.io.Serializable;

public class GetWithdrawDetailData implements Serializable {
    private String tx_id;
    private String platform;
    private String withdraw_time;
    private String withdraw_amount;
    private String withdraw_currency;
    private String fee_rate;
    private String fee;
    private String paid_amount;
    private String paid_currency;
    private String bank;
    private String card;
    private String withdraw_type;

    public String getTx_id() {
        return tx_id;
    }

    public void setTx_id(String tx_id) {
        this.tx_id = tx_id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getWithdraw_time() {
        return withdraw_time;
    }

    public void setWithdraw_time(String withdraw_time) {
        this.withdraw_time = withdraw_time;
    }

    public String getWithdraw_amount() {
        return withdraw_amount;
    }

    public void setWithdraw_amount(String withdraw_amount) {
        this.withdraw_amount = withdraw_amount;
    }

    public String getWithdraw_currency() {
        return withdraw_currency;
    }

    public void setWithdraw_currency(String withdraw_currency) {
        this.withdraw_currency = withdraw_currency;
    }

    public String getFee_rate() {
        return fee_rate;
    }

    public void setFee_rate(String fee_rate) {
        this.fee_rate = fee_rate;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getPaid_amount() {
        return paid_amount;
    }

    public void setPaid_amount(String paid_amount) {
        this.paid_amount = paid_amount;
    }

    public String getPaid_currency() {
        return paid_currency;
    }

    public void setPaid_currency(String paid_currency) {
        this.paid_currency = paid_currency;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getWithdraw_type() {
        return withdraw_type;
    }

    public void setWithdraw_type(String withdraw_type) {
        this.withdraw_type = withdraw_type;
    }

    private static final long serialVersionUID = 1L;
}
