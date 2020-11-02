package com.hz.cake.master.core.controller.order;

import com.alibaba.fastjson.JSON;
import com.hz.cake.master.core.common.exception.ExceptionMethod;
import com.hz.cake.master.core.common.utils.JsonResult;
import com.hz.cake.master.core.common.utils.StringUtil;
import com.hz.cake.master.core.common.utils.constant.ServerConstant;
import com.hz.cake.master.core.model.RequestEncryptionJson;
import com.hz.cake.master.core.model.ResponseEncryptionJson;
import com.hz.cake.master.core.model.bank.BankModel;
import com.hz.cake.master.core.model.channel.ChannelBankModel;
import com.hz.cake.master.core.model.channel.ChannelModel;
import com.hz.cake.master.core.model.merchant.MerchantChannelModel;
import com.hz.cake.master.core.model.merchant.MerchantModel;
import com.hz.cake.master.core.model.order.OrderModel;
import com.hz.cake.master.core.model.order.OrderOutModel;
import com.hz.cake.master.core.model.region.RegionModel;
import com.hz.cake.master.core.model.strategy.StrategyData;
import com.hz.cake.master.core.model.strategy.StrategyModel;
import com.hz.cake.master.core.protocol.request.order.ProtocolOrder;
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
        String sgid = "DF" + ComponentUtil.redisIdService.getNewId();
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

            // 添加代付订单
            OrderOutModel orderOutModel = HodgepodgeMethod.assembleOrderOutAdd(merchantData, requestModel, channelModel, sgid);
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

}
