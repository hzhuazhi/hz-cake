package com.hz.cake.master.core.controller.order;

import com.alibaba.fastjson.JSON;
import com.hz.cake.master.core.common.utils.JsonResult;
import com.hz.cake.master.core.common.utils.StringUtil;
import com.hz.cake.master.core.model.bank.BankPoolModel;
import com.hz.cake.master.core.model.channel.ChannelBankPoolModel;
import com.hz.cake.master.core.model.merchant.MerchantServiceChargeModel;
import com.hz.cake.master.core.model.statistics.StatisticsIpModel;
import com.hz.cake.master.core.protocol.request.order.ProtocolOrder;
import com.hz.cake.master.core.protocol.request.order.RequestOrder;
import com.hz.cake.master.core.common.exception.ExceptionMethod;
import com.hz.cake.master.core.common.utils.SignUtil;
import com.hz.cake.master.core.common.utils.constant.ServerConstant;
import com.hz.cake.master.core.model.RequestEncryptionJson;
import com.hz.cake.master.core.model.ResponseEncryptionJson;
import com.hz.cake.master.core.model.bank.BankModel;
import com.hz.cake.master.core.model.channel.ChannelBankModel;
import com.hz.cake.master.core.model.channel.ChannelModel;
import com.hz.cake.master.core.model.order.OrderModel;
import com.hz.cake.master.core.model.region.RegionModel;
import com.hz.cake.master.core.model.shortchain.ShortChainModel;
import com.hz.cake.master.core.model.strategy.StrategyData;
import com.hz.cake.master.core.model.strategy.StrategyModel;
import com.hz.cake.master.util.ComponentUtil;
import com.hz.cake.master.util.HodgepodgeMethod;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

/**
 * @Description 任务订单（平台派发订单）的Controller层
 * @Author yoko
 * @Date 2020/5/22 10:21
 * @Version 1.0
 */
@RestController
@RequestMapping("/cake/order")
public class OrderController {

    private static Logger log = LoggerFactory.getLogger(OrderController.class);

    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    /**
     * 15分钟.
     */
    public long FIFTEEN_MIN = 900;

    /**
     * 30分钟.
     */
    public long THIRTY_MIN = 30;

    @Value("${secret.key.token}")
    private String secretKeyToken;

    @Value("${secret.key.sign}")
    private String secretKeySign;


