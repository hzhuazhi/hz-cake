package com.hz.cake.master.core.service;

import com.hz.cake.master.core.common.service.BaseService;
import com.hz.cake.master.core.model.order.OrderOutModel;
import com.hz.cake.master.core.model.order.OrderOutPrepareModel;
import com.hz.cake.master.core.model.replacepay.ReplacePayGainModel;

/**
 * @Description 代付预备请求的Service层
 * @Author yoko
 * @Date 2020/10/29 17:46
 * @Version 1.0
 */
public interface OrderOutPrepareService<T> extends BaseService<T> {


    /**
     * @Description: 处理代付预备请求的逻辑
     * <p>
     *     1.添加代付预备请求的数据。
     *     2.添加代付订单信息。
     *     3.添加第三方代付主动拉取结果的数据。
     * </p>
     * @param orderOutPrepareModel - 代付预备请求信息
     * @param orderOutModel - 代付订单信息
     * @return: boolean
     * @author: yoko
     * @date: 2021/11/26 16:02
     * @version 1.0.0
     */
    public boolean handleOrderOutPrepare(OrderOutPrepareModel orderOutPrepareModel, OrderOutModel orderOutModel) throws Exception;

}
