package com.hz.cake.master.core.controller.order;

import com.alibaba.fastjson.JSON;
import com.hz.cake.master.core.common.exception.ExceptionMethod;
import com.hz.cake.master.core.common.utils.JsonResult;
import com.hz.cake.master.core.common.utils.StringUtil;
import com.hz.cake.master.core.common.utils.constant.ServerConstant;
import com.hz.cake.master.core.model.RequestEncryptionJson;
import com.hz.cake.master.core.model.ResponseEncryptionJson;
import com.hz.cake.master.core.model.channel.ChannelModel;
import com.hz.cake.master.core.model.merchant.MerchantChannelModel;
import com.hz.cake.master.core.model.merchant.MerchantModel;
import com.hz.cake.master.core.model.merchant.MerchantServiceChargeModel;
import com.hz.cake.master.core.model.order.OrderOutLimitModel;
import com.hz.cake.master.core.model.order.OrderOutModel;
import com.hz.cake.master.core.model.order.OrderOutPrepareModel;
import com.hz.cake.master.core.model.region.RegionModel;
import com.hz.cake.master.core.model.replacepay.ReplacePayGainModel;
import com.hz.cake.master.core.model.replacepay.ReplacePayModel;
import com.hz.cake.master.core.model.strategy.StrategyModel;
import com.hz.cake.master.core.protocol.request.order.ProtocolOrderOut;
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
import java.util.stream.Collectors;

import static com.hz.cake.master.util.ComponentUtil.replacePayService;

/**
 * @Description 代付订单的Controller层
 * @Author yoko
 * @Date 2020/10/29 21:37
 * @Version 1.0
 */
@RestController
@RequestMapping("/cake/orderOut")
public class OrderOutController {

