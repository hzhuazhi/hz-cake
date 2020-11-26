package com.hz.cake.master.core.service.impl;

import com.hz.cake.master.core.common.dao.BaseDao;
import com.hz.cake.master.core.common.service.impl.BaseServiceImpl;
import com.hz.cake.master.core.mapper.MerchantServiceChargeMapper;
import com.hz.cake.master.core.service.MerchantServiceChargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 卡商绑定渠道的手续费的Service层的实现层
 * @Author yoko
 * @Date 2020/11/26 13:21
 * @Version 1.0
 */
@Service
public class MerchantServiceChargeServiceImpl<T> extends BaseServiceImpl<T> implements MerchantServiceChargeService<T> {
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
    private MerchantServiceChargeMapper merchantServiceChargeMapper;

    public BaseDao<T> getDao() {
        return merchantServiceChargeMapper;
    }
}
