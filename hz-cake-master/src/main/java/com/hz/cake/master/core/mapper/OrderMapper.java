package com.hz.cake.master.core.mapper;

import com.hz.cake.master.core.common.dao.BaseDao;
import com.hz.cake.master.core.model.order.OrderModel;
import org.apache.ibatis.annotations.Mapper;


/**
 * @Description 任务订单的Dao层
 * @Author yoko
 * @Date 2020/5/21 19:32
 * @Version 1.0
 */
@Mapper
public interface OrderMapper<T> extends BaseDao<T> {

    /**
     * @Description: 根据订单号查询订单状态
     * @param model
     * @return
     * @author yoko
     * @date 2020/6/8 20:00
     */
    public int getOrderStatus(OrderModel model);

    /**
     * @Description: 更新订单的转账人信息
     * @param model - 订单信息
     * @return
     * @author yoko
     * @date 2021/5/6 16:15
     */
    public int updateTransferUserByOrderNo(OrderModel model);

}
