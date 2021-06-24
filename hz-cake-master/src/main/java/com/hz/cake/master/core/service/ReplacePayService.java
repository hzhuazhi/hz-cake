package com.hz.cake.master.core.service;

import com.hz.cake.master.core.common.service.BaseService;
import com.hz.cake.master.core.model.replacepay.ReplacePayModel;

import java.util.List;

/**
 * @Description 代付资源的Service层
 * @Author yoko
 * @Date 2021/6/9 14:03
 * @Version 1.0
 */
public interface ReplacePayService<T> extends BaseService<T> {


    /**
     * @Description: 获取代付的出码集合数据
     * @param model
     * @return
     * @author yoko
     * @date 2021/6/18 17:00
     */
    public List<ReplacePayModel> getReplacePayList(ReplacePayModel model);
}