    private static Logger log = LoggerFactory.getLogger(OrderOutController.class);

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
     * @Description: 代付
     * <p>
     *
     * </p>
     * @param request
     * @param response
     * @return com.gd.chain.common.utils.JsonResult<java.lang.Object>
     * @author yoko
     * @date 2019/11/25 22:58
     * local:http://localhost:8093/cake/orderOut/qrCode
     * 请求的属性类:RequestOrder
     * 必填字段:{"money":"2000","payType":2,"outTradeNo":"outTradeNo1","inBankCard":"inBankCard1","inBankName":"inBankName1","inAccountName":"inAccountName1","notifyUrl":"notify_url","returnUrl":"http://www.baidu.com","secretKey":"秘钥_1"}
     * 必填字段:{"money":"2000","payType":2,"outTradeNo":"outTradeNo1","inBankCard":"inBankCard1","inBankName":"inBankName1","inAccountName":"inAccountName1","inBankSubbranch":"inBankSubbranch1","inBankProvince":"inBankProvince1","inBankCity":"inBankCity1","notifyUrl":"notify_url","returnUrl":"http://www.baidu.com","secretKey":"秘钥_1"}
     * 加密字段:{"jsonData":"eyJtb25leSI6IjIwMDAiLCJwYXlUeXBlIjoyLCJvdXRUcmFkZU5vIjoib3V0VHJhZGVObzEiLCJpbkJhbmtDYXJkIjoiaW5CYW5rQ2FyZDEiLCJpbkJhbmtOYW1lIjoiaW5CYW5rTmFtZTEiLCJpbkFjY291bnROYW1lIjoiaW5BY2NvdW50TmFtZTEiLCJub3RpZnlVcmwiOiJub3RpZnlfdXJsIiwicmV0dXJuVXJsIjoiaHR0cDovL3d3dy5iYWlkdS5jb20iLCJzZWNyZXRLZXkiOiLnp5jpkqVfMiJ9"}
     * 客户端加密字段:ctime+cctime+秘钥=sign
     * 服务端加密字段:stime+秘钥=sign
     * result={
     *     "resultCode": "0",
     *     "message": "success",
     *     "data": {
     *         "jsonData": "eyJzaWduIjoiIiwic3RpbWUiOjE2MDQwNjcyNDQxODJ9"
     *     },
     *     "sgid": "DF202010302213581",
     *     "cgid": ""
     * }
     */
    @RequestMapping(value = "/qrCode", method = {RequestMethod.POST})
    public JsonResult<Object> qrCode(HttpServletRequest request, HttpServletResponse response, @RequestBody RequestEncryptionJson requestData) throws Exception{
        String sgid = "CDF" + ComponentUtil.redisIdService.getNewId();
        String cgid = "";
        String token = "";
        String ip = StringUtil.getIpAddress(request);
        String data = "";
        RegionModel regionModel = HodgepodgeMethod.assembleRegionModel(ip);

        ProtocolOrderOut requestModel = new ProtocolOrderOut();
        try{
            // 解密
            data = StringUtil.decoderBase64(requestData.jsonData);
            requestModel  = JSON.parseObject(data, ProtocolOrderOut.class);

            // check校验数据
            HodgepodgeMethod.checkOrderOutAdd(requestModel);

            if (requestModel.money.indexOf(".") <= -1){
                requestModel.money = requestModel.money + ".00";
            }

            // 策略数据：出码开关
            StrategyModel strategyQrCodeSwitchQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.OUT_QR_CODE_SWITCH.getStgType());
            StrategyModel strategyQrCodeSwitchModel = ComponentUtil.strategyService.getStrategyModel(strategyQrCodeSwitchQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            HodgepodgeMethod.checkStrategyByOutQrCodeSwitch(strategyQrCodeSwitchModel);

            // 策略数据：出码金额范围
            StrategyModel strategyOrderMoneyRangeQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.ORDER_OUT_MONEY_RANGE.getStgType());
            StrategyModel strategyOrderMoneyRangeModel = ComponentUtil.strategyService.getStrategyModel(strategyOrderMoneyRangeQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            HodgepodgeMethod.checkOutOrderMoney(strategyOrderMoneyRangeModel.getStgValue(), requestModel.money);

            // 策略数据：代付订单超时时间
            int replacePayInvalidTimeNum = 0;
            StrategyModel strategyReplacePayInvalidTimeNumQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.REPLACE_PAY_INVALID_TIME_NUM.getStgType());
            StrategyModel strategyReplacePayInvalidTimeNumModel = ComponentUtil.strategyService.getStrategyModel(strategyReplacePayInvalidTimeNumQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            replacePayInvalidTimeNum = strategyReplacePayInvalidTimeNumModel.getStgNumValue();

            // 策略数据：代付黑名单校验规则
            int replacePayBlackListRule = 0; // 代付黑名单校验规则：1根据姓名校验，2根据银行卡卡号校验，3根据姓名+银行卡校验
            StrategyModel strategyReplacePayBlackListRuleQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.REPLACE_PAY_BLACK_LIST_RULE.getStgType());
            StrategyModel strategyReplacePayBlackListRuleModel = ComponentUtil.strategyService.getStrategyModel(strategyReplacePayBlackListRuleQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            replacePayBlackListRule = strategyReplacePayBlackListRuleModel.getStgNumValue();


            OrderOutLimitModel orderOutLimitQuery = null;
            if (replacePayBlackListRule == 1){
                orderOutLimitQuery = HodgepodgeMethod.assembleOrderOutLimitQuery(requestModel.inAccountName, null);
            }else if (replacePayBlackListRule == 2){
                orderOutLimitQuery = HodgepodgeMethod.assembleOrderOutLimitQuery(null, requestModel.inBankCard);
            }else if (replacePayBlackListRule == 3){
                orderOutLimitQuery = HodgepodgeMethod.assembleOrderOutLimitQuery(requestModel.inAccountName, requestModel.inBankCard);
            }

            // check校验黑名单
            OrderOutLimitModel orderOutLimitModel = (OrderOutLimitModel)ComponentUtil.orderOutLimitService.findByObject(orderOutLimitQuery);
            HodgepodgeMethod.checkOrderOutLimitModelIsNotNull(orderOutLimitModel);


            // 根据秘钥获取商户信息
            ChannelModel channelQuery = HodgepodgeMethod.assembleChannelQuery(0, requestModel.secretKey, 1);
            ChannelModel channelModel = ComponentUtil.channelService.getChannel(channelQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            HodgepodgeMethod.checkOutOrderChannelIsNull(channelModel);

            // 根据渠道获取关联的卡商
            MerchantChannelModel merchantChannelQuery = HodgepodgeMethod.assembleMerchantChannelQuery(0,0,0, channelModel.getId(), 1);
            List<MerchantChannelModel> merchantChannelList = ComponentUtil.merchantChannelService.findByCondition(merchantChannelQuery);
            HodgepodgeMethod.checkMerchantChannelIsNull(merchantChannelList);

            // 获取卡商的集合ID
            List<Long> merchantIdList = null;
            if (merchantChannelList != null && merchantChannelList.size() > 0){
                merchantIdList = merchantChannelList.stream().map(MerchantChannelModel::getMerchantId).collect(Collectors.toList());
            }


            // 根据渠道与卡商的关联关系中的卡商ID获取卡商集合
            MerchantModel merchantModel = HodgepodgeMethod.assembleMerchantQuery(0, 0, 2, merchantIdList, requestModel.money, 1);
            List<MerchantModel> merchantList = ComponentUtil.merchantService.findByCondition(merchantModel);
            HodgepodgeMethod.checkMerchantIsNullByOutOrder(merchantList);

            // 获取根据渠道/商户绑定，获取上次最大的卡商ID
            long maxMerchantId = 0;

            // 获取上次给出的银行卡ID
            maxMerchantId = HodgepodgeMethod.getMaxMerchantByRedis(0, channelModel.getId());


            // 卡商集合按照上次给出过码进行排序
            List<MerchantModel> sortList = HodgepodgeMethod.sortMerchantList(merchantList, maxMerchantId);


            // 筛选可用的卡商
            MerchantModel merchantData = ComponentUtil.orderOutService.screenMerchantByMoney(sortList, requestModel.money, sgid);
            HodgepodgeMethod.checkScreenMerchantsNull(merchantData);

            String serviceCharge = "";// 卡商手续费
            // 获取卡商绑定渠道的手续费
            MerchantServiceChargeModel merchantServiceChargeQuery = HodgepodgeMethod.assembleMerchantServiceChargeQuery(0, merchantData.getId(), channelModel.getId(),1);
            MerchantServiceChargeModel merchantServiceChargeModel = (MerchantServiceChargeModel)ComponentUtil.merchantServiceChargeService.findByObject(merchantServiceChargeQuery);
            serviceCharge = HodgepodgeMethod.getMerchantServiceCharge(merchantServiceChargeModel);

            // 添加代付订单
            OrderOutModel orderOutModel = HodgepodgeMethod.assembleOrderOutAdd(merchantData, requestModel, channelModel, sgid, serviceCharge, replacePayInvalidTimeNum);
            int num = ComponentUtil.orderOutService.add(orderOutModel);
            HodgepodgeMethod.checkAddOrderOutIsOk(num);

            // 存储redis：给出的卡商ID，用于字段maxMerchantId
            HodgepodgeMethod.saveMaxMerchantByRedis(0, channelModel.getId(), merchantData.getId());

            // 组装返回客户端的数据
            long stime = System.currentTimeMillis();
            String strData = HodgepodgeMethod.assembleOrderOutDataResult(stime, token, orderOutModel, requestModel.returnUrl);
            // 数据加密
            String encryptionData = StringUtil.mergeCodeBase64(strData);
            ResponseEncryptionJson resultDataModel = new ResponseEncryptionJson();
            resultDataModel.jsonData = encryptionData;
            // 返回数据给客户端
            return JsonResult.successResult(resultDataModel, cgid, sgid);
        }catch (Exception e){
            Map<String,String> map = ExceptionMethod.getException(e, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO);
            log.error(String.format("this OrderOutController.qrCode() is error , the cgid=%s and sgid=%s and all data=%s!", cgid, sgid, data));
            if (!StringUtils.isBlank(map.get("dbCode"))){
                log.error(String.format("this OrderOutController.qrCode() is error codeInfo, the dbCode=%s and dbMessage=%s !", map.get("dbCode"), map.get("dbMessage")));
            }
            e.printStackTrace();
            return JsonResult.failedResult(map.get("message"), map.get("code"), cgid, sgid);
        }
    }




    /**
     * @Description: 衫德-代付
     * <p>
     *
     * </p>
     * @param request
     * @param response
     * @return com.gd.chain.common.utils.JsonResult<java.lang.Object>
     * @author yoko
     * @date 2019/11/25 22:58
     * local:http://localhost:8093/cake/orderOut/sandQrCode
     * 请求的属性类:RequestOrder
     * 必填字段:{"money":"2000","payType":2,"outTradeNo":"outTradeNo1","inBankCard":"inBankCard1","inBankName":"inBankName1","inAccountName":"inAccountName1","notifyUrl":"notify_url","returnUrl":"http://www.baidu.com","secretKey":"秘钥_1"}
     * 必填字段:{"money":"2000","payType":2,"outTradeNo":"outTradeNo1","inBankCard":"inBankCard1","inBankName":"inBankName1","inAccountName":"inAccountName1","inBankSubbranch":"inBankSubbranch1","inBankProvince":"inBankProvince1","inBankCity":"inBankCity1","notifyUrl":"notify_url","returnUrl":"http://www.baidu.com","secretKey":"秘钥_1"}
     * 加密字段:{"jsonData":"eyJtb25leSI6IjIwMDAiLCJwYXlUeXBlIjoyLCJvdXRUcmFkZU5vIjoib3V0VHJhZGVObzEiLCJpbkJhbmtDYXJkIjoiaW5CYW5rQ2FyZDEiLCJpbkJhbmtOYW1lIjoiaW5CYW5rTmFtZTEiLCJpbkFjY291bnROYW1lIjoiaW5BY2NvdW50TmFtZTEiLCJub3RpZnlVcmwiOiJub3RpZnlfdXJsIiwicmV0dXJuVXJsIjoiaHR0cDovL3d3dy5iYWlkdS5jb20iLCJzZWNyZXRLZXkiOiLnp5jpkqVfMiJ9"}
     * 客户端加密字段:ctime+cctime+秘钥=sign
     * 服务端加密字段:stime+秘钥=sign
     * result={
     *     "resultCode": "0",
     *     "message": "success",
     *     "data": {
     *         "jsonData": "eyJzaWduIjoiIiwic3RpbWUiOjE2MDQwNjcyNDQxODJ9"
     *     },
     *     "sgid": "DF202010302213581",
     *     "cgid": ""
     * }
     */
    @RequestMapping(value = "/sandQrCode", method = {RequestMethod.POST})
    public JsonResult<Object> sandQrCode(HttpServletRequest request, HttpServletResponse response, @RequestBody RequestEncryptionJson requestData) throws Exception{
        String sgid = "CDFA" + ComponentUtil.redisIdService.getNewId();
        String cgid = "";
        String token = "";
        String ip = StringUtil.getIpAddress(request);
        String data = "";
        RegionModel regionModel = HodgepodgeMethod.assembleRegionModel(ip);

        ProtocolOrderOut requestModel = new ProtocolOrderOut();
        try{
            // 解密
            data = StringUtil.decoderBase64(requestData.jsonData);
            requestModel  = JSON.parseObject(data, ProtocolOrderOut.class);

            log.info("");
            // check校验数据
            HodgepodgeMethod.checkOrderOutAdd(requestModel);

            if (requestModel.money.indexOf(".") <= -1){
                requestModel.money = requestModel.money + ".00";
            }

            int resourceType = 1;// 代付资源类型：1杉德支付，2金服支付


            // 策略数据：出码开关
            StrategyModel strategyQrCodeSwitchQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.OUT_QR_CODE_SWITCH.getStgType());
            StrategyModel strategyQrCodeSwitchModel = ComponentUtil.strategyService.getStrategyModel(strategyQrCodeSwitchQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            HodgepodgeMethod.checkStrategyByOutQrCodeSwitch(strategyQrCodeSwitchModel);

            // 策略数据：出码金额范围
            StrategyModel strategyOrderMoneyRangeQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.ORDER_OUT_MONEY_RANGE.getStgType());
            StrategyModel strategyOrderMoneyRangeModel = ComponentUtil.strategyService.getStrategyModel(strategyOrderMoneyRangeQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            HodgepodgeMethod.checkOutOrderMoney(strategyOrderMoneyRangeModel.getStgValue(), requestModel.money);


            // 策略数据：代付出码规则
            int replacePayRule = 0; // 代付出码规则:1从ID从小到大，2金额从小到大
            StrategyModel strategyReplacePayRuleQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.REPLACE_PAY_RULE.getStgType());
            StrategyModel strategyReplacePayRuleModel = ComponentUtil.strategyService.getStrategyModel(strategyReplacePayRuleQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            replacePayRule = strategyReplacePayRuleModel.getStgNumValue();


            // 策略数据：代付方式
            int replacePayType = 0; // 代付方式：1直接转账，2预备转账（预备是走TASK）
            StrategyModel strategyReplacePayTypeQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.REPLACE_PAY_TYPE.getStgType());
            StrategyModel strategyReplacePayTypeModel = ComponentUtil.strategyService.getStrategyModel(strategyReplacePayTypeQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            replacePayType = strategyReplacePayTypeModel.getStgNumValue();

            // 策略数据：代付黑名单校验规则
            int replacePayBlackListRule = 0; // 代付黑名单校验规则：1根据姓名校验，2根据银行卡卡号校验，3根据姓名+银行卡校验
            StrategyModel strategyReplacePayBlackListRuleQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.REPLACE_PAY_BLACK_LIST_RULE.getStgType());
            StrategyModel strategyReplacePayBlackListRuleModel = ComponentUtil.strategyService.getStrategyModel(strategyReplacePayBlackListRuleQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            replacePayBlackListRule = strategyReplacePayBlackListRuleModel.getStgNumValue();


            OrderOutLimitModel orderOutLimitQuery = null;
            if (replacePayBlackListRule == 1){
                log.info("");
                orderOutLimitQuery = HodgepodgeMethod.assembleOrderOutLimitQuery(requestModel.inAccountName, null);
            }else if (replacePayBlackListRule == 2){
                orderOutLimitQuery = HodgepodgeMethod.assembleOrderOutLimitQuery(null, requestModel.inBankCard);
            }else if (replacePayBlackListRule == 3){
                orderOutLimitQuery = HodgepodgeMethod.assembleOrderOutLimitQuery(requestModel.inAccountName, requestModel.inBankCard);
            }

            // check校验黑名单
            OrderOutLimitModel orderOutLimitModel = (OrderOutLimitModel)ComponentUtil.orderOutLimitService.findByObject(orderOutLimitQuery);
            HodgepodgeMethod.checkOrderOutLimitModelIsNotNull(orderOutLimitModel);



            // 根据秘钥获取商户信息
            ChannelModel channelQuery = HodgepodgeMethod.assembleChannelQuery(0, requestModel.secretKey, 1);
            ChannelModel channelModel = ComponentUtil.channelService.getChannel(channelQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            HodgepodgeMethod.checkOutOrderChannelIsNull(channelModel);


            if (channelModel.getReplacePayType() != null && channelModel.getReplacePayType() != 0){
                replacePayType = channelModel.getReplacePayType();
            }


            if (replacePayType == 1){
                // 直接转账
                // 根据渠道获取关联的卡商
                MerchantChannelModel merchantChannelQuery = HodgepodgeMethod.assembleMerchantChannelQuery(0,0,0, channelModel.getId(), 1);
                List<MerchantChannelModel> merchantChannelList = ComponentUtil.merchantChannelService.findByCondition(merchantChannelQuery);
                HodgepodgeMethod.checkMerchantChannelIsNull(merchantChannelList);

                // 获取卡商的集合ID
                List<Long> merchantIdList = null;
                if (merchantChannelList != null && merchantChannelList.size() > 0){
                    merchantIdList = merchantChannelList.stream().map(MerchantChannelModel::getMerchantId).collect(Collectors.toList());
                }


                // 根据渠道与卡商的关联关系中的卡商ID获取卡商集合
                MerchantModel merchantModel = HodgepodgeMethod.assembleMerchantQuery(0, 0, 2, merchantIdList, requestModel.money, 1);
                List<MerchantModel> merchantList = ComponentUtil.merchantService.findByCondition(merchantModel);
                HodgepodgeMethod.checkMerchantIsNullByOutOrder(merchantList);


                // 获取上次最大的代付ID
                long maxReplacePayId = 0;

                // 获取上次给出的代付ID
                maxReplacePayId = HodgepodgeMethod.getMaxReplacePayRedis(resourceType);

                // 查询代付集合数据
                ReplacePayModel replacePayModel = HodgepodgeMethod.assembleReplacePayQuery(merchantList, requestModel.money, resourceType);
                List<ReplacePayModel> replacePayList = ComponentUtil.replacePayService.getReplacePayList(replacePayModel);
                HodgepodgeMethod.checkReplacePayIsNullByOutOrder(replacePayList);

                // 代付集合进行排序
                List<ReplacePayModel> sortList = null;
                if (replacePayRule == 1){
                    // 1从ID从小到大，
                    sortList = HodgepodgeMethod.sortReplacePayList(replacePayList, maxReplacePayId);
                }else if (replacePayRule == 2){
                    // 2金额从小到大
                    sortList = replacePayList;
                    sortList.sort((x, y) -> Double.compare(Double.parseDouble(x.getDayMoney()), Double.parseDouble(y.getDayMoney())));
                }

                // 组装请求衫德的订单信息
                OrderOutModel orderOutSand = HodgepodgeMethod.assembleOrderOutSand(requestModel, sgid);

                // 筛选可用的代付
                ReplacePayModel replacePayData = ComponentUtil.orderOutService.screenReplacePay(sortList, merchantList, orderOutSand);
                HodgepodgeMethod.checkScreenReplacePayNull(replacePayData);

                String serviceCharge = "";// 卡商手续费
                // 获取卡商绑定渠道的手续费
                MerchantServiceChargeModel merchantServiceChargeQuery = HodgepodgeMethod.assembleMerchantServiceChargeQuery(0, replacePayData.getMerchantId(), channelModel.getId(),1);
                MerchantServiceChargeModel merchantServiceChargeModel = (MerchantServiceChargeModel)ComponentUtil.merchantServiceChargeService.findByObject(merchantServiceChargeQuery);
                serviceCharge = HodgepodgeMethod.getMerchantServiceCharge(merchantServiceChargeModel);

                // 添加代付订单
                OrderOutModel orderOutModel = HodgepodgeMethod.assembleOrderOutBySandAdd(orderOutSand, replacePayData, merchantList, channelModel, serviceCharge);
                int num = ComponentUtil.orderOutService.add(orderOutModel);
                HodgepodgeMethod.checkAddOrderOutIsOk(num);

                // 添加主动拉取订单结果的数据
                if (replacePayData.getGainDataType() == 2){
                    // 获取订单结果类型：1被动接收数据，2主动查询
                    ReplacePayGainModel replacePayGainModel = HodgepodgeMethod.assembleReplacePayGainAdd(orderOutModel, replacePayData);
                    ComponentUtil.replacePayGainService.add(replacePayGainModel);
                }

                // 存储redis：给出的代付ID，用于字段maxReplacePayId
                HodgepodgeMethod.saveMaxreplacePayByRedis(resourceType, replacePayData.getId());

                // 组装返回客户端的数据
                long stime = System.currentTimeMillis();
                String strData = HodgepodgeMethod.assembleOrderOutDataResult(stime, token, orderOutModel, requestModel.returnUrl);
                // 数据加密
                String encryptionData = StringUtil.mergeCodeBase64(strData);
                ResponseEncryptionJson resultDataModel = new ResponseEncryptionJson();
                resultDataModel.jsonData = encryptionData;
                // 返回数据给客户端
                return JsonResult.successResult(resultDataModel, cgid, sgid);
            }else{
                // 预备转账：需要先把订单数据存储到预付订单里面
                OrderOutPrepareModel orderOutPrepareAdd = HodgepodgeMethod.assembleOrderOutPrepareAdd(requestModel, channelModel, sgid, resourceType);
                int num  = ComponentUtil.orderOutPrepareService.add(orderOutPrepareAdd);
                if (num > 0){
                    return JsonResult.successResult(null, cgid, sgid);
                }else {
                    return JsonResult.failedResult(cgid, sgid);
                }
            }
        }catch (Exception e){
            Map<String,String> map = ExceptionMethod.getException(e, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO);
            log.error(String.format("this OrderOutController.sandQrCode() is error , the cgid=%s and sgid=%s and all data=%s!", cgid, sgid, data));
            if (!StringUtils.isBlank(map.get("dbCode"))){
                log.error(String.format("this OrderOutController.sandQrCode() is error codeInfo, the dbCode=%s and dbMessage=%s !", map.get("dbCode"), map.get("dbMessage")));
            }
            e.printStackTrace();
            return JsonResult.failedResult(map.get("message"), map.get("code"), cgid, sgid);
        }
    }




    /**
     * @Description: 金服-代付
     * <p>
     *
     * </p>
     * @param request
     * @param response
     * @return com.gd.chain.common.utils.JsonResult<java.lang.Object>
     * @author yoko
     * @date 2019/11/25 22:58
     * local:http://localhost:8093/cake/orderOut/jinfuQrCode
     * 请求的属性类:RequestOrder
     * 必填字段:{"money":"2000","payType":2,"outTradeNo":"outTradeNo1","inBankCard":"inBankCard1","inBankName":"inBankName1","inAccountName":"inAccountName1","notifyUrl":"notify_url","returnUrl":"http://www.baidu.com","secretKey":"秘钥_1"}
     * 必填字段:{"money":"2000","payType":2,"outTradeNo":"outTradeNo1","inBankCard":"inBankCard1","inBankName":"inBankName1","inAccountName":"inAccountName1","inBankSubbranch":"inBankSubbranch1","inBankProvince":"inBankProvince1","inBankCity":"inBankCity1","notifyUrl":"notify_url","returnUrl":"http://www.baidu.com","secretKey":"秘钥_1"}
     * 加密字段:{"jsonData":"eyJtb25leSI6IjIwMDAiLCJwYXlUeXBlIjoyLCJvdXRUcmFkZU5vIjoib3V0VHJhZGVObzEiLCJpbkJhbmtDYXJkIjoiaW5CYW5rQ2FyZDEiLCJpbkJhbmtOYW1lIjoiaW5CYW5rTmFtZTEiLCJpbkFjY291bnROYW1lIjoiaW5BY2NvdW50TmFtZTEiLCJub3RpZnlVcmwiOiJub3RpZnlfdXJsIiwicmV0dXJuVXJsIjoiaHR0cDovL3d3dy5iYWlkdS5jb20iLCJzZWNyZXRLZXkiOiLnp5jpkqVfMiJ9"}
     * 客户端加密字段:ctime+cctime+秘钥=sign
     * 服务端加密字段:stime+秘钥=sign
     * result={
     *     "resultCode": "0",
     *     "message": "success",
     *     "data": {
     *         "jsonData": "eyJzaWduIjoiIiwic3RpbWUiOjE2MDQwNjcyNDQxODJ9"
     *     },
     *     "sgid": "DF202010302213581",
     *     "cgid": ""
     * }
     */
    @RequestMapping(value = "/jinfuQrCode", method = {RequestMethod.POST})
    public JsonResult<Object> jinfuQrCode(HttpServletRequest request, HttpServletResponse response, @RequestBody RequestEncryptionJson requestData) throws Exception{
        String sgid = "JF" + ComponentUtil.redisIdService.getNewId();
        String cgid = "";
        String token = "";
        String ip = StringUtil.getIpAddress(request);
        String data = "";
        RegionModel regionModel = HodgepodgeMethod.assembleRegionModel(ip);

        ProtocolOrderOut requestModel = new ProtocolOrderOut();
        try{
            // 解密
            data = StringUtil.decoderBase64(requestData.jsonData);
            requestModel  = JSON.parseObject(data, ProtocolOrderOut.class);

            // check校验数据
            HodgepodgeMethod.checkOrderOutAdd(requestModel);

            if (requestModel.money.indexOf(".") <= -1){
                requestModel.money = requestModel.money + ".00";
            }

            int resourceType = 2;// 代付资源类型：1杉德支付，2金服支付


            // 策略数据：出码开关
            StrategyModel strategyQrCodeSwitchQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.OUT_QR_CODE_SWITCH.getStgType());
            StrategyModel strategyQrCodeSwitchModel = ComponentUtil.strategyService.getStrategyModel(strategyQrCodeSwitchQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            HodgepodgeMethod.checkStrategyByOutQrCodeSwitch(strategyQrCodeSwitchModel);

            // 策略数据：出码金额范围
            StrategyModel strategyOrderMoneyRangeQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.ORDER_OUT_MONEY_RANGE.getStgType());
            StrategyModel strategyOrderMoneyRangeModel = ComponentUtil.strategyService.getStrategyModel(strategyOrderMoneyRangeQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            HodgepodgeMethod.checkOutOrderMoney(strategyOrderMoneyRangeModel.getStgValue(), requestModel.money);


            // 策略数据：代付出码规则
            int replacePayRule = 0; // 代付出码规则:1从ID从小到大，2金额从小到大
            StrategyModel strategyReplacePayRuleQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.REPLACE_PAY_RULE.getStgType());
            StrategyModel strategyReplacePayRuleModel = ComponentUtil.strategyService.getStrategyModel(strategyReplacePayRuleQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            replacePayRule = strategyReplacePayRuleModel.getStgNumValue();


            // 策略数据：代付方式
            int replacePayType = 0; // 代付方式：1直接转账，2预备转账（预备是走TASK）
            StrategyModel strategyReplacePayTypeQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.REPLACE_PAY_TYPE.getStgType());
            StrategyModel strategyReplacePayTypeModel = ComponentUtil.strategyService.getStrategyModel(strategyReplacePayTypeQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            replacePayType = strategyReplacePayTypeModel.getStgNumValue();


            // 策略数据：代付黑名单校验规则
            int replacePayBlackListRule = 0; // 代付黑名单校验规则：1根据姓名校验，2根据银行卡卡号校验，3根据姓名+银行卡校验
            StrategyModel strategyReplacePayBlackListRuleQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.REPLACE_PAY_BLACK_LIST_RULE.getStgType());
            StrategyModel strategyReplacePayBlackListRuleModel = ComponentUtil.strategyService.getStrategyModel(strategyReplacePayBlackListRuleQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            replacePayBlackListRule = strategyReplacePayBlackListRuleModel.getStgNumValue();


            OrderOutLimitModel orderOutLimitQuery = null;
            if (replacePayBlackListRule == 1){
                log.info("1");
                orderOutLimitQuery = HodgepodgeMethod.assembleOrderOutLimitQuery(requestModel.inAccountName, null);
            }else if (replacePayBlackListRule == 2){
                orderOutLimitQuery = HodgepodgeMethod.assembleOrderOutLimitQuery(null, requestModel.inBankCard);
            }else if (replacePayBlackListRule == 3){
                orderOutLimitQuery = HodgepodgeMethod.assembleOrderOutLimitQuery(requestModel.inAccountName, requestModel.inBankCard);
            }

            // check校验黑名单
            OrderOutLimitModel orderOutLimitModel = (OrderOutLimitModel)ComponentUtil.orderOutLimitService.findByObject(orderOutLimitQuery);
            HodgepodgeMethod.checkOrderOutLimitModelIsNotNull(orderOutLimitModel);



            // 根据秘钥获取商户信息
            ChannelModel channelQuery = HodgepodgeMethod.assembleChannelQuery(0, requestModel.secretKey, 1);
            ChannelModel channelModel = ComponentUtil.channelService.getChannel(channelQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            HodgepodgeMethod.checkOutOrderChannelIsNull(channelModel);


            if (channelModel.getReplacePayType() != null && channelModel.getReplacePayType() != 0){
                replacePayType = channelModel.getReplacePayType();
            }


            if (replacePayType == 1){
                // 直接转账
                // 根据渠道获取关联的卡商
                MerchantChannelModel merchantChannelQuery = HodgepodgeMethod.assembleMerchantChannelQuery(0,0,0, channelModel.getId(), 1);
                List<MerchantChannelModel> merchantChannelList = ComponentUtil.merchantChannelService.findByCondition(merchantChannelQuery);
                HodgepodgeMethod.checkMerchantChannelIsNull(merchantChannelList);

                // 获取卡商的集合ID
                List<Long> merchantIdList = null;
                if (merchantChannelList != null && merchantChannelList.size() > 0){
                    merchantIdList = merchantChannelList.stream().map(MerchantChannelModel::getMerchantId).collect(Collectors.toList());
                }


                // 根据渠道与卡商的关联关系中的卡商ID获取卡商集合
                MerchantModel merchantModel = HodgepodgeMethod.assembleMerchantQuery(0, 0, 2, merchantIdList, requestModel.money, 1);
                List<MerchantModel> merchantList = ComponentUtil.merchantService.findByCondition(merchantModel);
                HodgepodgeMethod.checkMerchantIsNullByOutOrder(merchantList);


                // 获取上次最大的代付ID
                long maxReplacePayId = 0;

                // 获取上次给出的代付ID
                maxReplacePayId = HodgepodgeMethod.getMaxReplacePayRedis(resourceType);

                // 查询代付集合数据
                ReplacePayModel replacePayModel = HodgepodgeMethod.assembleReplacePayQuery(merchantList, requestModel.money, resourceType);
                List<ReplacePayModel> replacePayList = ComponentUtil.replacePayService.getReplacePayList(replacePayModel);
                HodgepodgeMethod.checkReplacePayIsNullByOutOrder(replacePayList);

                // 代付集合进行排序
                List<ReplacePayModel> sortList = null;
                if (replacePayRule == 1){
                    // 1从ID从小到大，
                    sortList = HodgepodgeMethod.sortReplacePayList(replacePayList, maxReplacePayId);
                }else if (replacePayRule == 2){
                    // 2金额从小到大
                    sortList = replacePayList;
                    sortList.sort((x, y) -> Double.compare(Double.parseDouble(x.getDayMoney()), Double.parseDouble(y.getDayMoney())));
                }

                // 组装请求金服的订单信息
                OrderOutModel orderOutJinFu = HodgepodgeMethod.assembleOrderOutJinFu(requestModel, sgid);

                // 筛选可用的代付
                ReplacePayModel replacePayData = ComponentUtil.orderOutService.screenReplacePayJinFu(sortList, merchantList, orderOutJinFu);
                HodgepodgeMethod.checkScreenReplacePayNull(replacePayData);

                String serviceCharge = "";// 卡商手续费
                // 获取卡商绑定渠道的手续费
                MerchantServiceChargeModel merchantServiceChargeQuery = HodgepodgeMethod.assembleMerchantServiceChargeQuery(0, replacePayData.getMerchantId(), channelModel.getId(),1);
                MerchantServiceChargeModel merchantServiceChargeModel = (MerchantServiceChargeModel)ComponentUtil.merchantServiceChargeService.findByObject(merchantServiceChargeQuery);
                serviceCharge = HodgepodgeMethod.getMerchantServiceCharge(merchantServiceChargeModel);

                // 添加代付订单
                OrderOutModel orderOutModel = HodgepodgeMethod.assembleOrderOutByJinFuAdd(orderOutJinFu, replacePayData, merchantList, channelModel, serviceCharge);
                int num = ComponentUtil.orderOutService.add(orderOutModel);
                HodgepodgeMethod.checkAddOrderOutIsOk(num);

                // 添加主动拉取订单结果的数据
                if (replacePayData.getGainDataType() == 2){
                    // 获取订单结果类型：1被动接收数据，2主动查询
                    ReplacePayGainModel replacePayGainModel = HodgepodgeMethod.assembleReplacePayGainAdd(orderOutModel, replacePayData);
                    ComponentUtil.replacePayGainService.add(replacePayGainModel);
                }

                // 存储redis：给出的代付ID，用于字段maxReplacePayId
                HodgepodgeMethod.saveMaxreplacePayByRedis(resourceType, replacePayData.getId());

                // 组装返回客户端的数据
                long stime = System.currentTimeMillis();
                String strData = HodgepodgeMethod.assembleOrderOutDataResult(stime, token, orderOutModel, requestModel.returnUrl);
                // 数据加密
                String encryptionData = StringUtil.mergeCodeBase64(strData);
                ResponseEncryptionJson resultDataModel = new ResponseEncryptionJson();
                resultDataModel.jsonData = encryptionData;
                // 返回数据给客户端
                return JsonResult.successResult(resultDataModel, cgid, sgid);
            }else{
                // 预备转账：需要先把订单数据存储到预付订单里面
                OrderOutPrepareModel orderOutPrepareAdd = HodgepodgeMethod.assembleOrderOutPrepareAdd(requestModel, channelModel, sgid, resourceType);
                int num  = ComponentUtil.orderOutPrepareService.add(orderOutPrepareAdd);
                if (num > 0){
                    return JsonResult.successResult(null, cgid, sgid);
                }else {
                    return JsonResult.failedResult(cgid, sgid);
                }
            }
        }catch (Exception e){
            Map<String,String> map = ExceptionMethod.getException(e, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO);
            log.error(String.format("this OrderOutController.jinfuQrCode() is error , the cgid=%s and sgid=%s and all data=%s!", cgid, sgid, data));
            if (!StringUtils.isBlank(map.get("dbCode"))){
                log.error(String.format("this OrderOutController.jinfuQrCode() is error codeInfo, the dbCode=%s and dbMessage=%s !", map.get("dbCode"), map.get("dbMessage")));
            }
            e.printStackTrace();
            return JsonResult.failedResult(map.get("message"), map.get("code"), cgid, sgid);
        }
    }





    /**
     * @Description: 汇潮-代付
     * <p>
     *
     * </p>
     * @param request
     * @param response
     * @return com.gd.chain.common.utils.JsonResult<java.lang.Object>
     * @author yoko
     * @date 2019/11/25 22:58
     * local:http://localhost:8093/cake/orderOut/huichaoQrCode
     * 请求的属性类:RequestOrder
     * 必填字段:{"money":"2000","payType":2,"outTradeNo":"outTradeNo1","inBankCard":"inBankCard1","inBankName":"inBankName1","inAccountName":"inAccountName1","notifyUrl":"notify_url","returnUrl":"http://www.baidu.com","secretKey":"秘钥_1"}
     * 必填字段:{"money":"2000","payType":2,"outTradeNo":"outTradeNo1","inBankCard":"inBankCard1","inBankName":"inBankName1","inAccountName":"inAccountName1","inBankSubbranch":"inBankSubbranch1","inBankProvince":"inBankProvince1","inBankCity":"inBankCity1","notifyUrl":"notify_url","returnUrl":"http://www.baidu.com","secretKey":"秘钥_1"}
     * 加密字段:{"jsonData":"eyJtb25leSI6IjIwMDAiLCJwYXlUeXBlIjoyLCJvdXRUcmFkZU5vIjoib3V0VHJhZGVObzEiLCJpbkJhbmtDYXJkIjoiaW5CYW5rQ2FyZDEiLCJpbkJhbmtOYW1lIjoiaW5CYW5rTmFtZTEiLCJpbkFjY291bnROYW1lIjoiaW5BY2NvdW50TmFtZTEiLCJub3RpZnlVcmwiOiJub3RpZnlfdXJsIiwicmV0dXJuVXJsIjoiaHR0cDovL3d3dy5iYWlkdS5jb20iLCJzZWNyZXRLZXkiOiLnp5jpkqVfMiJ9"}
     * 客户端加密字段:ctime+cctime+秘钥=sign
     * 服务端加密字段:stime+秘钥=sign
     * result={
     *     "resultCode": "0",
     *     "message": "success",
     *     "data": {
     *         "jsonData": "eyJzaWduIjoiIiwic3RpbWUiOjE2MDQwNjcyNDQxODJ9"
     *     },
     *     "sgid": "DF202010302213581",
     *     "cgid": ""
     * }
     */
    @RequestMapping(value = "/huichaoQrCode", method = {RequestMethod.POST})
    public JsonResult<Object> huichaoQrCode(HttpServletRequest request, HttpServletResponse response, @RequestBody RequestEncryptionJson requestData) throws Exception{
        String sgid = "HC" + ComponentUtil.redisIdService.getNewId();
        String cgid = "";
        String token = "";
        String ip = StringUtil.getIpAddress(request);
        String data = "";
        RegionModel regionModel = HodgepodgeMethod.assembleRegionModel(ip);

        ProtocolOrderOut requestModel = new ProtocolOrderOut();
        try{
            // 解密
            data = StringUtil.decoderBase64(requestData.jsonData);
            requestModel  = JSON.parseObject(data, ProtocolOrderOut.class);

            // check校验数据
            HodgepodgeMethod.checkOrderOutAdd(requestModel);

            if (requestModel.money.indexOf(".") <= -1){
                requestModel.money = requestModel.money + ".00";
            }

            int resourceType = 3;// 代付资源类型：1杉德支付，2金服支付，3汇潮支付


            // 策略数据：出码开关
            StrategyModel strategyQrCodeSwitchQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.OUT_QR_CODE_SWITCH.getStgType());
            StrategyModel strategyQrCodeSwitchModel = ComponentUtil.strategyService.getStrategyModel(strategyQrCodeSwitchQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            HodgepodgeMethod.checkStrategyByOutQrCodeSwitch(strategyQrCodeSwitchModel);

            // 策略数据：出码金额范围
            StrategyModel strategyOrderMoneyRangeQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.ORDER_OUT_MONEY_RANGE.getStgType());
            StrategyModel strategyOrderMoneyRangeModel = ComponentUtil.strategyService.getStrategyModel(strategyOrderMoneyRangeQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            HodgepodgeMethod.checkOutOrderMoney(strategyOrderMoneyRangeModel.getStgValue(), requestModel.money);


            // 策略数据：代付出码规则
            int replacePayRule = 0; // 代付出码规则:1从ID从小到大，2金额从小到大
            StrategyModel strategyReplacePayRuleQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.REPLACE_PAY_RULE.getStgType());
            StrategyModel strategyReplacePayRuleModel = ComponentUtil.strategyService.getStrategyModel(strategyReplacePayRuleQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            replacePayRule = strategyReplacePayRuleModel.getStgNumValue();


            // 策略数据：代付方式
            int replacePayType = 0; // 代付方式：1直接转账，2预备转账（预备是走TASK）
            StrategyModel strategyReplacePayTypeQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.REPLACE_PAY_TYPE.getStgType());
            StrategyModel strategyReplacePayTypeModel = ComponentUtil.strategyService.getStrategyModel(strategyReplacePayTypeQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            replacePayType = strategyReplacePayTypeModel.getStgNumValue();


            // 策略数据：代付黑名单校验规则
            int replacePayBlackListRule = 0; // 代付黑名单校验规则：1根据姓名校验，2根据银行卡卡号校验，3根据姓名+银行卡校验
            StrategyModel strategyReplacePayBlackListRuleQuery = HodgepodgeMethod.assembleStrategyQuery(ServerConstant.StrategyEnum.REPLACE_PAY_BLACK_LIST_RULE.getStgType());
            StrategyModel strategyReplacePayBlackListRuleModel = ComponentUtil.strategyService.getStrategyModel(strategyReplacePayBlackListRuleQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            replacePayBlackListRule = strategyReplacePayBlackListRuleModel.getStgNumValue();


            OrderOutLimitModel orderOutLimitQuery = null;
            if (replacePayBlackListRule == 1){
                log.info("3");
                orderOutLimitQuery = HodgepodgeMethod.assembleOrderOutLimitQuery(requestModel.inAccountName, null);
            }else if (replacePayBlackListRule == 2){
                orderOutLimitQuery = HodgepodgeMethod.assembleOrderOutLimitQuery(null, requestModel.inBankCard);
            }else if (replacePayBlackListRule == 3){
                orderOutLimitQuery = HodgepodgeMethod.assembleOrderOutLimitQuery(requestModel.inAccountName, requestModel.inBankCard);
            }

            // check校验黑名单
            OrderOutLimitModel orderOutLimitModel = (OrderOutLimitModel)ComponentUtil.orderOutLimitService.findByObject(orderOutLimitQuery);
            HodgepodgeMethod.checkOrderOutLimitModelIsNotNull(orderOutLimitModel);



            // 根据秘钥获取商户信息
            ChannelModel channelQuery = HodgepodgeMethod.assembleChannelQuery(0, requestModel.secretKey, 1);
            ChannelModel channelModel = ComponentUtil.channelService.getChannel(channelQuery, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_ZERO);
            HodgepodgeMethod.checkOutOrderChannelIsNull(channelModel);


            if (channelModel.getReplacePayType() != null && channelModel.getReplacePayType() != 0){
                replacePayType = channelModel.getReplacePayType();
            }


            if (replacePayType == 1){
                // 直接转账
                // 根据渠道获取关联的卡商
                MerchantChannelModel merchantChannelQuery = HodgepodgeMethod.assembleMerchantChannelQuery(0,0,0, channelModel.getId(), 1);
                List<MerchantChannelModel> merchantChannelList = ComponentUtil.merchantChannelService.findByCondition(merchantChannelQuery);
                HodgepodgeMethod.checkMerchantChannelIsNull(merchantChannelList);

                // 获取卡商的集合ID
                List<Long> merchantIdList = null;
                if (merchantChannelList != null && merchantChannelList.size() > 0){
                    merchantIdList = merchantChannelList.stream().map(MerchantChannelModel::getMerchantId).collect(Collectors.toList());
                }


                // 根据渠道与卡商的关联关系中的卡商ID获取卡商集合
                MerchantModel merchantModel = HodgepodgeMethod.assembleMerchantQuery(0, 0, 2, merchantIdList, requestModel.money, 1);
                List<MerchantModel> merchantList = ComponentUtil.merchantService.findByCondition(merchantModel);
                HodgepodgeMethod.checkMerchantIsNullByOutOrder(merchantList);


                // 获取上次最大的代付ID
                long maxReplacePayId = 0;

                // 获取上次给出的代付ID
                maxReplacePayId = HodgepodgeMethod.getMaxReplacePayRedis(resourceType);

                // 查询代付集合数据
                ReplacePayModel replacePayModel = HodgepodgeMethod.assembleReplacePayQuery(merchantList, requestModel.money, resourceType);
                List<ReplacePayModel> replacePayList = ComponentUtil.replacePayService.getReplacePayList(replacePayModel);
                HodgepodgeMethod.checkReplacePayIsNullByOutOrder(replacePayList);

                // 代付集合进行排序
                List<ReplacePayModel> sortList = null;
                if (replacePayRule == 1){
                    // 1从ID从小到大，
                    sortList = HodgepodgeMethod.sortReplacePayList(replacePayList, maxReplacePayId);
                }else if (replacePayRule == 2){
                    // 2金额从小到大
                    sortList = replacePayList;
                    sortList.sort((x, y) -> Double.compare(Double.parseDouble(x.getDayMoney()), Double.parseDouble(y.getDayMoney())));
                }

                // 组装请求汇潮的订单信息
                OrderOutModel orderOutJinFu = HodgepodgeMethod.assembleOrderOutHuiChao(requestModel, sgid);

                // 筛选可用的代付
                ReplacePayModel replacePayData = ComponentUtil.orderOutService.screenReplacePayHuiChao(sortList, merchantList, orderOutJinFu);
                HodgepodgeMethod.checkScreenReplacePayNull(replacePayData);

                String serviceCharge = "";// 卡商手续费
                // 获取卡商绑定渠道的手续费
                MerchantServiceChargeModel merchantServiceChargeQuery = HodgepodgeMethod.assembleMerchantServiceChargeQuery(0, replacePayData.getMerchantId(), channelModel.getId(),1);
                MerchantServiceChargeModel merchantServiceChargeModel = (MerchantServiceChargeModel)ComponentUtil.merchantServiceChargeService.findByObject(merchantServiceChargeQuery);
                serviceCharge = HodgepodgeMethod.getMerchantServiceCharge(merchantServiceChargeModel);

                // 添加代付订单
                OrderOutModel orderOutModel = HodgepodgeMethod.assembleOrderOutByJinFuAdd(orderOutJinFu, replacePayData, merchantList, channelModel, serviceCharge);
                int num = ComponentUtil.orderOutService.add(orderOutModel);
                HodgepodgeMethod.checkAddOrderOutIsOk(num);

                // 添加主动拉取订单结果的数据
                if (replacePayData.getGainDataType() == 2){
                    // 获取订单结果类型：1被动接收数据，2主动查询
                    ReplacePayGainModel replacePayGainModel = HodgepodgeMethod.assembleReplacePayGainAdd(orderOutModel, replacePayData);
                    ComponentUtil.replacePayGainService.add(replacePayGainModel);
                }

                // 存储redis：给出的代付ID，用于字段maxReplacePayId
                HodgepodgeMethod.saveMaxreplacePayByRedis(resourceType, replacePayData.getId());

                // 组装返回客户端的数据
                long stime = System.currentTimeMillis();
                String strData = HodgepodgeMethod.assembleOrderOutDataResult(stime, token, orderOutModel, requestModel.returnUrl);
                // 数据加密
                String encryptionData = StringUtil.mergeCodeBase64(strData);
                ResponseEncryptionJson resultDataModel = new ResponseEncryptionJson();
                resultDataModel.jsonData = encryptionData;
                // 返回数据给客户端
                return JsonResult.successResult(resultDataModel, cgid, sgid);
            }else{
                // 预备转账：需要先把订单数据存储到预付订单里面
                OrderOutPrepareModel orderOutPrepareAdd = HodgepodgeMethod.assembleOrderOutPrepareAdd(requestModel, channelModel, sgid, resourceType);
                int num  = ComponentUtil.orderOutPrepareService.add(orderOutPrepareAdd);
                if (num > 0){
                    return JsonResult.successResult(null, cgid, sgid);
                }else {
                    return JsonResult.failedResult(cgid, sgid);
                }
            }
        }catch (Exception e){
            Map<String,String> map = ExceptionMethod.getException(e, ServerConstant.PUBLIC_CONSTANT.SIZE_VALUE_TWO);
            log.error(String.format("this OrderOutController.huichaoQrCode() is error , the cgid=%s and sgid=%s and all data=%s!", cgid, sgid, data));
            if (!StringUtils.isBlank(map.get("dbCode"))){
                log.error(String.format("this OrderOutController.huichaoQrCode() is error codeInfo, the dbCode=%s and dbMessage=%s !", map.get("dbCode"), map.get("dbMessage")));
            }
            e.printStackTrace();
            return JsonResult.failedResult(map.get("message"), map.get("code"), cgid, sgid);
        }
    }


}
