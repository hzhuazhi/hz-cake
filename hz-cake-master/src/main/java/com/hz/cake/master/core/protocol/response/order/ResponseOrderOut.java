package com.hz.cake.master.core.protocol.response.order;

import com.hz.cake.master.core.protocol.base.BaseResponse;

import java.io.Serializable;

/**
 * @Description 协议：任务订单（平台派发代付订单）
 * @Author yoko
 * @Date 2020/10/30 21:47
 * @Version 1.0
 */
public class ResponseOrderOut extends BaseResponse implements Serializable {
    private static final long   serialVersionUID = 1233023131152L;

    public OrderOut orderOut;// 正式代付订单派单成功的数据
    public Integer rowCount;



    public ResponseOrderOut(){

    }

    public OrderOut getOrderOut() {
        return orderOut;
    }

    public void setOrderOut(OrderOut orderOut) {
        this.orderOut = orderOut;
    }

    @Override
    public Integer getRowCount() {
        return rowCount;
    }

    @Override
    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }
}
