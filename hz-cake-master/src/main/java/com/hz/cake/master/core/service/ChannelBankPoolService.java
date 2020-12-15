package com.hz.cake.master.core.service;

import com.hz.cake.master.core.common.service.BaseService;
import com.hz.cake.master.core.model.channel.ChannelBankPoolModel;

import java.util.List;

/**
 * @Description 商户的银行卡池子的Service层
 * @Author yoko
 * @Date 2020/12/15 12:07
 * @Version 1.0
 */
public interface ChannelBankPoolService<T> extends BaseService<T> {

    /**
     * @Description: 查询与渠道绑定的银行卡ID集合
     * <p>
     * </p>
     * @param model
     * @return
     * @author yoko
     * @date 2020/10/9 18:56
     */
    public List<Long> getBankRelationList(ChannelBankPoolModel model);
}
