package com.hz.cake.master.util;

import com.alibaba.fastjson.JSON;
import com.hz.cake.master.core.common.utils.StringUtil;
import com.hz.cake.master.core.model.bank.BankPoolModel;
import com.hz.cake.master.core.model.channel.ChannelBankPoolModel;
import com.hz.cake.master.core.model.merchant.*;
import com.hz.cake.master.core.model.order.OrderOutModel;
import com.hz.cake.master.core.model.statistics.StatisticsIpModel;
import com.hz.cake.master.core.model.strategy.StrategyData;
import com.hz.cake.master.core.protocol.request.issue.RequestIssue;
import com.hz.cake.master.core.protocol.request.order.ProtocolOrder;
import com.hz.cake.master.core.protocol.request.order.ProtocolOrderOut;
import com.hz.cake.master.core.protocol.request.order.RequestOrder;
import com.hz.cake.master.core.protocol.request.statistics.RequestStatisticsClickPay;
import com.hz.cake.master.core.protocol.response.ResponseData;
import com.hz.cake.master.core.protocol.response.issue.Issue;
import com.hz.cake.master.core.protocol.response.issue.ResponseIssue;
import com.hz.cake.master.core.protocol.response.order.Order;
import com.hz.cake.master.core.protocol.response.order.OrderOut;
import com.hz.cake.master.core.protocol.response.order.ResponseOrder;
import com.hz.cake.master.core.common.exception.ServiceException;
import com.hz.cake.master.core.common.utils.BeanUtils;
import com.hz.cake.master.core.common.utils.DateUtil;
import com.hz.cake.master.core.common.utils.ShortChainUtil;
import com.hz.cake.master.core.common.utils.constant.CacheKey;
import com.hz.cake.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.cake.master.core.common.utils.constant.ErrorCode;
import com.hz.cake.master.core.common.utils.constant.ServerConstant;
import com.hz.cake.master.core.model.bank.BankModel;
import com.hz.cake.master.core.model.channel.ChannelBankModel;
import com.hz.cake.master.core.model.channel.ChannelModel;
import com.hz.cake.master.core.model.issue.IssueModel;
import com.hz.cake.master.core.model.mobilecard.MobileCardModel;
import com.hz.cake.master.core.model.order.OrderModel;
import com.hz.cake.master.core.model.region.RegionModel;
import com.hz.cake.master.core.model.shortchain.ShortChainModel;
import com.hz.cake.master.core.model.statistics.StatisticsClickPayModel;
import com.hz.cake.master.core.model.strategy.StrategyModel;
import com.hz.cake.master.core.protocol.response.order.ResponseOrderOut;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


/**
 * @Description 公共方法类
 * @Author yoko
 * @Date 2020/1/7 20:25
 * @Version 1.0
 */
public class HodgepodgeMethod {
    private static Logger log = LoggerFactory.getLogger(HodgepodgeMethod.class);

    /**
     * @Description: 组装查询地域的查询条件
     * @param ip
     * @return RegionModel
     * @author yoko
     * @date 2019/12/18 18:41
     */
    public static RegionModel assembleRegionModel(String ip){
        RegionModel resBean = new RegionModel();
        resBean.setIp(ip);
        return resBean;
    }

    /**
     * @Description: 组装获取用户归属的省份跟城市
     * @param regionModel
     * @return
     * @author yoko
     * @date 2020/7/15 19:06
    */
    public static RegionModel getRegion(RegionModel regionModel){
        RegionModel resBean = new RegionModel();
        if (regionModel != null){
            // 获取地域信息
            if (!StringUtils.isBlank(regionModel.getIp())){
                regionModel = ComponentUtil.regionService.getCacheRegion(regionModel);
                resBean.setIp(regionModel.getIp());
                if (!StringUtils.isBlank(regionModel.getProvince())){
                    resBean.setProvince(regionModel.getProvince());
                }
                if (!StringUtils.isBlank(regionModel.getCity())){
                    resBean.setCity(regionModel.getCity());
                }
            }
        }
        return resBean;
    }

    /**
     * @Description: 公共的返回客户端的方法
     * @param stime - 服务器的时间
     * @param token - 登录token
     * @param sign - 签名
     * @return java.lang.String
     * @author yoko
     * @date 2019/11/13 21:45
     */
    public static String assembleResult(long stime, String token, String sign){
        ResponseData dataModel = new ResponseData();
        dataModel.stime = stime;
        if (!StringUtils.isBlank(token)){
            dataModel.token = token;
        }
        dataModel.sign = sign;
        return JSON.toJSONString(dataModel);
    }


    /**
     * @Description: check校验数据添加支付用户点击支付页统计的时候
     * @param requestModel
     * @return
     * @author yoko
     * @date 2020/05/14 15:57
     */
    public static void checkClickPayAdd(RequestStatisticsClickPay requestModel) throws Exception{
        // 1.校验所有数据
        if (requestModel == null ){
            throw new ServiceException(ErrorCode.ENUM_ERROR.ST00001.geteCode(), ErrorCode.ENUM_ERROR.ST00001.geteDesc());
        }
        // 校验标识值是否为空
        if (StringUtils.isBlank(requestModel.identif)){
            throw new ServiceException(ErrorCode.ENUM_ERROR.ST00002.geteCode(), ErrorCode.ENUM_ERROR.ST00002.geteDesc());
        }

        // 校验数据来源类型是否为空
        if (requestModel.dataType == null || requestModel.dataType == 0){
            throw new ServiceException(ErrorCode.ENUM_ERROR.ST00003.geteCode(), ErrorCode.ENUM_ERROR.ST00003.geteDesc());
        }
    }

    /**
     * @Description: 组装支付用户点击支付页统计的数据
     * @param requestModel - 用户支付点击数据
     * @param regionModel - 用户的地域信息
     * @return
     * @author yoko
     * @date 2020/7/15 19:10
     */
    public static StatisticsClickPayModel assembleStatisticsClickPayData(RequestStatisticsClickPay requestModel, RegionModel regionModel){
        StatisticsClickPayModel resBean = BeanUtils.copy(requestModel, StatisticsClickPayModel.class);
        if (regionModel != null){
            if (!StringUtils.isBlank(regionModel.getIp())){
                regionModel = ComponentUtil.regionService.getCacheRegion(regionModel);
                resBean.setIp(regionModel.getIp());
                if (!StringUtils.isBlank(regionModel.getProvince())){
                    resBean.setProvince(regionModel.getProvince());
                }
                log.info("");
                if (!StringUtils.isBlank(regionModel.getCity())){
                    resBean.setCity(regionModel.getCity());
                }
            }
        }
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }


    /**
     * @Description: check校验数据当派发订单的时候
     * @param requestModel
     * @return
     * @author yoko
     * @date 2020/05/14 15:57
     */
    public static void checkOrderAdd(ProtocolOrder requestModel) throws Exception{
        // 1.校验所有数据
        if (requestModel == null ){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OR00001.geteCode(), ErrorCode.ENUM_ERROR.OR00001.geteDesc());
        }

