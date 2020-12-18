package com.hz.cake.master.core.mapper;

import com.hz.cake.master.core.common.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 支付页统计用户IP的Dao层
 * @Author yoko
 * @Date 2020/12/18 2:03
 * @Version 1.0
 */
@Mapper
public interface StatisticsIpMapper<T> extends BaseDao<T> {
}