    /**
     * @Description: 派发订单-银行卡接口
     * <p>
     *
     * </p>
     * @param request
     * @param response
     * @return com.gd.chain.common.utils.JsonResult<java.lang.Object>
     * @author yoko
     * @date 2019/11/25 22:58
     * local:http://localhost:8093/cake/order/qrCode
     * 请求的属性类:RequestOrder
     * 必填字段:{"money":"1111","payType":2,"outTradeNo":"outTradeNo1","notifyUrl":"notify_url","returnUrl":"http://www.baidu.com","secretKey":"秘钥_7"}
     * 加密字段:{"jsonData":"eyJtb25leSI6IjExMTEiLCJwYXlUeXBlIjoyLCJvdXRUcmFkZU5vIjoib3V0VHJhZGVObzEiLCJub3RpZnlVcmwiOiJub3RpZnlfdXJsIiwicmV0dXJuVXJsIjoiaHR0cDovL3d3dy5iYWlkdS5jb20iLCJzZWNyZXRLZXkiOiLnp5jpkqVfNyJ9"}
     * 客户端加密字段:ctime+cctime+秘钥=sign
     * 服务端加密字段:stime+秘钥=sign
     * result={
     *     "resultCode": "0",
     *     "message": "success",
     *     "data": {
     *         "jsonData": "eyJvcmRlciI6eyJhY2NvdW50TmFtZSI6IuW8gOaIt+S6ul83XzQiLCJiYW5rQ2FyZCI6IumTtuihjOWNoV83XzQiLCJiYW5rQ29kZSI6IueugOeggV83XzQiLCJiYW5rTmFtZSI6IumTtuihjOWQjeensF83XzQiLCJpbnZhbGlkU2Vjb25kIjoiODk2IiwiaW52YWxpZFRpbWUiOiIyMDIwLTA5LTEzIDE2OjUwOjU2Iiwib3JkZXJNb25leSI6IjExMTEuMDAiLCJvcmRlck5vIjoiMjAyMDA5MTMxNjM1NTUwMDAwMDAxIiwib3JkZXJTdGF0dXMiOjEsInFyQ29kZVVybCI6Imh0dHAlM0ElMkYlMkZ3d3cueWJ6Zm0uY29tJTNBODAwMiUyRnBheTEyMyUyRmg1cXJjb2RlLmh0bWwlM0ZvcmRlck5vJTNEMjAyMDA5MTMxNjM1NTUwMDAwMDAxJTI2cmV0dXJuVXJsJTNEaHR0cCUzQSUyRiUyRnd3dy5iYWlkdS5jb20ifSwic2lnbiI6IiIsInN0aW1lIjoxNTk5OTg2MTU2MzU2fQ=="
     *     },
     *     "sgid": "202009131635550000001",
     *     "cgid": ""
     * }
     */
    @RequestMapping(value = "/qrCode", method = {RequestMethod.POST})
    public JsonResult<Object> qrCode(HttpServletRequest request, HttpServletResponse response, @RequestBody RequestEncryptionJson requestData) throws Exception{
        String sgid = "CDS" + ComponentUtil.redisIdService.getNewId();
        String cgid = "";
        String token = "";
        String ip = StringUtil.getIpAddress(request);
        String data = "";
        RegionModel regionModel = HodgepodgeMethod.assembleRegionModel(ip);

        ProtocolOrder requestModel = new ProtocolOrder();
        try{
            // 解密
            data = StringUtil.decoderBase64(requestData.jsonData);
            requestModel  = JSON.parseObject(data, ProtocolOrder.class);


            // check校验数据
            HodgepodgeMethod.checkOrderAdd(requestModel);

            if (requestModel.money.indexOf(".") <= -1){
                requestModel.money = requestModel.money + ".00";
            }


            // 策略数据：出码开关
            StrategyModel strategyQrCodeSwitchQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.QR_CODE_SWITCH.getStgType());
            StrategyModel strategyQrCodeSwitchModel = ComponentUtil.strategyService.getStrategyModel(strategyQrCodeSwitchQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            HodgepodgeMethod.checkStrategyByQrCodeSwitch(strategyQrCodeSwitchModel);

            // 策略数据：出码金额范围
            String  inMoneyRange = "";
            StrategyModel strategyOrderMoneyRangeQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.ORDER_MONEY_RANGE.getStgType());
            StrategyModel strategyOrderMoneyRangeModel = ComponentUtil.strategyService.getStrategyModel(strategyOrderMoneyRangeQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
//            HodgepodgeMethod.checkOrderMoney(strategyOrderMoneyRangeModel.getStgValue(), requestModel.money);

            // 策略数据：出码后银行卡金额的锁定时间
            int orderMoneyLockTime = 0;
            StrategyModel strategyOrderMoneyLockTimeQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.ORDER_MONEY_LOCK_TIME.getStgType());
            StrategyModel strategyOrderMoneyLockTimeModel = ComponentUtil.strategyService.getStrategyModel(strategyOrderMoneyLockTimeQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            orderMoneyLockTime = strategyOrderMoneyLockTimeModel.getStgNumValue();

            // 策略数据：出码后订单的支付时间
            int invalidTimeNum = 0;
            StrategyModel strategyInvalidTimeNumQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.INVALID_TIME_NUM.getStgType());
            StrategyModel strategyInvalidTimeNumModel = ComponentUtil.strategyService.getStrategyModel(strategyInvalidTimeNumQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            invalidTimeNum = strategyInvalidTimeNumModel.getStgNumValue();


            // 策略数据：银行卡金额给出策略（浮动：动态：1为减，2加，3随机加减，4为整数，5为随机数）
            int bankMoneyOut = 0;
            StrategyModel strategyBankMoneyOutQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.BANK_MONEY_OUT.getStgType());
            StrategyModel strategyBankMoneyOutModel = ComponentUtil.strategyService.getStrategyModel(strategyBankMoneyOutQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            bankMoneyOut = strategyBankMoneyOutModel.getStgNumValue();


            // 策略：获取自动上下线银行卡开关
            StrategyModel strategyBankUpAndDownSwitchQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.BANK_UP_AND_DOWN_SWITCH.getStgType());
            StrategyModel strategyBankUpAndDownSwitchModel = ComponentUtil.strategyService.getStrategyModel(strategyBankUpAndDownSwitchQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            int bankUpAndDownSwitch = strategyBankUpAndDownSwitchModel.getStgNumValue();


            // 策略数据：自动下线卡订单计算失败延迟时间
            int bankDownTimeNum = 0;
            StrategyModel strategyBankDownTimeNumQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.BANK_DOWN_TIME.getStgType());
            StrategyModel strategyBankDownTimeNumModel = ComponentUtil.strategyService.getStrategyModel(strategyBankDownTimeNumQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            bankDownTimeNum = strategyBankDownTimeNumModel.getStgNumValue();


            // 策略数据：派单是否锁金额
            int isLockMoney = 0;// 派单是否锁金额：1锁金额，2不锁金额
            StrategyModel strategyIsLockMoneyQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.IS_LOCK_MONEY.getStgType());
            StrategyModel strategyIsLockMoneyModel = ComponentUtil.strategyService.getStrategyModel(strategyIsLockMoneyQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            isLockMoney = strategyIsLockMoneyModel.getStgNumValue();


//            if (requestModel.payType == 3){
//                // 卡转卡，银行卡金额给出策略强制成整数
//                bankMoneyOut = 4;
//            }

            // 解析金额列表的值
            List<StrategyData> strategyDataList = JSON.parseArray(strategyBankMoneyOutModel.getStgBigValue(), StrategyData.class);



            // 根据秘钥获取商户信息
            ChannelModel channelQuery = HodgepodgeMethod.assembleChannelQuery(0, requestModel.secretKey, 1);
            ChannelModel channelModel = ComponentUtil.channelService.getChannel(channelQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            HodgepodgeMethod.checkChannelIsNull(channelModel);

            // 金额出码范围的check：如果渠道没有配置出码金额范围，则使用总策略的出码金额范围；如果渠道配置了出码金额范围，则使用渠道的出码金额范围
            if (!StringUtils.isBlank(channelModel.getInMoneyRange())){
                inMoneyRange = channelModel.getInMoneyRange();
            }else{
                inMoneyRange = strategyOrderMoneyRangeModel.getStgValue();
            }

            // check校验出码金额范围是否合规
            HodgepodgeMethod.checkOrderMoney(inMoneyRange, requestModel.money);

            // 赋值订单支付时间：如果渠道没有配置支付时间，则使用总策略的支付时间；如果渠道配置了支付时间，则使用渠道的支付时间
            if (channelModel.getInvalidTimeNum() != null && channelModel.getInvalidTimeNum() > 0){
                invalidTimeNum = channelModel.getInvalidTimeNum();
            }

            // 赋值出码后银行卡金额的锁定时间：如果渠道没有配置锁定时间，则使用总策略的锁定时间；如果渠道配置了锁定时间，则使用渠道的锁定时间
            if (channelModel.getMoneyLockTime() != null && channelModel.getMoneyLockTime() > 0){
                orderMoneyLockTime = channelModel.getMoneyLockTime();
            }



//            // 获取卡商集合：卡商的余额必须大于订单金额
//            MerchantModel merchantQuery = HodgepodgeMethod.assembleMerchantQuery(0, requestModel.money, 1);
//            List<MerchantModel> merchantList = ComponentUtil.merchantService.findByCondition(merchantQuery);
//            HodgepodgeMethod.checkMerchantIsNull(merchantList);
//
//            // 获取卡商的主键ID集合
//            List<Long> merchantIdList = merchantList.stream().map(MerchantModel::getMerchantId).collect(Collectors.toList());
//
//            // 获取目前正常使用的手机号
//            MobileCardModel mobileCardQuery = HodgepodgeMethod.assembleMobileCardQuery(0, null, 1, 2, 1);
//            List<MobileCardModel> mobileCardList = ComponentUtil.mobileCardService.findByCondition(mobileCardQuery);
//            HodgepodgeMethod.checkmobileCardIsNull(mobileCardList);
//
//            // 获取手机卡的主键ID集合
//            List<Long> mobileCardIdList = mobileCardList.stream().map(MobileCardModel::getId).collect(Collectors.toList());

            // 获取根据渠道/商户银行卡绑定类型，获取上次最大的银行卡ID
            long maxBankId = 0;

            List<Long> bankIdList = null;
            // 银行卡绑定类型：1需要绑定银行卡（一对一池子），2无需绑定银行卡（一对多），3银行卡池子
            int bankBindingType = 0;
            if (channelModel.getBankBindingType() == 1){
                bankBindingType = 2;
            }else if (channelModel.getBankBindingType() == 2){
                bankBindingType = 1;
            }
            if (bankBindingType == 1){
                // 需要绑定银行卡

                // 查询此渠道已绑定的银行卡ID集合
                ChannelBankModel channelBankQuery = HodgepodgeMethod.assembleChannelBankQuery(0,channelModel.getId(),0,1);
                bankIdList = ComponentUtil.channelBankService.getBankRelationList(channelBankQuery);
                // 需要绑定银行卡，校验是否有绑定的银行卡
                HodgepodgeMethod.checkBankRelationIsNull(bankIdList);

                // 获取上次此渠道给出的银行卡ID
                maxBankId = HodgepodgeMethod.getMaxBankByRedis(bankBindingType, channelModel.getId());

            }else if (bankBindingType == 2){
                // 无需绑定银行卡

//                // 查询已经绑定的银行卡ID集合
//                ChannelBankModel channelBankQuery = HodgepodgeMethod.assembleChannelBankQuery(0,0,0,0);
//                bankIdList = ComponentUtil.channelBankService.getBankRelationList(channelBankQuery);
//
//                // 获取上次给出的银行卡ID
//                maxBankId = HodgepodgeMethod.getMaxBankByRedis(bankBindingType, 0);


                // 查询商户的银行卡池子已经绑定的银行卡ID集合
                ChannelBankPoolModel channelBankPoolQuery = HodgepodgeMethod.assembleChannelBankPoolQuery(0,channelModel.getId(),0,1);
                bankIdList = ComponentUtil.channelBankPoolService.getBankRelationList(channelBankPoolQuery);
                if (bankIdList != null && bankIdList.size() != 0){
                    bankBindingType = 2;

                    // 获取上次给出的银行卡ID：这个渠道在一对多的池子有多张银行卡
                    maxBankId = HodgepodgeMethod.getMaxBankByRedis(bankBindingType, channelModel.getId());
                }else{
                    // 查询银行卡池子的银行卡ID集合
                    BankPoolModel bankPoolQuery = HodgepodgeMethod.assembleBankPoolQuery(0,0,1);
                    bankIdList = ComponentUtil.bankPoolService.getBankIdList(bankPoolQuery);
                    if (bankIdList != null && bankIdList.size() != 0){
                        bankBindingType = 3;

                        // 获取上次给出的银行卡ID
                        maxBankId = HodgepodgeMethod.getMaxBankByRedis(bankBindingType, 0);
                    }
                }

                // 校验池子是否有可用的银行卡ID集合
                HodgepodgeMethod.checkBankPoolIsNull(bankIdList);



            }

//            // 获取商户与银行卡绑定关系的集合
//            ChannelBankModel channelBankQuery = HodgepodgeMethod.assembleChannelBankQuery(0, channelModel.getId(),0, 1);
//            List<ChannelBankModel> channelBankList = ComponentUtil.channelBankService.findByCondition(channelBankQuery);
//
//            // 获取绑定关系的银行卡ID
//            List<Long> bankIdList = null;
//            if (channelBankList != null && channelBankList.size() > 0){
//                bankIdList = channelBankList.stream().map(ChannelBankModel::getBankId).collect(Collectors.toList());
//            }

            // 获取银行卡以及银行卡的放量策略数据
            BankModel bankByOrderQuery = HodgepodgeMethod.assembleBankByOrderQuery(requestModel.money, bankBindingType, bankIdList, bankUpAndDownSwitch);
            List<BankModel> bankList = ComponentUtil.bankService.getBankAndStrategy(bankByOrderQuery);
            HodgepodgeMethod.checkBankIsNull(bankList);

            // 银行卡集合按照上次给出过码进行排序
            List<BankModel> sortList = HodgepodgeMethod.sortBankList(bankList, maxBankId);


//            // 区分出与银行卡绑定关系以及未绑定关系
//            List<BankModel> bankAllList = HodgepodgeMethod.assembleBankByPriority(bankList, bankIdList, bankOutType);
            BankModel bankModel = null;
            if (isLockMoney == 1){
                // 锁定金额：筛选可用的银行卡
                bankModel = ComponentUtil.orderService.screenBankByMoney(sortList, requestModel.money, requestModel.payType, orderMoneyLockTime, bankMoneyOut, strategyDataList);
            }else if (isLockMoney == 2){
                // 无需锁定金额：筛选可用的银行卡
                bankModel = ComponentUtil.orderService.screenBankNotLockMoney(sortList, requestModel.money, requestModel.payType);
            }

            HodgepodgeMethod.checkScreenBankIsNull(bankModel);

            String serviceCharge = "";// 卡商手续费
            // 获取卡商绑定渠道的手续费
            MerchantServiceChargeModel merchantServiceChargeQuery = HodgepodgeMethod.assembleMerchantServiceChargeQuery(0, bankModel.getMerchantId(), channelModel.getId(),1);
            MerchantServiceChargeModel merchantServiceChargeModel = (MerchantServiceChargeModel)ComponentUtil.merchantServiceChargeService.findByObject(merchantServiceChargeQuery);
            serviceCharge = HodgepodgeMethod.getMerchantServiceCharge(merchantServiceChargeModel);

            // 添加订单
            OrderModel orderModel = HodgepodgeMethod.assembleOrderAdd(bankModel, requestModel, channelModel, sgid, invalidTimeNum, serviceCharge, bankBindingType, bankDownTimeNum);
            int num = ComponentUtil.orderService.add(orderModel);
            HodgepodgeMethod.checkAddOrderIsOk(num);

            // 存储redis：给出的银行卡ID，用于字段maxBankId
            HodgepodgeMethod.saveMaxBankByRedis(bankBindingType, channelModel.getId(), bankModel.getId());

            String payQrCodeUrl = "";// 要跳转的支付页
            if (requestModel.payType == 2){
                payQrCodeUrl = ComponentUtil.loadConstant.zfbQrCodeUrl;
            }else if (requestModel.payType == 3){
                payQrCodeUrl = ComponentUtil.loadConstant.cardQrCodeUrl;
            }
            // 组装返回客户端的数据
            long stime = System.currentTimeMillis();
//            String sign = SignUtil.getSgin(stime, secretKeySign); // stime+秘钥=sign
            String strData = HodgepodgeMethod.assembleOrderDataResult(stime, token, orderModel, requestModel.returnUrl, payQrCodeUrl, null);
            // 数据加密
            String encryptionData = StringUtil.mergeCodeBase64(strData);
            ResponseEncryptionJson resultDataModel = new ResponseEncryptionJson();
            resultDataModel.jsonData = encryptionData;
            // 返回数据给客户端
            return JsonResult.successResult(resultDataModel, cgid, sgid);
        }catch (Exception e){
            Map<String,String> map = ExceptionMethod.getException(e, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO);
            log.error(String.format("this OrderController.qrCode() is error , the cgid=%s and sgid=%s and all data=%s!", cgid, sgid, data));
            if (!StringUtils.isBlank(map.get("dbCode"))){
                log.error(String.format("this OrderController.qrCode() is error codeInfo, the dbCode=%s and dbMessage=%s !", map.get("dbCode"), map.get("dbMessage")));
            }
            e.printStackTrace();
            return JsonResult.failedResult(map.get("message"), map.get("code"), cgid, sgid);
        }
    }


    /**
     * @Description: 获取派单数据-详情-返回码的接口
     * @param request
     * @param response
     * @return com.gd.chain.common.utils.JsonResult<java.lang.Object>
     * @author yoko
     * @date 2019/11/25 22:58
     * local:http://localhost:8093/cake/order/getQrCode
     * 请求的属性类:RequestOrder
     * 必填字段:{"orderNo":"order_no_3"}
     * 加密字段:{"jsonData":"eyJvcmRlck5vIjoiMjAyMDA5MTMxNjM1NTUwMDAwMDAxIn0="}
     * 客户端加密字段:id+ctime+cctime+秘钥=sign
     * 服务端加密字段:stime+秘钥=sign
     * result={
     *     "resultCode": "0",
     *     "message": "success",
     *     "data": {
     *         "jsonData": "eyJvcmRlciI6eyJhY2NvdW50TmFtZSI6IuW8gOaIt+S6ul83XzQiLCJiYW5rQ2FyZCI6IumTtuihjOWNoV83XzQiLCJiYW5rQ29kZSI6IueugOeggV83XzQiLCJiYW5rTmFtZSI6IumTtuihjOWQjeensF83XzQiLCJpbnZhbGlkU2Vjb25kIjoiMjQyIiwiaW52YWxpZFRpbWUiOiIyMDIwLTA5LTEzIDE2OjUwOjU2Iiwib3JkZXJNb25leSI6IjExMTEuMDAiLCJvcmRlck5vIjoiMjAyMDA5MTMxNjM1NTUwMDAwMDAxIiwic2hvcnRDaGFpbiI6Imh0dHA6Ly93ejIuaW4vMk8xVmJnIn0sInNpZ24iOiI3MzgzMzcwMmQzZDUzZmVjNDdjYzAyOGIzMTg5ZjliZCIsInN0aW1lIjoxNTk5OTg2ODEzMjcwfQ=="
     *     }
     * }
     */
    @RequestMapping(value = "/getQrCode", method = {RequestMethod.POST})
    public JsonResult<Object> getQrCode(HttpServletRequest request, HttpServletResponse response, @RequestBody RequestEncryptionJson requestData) throws Exception{
        String data = "";

        String ip = StringUtil.getIpAddress(request);
        RegionModel regionModel = HodgepodgeMethod.assembleRegionModel(ip);

        RequestOrder requestModel = new RequestOrder();
        try{
            // 解密
            data = StringUtil.decoderBase64(requestData.jsonData);
            requestModel  = JSON.parseObject(data, RequestOrder.class);

            // check校验请求的数据
            HodgepodgeMethod.checkOrderByQrCodeData(requestModel);

            // 获取地域信息
            regionModel = HodgepodgeMethod.getRegion(regionModel);

            // 策略数据：出码后订单的支付时间
            int invalidTimeNum = 0;
            StrategyModel strategyInvalidTimeNumQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.INVALID_TIME_NUM.getStgType());
            StrategyModel strategyInvalidTimeNumModel = ComponentUtil.strategyService.getStrategyModel(strategyInvalidTimeNumQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            invalidTimeNum = strategyInvalidTimeNumModel.getStgNumValue();


            // 策略数据：黑名单IP
            String blacklistIp = "";
            StrategyModel strategyBlacklistIpQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.BLACKLIST_IP.getStgType());
            StrategyModel strategyBlacklistIpModel = ComponentUtil.strategyService.getStrategyModel(strategyBlacklistIpQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            blacklistIp = strategyBlacklistIpModel.getStgBigValue();
            boolean flag_ip = HodgepodgeMethod.checkBlacklistIp(blacklistIp, ip);


            // 策略数据：短链金额配置
            int shortChainMoney = 0;
            StrategyModel strategyQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.SHORT_CHAIN_MONEY.getStgType());
            StrategyModel strategyModel = ComponentUtil.strategyService.getStrategyModel(strategyQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            shortChainMoney = strategyModel.getStgNumValue();

            // 获取短链数据
            ShortChainModel shortChainModel = ComponentUtil.shortChainService.getShortChain(new ShortChainModel(), ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            HodgepodgeMethod.checkShortChainIsNull(shortChainModel);


            // 收款账号详情数据
            OrderModel orderData = null;
            if (flag_ip){
                // 被限制住的黑名单IP，不能查到订单信息
                OrderModel orderQuery = HodgepodgeMethod.assembleOrderByOrderNoQuery(requestModel.orderNo, 1);
                orderData = (OrderModel)ComponentUtil.orderService.findByObject(orderQuery);
            }


            if (orderData != null && orderData.getId() != null && orderData.getId() > 0){
                // 获取渠道信息
                ChannelModel channelQuery = HodgepodgeMethod.assembleChannelQuery(orderData.getChannelId(), null, 0);
                ChannelModel channelModel = ComponentUtil.channelService.getChannelById(channelQuery, 0);
                if (channelModel != null && channelModel.getId() != null){
                    if (channelModel.getInvalidTimeNum() != null && channelModel.getInvalidTimeNum() > 0){
                        invalidTimeNum = channelModel.getInvalidTimeNum();// 把渠道的支付时间进行赋值
                    }
                }
            }


            // 生成短链
            String shortChain = "";
//            String shortChain = HodgepodgeMethod.getShortChain(orderData, shortChainModel.getInterfaceAds(), shortChainMoney);
//            String shortChain = ShortChainUtil.getShortChainH5Url(shortChainModel.getInterfaceAds());

            // 添加支付页的用户IP统计数据
            StatisticsIpModel statisticsIpModel = HodgepodgeMethod.assembleStatisticsIpData(requestModel, regionModel);
            if (statisticsIpModel != null){
                ComponentUtil.statisticsIpService.add(statisticsIpModel);
            }

            // 组装返回客户端的数据
            long stime = System.currentTimeMillis();
            String sign = SignUtil.getSgin(stime, secretKeySign); // stime+秘钥=sign
            String strData = HodgepodgeMethod.assembleOrderByOrderNoDataResult(stime, sign, orderData, shortChain, invalidTimeNum);
            // 数据加密
            String encryptionData = StringUtil.mergeCodeBase64(strData);
            ResponseEncryptionJson resultDataModel = new ResponseEncryptionJson();
            resultDataModel.jsonData = encryptionData;
            // 返回数据给客户端
            return JsonResult.successResult(resultDataModel);
        }catch (Exception e){
            Map<String,String> map = ExceptionMethod.getException(e, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO);
            // 添加异常
            log.error(String.format("this OrderController.getQrCode() is error , the data=%s!", data));
            if (!StringUtils.isBlank(map.get("dbCode"))){
                log.error(String.format("this OrderController.getQrCode() is error codeInfo, the dbCode=%s and dbMessage=%s !", map.get("dbCode"), map.get("dbMessage")));
            }
            e.printStackTrace();
            return JsonResult.failedResult(map.get("message"), map.get("code"));
        }
    }



    /**
     * @Description: 获取派单的订单状态
     * @param request
     * @param response
     * @return com.gd.chain.common.utils.JsonResult<java.lang.Object>
     * @author yoko
     * @date 2019/11/25 22:58
     * local:http://localhost:8093/cake/order/getOrderStatus
     * 请求的属性类:RequestOrder
     * 必填字段:{"orderNo":"202009131631330000001"}
     * 加密字段:{"jsonData":"eyJvcmRlck5vIjoiMjAyMDA5MTMxNjM1NTUwMDAwMDAxIn0="}
     * 客户端加密字段:id+ctime+cctime+秘钥=sign
     * 服务端加密字段:stime+秘钥=sign
     * result={
     *     "resultCode": "0",
     *     "message": "success",
     *     "data": {
     *         "jsonData": "eyJvcmRlclN0YXR1cyI6MX0="
     *     }
     * }
     */
    @RequestMapping(value = "/getOrderStatus", method = {RequestMethod.POST})
    public JsonResult<Object> getOrderStatus(HttpServletRequest request, HttpServletResponse response, @RequestBody RequestEncryptionJson requestData) throws Exception{
        String data = "";

        RequestOrder requestModel = new RequestOrder();
        try{
            // 解密
            data = StringUtil.decoderBase64(requestData.jsonData);
            requestModel  = JSON.parseObject(data, RequestOrder.class);

            // check校验请求的数据
            HodgepodgeMethod.checkOrderByQrCodeData(requestModel);

            // 收款账号详情数据
            OrderModel orderQuery = HodgepodgeMethod.assembleOrderByOrderNoQuery(requestModel.orderNo, 4);
            int orderStatus = ComponentUtil.orderService.getOrderStatus(orderQuery);
            if (orderStatus != 0){
                orderStatus = 1;
            }
            // 组装返回客户端的数据
            long stime = System.currentTimeMillis();
            String sign = SignUtil.getSgin(stime, secretKeySign); // stime+秘钥=sign
            String strData = HodgepodgeMethod.assembleOrderStatusResult(stime, sign, orderStatus);
            // 数据加密
            String encryptionData = StringUtil.mergeCodeBase64(strData);
            ResponseEncryptionJson resultDataModel = new ResponseEncryptionJson();
            resultDataModel.jsonData = encryptionData;

            // 返回数据给客户端
            return JsonResult.successResult(resultDataModel);
        }catch (Exception e){
            Map<String,String> map = ExceptionMethod.getException(e, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO);
            // 添加异常
            log.error(String.format("this OrderController.getOrderStatus() is error , the data=%s!", data));
            if (!StringUtils.isBlank(map.get("dbCode"))){
                log.error(String.format("this OrderController.getOrderStatus() is error codeInfo, the dbCode=%s and dbMessage=%s !", map.get("dbCode"), map.get("dbMessage")));
            }
            e.printStackTrace();
            return JsonResult.failedResult(map.get("message"), map.get("code"));
        }
    }



    /**
     * @Description: 更新订单的转账用户
     * @param request
     * @param response
     * @return com.gd.chain.common.utils.JsonResult<java.lang.Object>
     * @author yoko
     * @date 2019/11/25 22:58
     * local:http://localhost:8093/cake/order/updateTransferUser
     * 请求的属性类:RequestOrder
     * 必填字段:{"orderNo":"202009131631330000001", "transferUser": "张三"}
     * 加密字段:{"jsonData":"eyJvcmRlck5vIjoiQ0RTMjAyMTA0MDExNTQzMTExIiwgInRyYW5zZmVyVXNlciI6ICLlvKDkuIkifQ=="}
     * 客户端加密字段:id+ctime+cctime+秘钥=sign
     * 服务端加密字段:stime+秘钥=sign
     * result={
     *     "resultCode": "0",
     *     "message": "success",
     *     "data": {
     *         "jsonData": "eyJvcmRlclN0YXR1cyI6MX0="
     *     }
     * }
     */
    @RequestMapping(value = "/updateTransferUser", method = {RequestMethod.POST})
    public JsonResult<Object> updateTransferUser(HttpServletRequest request, HttpServletResponse response, @RequestBody RequestEncryptionJson requestData) throws Exception{
        String ip = StringUtil.getIpAddress(request);
        String data = "";

        RequestOrder requestModel = new RequestOrder();
        try{
            // 解密
            data = StringUtil.decoderBase64(requestData.jsonData);
            requestModel  = JSON.parseObject(data, RequestOrder.class);

            // check校验请求的数据
            HodgepodgeMethod.checkUpdateTransferUserData(requestModel);

            // 校验IP是否频繁操作
            HodgepodgeMethod.checkUpdateTransferUserIpByRedis(ip);


            // 查询订单信息
            OrderModel orderQuery = HodgepodgeMethod.assembleOrderByOrderNoQuery(requestModel.orderNo,0);
            OrderModel orderModel = (OrderModel)ComponentUtil.orderService.findByObject(orderQuery);
            // check校验订单信息
            HodgepodgeMethod.checkOrderByUpdateTransferUser(orderModel);

            // 正式更新转账人姓名
            OrderModel orderUpdate = HodgepodgeMethod.assembleOrderByUpdateTransferUser(requestModel.orderNo, 1, requestModel.transferUser);
            ComponentUtil.orderService.updateTransferUserByOrderNo(orderUpdate);

            // 组装返回客户端的数据
            long stime = System.currentTimeMillis();
            String sign = SignUtil.getSgin(stime, secretKeySign); // stime+秘钥=sign
            String strData = HodgepodgeMethod.assembleResult(stime, null, sign);
            // 数据加密
            String encryptionData = StringUtil.mergeCodeBase64(strData);
            ResponseEncryptionJson resultDataModel = new ResponseEncryptionJson();
            resultDataModel.jsonData = encryptionData;

            // 添加转账人IP
            HodgepodgeMethod.saveUpdateTransferUserIpByRedis(ip);

            // 返回数据给客户端
            return JsonResult.successResult(resultDataModel);
        }catch (Exception e){
            // 添加转账人IP
            HodgepodgeMethod.saveUpdateTransferUserIpByRedis(ip);

            Map<String,String> map = ExceptionMethod.getException(e, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO);
            // 添加异常
            log.error(String.format("this OrderController.updateTransferUser() is error , the data=%s!", data));
            if (!StringUtils.isBlank(map.get("dbCode"))){
                log.error(String.format("this OrderController.updateTransferUser() is error codeInfo, the dbCode=%s and dbMessage=%s !", map.get("dbCode"), map.get("dbMessage")));
            }
            e.printStackTrace();
            return JsonResult.failedResult(map.get("message"), map.get("code"));
        }
    }

}