        // 校验金额是否为空
        if (StringUtils.isBlank(requestModel.money)){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OR00002.geteCode(), ErrorCode.ENUM_ERROR.OR00002.geteDesc());
        }else {
            // 金额是否有效
            if (requestModel.money.indexOf(".") > -1){
                boolean flag = StringUtil.isNumberByMoney(requestModel.money);
                if (!flag){
                    throw new ServiceException(ErrorCode.ENUM_ERROR.OR00006.geteCode(), ErrorCode.ENUM_ERROR.OR00006.geteDesc());
                }
            }else {
                boolean flag = StringUtil.isNumer(requestModel.money);
                if (!flag){
                    throw new ServiceException(ErrorCode.ENUM_ERROR.OR00007.geteCode(), ErrorCode.ENUM_ERROR.OR00007.geteDesc());
                }
            }
        }

        // 校验支付类型为空
        if (requestModel.payType == null || requestModel.payType == 0){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OR00003.geteCode(), ErrorCode.ENUM_ERROR.OR00003.geteDesc());
        }

        if (StringUtils.isBlank(requestModel.secretKey)){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OR00008.geteCode(), ErrorCode.ENUM_ERROR.OR00008.geteDesc());
        }
    }

    /**
     * @Description: 组装查询策略数据条件的方法
     * @return com.pf.play.rule.core.model.strategy.StrategyModel
     * @author yoko
     * @date 2020/5/19 17:12
     */
    public static StrategyModel assembleStrategyQuery(int stgType){
        StrategyModel resBean = new StrategyModel();
        resBean.setStgType(stgType);
        return resBean;
    }


    /**
     * @Description: 校验策略类型数据:出码开关-判断此时是否属于正常出码
     * @return void
     * @author yoko
     * @date 2019/12/2 14:35
     */
    public static void checkStrategyByQrCodeSwitch(StrategyModel strategyModel) throws Exception{
        if (strategyModel == null){
            throw new ServiceException(ErrorCode.ENUM_ERROR.S00001.geteCode(), ErrorCode.ENUM_ERROR.S00001.geteDesc());
        }
        if (strategyModel.getStgNumValue() == 1){
            throw new ServiceException(ErrorCode.ENUM_ERROR.S00002.geteCode(), ErrorCode.ENUM_ERROR.S00002.geteDesc());
        }
        if (strategyModel.getStgNumValue() == 2){
            if (StringUtils.isBlank(strategyModel.getStgValue())){
                throw new ServiceException(ErrorCode.ENUM_ERROR.S00003.geteCode(), ErrorCode.ENUM_ERROR.S00003.geteDesc());
            }else{
                String[] str = strategyModel.getStgValue().split("-");
                boolean flag = DateUtil.isBelong(str[0], str[1]);
                if (!flag){
                    throw new ServiceException(ErrorCode.ENUM_ERROR.S00004.geteCode(), ErrorCode.ENUM_ERROR.S00004.geteDesc());
                }
            }
        }
    }


    /**
     * @Description: check订单是否在策略规定的范围内
     * @param orderMoneyRange - 策略规定的订单金额范围
     * @param orderMoney - 订单金额
     * @return java.lang.String
     * @author yoko
     * @date 2020/6/6 11:48
     */
    public static void checkOrderMoney(String orderMoneyRange, String orderMoney) throws Exception{
        String [] rule = orderMoneyRange.split("-");
        double start = Double.parseDouble(rule[0]);
        double end = Double.parseDouble(rule[1]);
        double money = Double.parseDouble(orderMoney);
        if (money < start || money > end){
            throw new ServiceException(ErrorCode.ENUM_ERROR.S00005.geteCode(), ErrorCode.ENUM_ERROR.S00005.geteDesc());
        }
    }


    /**
     * @Description: 组装查询卡商的查询方法
     * @param id - 主键ID
     * @param orderMoney - 订单金额
     * @param useStatus - 使用状态
     * @return MerchantModel
     * @author yoko
     * @date 2020/9/9 17:16
     */
    public static MerchantModel assembleMerchantQuery(long id, String orderMoney, int useStatus){
        MerchantModel resBean = new MerchantModel();
        if (id > 0){
            resBean.setId(id);
        }
        if (!StringUtils.isBlank(orderMoney)){
            BigDecimal bd = new BigDecimal(orderMoney);
            resBean.setMoney(bd);
        }
        if (useStatus > 0){
            resBean.setUseStatus(useStatus);
        }
        return resBean;
    }


    /**
     * @Description: 组装查询商户的查询方法
     * @param id - 主键ID
     * @param secretKey - 秘钥
     * @param useStatus - 使用状态
     * @return MerchantModel
     * @author yoko
     * @date 2020/9/9 17:16
     */
    public static ChannelModel assembleChannelQuery(long id, String secretKey, int useStatus){
        ChannelModel resBean = new ChannelModel();
        if (id > 0){
            resBean.setId(id);
        }
        if (!StringUtils.isBlank(secretKey)){
            resBean.setSecretKey(secretKey);
        }
        if (useStatus > 0){
            resBean.setUseStatus(useStatus);
        }
        return resBean;
    }

    /**
     * @Description: check商户数据是否为空
     * @param channelModel
     * @return
     * @author yoko
     * @date 2020/05/14 15:57
     */
    public static void checkChannelIsNull(ChannelModel channelModel) throws Exception{
        if (channelModel == null || channelModel.getId() == null || channelModel.getId() <= 0){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OR00009.geteCode(), ErrorCode.ENUM_ERROR.OR00009.geteDesc());
        }
    }

    /**
     * @Description: check卡商数据是否为空
     * @param merchantList
     * @return
     * @author yoko
     * @date 2020/05/14 15:57
     */
    public static void checkMerchantIsNull(List<MerchantModel> merchantList) throws Exception{
        if (merchantList == null || merchantList.size() <= 0){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OR00010.geteCode(), ErrorCode.ENUM_ERROR.OR00010.geteDesc());
        }
    }

    /**
     * @Description: 组装查询手机号的查询方法
     * @param id - 主键ID
     * @param phoneNum - 手机号
     * @param isArrears - 是否欠费：1未欠费，2欠费
     * @param heartbeatStatus - 心跳状态：1初始化异常，2正常
     * @param useStatus - 使用状态:1初始化有效正常使用，2无效暂停使用
     * @return MobileCardModel
     * @author yoko
     * @date 2020/9/12 14:53
     */
    public static MobileCardModel assembleMobileCardQuery(long id, String phoneNum, int isArrears, int heartbeatStatus, int useStatus){
        MobileCardModel resBean = new MobileCardModel();
        if (id > 0){
            resBean.setId(id);
        }
        if (!StringUtils.isBlank(phoneNum)){
            resBean.setPhoneNum(phoneNum);
        }
        if (isArrears > 0){
            resBean.setIsArrears(isArrears);
        }
        if (heartbeatStatus > 0){
            resBean.setHeartbeatStatus(heartbeatStatus);
        }
        if (useStatus > 0){
            resBean.setUseStatus(useStatus);
        }
        return resBean;
    }

    /**
     * @Description: check校验手机卡数据是否为空
     * @param mobileCardList
     * @return
     * @author yoko
     * @date 2020/9/12 15:00
    */
    public static void checkmobileCardIsNull(List<MobileCardModel> mobileCardList) throws Exception{
        if (mobileCardList == null || mobileCardList.size() <= 0){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OR00011.geteCode(), ErrorCode.ENUM_ERROR.OR00011.geteDesc());
        }
    }


    /**
     * @Description: 组装查询商户与银行卡绑定关系的查询方法
     * @param id - 主键ID
     * @param channelId - 商户ID
     * @param bankId - 银行卡ID
     * @param useStatus - 使用状态
     * @return MerchantModel
     * @author yoko
     * @date 2020/9/9 17:16
     */
    public static ChannelBankModel assembleChannelBankQuery(long id, long channelId, long bankId, int useStatus){
        ChannelBankModel resBean = new ChannelBankModel();
        if (id > 0){
            resBean.setId(id);
        }
        if (channelId > 0){
            resBean.setChannelId(channelId);
        }
        if (bankId > 0){
            resBean.setBankId(bankId);
        }
        if (useStatus > 0){
            resBean.setUseStatus(useStatus);
        }
        return resBean;
    }

    /**
     * @Description: 组装银行卡以及放量策略的查询条件
     * @param orderMoney - 订单金额
     * @param bankBindingType - 银行卡绑定类型：1无需绑定银行卡，2需要绑定银行卡
     * @param bankIdList - 银行卡ID集合
     * @param bankUpAndDownSwitch - 自动上下线银行卡开关：1关闭自动上下线卡，2打开自动上下线卡
     * @return
     * @author yoko
     * @date 2020/9/12 20:15
    */
    public static BankModel assembleBankByOrderQuery(String orderMoney, int bankBindingType, List<Long> bankIdList, int bankUpAndDownSwitch){
        BankModel resBean = new BankModel();
        if (!StringUtils.isBlank(orderMoney)){
            BigDecimal bd = new BigDecimal(orderMoney);
            resBean.setMoney(bd);
        }
//        if (bankBindingType == 1){
//            // 无需绑定银行卡
//            if (bankIdList != null && bankIdList.size() > 0){
//                resBean.setNoBankIdList(bankIdList);
//            }
//        }else if (bankBindingType == 2){
//            // 需要绑定银行卡
//            if (bankIdList != null && bankIdList.size() > 0){
//                resBean.setYesBankIdList(bankIdList);
//            }
//        }
        if (bankIdList != null && bankIdList.size() > 0){
            resBean.setYesBankIdList(bankIdList);
        }
        if (bankUpAndDownSwitch == 2){
            resBean.setBankUpAndDownSwitch(2);
        }
        return resBean;
    }

    /**
     * @Description: check校验银行卡以及银行卡的放量策略数据是否为空
     * @param bankList
     * @return
     * @author yoko
     * @date 2020/9/12 20:19
    */
    public static void checkBankIsNull(List<BankModel> bankList) throws Exception{
        if (bankList == null || bankList.size() <= 0){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OR00012.geteCode(), ErrorCode.ENUM_ERROR.OR00012.geteDesc());
        }
    }


    /**
     * @Description: check校验渠道与银行卡绑定数据是否为空
     * @param bankIdList
     * @return
     * @author yoko
     * @date 2020/9/12 20:19
     */
    public static void checkBankRelationIsNull(List<Long> bankIdList) throws Exception{
        if (bankIdList == null || bankIdList.size() <= 0){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OR00018.geteCode(), ErrorCode.ENUM_ERROR.OR00018.geteDesc());
        }
    }

    /**
     * @Description: 根据银行卡优先级排序银行卡集合
     * <p>
     *     商户与银行卡绑定关系的优先级排在集合的前面；
     *     商户未与银行卡绑定关系的优先级排在集合的后面
     *
     *     如果bankOutType=1，则需要只给出绑定关系的银行卡
     * </p>
     * @param bankList - 银行卡集合
     * @param bankIdList - 渠道绑定的银行卡ID集合
     * @param bankOutType -给出银行卡是否要绑定才给出 ：1需要绑定，2无需绑定
     * @return java.util.List<BankModel>
     * @author yoko
     * @date 2020/9/12 20:26
     */
    public static List<BankModel> assembleBankByPriority(List<BankModel> bankList, List<Long> bankIdList, int bankOutType){
        List<BankModel> resList = new ArrayList<>();
        if (bankIdList == null || bankIdList.size() <= 0){
            resList = bankList;
        }else {
            List<BankModel> yesList = new ArrayList<>();
            List<BankModel> noList = new ArrayList<>();
            for (BankModel bankModel : bankList){
                int num = 0;
                for (long bankId : bankIdList){
                    if (bankModel.getId() == bankId){
                        num = 1;
                        break;
                    }
                }
                if (num != 0){
                    yesList.add(bankModel);
                }else {
                    if (bankOutType == 2){
                        noList.add(bankModel);
                    }
                }
            }

            if (yesList != null && yesList.size() > 0){
                resList.addAll(yesList);
            }
            if (noList != null && noList.size() > 0){
                resList.addAll(noList);
            }

        }
        return resList;
    }

    /**
     * @Description: check筛选的银行卡是否为空
     * @param bankModel
     * @return
     * @author yoko
     * @date 2020/9/13 14:41
    */
    public static void checkScreenBankIsNull(BankModel bankModel) throws Exception{
        if (bankModel == null || bankModel.getId() == null || bankModel.getId() <= 0){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OR00013.geteCode(), ErrorCode.ENUM_ERROR.OR00013.geteDesc());
        }
    }


    /**
     * @Description: 组装添加订单的方法
     * @param bankModel - 筛选出的银行卡
     * @param requestModel - 请求数据
     * @param channelModel - 商户信息
     * @param orderNo - 订单号
     * @param invalidTimeNum - 策略中的失效时间
     * @param serviceCharge - 卡商的手续费
     * @param bankDownTimeNum - 自动上下卡检测卡订单失败率的延迟时间
     * @return OrderModel
     * @author yoko
     * @date 2020/9/13 14:41
     */
    public static OrderModel assembleOrderAdd(BankModel bankModel, ProtocolOrder requestModel, ChannelModel channelModel, String orderNo, int invalidTimeNum, String serviceCharge, int bankPoolType,
                                              int bankDownTimeNum){
        OrderModel resBean = new OrderModel();
        resBean.setOrderNo(orderNo);
        resBean.setBankId(bankModel.getId());
        resBean.setOrderType(requestModel.payType);
        resBean.setOrderMoney(requestModel.money);
        resBean.setDistributionMoney(bankModel.getDistributionMoney());
        resBean.setOutTradeNo(requestModel.outTradeNo);
        // 订单失效时间
        String invalidTime = DateUtil.addDateMinute(invalidTimeNum);
        resBean.setInvalidTime(invalidTime);
        String downTime = DateUtil.addDateMinute(bankDownTimeNum);
        resBean.setDownTime(downTime);
        resBean.setNotifyUrl(requestModel.notifyUrl);
        resBean.setBankName(bankModel.getBankName());
        resBean.setBankCard(bankModel.getBankCard());
        resBean.setAccountName(bankModel.getAccountName());
        resBean.setBankCode(bankModel.getBankCode());
        resBean.setMobileCardId(bankModel.getMobileCardId());
        resBean.setPhoneNum(bankModel.getPhoneNum());
        resBean.setMerchantId(bankModel.getMerchantId());
        if (!StringUtils.isBlank(bankModel.getAcName())){
            resBean.setMerchantName(bankModel.getAcName());
        }
        if (bankModel.getMerchantSiteId() != null && bankModel.getMerchantSiteId() > 0){
            resBean.setMerchantSiteId(bankModel.getMerchantSiteId());
        }
        if (!StringUtils.isBlank(bankModel.getCardSiteName())){
            resBean.setCardSiteName(bankModel.getCardSiteName());
        }

        if (channelModel != null && channelModel.getId() != null && channelModel.getId() > 0){
            resBean.setChannelId(channelModel.getId());
            if (!StringUtils.isBlank(channelModel.getAlias())){
                resBean.setChannelName(channelModel.getAlias());
            }
        }
        if (!StringUtils.isBlank(serviceCharge)){
            resBean.setServiceCharge(serviceCharge);
        }
        if (bankPoolType > 0){
            resBean.setBankPoolType(bankPoolType);
        }

        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }

    /**
     * @Description: check校验添加订单信息是否正常
     * @param num
     * @return
     * @author yoko
     * @date 2020/9/13 14:49
    */
    public static void checkAddOrderIsOk(int num) throws Exception{
        if (num <= 0){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OR00014.geteCode(), ErrorCode.ENUM_ERROR.OR00014.geteDesc());
        }
    }


    /**
     * @Description: 用户派单成功的数据组装返回客户端的方法
     * @param stime - 服务器的时间
     * @param sign - 签名
     * @param orderModel - 用户派单的详情
     * @param returnUrl - 支付完成之后自动跳转的地址
     * @param payQrCodeUrl - 生成的HTML页面的地址
     * @param shortChain - 短链
     * @return java.lang.String
     * @author yoko
     * @date 2019/11/25 22:45
     */
    public static String assembleOrderDataResult(long stime, String sign, OrderModel orderModel, String returnUrl, String payQrCodeUrl, String shortChain) throws Exception{
        ResponseOrder dataModel = new ResponseOrder();
        if (orderModel != null){
            Order order = new Order();
            order.orderNo = orderModel.getOrderNo();
            if (!StringUtils.isBlank(orderModel.getQrCode())){
                order.qrCode = orderModel.getQrCode();
            }
            order.orderMoney = orderModel.getOrderMoney();
            order.orderStatus = 1;
            order.invalidTime = orderModel.getInvalidTime();
            int invalidSecond = DateUtil.calLastedTime(orderModel.getInvalidTime());
            order.invalidSecond = String.valueOf(invalidSecond);
            order.bankName = orderModel.getBankName();
            order.bankCard = orderModel.getBankCard();
            order.accountName = orderModel.getAccountName();
            order.bankCode = orderModel.getBankCode();

            String resQrCodeUrl = "";
            if (!StringUtils.isBlank(returnUrl)){
                resQrCodeUrl = payQrCodeUrl + "?" + "orderNo=" +  orderModel.getOrderNo() + "&" + "returnUrl=" + returnUrl;
            }else {
                resQrCodeUrl = payQrCodeUrl + "?" + "orderNo=" +  orderModel.getOrderNo() + "&" + "returnUrl=";
            }
            order.qrCodeUrl = URLEncoder.encode(resQrCodeUrl,"UTF-8");
            if (!StringUtils.isBlank(shortChain)){
                order.shortChain = shortChain;
            }
            dataModel.order = order;
        }
        dataModel.setStime(stime);
        dataModel.setSign(sign);
        return JSON.toJSONString(dataModel);
    }


    /**
     * @Description: check校验数据获取派单数据-详情-返回码的接口时
     * @param requestModel
     * @return
     * @author yoko
     * @date 2020/05/14 15:57
     */
    public static void checkOrderByQrCodeData(RequestOrder requestModel) throws Exception{
        // 1.校验所有数据
        if (requestModel == null ){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OR00015.geteCode(), ErrorCode.ENUM_ERROR.OR00015.geteDesc());
        }

        if (StringUtils.isBlank(requestModel.orderNo)){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OR00016.geteCode(), ErrorCode.ENUM_ERROR.OR00016.geteDesc());
        }
    }

    /**
     * @Description: check短链数据是否为空
     * @param shortChainModel
     * @return
     * @author yoko
     * @date 2020/9/13 15:48
    */
    public static void checkShortChainIsNull(ShortChainModel shortChainModel) throws Exception{
        if (shortChainModel == null || shortChainModel.getId() == null || shortChainModel.getId() <= 0){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OR00017.geteCode(), ErrorCode.ENUM_ERROR.OR00017.geteDesc());
        }
    }

    /**
     * @Description: 组装根据派单的订单号查询派单信息
     * @param orderNo - 派单的订单号
     * @return com.hz.fine.master.core.model.did.DidCollectionAccountModel
     * @author yoko
     * @date 2020/5/18 11:41
     */
    public static OrderModel assembleOrderByOrderNoQuery(String orderNo, int orderStatus){
        OrderModel resBean = new OrderModel();
        resBean.setOrderNo(orderNo);
        if (orderStatus > 0){
            resBean.setOrderStatus(orderStatus);
        }
        return resBean;
    }



    /**
     * @Description: 根据订单号查询的订单数据组装返回客户端的方法
     * @param stime - 服务器的时间
     * @param sign - 签名
     * @param orderModel - 用户派单的详情
     * @param shortChain - 短链
     * @param payTime - 订单的支付时间
     * @return java.lang.String
     * @author yoko
     * @date 2019/11/25 22:45
     */
    public static String assembleOrderByOrderNoDataResult(long stime, String sign, OrderModel orderModel, String shortChain, int payTime) throws Exception{
        ResponseOrder dataModel = new ResponseOrder();
        if (orderModel != null){
            Order order = new Order();
            order.orderNo = orderModel.getOrderNo();
            if (!StringUtils.isBlank(orderModel.getQrCode())){
                order.qrCode = orderModel.getQrCode();
            }
            order.orderMoney = orderModel.getOrderMoney();
            order.distributionMoney = orderModel.getDistributionMoney();

            //计算差额
            order.differenceMoney = StringUtil.getBigDecimalSubtractByStr(orderModel.getOrderMoney(), orderModel.getDistributionMoney()).replace("-", "");

            order.invalidTime = orderModel.getInvalidTime();
            int invalidSecond = DateUtil.calLastedTime(orderModel.getInvalidTime());
            order.invalidSecond = String.valueOf(invalidSecond);
            order.bankName = orderModel.getBankName();
            order.bankCard = orderModel.getBankCard();
            order.accountName = orderModel.getAccountName();
            order.bankCode = orderModel.getBankCode();
            if (!StringUtils.isBlank(shortChain)){
                order.shortChain = shortChain;
            }
            if (payTime > 0){
                order.payTime = payTime;
            }
            dataModel.order = order;
        }
        dataModel.setStime(stime);
        dataModel.setSign(sign);
        return JSON.toJSONString(dataModel);
    }


    /**
     * @Description: 调用短链的公共方法，生成短链
     * @param orderModel - 订单信息
     * @param interfaceAds - 短链的API
     * @param shortChainMoney  -  短链生成是否带金额的参数：1不带金额参数，2带金额参数。
     * @return
     * @author yoko
     * @date 2020/9/13 16:18
    */
    public static String getShortChain(OrderModel orderModel, String interfaceAds, int shortChainMoney){
        String str = null;
        if (orderModel != null && orderModel.getId() != null && orderModel.getId() > 0){
            if (orderModel.getOrderType() == 2){
                // 支付宝转卡
                BankModel bankModel = BeanUtils.copy(orderModel, BankModel.class);
                if (bankModel != null && !StringUtils.isBlank(bankModel.getBankCard())){
                    // 生成短链
                    str = ShortChainUtil.getShortChainUrl(bankModel, interfaceAds, shortChainMoney, orderModel.getOrderMoney());
                }
            }
        }
        return str;
    }


    /**
     * @Description: 查询派单成功的订单状态数据组装返回客户端的方法
     * @param stime - 服务器的时间
     * @param sign - 签名
     * @param orderStatus - 不等于0表示成功
     * @return java.lang.String
     * @author yoko
     * @date 2019/11/25 22:45
     */
    public static String assembleOrderStatusResult(long stime, String sign, int orderStatus){
        ResponseOrder resBean = new ResponseOrder();
        Order dataModel = new Order();
        dataModel.orderStatus = orderStatus;
        resBean.order = dataModel;
        resBean.setStime(stime);
        resBean.setSign(sign);
        return JSON.toJSONString(dataModel);
    }

    /**
     * @Description: 判断时间是否是在当前时间范围内
     * @param openTimeSlot
     * @return
     * @author yoko
     * @date 2020/9/23 14:15
    */
    public static boolean checkOpenTimeSlot(String openTimeSlot){
        boolean flag = false;
        String[] strArr = openTimeSlot.split("#");
        for (String str : strArr){
            String[] str_ = str.split("-");
            boolean flag_ = DateUtil.isBelong(str_[0], str_[1]);
            if (flag_){
                return true;
            }
        }
        return flag;
    }

    /**
     * @Description: check校验数据下发数据-集合
     * @param requestModel
     * @return
     * @author yoko
     * @date 2020/05/14 15:57
     */
    public static void checkIssueGetDataList(RequestIssue requestModel) throws Exception{
        // 1.校验所有数据
        if (requestModel == null ){
            throw new ServiceException(ErrorCode.ENUM_ERROR.I00001.geteCode(), ErrorCode.ENUM_ERROR.I00001.geteDesc());
        }
    }


    /**
     * @Description: 下发集合的数据组装返回客户端的方法
     * @param stime - 服务器的时间
     * @param sign - 签名
     * @param issueList - 下发集合
     * @param rowCount - 总行数
     * @return java.lang.String
     * @author yoko
     * @date 2019/11/25 22:45
     */
    public static String assembleIssueListResult(long stime, String sign, List<IssueModel> issueList, Integer rowCount){
        ResponseIssue dataModel = new ResponseIssue();
        if (issueList != null && issueList.size() > ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO){
            List<Issue> dataList = BeanUtils.copyList(issueList, Issue.class);
            dataModel.dataList = dataList;
        }
        if (rowCount != null){
            dataModel.rowCount = rowCount;
        }
        dataModel.setStime(stime);
        dataModel.setSign(sign);
        return JSON.toJSONString(dataModel);
    }


    /**
     * @Description: check校验数据下发数据-详情
     * @param requestModel
     * @return
     * @author yoko
     * @date 2020/05/14 15:57
     */
    public static void checkIssueGetData(RequestIssue requestModel) throws Exception{
        // 1.校验所有数据
        if (requestModel == null ){
            throw new ServiceException(ErrorCode.ENUM_ERROR.I00002.geteCode(), ErrorCode.ENUM_ERROR.I00002.geteDesc());
        }
    }


    /**
     * @Description: 下发集合的数据组装返回客户端的方法
     * @param stime - 服务器的时间
     * @param sign - 签名
     * @param issueModel - 下发详情
     * @return java.lang.String
     * @author yoko
     * @date 2019/11/25 22:45
     */
    public static String assembleIssueResult(long stime, String sign, IssueModel issueModel){
        ResponseIssue dataModel = new ResponseIssue();
        if (issueModel != null){
            Issue data = BeanUtils.copy(issueModel, Issue.class);
            dataModel.dataModel = data;
        }
        dataModel.setStime(stime);
        dataModel.setSign(sign);
        return JSON.toJSONString(dataModel);
    }


    /**
     * @Description: check校验数据下发数据-新增
     * @param requestModel
     * @return
     * @author yoko
     * @date 2020/05/14 15:57
     */
    public static void checkIssueAdd(RequestIssue requestModel) throws Exception{
        // 1.校验所有数据
        if (requestModel == null ){
            throw new ServiceException(ErrorCode.ENUM_ERROR.I00003.geteCode(), ErrorCode.ENUM_ERROR.I00003.geteDesc());
        }

        // 校验支付平台的订单号
        if (StringUtils.isBlank(requestModel.outTradeNo)){
            throw new ServiceException(ErrorCode.ENUM_ERROR.I00003.geteCode(), ErrorCode.ENUM_ERROR.I00003.geteDesc());
        }

        // 校验支付平台的订单金额
        if (StringUtils.isBlank(requestModel.orderMoney)){
            throw new ServiceException(ErrorCode.ENUM_ERROR.I00004.geteCode(), ErrorCode.ENUM_ERROR.I00004.geteDesc());
        }else {
            // 金额是否有效
            if (requestModel.orderMoney.indexOf(".") > -1){
                boolean flag = StringUtil.isNumberByMoney(requestModel.orderMoney);
                if (!flag){
                    throw new ServiceException(ErrorCode.ENUM_ERROR.I00005.geteCode(), ErrorCode.ENUM_ERROR.I00005.geteDesc());
                }
            }else {
                boolean flag = StringUtil.isNumer(requestModel.orderMoney);
                if (!flag){
                    throw new ServiceException(ErrorCode.ENUM_ERROR.I00006.geteCode(), ErrorCode.ENUM_ERROR.I00006.geteDesc());
                }
            }
        }

        // 校验银行名称
        if (StringUtils.isBlank(requestModel.bankName)){
            throw new ServiceException(ErrorCode.ENUM_ERROR.I00007.geteCode(), ErrorCode.ENUM_ERROR.I00007.geteDesc());
        }

        // 校验银行卡卡号
        if (StringUtils.isBlank(requestModel.bankCard)){
            throw new ServiceException(ErrorCode.ENUM_ERROR.I00008.geteCode(), ErrorCode.ENUM_ERROR.I00008.geteDesc());
        }

        // 校验银行卡开户人
        if (StringUtils.isBlank(requestModel.accountName)){
            throw new ServiceException(ErrorCode.ENUM_ERROR.I00009.geteCode(), ErrorCode.ENUM_ERROR.I00009.geteDesc());
        }
    }


    /**
     * @Description: 组装查询下发的查询条件
     * @param id - 主键ID
     * @param orderNo - 订单号
     * @param outTradeNo - 支付平台订单号：下游上报的订单号
     * @param orderStatus - 订单状态：1初始化，2超时/失败/审核驳回，3成功
     * @param ascriptionType - 订单分配归属类型：1归属卡商，2归属平台
     * @param isDistribution - 是否已分配完毕归属：1初始化/未分配，2已分配
     * @param isComplete - 是否已归集完毕：1初始化/未归集完毕，2已归集完毕；此状态：是归属类型属于平台方，平台方需要向卡商发布充值订单，发布完毕，如果卡商都已经充值完毕到我方卡，则修改此状态，修改成归集完毕的状态
     * @param checkStatus - 审核状态：1初始化，2审核收款失败，3审核收款成功
     * @param whereCheckStatus - SQL查询条件 审核状态：1初始化，2审核收款失败，3审核收款成功
     * @return IssueModel
     * @author yoko
     * @date 2020/9/23 15:03
     */
    public static IssueModel assembleIssueQuery(long id, String orderNo, String outTradeNo, int orderStatus, int ascriptionType, int isDistribution, int isComplete, int checkStatus, int whereCheckStatus){
        IssueModel resBean = new IssueModel();
        if (id > 0){
            resBean.setId(id);
        }
        if (!StringUtils.isBlank(orderNo)){
            resBean.setOrderNo(orderNo);
        }
        if (!StringUtils.isBlank(outTradeNo)){
            resBean.setOutTradeNo(outTradeNo);
        }
        if (orderStatus > 0){
            resBean.setOrderStatus(orderStatus);
        }
        if (ascriptionType > 0){
            resBean.setAscriptionType(ascriptionType);
        }
        if (isDistribution > 0){
            resBean.setIsDistribution(isDistribution);
        }
        if (isComplete > 0){
            resBean.setIsComplete(isComplete);
        }
        if (checkStatus > 0){
            resBean.setCheckStatus(checkStatus);
        }
        if (whereCheckStatus > 0){
            resBean.setWhereCheckStatus(whereCheckStatus);
        }
        return resBean;
    }


    /**
     * @Description: check下发新增：数据是否有重复录入
     * @param issueModel
     * @return
     * @author yoko
     * @date 2020/9/23 15:10
    */
    public static void checkIssueIsNotNull(IssueModel issueModel) throws Exception{
        if (issueModel != null){
            throw new ServiceException(ErrorCode.ENUM_ERROR.I00010.geteCode(), ErrorCode.ENUM_ERROR.I00010.geteDesc());
        }
    }

    /**
     * @Description: 组装生成要添加的下发数据
     * @param requestModel - 支付平台请求的数据
     * @param orderNo - 订单号
     * @return IssueModel
     * @author yoko
     * @date 2020/9/23 15:13
     */
    public static IssueModel assembleIssueAdd(RequestIssue requestModel, String orderNo){
        IssueModel resBean = BeanUtils.copy(requestModel, IssueModel.class);
        resBean.setOrderNo(orderNo);
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }


    /**
     * @Description: check校验数据下发数据-更新审核状态
     * @param requestModel
     * @return
     * @author yoko
     * @date 2020/05/14 15:57
     */
    public static void checkIssueUpdateCheck(RequestIssue requestModel) throws Exception{
        // 1.校验所有数据
        if (requestModel == null ){
            throw new ServiceException(ErrorCode.ENUM_ERROR.I00011.geteCode(), ErrorCode.ENUM_ERROR.I00011.geteDesc());
        }

        // 校验支付平台的订单号
        if (StringUtils.isBlank(requestModel.outTradeNo)){
            throw new ServiceException(ErrorCode.ENUM_ERROR.I00012.geteCode(), ErrorCode.ENUM_ERROR.I00012.geteDesc());
        }

        // 校验审核状态
        if (requestModel.checkStatus == null || requestModel.getCheckStatus() == 0){
            throw new ServiceException(ErrorCode.ENUM_ERROR.I00013.geteCode(), ErrorCode.ENUM_ERROR.I00013.geteDesc());
        }


    }

    /**
     * @Description: check下发审核状态更新：数据是否存在
     * @param issueModel
     * @return
     * @author yoko
     * @date 2020/9/23 15:10
     */
    public static void checkIssueIsNull(IssueModel issueModel) throws Exception{
        if (issueModel == null || issueModel.getId() == null || issueModel.getId() <= 0){
            throw new ServiceException(ErrorCode.ENUM_ERROR.I00014.geteCode(), ErrorCode.ENUM_ERROR.I00014.geteDesc());
        }
    }

    /**
     * @Description: check下发数据中，此订单的状态是否是成功状态
     * <p>
     *     成功状态来由：由卡商在充值订单中更新了充值成功的状态
     * </p>
     * @param orderStatus
     * @return
     * @author yoko
     * @date 2020/9/23 15:46
    */
    public static void checkIssueOrderStatus(int orderStatus) throws Exception{
        if (orderStatus != 3){
            throw new ServiceException(ErrorCode.ENUM_ERROR.I00015.geteCode(), ErrorCode.ENUM_ERROR.I00015.geteDesc());
        }
    }

    /**
     * @Description: check下发数据中，此订单的状态是否是成功状态
     * <p>
     *     成功状态来由：由卡商在充值订单中更新了充值成功的状态
     * </p>
     * @param checkStatus
     * @return
     * @author yoko
     * @date 2020/9/23 15:46
     */
    public static void checkIssueCheckStatus(int checkStatus) throws Exception{
        if (checkStatus == 3){
            throw new ServiceException(ErrorCode.ENUM_ERROR.I00016.geteCode(), ErrorCode.ENUM_ERROR.I00016.geteDesc());
        }
    }

    /**
     * @Description: check下发数据中，此订单的分配状态是否是已分配完毕状态
     * <p>
     *     只有分配完毕，才能执行后续操作的
     * </p>
     * @param isDistribution
     * @return
     * @author yoko
     * @date 2020/9/23 15:46
     */
    public static void checkIssueIsDistribution(int isDistribution) throws Exception{
        if (isDistribution == 1){
            throw new ServiceException(ErrorCode.ENUM_ERROR.I00017.geteCode(), ErrorCode.ENUM_ERROR.I00017.geteDesc());
        }
    }


    /**
     * @Description: 组装查询下发的更新方法
     * @param id - 主键ID
     * @param orderNo - 订单号
     * @param outTradeNo - 支付平台订单号：下游上报的订单号
     * @param orderStatus - 订单状态：1初始化，2超时/失败/审核驳回，3成功
     * @param pictureAds - 转账成功图片凭证
     * @param myBankInfo - 我方银行卡信息备注：假如归属类型：我方/平台，填写我方银行卡的信息
     * @param ascriptionType - 订单分配归属类型：1归属卡商，2归属平台
     * @param isDistribution - 是否已分配完毕归属：1初始化/未分配，2已分配
     * @param isComplete - 是否已归集完毕：1初始化/未归集完毕，2已归集完毕；此状态：是归属类型属于平台方，平台方需要向卡商发布充值订单，发布完毕，如果卡商都已经充值完毕到我方卡，则修改此状态，修改成归集完毕的状态
     * @param checkStatus - 审核状态：1初始化，2审核收款失败，3审核收款成功
     * @param checkInfo -  审核失败缘由，审核失败的原因
     * @param dataExplain - 数据说明：做解说用的
     * @param whereCheckStatus - SQL查询条件 审核状态：1初始化，2审核收款失败，3审核收款成功
     * @return IssueModel
     * @author yoko
     * @date 2020/9/23 15:03
     */
    public static IssueModel assembleIssueUpdate(long id, String orderNo, String outTradeNo, int orderStatus, String pictureAds,
                                                 String myBankInfo, int ascriptionType,int isDistribution, int isComplete, int checkStatus, String checkInfo,
                                                 String dataExplain, int whereCheckStatus){
        IssueModel resBean = new IssueModel();
        if (id > 0){
            resBean.setId(id);
        }
        if (!StringUtils.isBlank(orderNo)){
            resBean.setOrderNo(orderNo);
        }
        if (!StringUtils.isBlank(outTradeNo)){
            resBean.setOutTradeNo(outTradeNo);
        }
        if (orderStatus > 0){
            resBean.setOrderStatus(orderStatus);
        }
        if (!StringUtils.isBlank(pictureAds)){
            resBean.setPictureAds(pictureAds);
        }
        if (!StringUtils.isBlank(myBankInfo)){
            resBean.setMyBankInfo(myBankInfo);
        }
        if (ascriptionType > 0){
            resBean.setAscriptionType(ascriptionType);
        }
        if (isDistribution > 0){
            resBean.setIsDistribution(isDistribution);
        }
        if (isComplete > 0){
            resBean.setIsComplete(isComplete);
        }
        if (checkStatus > 0){
            resBean.setCheckStatus(checkStatus);
        }
        if (!StringUtils.isBlank(checkInfo)){
            resBean.setCheckInfo(checkInfo);
        }
        if (!StringUtils.isBlank(dataExplain)){
            resBean.setDataExplain(dataExplain);
        }
        if (whereCheckStatus > 0){
            resBean.setWhereCheckStatus(whereCheckStatus);
        }
        return resBean;
    }



    /**
     * @Description: 组装查询卡商充值的信息
     * @param id - 主键ID
     * @param merchantId - 归属的账号ID：对应表tb_fr_merchant的主键ID，并且角色类型是卡商
     * @param orderNo - 订单号
     * @param orderType - 订单类型：1预付款订单，2平台发起订单，3下发订单
     * @param issueOrderNo - 下发表的订单号：对应表tb_fr_issue的order_no；也可以把它称之为关联订单号
     * @param orderStatus - 订单状态：1初始化，2超时/失败/审核驳回，3成功
     * @param operateStatus - 操作状态：1初始化，2系统放弃，3手动放弃，4锁定
     * @param isSynchro - 是否需要数据同步：1不需要同步，2需要同步
     * @param checkStatus - 审核状态：1初始化，2审核收款失败，3审核收款成功
     * @param checkInfo - 审核失败缘由，审核失败的原因
    * @param invalidTime
     * @return MerchantRechargeModel
     * @author yoko
     * @date 2020/9/23 17:16
     */
    public static MerchantRechargeModel assembleMerchantRechargeQuery(long id, long merchantId, String orderNo, int orderType, String issueOrderNo,
                                                                      int orderStatus, int operateStatus,
                                                                       int isSynchro, int checkStatus, String checkInfo, String invalidTime){
        MerchantRechargeModel resBean = new MerchantRechargeModel();
        if (id > 0){
            resBean.setId(id);
        }
        if (merchantId > 0){
            resBean.setMerchantId(merchantId);
        }
        if (!StringUtils.isBlank(orderNo)){
            resBean.setOrderNo(orderNo);
        }
        if (orderType > 0){
            resBean.setOrderType(orderType);
        }
        if (!StringUtils.isBlank(issueOrderNo)){
            resBean.setIssueOrderNo(issueOrderNo);
        }
        if (orderStatus > 0){
            resBean.setOrderStatus(orderStatus);
        }
        if (operateStatus > 0){
            resBean.setOperateStatus(operateStatus);
        }
        if (isSynchro > 0){
            resBean.setIsSynchro(isSynchro);
        }
        if (checkStatus > 0){
            resBean.setCheckStatus(checkStatus);
        }
        if (!StringUtils.isBlank(checkInfo)){
            resBean.setCheckInfo(checkInfo);
        }
        if (!StringUtils.isBlank(invalidTime)){
            resBean.setInvalidTime(invalidTime);
        }
        return resBean;
    }



    /**
     * @Description: 组装更新卡商充值的信息
     * @param id - 主键ID
     * @param issueOrderNo - 下发表的订单号：对应表tb_fr_issue的order_no；也可以把它称之为关联订单号
     * @param orderStatus - 订单状态：1初始化，2超时/失败/审核驳回，3成功
     * @param pictureAds - 充值记录银行卡转账图片凭证
     * @param operateStatus - 操作状态：1初始化，2系统放弃，3手动放弃，4锁定
     * @param isSynchro - 是否需要数据同步：1不需要同步，2需要同步
     * @param checkStatus - 审核状态：1初始化，2审核收款失败，3审核收款成功
     * @param checkInfo - 审核失败缘由，审核失败的原因
    * @param invalidTime
     * @return MerchantRechargeModel
     * @author yoko
     * @date 2020/9/23 17:16
     */
    public static MerchantRechargeModel assembleMerchantRechargeUpdate(long id, String issueOrderNo, int orderStatus, String pictureAds, int operateStatus,
                                                                       int isSynchro, int checkStatus, String checkInfo, String invalidTime){
        MerchantRechargeModel resBean = new MerchantRechargeModel();
        if (id > 0){
            resBean.setId(id);
        }
        if (!StringUtils.isBlank(issueOrderNo)){
            resBean.setIssueOrderNo(issueOrderNo);
        }
        if (orderStatus > 0){
            resBean.setOrderStatus(orderStatus);
        }
        if (!StringUtils.isBlank(pictureAds)){
            resBean.setPictureAds(pictureAds);
        }
        if (operateStatus > 0){
            resBean.setOperateStatus(operateStatus);
        }
        if (isSynchro > 0){
            resBean.setIsSynchro(isSynchro);
        }
        if (checkStatus > 0){
            resBean.setCheckStatus(checkStatus);
        }
        if (!StringUtils.isBlank(checkInfo)){
            resBean.setCheckInfo(checkInfo);
        }
        if (!StringUtils.isBlank(invalidTime)){
            resBean.setInvalidTime(invalidTime);
        }
        return resBean;
    }


    /**
     * @Description: 获取上一次使用过的银行卡ID
     * <p>
     *     从缓存中获取上次给出码的银行卡ID
     *     数据来由：每次给出之后，把银行卡ID进行纪录
     * </p>
     * @param bankBindingType - 银行卡绑定类型
     * @param channelId - 渠道的主键ID
     * @return
     * @author yoko
     * @date 2020/5/21 15:38
     */
    public static long getMaxBankByRedis(int bankBindingType, long channelId) throws Exception{
        String strKeyCache = CachedKeyUtils.getCacheKey(CacheKey.BANK_BINDING_TYPE, bankBindingType, channelId);
        String strCache = (String) ComponentUtil.redisService.get(strKeyCache);
        if (!StringUtils.isBlank(strCache)) {
            return Long.parseLong(strCache);
        }else{
            return 0;
        }
    }


    /**
     * @Description: 银行卡集合排序
     * <p>
     *     排序方式：小于上次给过的银行卡ID放在集合的后面，大于上次给过的银行卡ID放集合的前面
     * </p>
     * @param bankList - 银行卡集合
     * @param maxBankId - 上次给出的银行卡ID
     * @return java.util.List<BankModel>
     * @author yoko
     * @date 2020/10/10 11:49
     */
    public static List<BankModel> sortBankList(List<BankModel> bankList, long maxBankId){
        if (maxBankId > 0){
            List<BankModel> resList = new ArrayList<>();
            List<BankModel> noList = new ArrayList<>();// 没有给出过出码的银行集合
            List<BankModel> yesList = new ArrayList<>();// 有给出过出码的银行集合
            for (BankModel bankModel : bankList){
                if (bankModel.getId() > maxBankId){
                    noList.add(bankModel);
                }else {
                    yesList.add(bankModel);
                }
            }
            if (noList != null && noList.size() > 0){
                resList.addAll(noList);
            }
            if (yesList != null && yesList.size() > 0){
                resList.addAll(yesList);
            }
            return resList;
        }else {
            return bankList;
        }
    }

    /**
     * @Description: 获取是减金额还是加金额
     * @param bankMoneyOut - 动态：1为减，2加，3随机加减，4为整数
     * @return 
     * @author yoko
     * @date 2020/10/10 14:50 
    */
    public static int getAddAndSubtract(int bankMoneyOut){
        int num = 0;
        if (bankMoneyOut == 1){
            num = 1;
        }else if (bankMoneyOut == 2){
            num = 2;
        }else if (bankMoneyOut == 3){
            int random = new Random().nextInt(2);//生成随机数0跟1，0表示减，1表示加
            if (random == 0){
                num = 1;
            }else{
                num = 2;
            }
        }else if(bankMoneyOut == 5){
            num = 3;// 随机减
        }
        return num;
    }

    /**
     * @Description: redis：添加给出的银行卡ID
     * @param bankBindingType - 渠道与银行卡绑定类型
     * @param channelId - 渠道ID
     * @param bankId - 银行卡ID
     * @return void
     * @author yoko
     * @date 2020/10/10 15:44
     */
    public static void saveMaxBankByRedis(int bankBindingType, long channelId, long bankId){
        if (bankBindingType == 3){
            channelId = 0;
        }
        String strKeyCache = CachedKeyUtils.getCacheKey(CacheKey.BANK_BINDING_TYPE, bankBindingType, channelId);
        ComponentUtil.redisService.set(strKeyCache, String.valueOf(bankId));
    }



    /**
     * @Description: check校验数据当派发订单的时候-代付订单
     * @param requestModel
     * @return
     * @author yoko
     * @date 2020/05/14 15:57
     */
    public static void checkOrderOutAdd(ProtocolOrderOut requestModel) throws Exception{
        // 1.校验所有数据
        if (requestModel == null ){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OU00001.geteCode(), ErrorCode.ENUM_ERROR.OU00001.geteDesc());
        }

        // 校验金额是否为空
        if (StringUtils.isBlank(requestModel.money)){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OU00002.geteCode(), ErrorCode.ENUM_ERROR.OU00002.geteDesc());
        }else {
            // 金额是否有效
            if (requestModel.money.indexOf(".") > -1){
                boolean flag = StringUtil.isNumberByMoney(requestModel.money);
                if (!flag){
                    throw new ServiceException(ErrorCode.ENUM_ERROR.OU00003.geteCode(), ErrorCode.ENUM_ERROR.OU00003.geteDesc());
                }
            }else {
                boolean flag = StringUtil.isNumer(requestModel.money);
                if (!flag){
                    throw new ServiceException(ErrorCode.ENUM_ERROR.OU00004.geteCode(), ErrorCode.ENUM_ERROR.OU00004.geteDesc());
                }
            }
        }

        if (StringUtils.isBlank(requestModel.secretKey)){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OU00005.geteCode(), ErrorCode.ENUM_ERROR.OU00005.geteDesc());
        }
    }


    /**
     * @Description: 校验策略类型数据:出码开关-判断此时是否属于正常出码-代付订单
     * @return void
     * @author yoko
     * @date 2019/12/2 14:35
     */
    public static void checkStrategyByOutQrCodeSwitch(StrategyModel strategyModel) throws Exception{
        if (strategyModel == null){
            throw new ServiceException(ErrorCode.ENUM_ERROR.S00006.geteCode(), ErrorCode.ENUM_ERROR.S00006.geteDesc());
        }
        if (strategyModel.getStgNumValue() == 1){
            throw new ServiceException(ErrorCode.ENUM_ERROR.S00007.geteCode(), ErrorCode.ENUM_ERROR.S00007.geteDesc());
        }
        if (strategyModel.getStgNumValue() == 2){
            if (StringUtils.isBlank(strategyModel.getStgValue())){
                throw new ServiceException(ErrorCode.ENUM_ERROR.S00008.geteCode(), ErrorCode.ENUM_ERROR.S00008.geteDesc());
            }else{
                String[] str = strategyModel.getStgValue().split("-");
                boolean flag = DateUtil.isBelong(str[0], str[1]);
                if (!flag){
                    throw new ServiceException(ErrorCode.ENUM_ERROR.S00009.geteCode(), ErrorCode.ENUM_ERROR.S00009.geteDesc());
                }
            }
        }
    }


    /**
     * @Description: check订单是否在策略规定的范围内-代付金额
     * @param orderMoneyRange - 策略规定的订单金额范围
     * @param orderMoney - 订单金额
     * @return java.lang.String
     * @author yoko
     * @date 2020/6/6 11:48
     */
    public static void checkOutOrderMoney(String orderMoneyRange, String orderMoney) throws Exception{
        String [] rule = orderMoneyRange.split("-");
        double start = Double.parseDouble(rule[0]);
        double end = Double.parseDouble(rule[1]);
        double money = Double.parseDouble(orderMoney);
        if (money < start || money > end){
            throw new ServiceException(ErrorCode.ENUM_ERROR.S00010.geteCode(), ErrorCode.ENUM_ERROR.S00010.geteDesc());
        }
    }


    /**
     * @Description: check商户数据是否为空-代付订单
     * @param channelModel
     * @return
     * @author yoko
     * @date 2020/05/14 15:57
     */
    public static void checkOutOrderChannelIsNull(ChannelModel channelModel) throws Exception{
        if (channelModel == null || channelModel.getId() == null || channelModel.getId() <= 0){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OU00006.geteCode(), ErrorCode.ENUM_ERROR.OU00006.geteDesc());
        }
    }


    /**
     * @Description: 组装查询卡商与渠道关联关系
     * @param id - 主键ID
     * @param merchantId - 卡商ID
     * @param merchantSiteId - 卡站点ID
     * @param channelId - 渠道ID
     * @param useStatus - 使用状态
     * @return com.hz.cake.master.core.model.merchant.MerchantChannelModel
     * @author yoko
     * @date 2020/10/30 14:04
     */
    public static MerchantChannelModel assembleMerchantChannelQuery(long id, long merchantId, long merchantSiteId, long channelId, int useStatus){
        MerchantChannelModel resBean = new MerchantChannelModel();
        if (id > 0){
            resBean.setId(id);
        }
        if (merchantId > 0){
            resBean.setMerchantId(merchantId);
        }
        if (merchantSiteId > 0){
            resBean.setMerchantSiteId(merchantSiteId);
        }
        if (channelId > 0){
            resBean.setChannelId(channelId);
        }
        if (useStatus > 0){
            resBean.setUseStatus(useStatus);
        }
        return resBean;
    }
    
    
    /**
     * @Description: check校验卡商与渠道的关联关系是否为空
     * @param merchantChannelList
     * @return 
     * @author yoko
     * @date 2020/10/30 14:12
    */
    public static void checkMerchantChannelIsNull(List<MerchantChannelModel> merchantChannelList) throws Exception{
        if (merchantChannelList == null || merchantChannelList.size() <= 0){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OU00007.geteCode(), ErrorCode.ENUM_ERROR.OU00007.geteDesc());
        }
    }

    /**
     * @Description: 组装查询卡商信息的方法
     * @param id - 主键ID
     * @param merchantType - 卡商类型：1我方卡商，2第三方卡商
     * @param operateType - 卡商运营类型/运营性质：1代收，2代付
     * @param idList - 卡商主键ID集合
     * @param orderMoney - 订单金额
     * @param useStatus - 使用状态:1初始化有效正常使用，2无效暂停使用
     * @return com.hz.cake.master.core.model.merchant.MerchantModel
     * @author yoko
     * @date 2020/10/30 14:40
     */
    public static MerchantModel assembleMerchantQuery(long id, int merchantType, int operateType, List<Long> idList, String orderMoney, int useStatus){
        MerchantModel resBean = new MerchantModel();
        if (!StringUtils.isBlank(orderMoney)){
            BigDecimal bd = new BigDecimal(orderMoney);
            resBean.setMoney(bd);
        }
        if (id > 0){
            resBean.setId(id);
        }
        if (merchantType > 0){
            resBean.setMerchantType(merchantType);
        }
        if (operateType > 0){
            resBean.setOperateType(operateType);
        }
        if (idList != null && idList.size() > 0){
            resBean.setIdList(idList);
        }
        if (useStatus > 0){
            resBean.setUseStatus(useStatus);
        }
        return resBean;
    }

    /**
     * @Description: check校验卡商集合是否为空
     * @param merchantList
     * @return
     * @author yoko
     * @date 2020/10/30 14:12
     */
    public static void checkMerchantIsNullByOutOrder(List<MerchantModel> merchantList) throws Exception{
        if (merchantList == null || merchantList.size() <= 0){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OU00008.geteCode(), ErrorCode.ENUM_ERROR.OU00008.geteDesc());
        }
    }


    /**
     * @Description: 获取上一次使用过的卡商ID
     * <p>
     *     从缓存中获取上次给出码的卡商ID
     *     数据来由：每次给出之后，把卡商ID进行纪录
     * </p>
     * @param bankBindingType - 卡商绑定类型
     * @param channelId - 渠道的主键ID
     * @return
     * @author yoko
     * @date 2020/5/21 15:38
     */
    public static long getMaxMerchantByRedis(int bankBindingType, long channelId) throws Exception{
        String strKeyCache = CachedKeyUtils.getCacheKey(CacheKey.MERCHANT_BINDING_TYPE, bankBindingType, channelId);
        String strCache = (String) ComponentUtil.redisService.get(strKeyCache);
        if (!StringUtils.isBlank(strCache)) {
            return Long.parseLong(strCache);
        }else{
            return 0;
        }
    }



    /**
     * @Description: 卡商集合排序
     * <p>
     *     排序方式：小于上次给过的卡商ID放在集合的后面，大于上次给过的卡商ID放集合的前面
     * </p>
     * @param merchantList - 卡商集合
     * @param maxMerchantId - 上次给出的卡商ID
     * @return java.util.List<BankModel>
     * @author yoko
     * @date 2020/10/10 11:49
     */
    public static List<MerchantModel> sortMerchantList(List<MerchantModel> merchantList, long maxMerchantId){
        if (maxMerchantId > 0){
            List<MerchantModel> resList = new ArrayList<>();
            List<MerchantModel> noList = new ArrayList<>();// 没有给出过出码的银行集合
            List<MerchantModel> yesList = new ArrayList<>();// 有给出过出码的银行集合
            for (MerchantModel merchantModel : merchantList){
                if (merchantModel.getId() > maxMerchantId){
                    noList.add(merchantModel);
                }else {
                    yesList.add(merchantModel);
                }
            }
            if (noList != null && noList.size() > 0){
                resList.addAll(noList);
            }
            if (yesList != null && yesList.size() > 0){
                resList.addAll(yesList);
            }
            return resList;
        }else {
            return merchantList;
        }
    }

    /**
     * @Description: 组装变更卡商余额的方法
     * @param id - 卡商主键ID
     * @param type - 余额加减：1加，2减
     * @param money - 变更的金额值
     * @return com.hz.cake.master.core.model.merchant.MerchantModel
     * @author yoko
     * @date 2020/10/30 19:57
     */
    public static MerchantModel assembleMerchantByChanagerBalance(long id, int type, String money){
        MerchantModel resBean = new MerchantModel();
        if (id > 0){
            resBean.setId(id);
        }
        if (type > 0){
            if (type == 1){
                resBean.setAddBalance("1");
            }else if (type == 2){
                resBean.setSubtractBalance("1");
            }
        }
        if (!StringUtils.isBlank(money)){
            resBean.setOrderMoney(money);
        }
        return resBean;
    }

    /**
     * @Description: 组装添加流水或查询流水的方法
     * @param id - 主键ID
     * @param merchantId - 卡商ID
     * @param orderNo - 订单号
     * @param orderType - 订单类型：1代收，2代付
     * @param money - 订单金额
     * @param orderStatus - 订单状态：1初始化，2超时/失败，3有质疑，4成功，5表示订单超时且操作状态属于初始化的
     * @param delayTime - 延迟运行时间：当订单属于超时状态：则系统时间需要大于此时间才能进行逻辑操作
     * @param lockTime - 锁定时间
     * @param type - 操作类型：1查询，2添加数据
     * @return com.hz.cake.master.core.model.merchant.MerchantBalanceDeductModel
     * @author yoko
     * @date 2020/10/30 20:21
     */
    public static MerchantBalanceDeductModel assembleMerchantBalanceDeduct(long id, long merchantId, String orderNo, int orderType, String money, int orderStatus,
                                                                           String delayTime, String lockTime, int type){
        MerchantBalanceDeductModel resBean = new MerchantBalanceDeductModel();
        if (id > 0){
            resBean.setId(id);
        }
        if (merchantId > 0){
            resBean.setMerchantId(merchantId);
        }
        if (!StringUtils.isBlank(orderNo)){
            resBean.setOrderNo(orderNo);
        }
        if (orderType > 0){
            resBean.setOrderType(orderType);
        }
        if (!StringUtils.isBlank(money)){
            resBean.setMoney(money);
        }
        if (!StringUtils.isBlank(delayTime)){
            resBean.setDelayTime(delayTime);
        }
        if (!StringUtils.isBlank(lockTime)){
            resBean.setLockTime(lockTime);
        }
        if (type > 0){
            if (type == 2){
                resBean.setCurday(DateUtil.getDayNumber(new Date()));
                resBean.setCurhour(DateUtil.getHour(new Date()));
                resBean.setCurminute(DateUtil.getCurminute(new Date()));
            }
        }
        return resBean;
    }

    /**
     * @Description: check筛选的卡商是否为空
     * @param merchantModel
     * @return
     * @author yoko
     * @date 2020/9/13 14:41
     */
    public static void checkScreenMerchantsNull(MerchantModel merchantModel) throws Exception{
        if (merchantModel == null || merchantModel.getId() == null || merchantModel.getId() <= 0){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OU00009.geteCode(), ErrorCode.ENUM_ERROR.OU00009.geteDesc());
        }
    }



    /**
     * @Description: 组装添加代付订单的方法
     * @param merchantModel - 筛选出的卡商
     * @param requestModel - 请求数据
     * @param channelModel - 商户信息
     * @param orderNo - 订单号
     * @param serviceCharge - 卡商手续费
     * @return OrderModel
     * @author yoko
     * @date 2020/9/13 14:41
     */
    public static OrderOutModel assembleOrderOutAdd(MerchantModel merchantModel, ProtocolOrderOut requestModel, ChannelModel channelModel, String orderNo, String serviceCharge){
        OrderOutModel resBean = new OrderOutModel();
        resBean.setOrderNo(orderNo);
        resBean.setOrderMoney(requestModel.money);
        resBean.setOutTradeNo(requestModel.outTradeNo);
        resBean.setOrderType(merchantModel.getPayType());
        if (!StringUtils.isBlank(serviceCharge)){
            resBean.setServiceCharge(serviceCharge);
        }
        resBean.setInBankCard(requestModel.inBankCard);
        resBean.setInBankName(requestModel.inBankName);
        resBean.setInAccountName(requestModel.inAccountName);
        if (!StringUtils.isBlank(requestModel.inBankSubbranch)){
            resBean.setInBankSubbranch(requestModel.inBankSubbranch);
        }
        if (!StringUtils.isBlank(requestModel.inBankProvince)){
            resBean.setInBankProvince(requestModel.inBankProvince);
        }
        if (!StringUtils.isBlank(requestModel.inBankCity)){
            resBean.setInBankCity(requestModel.inBankCity);
        }

        resBean.setMerchantId(merchantModel.getId());
        if (!StringUtils.isBlank(merchantModel.getAcName())){
            resBean.setMerchantName(merchantModel.getAcName());
        }
        resBean.setChannelId(channelModel.getId());
        if (!StringUtils.isBlank(channelModel.getAlias())){
            resBean.setChannelName(channelModel.getAlias());
        }
        if (!StringUtils.isBlank(requestModel.notifyUrl)){
            resBean.setNotifyUrl(requestModel.notifyUrl);
        }

        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }

    /**
     * @Description: check校验添加代付订单信息是否正常
     * @param num
     * @return
     * @author yoko
     * @date 2020/9/13 14:49
     */
    public static void checkAddOrderOutIsOk(int num) throws Exception{
        if (num <= 0){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OU00010.geteCode(), ErrorCode.ENUM_ERROR.OU00010.geteDesc());
        }
    }


    /**
     * @Description: redis：添加给出的银行卡ID
     * @param bankBindingType - 卡商与渠道绑定类型
     * @param channelId - 渠道ID
     * @param merchantId - 卡商ID
     * @return void
     * @author yoko
     * @date 2020/10/10 15:44
     */
    public static void saveMaxMerchantByRedis(int bankBindingType, long channelId, long merchantId){
        if (bankBindingType == 1){
            channelId = 0;
        }
        String strKeyCache = CachedKeyUtils.getCacheKey(CacheKey.MERCHANT_BINDING_TYPE, bankBindingType, channelId);
        ComponentUtil.redisService.set(strKeyCache, String.valueOf(merchantId));
    }


    /**
     * @Description: 用户代付订单派单成功的数据组装返回客户端的方法
     * @param stime - 服务器的时间
     * @param sign - 签名
     * @param orderOutModel - 代付订单的详情
     * @return java.lang.String
     * @author yoko
     * @date 2019/11/25 22:45
     */
    public static String assembleOrderOutDataResult(long stime, String sign, OrderOutModel orderOutModel, String returnUrl) throws Exception{
        ResponseOrderOut dataModel = new ResponseOrderOut();
        if (orderOutModel != null){
            OrderOut orderOut = new OrderOut();
            orderOut.orderNo = orderOutModel.getOrderNo();
            orderOut.orderMoney = orderOutModel.getOrderMoney();
            orderOut.orderStatus = 1;
            dataModel.orderOut = orderOut;
        }
        dataModel.setStime(stime);
        dataModel.setSign(sign);
        return JSON.toJSONString(dataModel);
    }


    /**
     * @Description: 组装查询卡商绑定渠道的手续费的方法
     * @param id - 主键ID
     * @param merchantId - 卡商ID
     * @param channelId - 渠道ID
     * @param useStatus - 使用状态:1初始化有效正常使用，2无效暂停使用
     * @return com.hz.cake.master.core.model.merchant.MerchantServiceChargeModel
     * @author yoko
     * @date 2020/11/26 15:04
     */
    public static MerchantServiceChargeModel assembleMerchantServiceChargeQuery(long id, long merchantId, long channelId, int useStatus){
        MerchantServiceChargeModel resBean = new MerchantServiceChargeModel();
        if (id > 0){
            resBean.setId(id);
        }
        if (merchantId > 0){
            resBean.setMerchantId(merchantId);
        }
        if (channelId > 0){
            resBean.setChannelId(channelId);
        }
        if (useStatus > 0){
            resBean.setUseStatus(useStatus);
        }
        return resBean;
    }


    /**
     * @Description: 获取卡商的手续费
     * @param merchantServiceChargeModel
     * @return
     * @author yoko
     * @date 2020/05/14 15:57
     */
    public static String getMerchantServiceCharge(MerchantServiceChargeModel merchantServiceChargeModel){
        String str = "";
        if (merchantServiceChargeModel == null || merchantServiceChargeModel.getId() == null || merchantServiceChargeModel.getId() <= 0){
            return str;
        }else {
            if (!StringUtils.isBlank(merchantServiceChargeModel.getServiceCharge())){
                str = merchantServiceChargeModel.getServiceCharge();
            }
        }
        return str;
    }


    /**
     * @Description: 组装查询商户的银行卡池子的查询方法
     * @param id - 主键ID
     * @param channelId - 商户ID
     * @param bankId - 银行卡ID
     * @param useStatus - 使用状态
     * @return MerchantModel
     * @author yoko
     * @date 2020/9/9 17:16
     */
    public static ChannelBankPoolModel assembleChannelBankPoolQuery(long id, long channelId, long bankId, int useStatus){
        ChannelBankPoolModel resBean = new ChannelBankPoolModel();
        if (id > 0){
            resBean.setId(id);
        }
        if (channelId > 0){
            resBean.setChannelId(channelId);
        }
        if (bankId > 0){
            resBean.setBankId(bankId);
        }
        if (useStatus > 0){
            resBean.setUseStatus(useStatus);
        }
        return resBean;
    }


    /**
     * @Description: 组装查询银行卡池子的查询方法
     * @param id - 主键ID
     * @param bankId - 银行卡ID
     * @param useStatus - 使用状态
     * @return MerchantModel
     * @author yoko
     * @date 2020/9/9 17:16
     */
    public static BankPoolModel assembleBankPoolQuery(long id, long bankId, int useStatus){
        BankPoolModel resBean = new BankPoolModel();
        if (id > 0){
            resBean.setId(id);
        }
        if (bankId > 0){
            resBean.setBankId(bankId);
        }
        if (useStatus > 0){
            resBean.setUseStatus(useStatus);
        }
        return resBean;
    }


    /**
     * @Description: check校验池子中银行卡ID集合是否为空
     * @param bankIdList
     * @return
     * @author yoko
     * @date 2020/9/12 20:19
     */
    public static void checkBankPoolIsNull(List<Long> bankIdList) throws Exception{
        if (bankIdList == null || bankIdList.size() <= 0){
            throw new ServiceException(ErrorCode.ENUM_ERROR.OR00019.geteCode(), ErrorCode.ENUM_ERROR.OR00019.geteDesc());
        }
    }



    /**
     * @Description: 组装支付用户的IP页统计的数据
     * @param requestModel - 前端传的订单信息
     * @param regionModel - 用户的地域信息
     * @return
     * @author yoko
     * @date 2020/7/15 19:10
     */
    public static StatisticsIpModel assembleStatisticsIpData(RequestOrder requestModel, RegionModel regionModel){
        StatisticsIpModel resBean = new StatisticsIpModel();
        if (!StringUtils.isBlank(requestModel.orderNo)){
            resBean.setOrderNo(requestModel.orderNo);
        }else {
            return null;
        }
        if (regionModel != null){
            if (!StringUtils.isBlank(regionModel.getIp())){
                regionModel = ComponentUtil.regionService.getCacheRegion(regionModel);
                resBean.setIp(regionModel.getIp());
                log.info("");
                if (!StringUtils.isBlank(regionModel.getProvince())){
                    resBean.setProvince(regionModel.getProvince());
                }
                if (!StringUtils.isBlank(regionModel.getCity())){
                    resBean.setCity(regionModel.getCity());
                }
            }
        }
        resBean.setCurday(DateUtil.getDayNumber(new Date()));
        resBean.setCurhour(DateUtil.getHour(new Date()));
        resBean.setCurminute(DateUtil.getCurminute(new Date()));
        return resBean;
    }

    /**
     * @Description: check用户IP是否在黑名单IP中
     * @param blacklistIp - 黑名单IP
     * @param ip - 用户IP
     * @return boolean
     * @author yoko
     * @date 2020/12/18 16:27
     */
    public static boolean checkBlacklistIp(String blacklistIp, String ip){
        if (!StringUtils.isBlank(blacklistIp) && !StringUtils.isBlank(ip)){
            if (blacklistIp.indexOf(ip) > -1){
                return false;
            }else{
                return true;
            }
        }else {
            return true;
        }
    }








    public static void main(String [] args) throws Exception{
        String sb1 = "2020-08-31 10:21:39";
//        DateUtil.DateUtil.calLastedTime(orderModel.getInvalidTime());
        int sb2 = DateUtil.calLastedTime(sb1);
        System.out.println("sb2:" + sb2);

        String str = "00:01-19:12#12:01-19:00#20:01-23:59";
        boolean flag = checkOpenTimeSlot(str);
        System.out.println("flag:" + flag);
        String money ="300.01";
        if (!StringUtils.isBlank(money)){
            if (money.indexOf(".") <= -1){
                money = money + ".00";
            }
            System.out.println("money:" + money);
        }

        String sb3 = "500.00";
        String sb4 = "499.98";
//        String sb3 = "500.00";
//        String sb4 = "500.00";
        String sb5 = StringUtil.getBigDecimalSubtractByStr(sb3, sb4).replace("-", "");
        System.out.println("sb5:" + sb5);
        String ipList = "1.2.127.255,1.2.5.255,1.8.8.255";
        String ip = "1.2.5.255";
        boolean flag_ip = checkBlacklistIp(ipList, ip);
        System.out.println("flag_ip:" + flag_ip);

        StrategyData bean1 = new StrategyData();
        bean1.setId(1L);
        bean1.setStgValue("0.01");

        StrategyData bean2 = new StrategyData();
        bean2.setId(2L);
        bean2.setStgValue("0.02");

        StrategyData bean3 = new StrategyData();
        bean3.setId(3L);
        bean3.setStgValue("0.03");

        StrategyData bean4 = new StrategyData();
        bean4.setId(4L);
        bean4.setStgValue("0.04");

        StrategyData bean5 = new StrategyData();
        bean5.setId(5L);
        bean5.setStgValue("0.05");

        StrategyData bean6 = new StrategyData();
        bean6.setId(6L);
        bean6.setStgValue("0.06");

        StrategyData bean7 = new StrategyData();
        bean7.setId(7L);
        bean7.setStgValue("0.07");

        StrategyData bean8 = new StrategyData();
        bean8.setId(8L);
        bean8.setStgValue("0.08");

        StrategyData bean9 = new StrategyData();
        bean9.setId(9L);
        bean9.setStgValue("0.09");

        StrategyData bean10 = new StrategyData();
        bean10.setId(10L);
        bean10.setStgValue("0.10");

        List<StrategyData> list = new ArrayList<>();
        list.add(bean1);
        list.add(bean2);
        list.add(bean3);
        list.add(bean4);
        list.add(bean5);
        list.add(bean6);
        list.add(bean7);
        list.add(bean8);
        list.add(bean9);
        list.add(bean10);
//        for (int i = 0; i<=100; i++){
//            int random = new Random().nextInt(list.size());
//            System.out.println("ID:"+ list.get(random).getId() + ",value:" + list.get(random).getStgValue());
//        }

        int count = 0;
        while (true){
            System.out.println("count:" + count);
            if (count <= 6){
                System.out.println("count1111:" + count);
                int random = new Random().nextInt(list.size());
                System.out.println("random:" + random);
                if (random == 3){
                    break;
                }
            }else {
                System.out.println("超过次数了");
                break;
            }
            count ++;
        }



    }




    

}
