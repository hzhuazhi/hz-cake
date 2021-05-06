package com.hz.cake.master.core.common.utils.constant;

/**
 * @author df
 * @Description:异常状态码
 * @create 2018-07-27 11:13
 **/
public class ErrorCode {

    /**
     * 常量异常
     */
    public final class ERROR_CONSTANT {
        /**
         * 没有被捕捉到的异常
         * 默认系统异常状态码=255
         */
        public static final String DEFAULT_EXCEPTION_ERROR_CODE = "255";

        /**
         * 没有被捕捉到的异常
         * 默认系统异常错误信息=SYS_ERROR
         */
        public static final String DEFAULT_EXCEPTION_ERROR_MESSAGE = "ERROR";

        /**
         * 被捕捉到的异常，并且捕捉的异常错误码为空，则默认异常状态码
         * 默认捕捉的异常状态码=256
         */
        public static final String DEFAULT_SERVICE_EXCEPTION_ERROR_CODE = "256";

        /**
         * 被捕捉到的异常，但是错误信息为空，则默认异常信息提醒
         * 默认捕捉的异常信息提醒=错误
         */
        public static final String DEFAULT_SERVICE_EXCEPTION_ERROR_MESSAGE = "错误";

    }


    /**
     * 异常-枚举类
     */
    public enum ENUM_ERROR {

        /***********************************************
         * OR打头表示订单的错误
         **********************************************/
        OR00001("OR00001", "错误,请重试!", "派发订单时,所有数据都为空!"),
        OR00002("OR00002", "错误,请重试!", "派发订单时,金额数据为空!"),
        OR00003("OR00003", "错误,请重试!", "派发订单时,支付类型数据为空!"),
        OR00004("OR00004", "错误,请重试!", "派发订单时,没有筛选出有效的收款账号!"),
        OR00005("OR00005", "错误,请重试!", "派发订单时,有效的用户集合数据为空!"),
        OR00006("OR00006", "金额小数点只能有2位!", "派发订单时,金额带的小数点位数错误!"),
        OR00007("OR00007", "请填写正常的数字金额!", "派发订单时,金额填写有误!"),
        OR00008("OR00008", "商户秘钥为空!", "派发订单时,商户秘钥为空!"),
        OR00009("OR00009", "错误,请重试!", "派发订单时,商户数据为空!"),
        OR00010("OR00010", "错误,请重试!", "派发订单时,卡商数据为空!"),
        OR00011("OR00011", "错误,请重试!", "派发订单时,手机卡数据为空!"),
        OR00012("OR00012", "错误,请重试!", "派发订单时,卡商的银行卡以及银行卡放量策略数据为空!"),
        OR00013("OR00013", "错误,请重试!", "派发订单时,未筛选出可用的银行卡!"),
        OR00014("OR00014", "错误,请重试!", "派发订单时,添加订单数据响应行为0!"),

        OR00015("OR00015", "错误,请重试!", "获取派单数据-详情-返回码的接口时,所有数据都为空!"),
        OR00016("OR00016", "错误,请重试!", "获取派单数据-详情-返回码的接口时,订单号数据都为空!"),
        OR00017("OR00017", "错误,请重试!", "获取派单数据-详情-返回码的接口时,获取短链API数据为空!"),

        OR00018("OR00018", "错误,请重试!", "派发订单时,此商户/渠道的银行卡绑定类型是需要绑定银行卡,但是部署配置未有绑定数据!"),
        OR00019("OR00019", "错误,请重试!", "派发订单时,此商户/渠道的银行卡绑定类型是无需绑定银行卡,但是池子中没有银行卡的部署数据!"),

        OR00020("OR00020", "错误,请重试!", "更新订单的转账用户的接口时,所有数据都为空!"),
        OR00021("OR00021", "错误,请重试!", "更新订单的转账用户的接口时,订单号数据都为空!"),
        OR00022("OR00022", "错误,请重试!", "更新订单的转账用户的接口时,转账用户输入长度超过设定长度!"),
        OR00023("OR00023", "请填写付款人姓名!", "更新订单的转账用户的接口时,付款人数据都为空!"),
        OR00024("OR00024", "频繁操作,请稍后再试!", "更新订单的转账用户的接口时,频繁操作!"),
        OR00025("OR00025", "订单已超时,请您重新拉单!", "更新订单的转账用户的接口时,订单已超时!"),
        OR00026("OR00026", "错误,请重试!", "更新订单的转账用户的接口时,根据订单号查询订单信息数据为空!"),





