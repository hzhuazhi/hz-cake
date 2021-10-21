package com.hz.cake.master.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.hz.cake.master.core.common.dao.BaseDao;
import com.hz.cake.master.core.common.exception.ServiceException;
import com.hz.cake.master.core.common.service.impl.BaseServiceImpl;
import com.hz.cake.master.core.common.utils.constant.CacheKey;
import com.hz.cake.master.core.common.utils.constant.CachedKeyUtils;
import com.hz.cake.master.core.common.utils.jinfupay.JinFuApi;
import com.hz.cake.master.core.common.utils.jinfupay.model.JinFuPayResponse;
import com.hz.cake.master.core.common.utils.sandpay.method.AgentPay;
import com.hz.cake.master.core.common.utils.sandpay.model.AgentPayResponse;
import com.hz.cake.master.core.mapper.MerchantBalanceDeductMapper;
import com.hz.cake.master.core.mapper.MerchantMapper;
import com.hz.cake.master.core.mapper.OrderOutMapper;
import com.hz.cake.master.core.model.channel.ChannelModel;
import com.hz.cake.master.core.model.merchant.MerchantBalanceDeductModel;
import com.hz.cake.master.core.model.merchant.MerchantModel;
import com.hz.cake.master.core.model.order.OrderOutModel;
import com.hz.cake.master.core.model.replacepay.ReplacePayModel;
import com.hz.cake.master.core.protocol.request.order.ProtocolOrderOut;
import com.hz.cake.master.core.service.OrderOutService;
import com.hz.cake.master.util.ComponentUtil;
import com.hz.cake.master.util.HodgepodgeMethod;
import com.hz.cake.master.util.TaskMethod;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description 代付订单的Service层的实现层
 * @Author yoko
 * @Date 2020/10/29 17:47
 * @Version 1.0
 */
@Service
public class OrderOutServiceImpl<T> extends BaseServiceImpl<T> implements OrderOutService<T> {
    private static Logger log = LoggerFactory.getLogger(OrderOutServiceImpl.class);

    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;

    /**
     * 3分钟
     */
    public long THREE_MIN = 180;

    /**
     * 11分钟.
     */
    public long ELEVEN_MIN = 660;

    public long TWO_HOUR = 2;



    @Autowired
    private OrderOutMapper orderOutMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private MerchantBalanceDeductMapper merchantBalanceDeductMapper;

    public BaseDao<T> getDao() {
        return orderOutMapper;
    }

    @Override
    public MerchantModel screenMerchantByMoney(List<MerchantModel> merchantList, String orderMoney, String orderNo) throws Exception{
        MerchantModel merchantModel = null;
        for (MerchantModel merchantData : merchantList){
            merchantModel = getMerchantData(merchantData, orderMoney, orderNo);
            if (merchantModel != null && merchantModel.getId() != null && merchantModel.getId() > 0){
                return merchantModel;
            }
        }
        return null;
    }

    @Override
    public ReplacePayModel screenReplacePay(List<ReplacePayModel> replacePayList, List<MerchantModel> merchantList, OrderOutModel orderOutModel) throws Exception {
        ReplacePayModel replacePayModel = null;
        for (ReplacePayModel replacePayData : replacePayList){
            // 删选代付，调用衫德代付
            replacePayModel = getReplacePayData(replacePayData, orderOutModel);
            if (replacePayModel != null && replacePayModel.getId() != null && replacePayModel.getId() > 0){
                // 扣除此卡商的余额
                for (MerchantModel merchantData : merchantList){
                    if (merchantData.getId() == replacePayModel.getMerchantId()){
                        getMerchantData(merchantData, orderOutModel.getOrderMoney(), orderOutModel.getOrderNo());
                    }
                }
                return replacePayModel;
            }
        }

        return null;
    }


