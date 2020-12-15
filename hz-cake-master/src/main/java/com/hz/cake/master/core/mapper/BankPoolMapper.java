package com.hz.cake.master.core.mapper;

import com.hz.cake.master.core.common.dao.BaseDao;
import com.hz.cake.master.core.model.bank.BankPoolModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description 银行卡池子的Dao层
 * @Author yoko
 * @Date 2020/12/15 12:05
 * @Version 1.0
 */
@Mapper
public interface BankPoolMapper<T> extends BaseDao<T> {

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
