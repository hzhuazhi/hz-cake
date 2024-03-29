package com.hz.cake.master.util;


import com.hz.cake.master.core.common.redis.RedisIdService;
import com.hz.cake.master.core.common.redis.RedisService;
import com.hz.cake.master.core.common.utils.constant.LoadConstant;
import com.hz.cake.master.core.service.*;
import com.hz.cake.master.core.service.*;

/**
 * 工具类
 */
public class ComponentUtil {
    public static RedisIdService redisIdService;
    public static RedisService redisService;
    public static LoadConstant loadConstant;
    public static RegionService regionService;
    public static StrategyService strategyService;


    public static MobileCardService mobileCardService;
    public static MobileCardShortMsgService mobileCardShortMsgService;
    public static ShortMsgStrategyService shortMsgStrategyService;
    public static ShortMsgArrearsService shortMsgArrearsService;
    public static BankTypeService bankTypeService;
    public static BankService bankService;
    public static BankCollectionService bankCollectionService;
    public static BankStrategyService bankStrategyService;
    public static BankShortMsgStrategyService bankShortMsgStrategyService;
    public static BankShortMsgService bankShortMsgService;
    public static ChannelService channelService;
    public static ChannelBankService channelBankService;
    public static MerchantService merchantService;
    public static MerchantRechargeService merchantRechargeService;
    public static OrderService orderService;
    public static OrderReplenishService orderReplenishService;
    public static StatisticsClickPayService statisticsClickPayService;
    public static ShortChainService shortChainService;
    public static MobileCardHeartbeatService mobileCardHeartbeatService;
    public static IssueService issueService;
    public static MerchantChannelService merchantChannelService;
    public static OrderOutService orderOutService;
    public static MerchantBalanceDeductService merchantBalanceDeductService;
    public static MerchantServiceChargeService merchantServiceChargeService;
    public static ChannelBankPoolService channelBankPoolService;
    public static BankPoolService bankPoolService;
    public static StatisticsIpService statisticsIpService;
    public static ReplacePayService replacePayService;
    public static ReplacePayStrategyService replacePayStrategyService;
    public static ReplacePayInfoService replacePayInfoService;
    public static ReplacePayGainService replacePayGainService;
    public static ReplacePayGainResultService replacePayGainResultService;
    public static OrderOutPrepareService orderOutPrepareService;
    public static OrderOutLimitService orderOutLimitService;

    public static ZbWhitelistService zbWhitelistService;


}