    /**
     * @Description: check校验被筛选的商户的使用状态
     * @param merchantModel - 银行卡数据
     * @param orderMoney - 订单金额
     * @param orderNo - 订单号
     * @return BankModel
     * @author yoko
     * @date 2020/9/12 21:02
     */
    public MerchantModel getMerchantData(MerchantModel merchantModel, String orderMoney, String orderNo) throws Exception{
        // 判断此卡商是否被锁住-正要使用锁
        String lockKey_merchant = CachedKeyUtils.getCacheKey(CacheKey.LOCK_MERCHANT, merchantModel.getId());
        boolean flagLock_merchant = ComponentUtil.redisIdService.lock(lockKey_merchant);
        if (flagLock_merchant){

            // 锁住此卡商-更改金额锁
            String lockKey_merchant_money = CachedKeyUtils.getCacheKey(CacheKey.LOCK_MERCHANT_MONEY, merchantModel.getId());
            boolean flagLock_merchant_money = ComponentUtil.redisIdService.lock(lockKey_merchant_money);
            if (flagLock_merchant_money){

                // 组装要更扣减卡商余额的方法
                MerchantModel updateBalance = HodgepodgeMethod.assembleMerchantByChanagerBalance(merchantModel.getId(), 2, orderMoney);

                // 组装添加卡商扣款流水
                MerchantBalanceDeductModel merchantBalanceDeductModel = HodgepodgeMethod.assembleMerchantBalanceDeduct(0, merchantModel.getId(), orderNo, 2, orderMoney,
                        0, null, null, 2);
                boolean flag = handleMerchantMoneyByOutOrder(updateBalance, merchantBalanceDeductModel);
                if (!flag){
                    // 解锁
                    ComponentUtil.redisIdService.delLock(lockKey_merchant_money);
                    return null;
                }
                // 解锁
                ComponentUtil.redisIdService.delLock(lockKey_merchant_money);

                // 解锁
                ComponentUtil.redisIdService.delLock(lockKey_merchant);
                return merchantModel;
            }
            // 解锁
            ComponentUtil.redisIdService.delLock(lockKey_merchant);
        }
        return null;
    }


    /**
     * @Description: 代付订单，派单给卡商要执行的SQL
     * <p>
     *     1.更新卡商余额。
     *     2.添加卡商余额扣减流水
     * </p>
     * @param updateBalance - 卡商要更新的余额
     * @param merchantBalanceDeductModel - 卡商 要添加的扣减流水
     * @return 
     * @author yoko
     * @date 2020/10/30 20:48 
    */
    @Transactional(rollbackFor=Exception.class)
    public boolean handleMerchantMoneyByOutOrder(MerchantModel updateBalance, MerchantBalanceDeductModel merchantBalanceDeductModel) throws Exception {
        int num1 = 0;
        int num2 = 0;

        num1 = merchantMapper.updateBalance(updateBalance);
        num2 = merchantBalanceDeductMapper.add(merchantBalanceDeductModel);
        if (num1> 0 && num2 >0){
            return true;
        }else {
            throw new ServiceException("handleMerchantMoneyByOutOrder", "二个执行更新SQL其中有一个或者多个响应行为0");
//                throw new RuntimeException();
        }
    }




    /**
     * @Description: check校验被筛选的代付的使用状态
     * @param replacePayModel - 代付信息
     * @param orderOutModel - 订单信息
     * @return BankModel
     * @author yoko
     * @date 2020/9/12 21:02
     */
    public ReplacePayModel getReplacePayData(ReplacePayModel replacePayModel, OrderOutModel orderOutModel) throws Exception{
        // 判断此代付是否被锁住
        String lockKey_replacePay = CachedKeyUtils.getCacheKey(CacheKey.LOCK_REPLACE_PAY, replacePayModel.getId());
        boolean flagLock_replacePay = ComponentUtil.redisIdService.lock(lockKey_replacePay);
        if (flagLock_replacePay){
            // 校验代付是否受到付款限制
            boolean flag = checkReplacePayLimit(replacePayModel);
            if (flag){
                if (!StringUtils.isBlank(replacePayModel.getOpenTimeSlot())){
                    // 校验代付的放量时间
                    boolean flag_openTime = HodgepodgeMethod.checkOpenTimeSlot(replacePayModel.getOpenTimeSlot());
                    if (flag_openTime){
                        // 请求衫德代付
                        AgentPayResponse sandResponse = AgentPay.sandAgentPay(replacePayModel, orderOutModel);
                        if (sandResponse != null && sandResponse.respCode.equals("0000")){
                            log.info("OrderOutServiceImpl.getReplacePayData().sandResponse:" + JSON.toJSONString(sandResponse));

//                            // 金额还原
//                            if (orderOutModel.getOrderMoney().length()==12){
//                                orderOutModel.setOrderMoney(HodgepodgeMethod.sandPayBalance(orderOutModel.getOrderMoney()));
//                            }
                            // 解锁
                            ComponentUtil.redisIdService.delLock(lockKey_replacePay);
                            return replacePayModel;
                        }
                    }
                }

            }
            // 解锁
            ComponentUtil.redisIdService.delLock(lockKey_replacePay);
        }
        return null;
    }







