package com.yph.shwork.pingpong.dto;

import java.io.Serializable;
import java.util.List;

public class StoreInfo implements Serializable {

    private List<String> account_id_list;
    private String store_name;
    private String alias_name;
    private String platform;
    private String seller_id;
    private static final long serialVersionUID = 1L;

    public List<String> getAccount_id_list() {
        return account_id_list;
    }

    public void setAccount_id_list(List<String> account_id_list) {
        this.account_id_list = account_id_list;
    }

    public String getStore_name() {
        return store_name;
    }

    public void setStore_name(String store_name) {
        this.store_name = store_name;
    }

    public String getAlias_name() {
        return alias_name;
    }

    public void setAlias_name(String alias_name) {
        this.alias_name = alias_name;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getSeller_id() {
        return seller_id;
    }

    public void setSeller_id(String seller_id) {
        this.seller_id = seller_id;
    }
}
