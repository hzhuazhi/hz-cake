package com.hz.cake.master.core.mapper;

import com.hz.cake.master.core.common.dao.BaseDao;
import com.hz.cake.master.core.model.replacepay.ReplacePayModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description 代付资源的Dao层
 * @Author yoko
 * @Date 2021/6/9 14:50
 * @Version 1.0
 */
@Mapper
public interface ReplacePayMapper<T> extends BaseDao<T> {

    /**
     * @Description: 获取代付的出码集合数据
     * @param model
     * @return
     * @author yoko
     * @date 2021/6/18 17:00
    */
    public List<ReplacePayModel> getReplacePayList(ReplacePayModel model);
}
