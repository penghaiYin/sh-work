package com.yph.shwork.pcard.entity;

import java.io.Serializable;

public class ResultEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private TransactionEntity transactions;

    public TransactionEntity getTransactions() {
        return transactions;
    }

    public void setTransactions(TransactionEntity transactions) {
        this.transactions = transactions;
    }
}
