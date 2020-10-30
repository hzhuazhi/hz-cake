package com.hz.cake.master.core.protocol.request.order;

import java.io.Serializable;

/**
 * @Description 出码的协议-第三方接口-代付
 * @Author yoko
 * @Date 2020/10/29 21:47
 * @Version 1.0
 */
public class ProtocolOrderOut implements Serializable {
    private static final long   serialVersionUID = 1233283332519L;

    /**
     * 订单金额
     */
    public String money;

    /**
     * 商家订单号
     */
    public String outTradeNo;

    /**
     * 收款银行卡卡号
     */
    public String inBankCard;

    /**
     * 收款银行
     */
    public String inBankName;

    /**
     * 收款开户名
     */
    public String inAccountName;


    /**
     * 商户秘钥
     */
    public String secretKey;

    /**
     * 同步地址
     */
    public String notifyUrl;

    /**
     * 支付成功之后自动跳转的地址
     */
    public String returnUrl;

    public ProtocolOrderOut(){

    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getInBankCard() {
        return inBankCard;
    }

    public void setInBankCard(String inBankCard) {
        this.inBankCard = inBankCard;
    }

    public String getInBankName() {
        return inBankName;
    }

    public void setInBankName(String inBankName) {
        this.inBankName = inBankName;
    }

    public String getInAccountName() {
        return inAccountName;
    }

    public void setInAccountName(String inAccountName) {
        this.inAccountName = inAccountName;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
}
