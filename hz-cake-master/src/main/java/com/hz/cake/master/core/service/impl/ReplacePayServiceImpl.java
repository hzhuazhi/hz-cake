package com.hz.cake.master.core.service.impl;

import com.hz.cake.master.core.common.dao.BaseDao;
import com.hz.cake.master.core.common.service.impl.BaseServiceImpl;
import com.hz.cake.master.core.mapper.ReplacePayMapper;
import com.hz.cake.master.core.model.replacepay.ReplacePayModel;
import com.hz.cake.master.core.service.ReplacePayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 代付资源的Service层的实现层
 * @Author yoko
 * @Date 2021/6/9 14:03
 * @Version 1.0
 */
@Service
public class ReplacePayServiceImpl<T> extends BaseServiceImpl<T> implements ReplacePayService<T> {

    private static Logger log = LoggerFactory.getLogger(ReplacePayServiceImpl.class);

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
    private ReplacePayMapper replacePayMapper;



    public BaseDao<T> getDao() {
        return replacePayMapper;
    }

    @Override
    public List<ReplacePayModel> getReplacePayList(ReplacePayModel model) {
        return replacePayMapper.getReplacePayList(model);
    }
}