    /**
     * @Description: check校验代付限量
     * @param replacePayModel - 代付信息
     * @return
     * @author yoko
     * @date 2020/9/12 21:19
     */
    public boolean checkReplacePayLimit(ReplacePayModel replacePayModel){
        boolean flag = true;
        String dayMoney = getRedisDataByKey(CacheKey.OUT_DAY_MONEY, replacePayModel.getId());
        if (!StringUtils.isBlank(dayMoney)){
            return false;
        }
        String monthMoney = getRedisDataByKey(CacheKey.OUT_MONTH_MONEY, replacePayModel.getId());
        if (!StringUtils.isBlank(monthMoney)){
            return false;
        }
        String dayNum = getRedisDataByKey(CacheKey.OUT_DAY_NUM, replacePayModel.getId());
        if (!StringUtils.isBlank(dayNum)){
            return false;
        }
        return flag;
    }


    /**
     * @Description: 组装缓存key查询缓存中存在的数据
     * @param cacheKey - 缓存的类型key
     * @param obj - 数据的ID
     * @return
     * @author yoko
     * @date 2020/5/20 14:59
     */
    public String getRedisDataByKey(String cacheKey, Object obj){
        String str = null;
        String strKeyCache = CachedKeyUtils.getCacheKey(cacheKey, obj);
        String strCache = (String) ComponentUtil.redisService.get(strKeyCache);
        if (StringUtils.isBlank(strCache)){
            return str;
        }else{
            str = strCache;
            return str;
        }
    }




    @Override
    public ReplacePayModel screenReplacePayJinFu(List<ReplacePayModel> replacePayList, List<MerchantModel> merchantList, OrderOutModel orderOutModel) throws Exception {
        ReplacePayModel replacePayModel = null;
        for (ReplacePayModel replacePayData : replacePayList){
            // 删选代付，调用金服代付
            replacePayModel = getReplacePayDataJinFu(replacePayData, orderOutModel);
            if (replacePayModel != null && replacePayModel.getId() != null && replacePayModel.getId() > 0){
                log.info("");
                // 扣除此卡商的余额
                for (MerchantModel merchantData : merchantList){
                    if (merchantData.getId() == replacePayModel.getMerchantId()){
                        getMerchantData(merchantData, orderOutModel.getOrderMoney(), orderOutModel.getOrderNo());
                    }
                }
                return replacePayModel;
            }
        }

        return null;
    }




    /**
     * @Description: check校验被筛选的代付的使用状态-金服
     * @param replacePayModel - 代付信息
     * @param orderOutModel - 订单信息
     * @return BankModel
     * @author yoko
     * @date 2020/9/12 21:02
     */
    public ReplacePayModel getReplacePayDataJinFu(ReplacePayModel replacePayModel, OrderOutModel orderOutModel) throws Exception{
        // 判断此代付是否被锁住
        String lockKey_replacePay = CachedKeyUtils.getCacheKey(CacheKey.LOCK_REPLACE_PAY, replacePayModel.getId());
        boolean flagLock_replacePay = ComponentUtil.redisIdService.lock(lockKey_replacePay);
        if (flagLock_replacePay){
            // 校验代付是否受到付款限制
            boolean flag = checkReplacePayLimit(replacePayModel);
            if (flag){
                if (!StringUtils.isBlank(replacePayModel.getOpenTimeSlot())){
                    // 校验代付的放量时间
                    boolean flag_openTime = HodgepodgeMethod.checkOpenTimeSlot(replacePayModel.getOpenTimeSlot());
                    if (flag_openTime){
                        // 请求金服代付
                        JinFuPayResponse jinFuPayResponse = JinFuApi.jinFuPay(replacePayModel, orderOutModel);
                        if (jinFuPayResponse != null && jinFuPayResponse.code == 0){
                            // 解锁
                            ComponentUtil.redisIdService.delLock(lockKey_replacePay);
                            return replacePayModel;
                        }
                    }
                }

            }
            // 解锁
            ComponentUtil.redisIdService.delLock(lockKey_replacePay);
        }
        return null;
    }






}