        /***********************************************
         * OU打头表示代付订单的错误
         **********************************************/
        OU00001("OU00001", "错误,请重试!", "代付订单时,所有数据都为空!"),
        OU00002("OU00002", "错误,请重试!", "派发订单时,金额数据为空!"),
        OU00003("OU00003", "金额小数点只能有2位!", "代付订单时,金额带的小数点位数错误!"),
        OU00004("OU00004", "请填写正常的数字金额!", "代付订单时,金额填写有误!"),
        OU00005("OU00005", "商户秘钥为空!", "代付订单时,商户秘钥为空!"),
        OU00006("OU00006", "错误,请重试!", "代付订单时,商户数据为空!"),
        OU00007("OU00007", "错误,请重试!", "代付订单时,商户与渠道关联关系数据为空!"),
        OU00008("OU00008", "错误,请重试!", "代付订单时,卡商数据为空!"),
        OU00009("OU00009", "错误,请重试!", "代付订单时,未筛选出可用的卡商!"),
        OU00010("OU00010", "错误,请重试!", "代付订单时,添加代付订单数据响应行为0!"),


        /***********************************************
         * I打头表示策略数据的错误
         **********************************************/
        I00001("I00001", "错误,请重试!", "获取下发数据-集合时,所有数据都为空!"),
        I00002("I00002", "错误,请重试!", "获取下发数据-详情时,所有数据都为空!"),

        I00003("I00003", "错误,请重试!", "新增下发数据时,支付平台订单号数据为空!"),
        I00004("I00004", "错误,请重试!", "新增下发数据时,订单金额数据为空!"),
        I00005("I00005", "金额小数点只能有2位!", "新增下发数据时,金额带的小数点位数错误!"),
        I00006("I00006", "请填写正常的数字金额!", "新增下发数据时,金额填写有误!"),
        I00007("I00007", "错误,请重试!", "新增下发数据时,银行名称数据为空!"),
        I00008("I00008", "错误,请重试!", "新增下发数据时,银行卡卡号数据为空!"),
        I00009("I00009", "错误,请重试!", "新增下发数据时,银行的开户人数据为空!"),
        I00010("I00010", "错误,请重试!", "新增下发数据时,上报的订单号已存在数据库,属于重复上报!"),

        I00011("I00011", "错误,请重试!", "更新下发审核状态时,所有数据都为空!"),
        I00012("I00012", "错误,请重试!", "更新下发审核状态时,支付平台订单号数据为空!"),
        I00013("I00013", "错误,请重试!", "更新下发审核状态时,审核状态数据为空!"),
        I00014("I00014", "错误,请重试!", "更新下发审核状态时,根据支付平台订单查询数据为空!"),
        I00015("I00015", "错误,请重试!", "更新下发审核状态时,下发的数据中,订单状态不是成功的,更新审核状态无效!"),
        I00016("I00016", "错误,请重试!", "更新下发审核状态时,下发数据中的审核状态已是审核成功状态,无需重复审核!"),
        I00017("I00017", "错误,请重试!", "更新下发审核状态时,下发数据中的分配归属状态未分配状态,无法进行审核操作!"),


        /***********************************************
         * S打头表示策略数据的错误
         **********************************************/
        S00001("S00001", "错误,请重试!", "出码开关策略数据为空!"),
        S00002("S00002", "目前出码处于关闭状态!", "目前出码处于关闭状态!"),
        S00003("S00003", "错误,请重试!", "出码开关时间具体值策略数据为空!"),
        S00004("S00004", "目前不在出码时间范围内!", "出码开关时间,目前不在出码时间范围内!"),

        S00005("S00005", "订单金额不符合出码金额范围!", "出码时,订单金额不符合出码金额范围内!"),

        S00006("S00006", "错误,请重试!", "出码开关策略数据为空!"),
        S00007("S00007", "目前出码处于关闭状态!", "目前出码处于关闭状态!"),
        S00008("S00008", "错误,请重试!", "出码开关时间具体值策略数据为空!"),
        S00009("S00009", "目前不在出码时间范围内!", "出码开关时间,目前不在出码时间范围内!"),

        S00010("S00010", "代付订单金额不符合出码金额范围!", "出码时,代付订单金额不符合出码金额范围内!"),


        /***********************************************
         * ST打头表示统计的错误
         **********************************************/
        ST00001("ST00001", "错误,请重试!", "添加支付用户点击支付页统计时,所有数据都为空!"),
        ST00002("ST00002", "错误,请重试!", "添加支付用户点击支付页统计时,标识值数据为空!"),
        ST00003("ST00003", "错误,请重试!", "添加支付用户点击支付页统计时,数据来源类型数据为空!"),




        ;

        /**
         * 错误码
         */
        private String eCode;
        /**
         * 给客户端看的错误信息
         */
        private String eDesc;
        /**
         * 插入数据库的错误信息
         */
        private String dbDesc;




        private ENUM_ERROR(String eCode, String eDesc,String dbDesc) {
            this.eCode = eCode;
            this.eDesc = eDesc;
            this.dbDesc  = dbDesc;
        }

        public String geteCode() {
            return eCode;
        }

        public void seteCode(String eCode) {
            this.eCode = eCode;
        }

        public String geteDesc() {
            return eDesc;
        }

        public void seteDesc(String eDesc) {
            this.eDesc = eDesc;
        }

        public String getDbDesc() {
            return dbDesc;
        }

        public void setDbDesc(String dbDesc) {
            this.dbDesc = dbDesc;
        }
    }
}
