package com.hz.cake.master.core.mapper;

import com.hz.cake.master.core.common.dao.BaseDao;
import com.hz.cake.master.core.model.merchant.MerchantModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 卡商扩充数据的Dao层
 * @Author yoko
 * @Date 2020/9/8 17:05
 * @Version 1.0
 */
@Mapper
public interface MerchantMapper<T> extends BaseDao<T> {

    /**
     * @Description: 更新卡商的使用状态
     * @param model
     * @return
     * @author yoko
     * @date 2020/9/5 19:21
     */
    public int updateUseStatus(MerchantModel model);


    /**
     * @Description: 更新卡商的余额
     * <p>
     *     余额加减，
     *     字段addBalance不为空，则余额加；
     *     字段subtractBalance不为空，则余额减
     * </p>
     * @param model
     * @return
     * @author yoko
     * @date 2020/10/30 17:04
    */
    public int updateBalance(MerchantModel model);
}
