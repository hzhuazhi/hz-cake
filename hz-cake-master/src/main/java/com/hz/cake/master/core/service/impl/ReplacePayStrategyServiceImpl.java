package com.hz.cake.master.core.service.impl;

import com.hz.cake.master.core.common.dao.BaseDao;
import com.hz.cake.master.core.common.service.impl.BaseServiceImpl;
import com.hz.cake.master.core.mapper.ReplacePayStrategyMapper;
import com.hz.cake.master.core.service.ReplacePayStrategyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 代付放量策略的Service层的实现层
 * @Author yoko
 * @Date 2021/6/15 18:19
 * @Version 1.0
 */
@Service
public class ReplacePayStrategyServiceImpl<T> extends BaseServiceImpl<T> implements ReplacePayStrategyService<T> {

    private static Logger log = LoggerFactory.getLogger(ReplacePayStrategyServiceImpl.class);

    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    /**
     * 15分钟.
     */
    public long FIFTEEN_MIN = 900;

    public long TWO_HOUR = 2;

    @Autowired
    private ReplacePayStrategyMapper replacePayStrategyMapper;



    public BaseDao<T> getDao() {
        return replacePayStrategyMapper;
    }
}
