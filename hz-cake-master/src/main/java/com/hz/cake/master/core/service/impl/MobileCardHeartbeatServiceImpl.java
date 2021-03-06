package com.hz.cake.master.core.service.impl;

import com.hz.cake.master.core.common.dao.BaseDao;
import com.hz.cake.master.core.common.service.impl.BaseServiceImpl;
import com.hz.cake.master.core.mapper.MobileCardHeartbeatMapper;
import com.hz.cake.master.core.service.MobileCardHeartbeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 手机卡的心跳纪录的Service层的实现层
 * @Author yoko
 * @Date 2020/9/20 14:40
 * @Version 1.0
 */
@Service
public class MobileCardHeartbeatServiceImpl<T> extends BaseServiceImpl<T> implements MobileCardHeartbeatService<T> {
    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    public long TWO_HOUR = 2;

    @Autowired
    private MobileCardHeartbeatMapper mobileCardHeartbeatMapper;

    public BaseDao<T> getDao() {
        return mobileCardHeartbeatMapper;
    }
}
