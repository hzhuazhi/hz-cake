package com.hz.cake.master.core.mapper;

import com.hz.cake.master.core.common.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 卡商绑定渠道的手续费的Dao层
 * @Author yoko
 * @Date 2020/11/26 13:19
 * @Version 1.0
 */
@Mapper
public interface MerchantServiceChargeMapper<T> extends BaseDao<T> {
}
