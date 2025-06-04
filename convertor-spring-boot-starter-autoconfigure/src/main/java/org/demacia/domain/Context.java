package org.demacia.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 上下文对象
 * 用于在系统中传递和处理请求过程中所需的各类信息
 *
 * @author hepenglin
 * @since 2024-07-20 10:09
 **/
@Data
public class Context {

    /**
     * 调用类型。
     * <p>
     * 1-接收，2-发送。
     */
    private Integer callType;

    /**
     * 是否直接调用。
     * <p>
     * 默认值为 {@code true}，用于区分是否记录日志和清除上下文。
     */
    private boolean directCall = true;

    /**
     * 标志是否内部调用。
     * <p>
     * 默认值为 {@code false}，表示外部接口调用。
     */
    private boolean internalRetry = false;

    /**
     * 业务规则ID。
     * <p>
     * 用于标识当前上下文处理所依据的业务规则。
     */
    private String ruleCode;

    /**
     * 路径参数。
     * <p>
     * 采用 Map 形式存储，便于灵活处理各类请求参数。
     */
    private Map<String, ?> queryParams;

    /**
     * 请求头。
     * <p>
     * 采用 Map 形式存储，便于灵活处理各类请求参数。
     */
    private Map<String, ?> headers;

    /**
     * 请求参数。
     * <p>
     * 采用 Map 形式存储，便于灵活处理各类请求参数。
     */
    private Map<String, Object> params;

    /**
     * 请求报文。
     * <p>
     * 采用 String 形式存储，便于灵活处理各类请求参数。
     */
    private String reqMsg;

    /**
     * 转换后的Map，表单提交时。
     * <p>
     * 采用 Map 形式存储，便于灵活处理各类请求参数。
     */
    private Map<String, Object> target;

    /**
     * 响应报文。
     * <p>
     * 采用 String 形式存储，便于灵活处理各类请求参数。
     */
    private String rspMsg;

    /**
     * 接口重放保存参数。
     * <p>
     * 采用 JSON 格式存储。
     */
    private String retryParams;

    /**
     * 请求数据。
     * <p>
     * 请求数据。
     */
    private Req req;

    /**
     * OMS标准响应结果。
     * <p>
     * 临时添加、适配原来接口、后续可以删除。
     */
    private Rsp rsp;

    /**
     * 订单数据。
     * <p>
     * 用于存储与当前请求相关的订单信息。
     */
    private Pre pre;

    /**
     * 临时数据。
     * <p>
     * target中包含 #号时，将当前值放入temp
     */
    private Map<String, Object> temp = new HashMap<>();

    /**
     * 解析结果。
     * <p>
     * 采用 LinkedList 存储，用于存放解析过程中的各类结果数据。
     */
    private LinkedList<Object> parseResults = new LinkedList<>();

    /**
     * API 应用信息。
     * <p>
     * 存储当前请求对应 API 应用的详细信息。
     */
    private App app;

    /**
     * API 服务信息。
     * <p>
     * 存储当前请求对应 API 服务的详细信息。
     */
    private Api api;
}
