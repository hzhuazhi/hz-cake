package com.hz.cake.master.core.model.channel;

import com.hz.cake.master.core.protocol.page.BasePage;

import java.io.Serializable;

/**
 * @Description 商户信息的实体属性Bean
 * @Author yoko
 * @Date 2020/9/8 20:48
 * @Version 1.0
 */
public class ChannelModel extends BasePage implements Serializable {
    private static final long   serialVersionUID = 1203223201110L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 名称/别名
     */
    private String alias;

    /**
     * 商户秘钥
     */
    private String secretKey;

    /**
     * 银行卡绑定类型：1无需绑定银行卡，2需要绑定银行卡
     */
    private Integer bankBindingType;

    /**
     * 渠道类型：1代收，2大包，3代付
     */
    private Integer channelType;

    /**
     * 代收金额订单范围：每单代收的金额范围以“-”进行分割的
     */
    private String inMoneyRange;

    /**
     * 代付金额订单范围：每单代付的金额范围以“-”进行分割的
     */
    private String outMoneyRange;

    /**
     * 出码后银行卡金额的锁定时间
     */
    private Integer moneyLockTime;

    /**
     * 出码后订单的支付时间
     */
    private Integer invalidTimeNum;

    /**
     * 代付方式：0初始化，1直接转账，2预备转账（预备是走TASK）
     */
    private Integer replacePayType;


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

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
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

    public Integer getBankBindingType() {
        return bankBindingType;
    }

    public void setBankBindingType(Integer bankBindingType) {
        this.bankBindingType = bankBindingType;
    }

    public String getInMoneyRange() {
        return inMoneyRange;
    }

    public void setInMoneyRange(String inMoneyRange) {
        this.inMoneyRange = inMoneyRange;
    }

    public String getOutMoneyRange() {
        return outMoneyRange;
    }

    public void setOutMoneyRange(String outMoneyRange) {
        this.outMoneyRange = outMoneyRange;
    }

    public Integer getMoneyLockTime() {
        return moneyLockTime;
    }

    public void setMoneyLockTime(Integer moneyLockTime) {
        this.moneyLockTime = moneyLockTime;
    }

    public Integer getInvalidTimeNum() {
        return invalidTimeNum;
    }

    public void setInvalidTimeNum(Integer invalidTimeNum) {
        this.invalidTimeNum = invalidTimeNum;
    }

    public Integer getChannelType() {
        return channelType;
    }

    public void setChannelType(Integer channelType) {
        this.channelType = channelType;
    }

    public Integer getReplacePayType() {
        return replacePayType;
    }

    public void setReplacePayType(Integer replacePayType) {
        this.replacePayType = replacePayType;
    }
}
