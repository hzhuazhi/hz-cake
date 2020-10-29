package com.hz.cake.master.core.service.impl;

import com.hz.cake.master.core.common.dao.BaseDao;
import com.hz.cake.master.core.common.service.impl.BaseServiceImpl;
import com.hz.cake.master.core.mapper.OrderOutMapper;
import com.hz.cake.master.core.service.OrderOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 代付订单的Service层的实现层
 * @Author yoko
 * @Date 2020/10/29 17:47
 * @Version 1.0
 */
@Service
public class OrderOutServiceImpl<T> extends BaseServiceImpl<T> implements OrderOutService<T> {
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
    private OrderOutMapper orderOutMapper;

    public BaseDao<T> getDao() {
        return orderOutMapper;
    }
}
