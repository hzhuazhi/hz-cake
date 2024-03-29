package com.hz.cake.master.core.runner;

import com.hz.cake.master.core.common.redis.RedisIdService;
import com.hz.cake.master.core.common.redis.RedisService;
import com.hz.cake.master.core.common.utils.constant.LoadConstant;
import com.hz.cake.master.core.service.*;
import com.hz.cake.master.util.ComponentUtil;
import com.hz.cake.master.core.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
@Order(0)
public class AutowireRunner implements ApplicationRunner {
    private final static Logger log = LoggerFactory.getLogger(AutowireRunner.class);

    /**
     * 5分钟.
     */
    public long FIVE_MIN = 300;


    Thread runThread = null;

    @Autowired
    private RedisIdService redisIdService;
    @Autowired
    private RedisService redisService;

    @Resource
    private LoadConstant loadConstant;

    @Autowired
    private RegionService regionService;

    @Autowired
    private StrategyService strategyService;




    @Autowired
    private MobileCardService mobileCardService;

    @Autowired
    private MobileCardShortMsgService mobileCardShortMsgService;

    @Autowired
    private ShortMsgStrategyService shortMsgStrategyService;

    @Autowired
    private ShortMsgArrearsService shortMsgArrearsService;

    @Autowired
    private BankTypeService bankTypeService;

    @Autowired
    private BankService bankService;

    @Autowired
    private BankCollectionService bankCollectionService;

    @Autowired
    private BankStrategyService bankStrategyService;

    @Autowired
    private BankShortMsgStrategyService bankShortMsgStrategyService;

    @Autowired
    private BankShortMsgService bankShortMsgService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ChannelBankService channelBankService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private MerchantRechargeService merchantRechargeService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderReplenishService orderReplenishService;

    @Autowired
    private StatisticsClickPayService statisticsClickPayService;

    @Autowired
    private ShortChainService shortChainService;

    @Autowired
    private MobileCardHeartbeatService mobileCardHeartbeatService;

    @Autowired
    private IssueService issueService;

    @Autowired
    private MerchantChannelService merchantChannelService;

    @Autowired
    private OrderOutService orderOutService;

    @Autowired
    private MerchantBalanceDeductService merchantBalanceDeductService;

    @Autowired
    private MerchantServiceChargeService merchantServiceChargeService;

    @Autowired
    private ChannelBankPoolService channelBankPoolService;

    @Autowired
    private BankPoolService bankPoolService;

    @Autowired
    private StatisticsIpService statisticsIpService;

    @Autowired
    private ReplacePayService replacePayService;

    @Autowired
    private ReplacePayStrategyService replacePayStrategyService;

    @Autowired
    private ReplacePayInfoService replacePayInfoService;

    @Autowired
    private ReplacePayGainService replacePayGainService;

    @Autowired
    private ReplacePayGainResultService replacePayGainResultService;

    @Autowired
    private OrderOutPrepareService orderOutPrepareService;

    @Autowired
    private OrderOutLimitService orderOutLimitService;

    @Autowired
    private ZbWhitelistService zbWhitelistService;








    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("AutowireRunner ...");
        ComponentUtil.redisIdService = redisIdService;
        ComponentUtil.redisService = redisService;
        ComponentUtil.loadConstant = loadConstant;
        ComponentUtil.regionService = regionService;
        ComponentUtil.strategyService = strategyService;



        ComponentUtil.mobileCardService = mobileCardService;
        ComponentUtil.mobileCardShortMsgService = mobileCardShortMsgService;
        ComponentUtil.shortMsgStrategyService = shortMsgStrategyService;
        ComponentUtil.shortMsgArrearsService = shortMsgArrearsService;
        ComponentUtil.bankTypeService = bankTypeService;
        ComponentUtil.bankService = bankService;
        ComponentUtil.bankCollectionService = bankCollectionService;
        ComponentUtil.bankStrategyService = bankStrategyService;
        ComponentUtil.bankShortMsgStrategyService = bankShortMsgStrategyService;
        ComponentUtil.bankShortMsgService = bankShortMsgService;
        ComponentUtil.channelService = channelService;
        ComponentUtil.channelBankService = channelBankService;
        ComponentUtil.merchantService = merchantService;
        ComponentUtil.merchantRechargeService = merchantRechargeService;
        ComponentUtil.orderService = orderService;
        ComponentUtil.orderReplenishService = orderReplenishService;
        ComponentUtil.statisticsClickPayService = statisticsClickPayService;
        ComponentUtil.shortChainService = shortChainService;
        ComponentUtil.mobileCardHeartbeatService = mobileCardHeartbeatService;
        ComponentUtil.issueService = issueService;
        ComponentUtil.merchantChannelService = merchantChannelService;
        ComponentUtil.orderOutService = orderOutService;
        ComponentUtil.merchantBalanceDeductService = merchantBalanceDeductService;
        ComponentUtil.merchantServiceChargeService = merchantServiceChargeService;
        ComponentUtil.channelBankPoolService = channelBankPoolService;
        ComponentUtil.bankPoolService = bankPoolService;
        ComponentUtil.statisticsIpService = statisticsIpService;
        ComponentUtil.replacePayService = replacePayService;
        ComponentUtil.replacePayStrategyService = replacePayStrategyService;
        ComponentUtil.replacePayInfoService = replacePayInfoService;
        ComponentUtil.replacePayGainService = replacePayGainService;
        ComponentUtil.replacePayGainResultService = replacePayGainResultService;
        ComponentUtil.orderOutPrepareService = orderOutPrepareService;
        ComponentUtil.orderOutLimitService = orderOutLimitService;

        ComponentUtil.zbWhitelistService = zbWhitelistService;

        runThread = new RunThread();
        runThread.start();






    }

    /**
     * @author df
     * @Description: TODO(模拟请求)
     * <p>1.随机获取当日金额的任务</p>
     * <p>2.获取代码信息</p>
     * @create 20:21 2019/1/29
     **/
    class RunThread extends Thread{
        @Override
        public void run() {
            log.info("启动啦............");
        }
    }




}
