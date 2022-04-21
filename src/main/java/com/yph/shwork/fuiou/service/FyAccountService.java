package com.yph.shwork.fuiou.service;

import com.oigbuy.common.exception.BusiException;
import com.oigbuy.common.pojo.check.orderwithdrawal.entity.FyAccount;
import com.oigbuy.finance.dao.finance.orderwithdrawal.FyAccountDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FyAccountService {

    @Autowired
    private FyAccountDao fyAccountDao;

    public FyAccount checkFyAccount(String fyAccountCode) {
        FyAccount fyAccount = fyAccountDao.getByAccountCode(fyAccountCode);
        if (fyAccount == null) {
            throw new BusiException("未查到付款公司，代号：" + fyAccountCode);
        }
        return fyAccount;
    }
}
