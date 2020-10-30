package com.hz.cake.master.core.service;

import com.hz.cake.master.core.common.service.BaseService;
import com.hz.cake.master.core.model.channel.ChannelModel;
import com.hz.cake.master.core.model.merchant.MerchantModel;
import com.hz.cake.master.core.model.order.OrderOutModel;
import com.hz.cake.master.core.protocol.request.order.ProtocolOrderOut;

import java.util.List;

/**
 * @Description 代付订单的Service层
 * @Author yoko
 * @Date 2020/10/29 17:46
 * @Version 1.0
 */
public interface OrderOutService<T> extends BaseService<T> {

    /**
     * @Description: 筛选卡商
     * @param merchantList - 卡商集合
     * @param orderMoney - 订单金额
     * @param orderNo - 订单号
     * @return
     * @author yoko
     * @date 2020/10/30 17:46
    */
    public MerchantModel screenMerchantByMoney(List<MerchantModel> merchantList, String orderMoney, String orderNo) throws Exception;
}
