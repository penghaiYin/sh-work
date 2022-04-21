package com.yph.shwork.pcard.entity;

import java.io.Serializable;
import java.util.List;

public class TransactionEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<ItemEntity> items;
    private Integer total;
    private String next;

    public List<ItemEntity> getItems() {
        return items;
    }

    public void setItems(List<ItemEntity> items) {
        this.items = items;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }
}
