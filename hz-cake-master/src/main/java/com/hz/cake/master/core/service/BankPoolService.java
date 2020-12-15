package com.hz.cake.master.core.service;

import com.hz.cake.master.core.common.service.BaseService;
import com.hz.cake.master.core.model.bank.BankPoolModel;

import java.util.List;

/**
 * @Description 银行卡池子的Service层
 * @Author yoko
 * @Date 2020/12/15 12:07
 * @Version 1.0
 */
public interface BankPoolService<T> extends BaseService<T> {

    /**
     * @Description: 查询银行卡ID集合
     * <p>
     * </p>
     * @param model
     * @return
     * @author yoko
     * @date 2020/10/9 18:56
     */
    public List<Long> getBankIdList(BankPoolModel model);
}
