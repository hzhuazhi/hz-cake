package com.hz.cake.master.core.service.impl;

import com.hz.cake.master.core.common.dao.BaseDao;
import com.hz.cake.master.core.common.service.impl.BaseServiceImpl;
import com.hz.cake.master.core.mapper.OrderOutPrepareMapper;
import com.hz.cake.master.core.service.OrderOutPrepareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @Description 代付预备请求的Service层的实现层
 * @Author yoko
 * @Date 2020/10/29 17:47
 * @Version 1.0
 */
@Service
public class OrderOutPrepareServiceImpl<T> extends BaseServiceImpl<T> implements OrderOutPrepareService<T> {
    private static Logger log = LoggerFactory.getLogger(OrderOutPrepareServiceImpl.class);

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
    private OrderOutPrepareMapper orderOutPrepareMapper;


    public BaseDao<T> getDao() {
        return orderOutPrepareMapper;
    }






}
