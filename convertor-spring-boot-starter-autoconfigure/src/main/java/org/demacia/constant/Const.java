package org.demacia.constant;

/**
 * @author hepenglin
 * @since 2024-07-20 15:46
 **/
public class Const {

    /**
     * 行号的键名。
     * <p>
     * 用于标识行号。
     */
    public static final String AUTO_INCREMENT_LINE_NO = "autoIncrementLineNo";

    /**
     * 转换后 Map 的键名。
     * <p>
     * 转换后 Map 的键名。
     */
    public static final String TARGET = "target";


    public static class RuleType {
        /**
         * 请求转换规则的 RULE_TYPE 字段
         * <p>
         * 用于标识标准。
         */
        public static final String REQ = "REQ";

        /**
         * 前置映射规则的 RULE_TYPE 字段
         * <p>
         * 用于获取仓库或货主编码。
         */
        public static final String PRE = "PRE";

        /**
         * DTO映射转换规则的 RULE_TYPE 字段
         * <p>
         * 用于标识存储转移。
         */
        public static final String DTO = "DTO";

        /**
         * 响应转换规则的 RULE_TYPE 字段
         * <p>
         * 用于标识响应。
         */
        public static final String RSP = "RSP";

        /**
         * 请求路径转换规则的 RULE_TYPE 字段
         * <p>
         * 用于标识请求路径参数。
         */
        public static final String URL = "URL";

        /**
         * 请求头转换规则的 RULE_TYPE 字段
         * <p>
         * 用于标识请求头参数。
         */
        public static final String RHD = "RHD";
    }

    /**
     * 签名方式。
     * <p>
     * 签名方式。
     */
    public static class SignMethod {
        public static final String NOT_REQUIRED = "";
    }

    /**
     * 成功状态
     * <p>
     * 用于标识成功状态。
     */
    public static final String SUCCESS = "Y";

    /**
     * 失败状态
     * <p>
     * 用于标识失败状态。
     */
    public static final String FAILURE = "N";

    /**
     * 外部单号
     * <p>
     * 外部单号
     */
    public static final String OUTER_NO = "outerNo";

    /**
     * 接收使用消息类型
     * <p>
     * 接收使用消息类型
     */
    public static class MsgFormat {
        public static final String JSON = "json";
        public static final String XML = "xml";
    }

    /**
     * 下发使用消息类型
     * <p>
     * 下发使用消息类型
     */
    public static class MessageFormat {
        public static final String JSON = "json";
        public static final String XML = "xml";
        public static final String FORM = "form";

    }

    /**
     * 调用类型
     * <p>
     * 1-接收，2-发送
     */
    public static class Direction {
        public static final int RECEIVE = 1;
        public static final int SEND = 2;
    }

    /**
     * 场景
     * <p>
     * 场景
     */
    public static class Scene {
        public static final int ERP = 0;
        public static final int WMS = 1;
        public static final int TMS = 2;
        public static final int BMS = 3;
    }

    /**
     * 是否必填
     * <p>
     * 是否必填
     */
    public static class Requirement {
        public static final String OPTIONAL = "0";
        public static final String REQUIRED = "1";
    }

    /**
     * 脚本类型
     * <p>
     * 脚本类型
     */
    public static class DmlType {
        public static final String INSERT = "1";
        public static final String SPECIFIC = "2";
    }
}
