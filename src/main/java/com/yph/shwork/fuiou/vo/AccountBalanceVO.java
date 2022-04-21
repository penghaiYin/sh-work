package com.yph.shwork.fuiou.vo;

public class AccountBalanceVO {
    private String accountType;
    private String accountStatus;
    private Integer totalBalance;
    private Integer frozenBalance;
    private Integer availableBalance;

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Integer getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(Integer totalBalance) {
        this.totalBalance = totalBalance;
    }

    public Integer getFrozenBalance() {
        return frozenBalance;
    }

    public void setFrozenBalance(Integer frozenBalance) {
        this.frozenBalance = frozenBalance;
    }

    public Integer getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(Integer availableBalance) {
        this.availableBalance = availableBalance;
    }
}
