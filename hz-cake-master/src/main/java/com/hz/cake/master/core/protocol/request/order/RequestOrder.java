package com.hz.cake.master.core.protocol.request.order;


import com.hz.cake.master.core.protocol.base.BaseRequest;

import java.io.Serializable;

/**
 * @Description 协议：任务订单（平台派发订单）-内部调用
 * @Author yoko
 * @Date 2020/5/22 10:54
 * @Version 1.0
 */
public class RequestOrder extends BaseRequest implements Serializable {
    private static final long   serialVersionUID = 1233283332513L;

    /**
     * 订单号
     */
    public String orderNo;

    /**
     * 转账用户：用户转账的银行归属名字
     */
    public String transferUser;

    public RequestOrder(){

    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getTransferUser() {
        return transferUser;
    }

    public void setTransferUser(String transferUser) {
        this.transferUser = transferUser;
    }
}
