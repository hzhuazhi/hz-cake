package com.hz.cake.master.core.model.channel;

import com.hz.cake.master.core.protocol.page.BasePage;

import java.io.Serializable;

/**
 * @Description 商户与银行卡的绑定关系的实体属性Bean
 * @Author yoko
 * @Date 2020/9/8 20:48
 * @Version 1.0
 */
public class ChannelBankModel extends BasePage implements Serializable {
    private static final long   serialVersionUID = 1203223201111L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 名称/别名
     */
    private String alias;


    /**
     * 商户ID：对应表tb_fr_channel的主键ID
     */
    private Long channelId;

    /**
     * 卡商银行卡主键ID：对应表tb_fr_merchant_bank的主键ID
     */
    private Long bankId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 使用状态:1初始化有效正常使用，2无效暂停使用
     */
    private Integer useStatus;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 是否有效：0有效，1无效/删除
     */
    private Integer yn;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public Long getBankId() {
        return bankId;
    }

    public void setBankId(Long bankId) {
        this.bankId = bankId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getUseStatus() {
        return useStatus;
    }

    public void setUseStatus(Integer useStatus) {
        this.useStatus = useStatus;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getYn() {
        return yn;
    }

    public void setYn(Integer yn) {
        this.yn = yn;
    }
}
