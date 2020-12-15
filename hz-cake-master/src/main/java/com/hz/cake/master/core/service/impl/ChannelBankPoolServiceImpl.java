package com.hz.cake.master.core.service.impl;

import com.hz.cake.master.core.common.dao.BaseDao;
import com.hz.cake.master.core.common.service.impl.BaseServiceImpl;
import com.hz.cake.master.core.mapper.ChannelBankPoolMapper;
import com.hz.cake.master.core.model.channel.ChannelBankPoolModel;
import com.hz.cake.master.core.service.ChannelBankPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 商户的银行卡池子的Service层的实现层
 * @Author yoko
 * @Date 2020/12/15 12:29
 * @Version 1.0
 */
@Service
public class ChannelBankPoolServiceImpl<T> extends BaseServiceImpl<T> implements ChannelBankPoolService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    /**
     * 3分钟
     */
    public long THREE_MIN = 180;

    /**
     * 11分钟.
     */
    public long ELEVEN_MIN = 660;

    public long TWO_HOUR = 2;



    @Autowired
    private ChannelBankPoolMapper channelBankPoolMapper;

    public BaseDao<T> getDao() {
        return channelBankPoolMapper;
    }

    @Override
    public List<Long> getBankRelationList(ChannelBankPoolModel model) {
        return channelBankPoolMapper.getBankRelationList(model);
    }
}
