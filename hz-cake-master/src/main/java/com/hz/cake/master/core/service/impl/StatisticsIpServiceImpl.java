package com.hz.cake.master.core.service.impl;

import com.hz.cake.master.core.common.dao.BaseDao;
import com.hz.cake.master.core.common.service.impl.BaseServiceImpl;
import com.hz.cake.master.core.mapper.StatisticsIpMapper;
import com.hz.cake.master.core.service.StatisticsIpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 支付页统计用户IP的Service层的实现层
 * @Author yoko
 * @Date 2020/12/18 2:01
 * @Version 1.0
 */
@Service
public class StatisticsIpServiceImpl<T> extends BaseServiceImpl<T> implements StatisticsIpService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private StatisticsIpMapper statisticsIpMapper;

    public BaseDao<T> getDao() {
        return statisticsIpMapper;
    }
}
