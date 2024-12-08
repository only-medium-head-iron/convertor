package org.demacia.send;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.demacia.Convertor;
import org.demacia.mapper.RuleMapper;
import org.demacia.constant.Const;
import org.demacia.domain.ApiApp;
import org.demacia.domain.ApiService;
import org.demacia.domain.Context;
import org.demacia.rule.RuleMapping;
import org.demacia.step.Step;
import org.demacia.util.MessageFormatter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author hepenglin
 * @since 2024-08-08 15:31
 **/
@Slf4j
public abstract class AbstractSendHandler implements SendHandler {

    @Resource
    private Convertor convertor;

    @Resource
    private RuleMapper ruleMapper;

    private LinkedList<Step> steps;

    /**
     * 处理上下文对象，在转换前后执行特定操作
     *
     * @param context 上下文对象，包含需要转换的数据和规则信息
     */
    @Override
    public void handle(Context context) {
        // 转换前调用前置处理方法，默认实现查找货主仓库等信息
        beforeConvert(context);

        // 通过字段映射方式转换报文转换
        Object object = convertor.convert(context.getRuleCode(), BeanUtil.beanToMap(context));

        // 根据配置的消息格式格式化请求消息
        formatRequestMessage(context, object);

        // 转换后调用后置处理方法，默认实现推送处理逻辑
        afterConvert(context);

        // 返回结果后调用处理方法，可以根据请求响应结果做相应的处理
        afterReturn(context);
    }

    /**
     * 格式化请求消息，根据消息类型确定消息格式，并将格式化后的消息设置到上下文对象中
     * @param context 上下文对象，包含转换所需的数据
     * @param object 转换后的对象
     */
    private void formatRequestMessage(Context context, Object object) {
        if (ObjectUtil.isEmpty(object)) {
            return;
        }
        // 可以替换成 ObjectMapper.convertValue(object, Map.class)，进行逐级转换
        Map<String, Object> parseResult = BeanUtil.beanToMap(object);
        context.setTarget(parseResult);
        ApiService apiService = context.getApiService();
        String messageFormat = apiService.getMessageFormat();
        String reqMsg = MessageFormatter.determineMsgFormat(parseResult, messageFormat);
        context.setReqMsg(reqMsg);
    }

    /**
     * 初始化步骤链，将步骤添加到链表中，以确定转换的顺序
     */
    @PostConstruct
    public void init() {
        this.steps = CollUtil.newLinkedList();
    }

    /**
     * 在转换过程开始之前执行一系列步骤
     *
     * @param context 转换上下文，包含可能需要传递给各个步骤的必要信息
     */
    public void beforeConvert(Context context) {
        for (Step step : this.steps) {
            step.run(context);
        }
    }

    /**
     * 在对象转换后执行操作
     * 此方法的目的是将对象转换为指定格式的消息，并通过HTTP/HTTPS发送
     * 它首先将对象解析为map格式，然后根据消息类型确定消息格式
     * 接着，它会构建URL和请求消息，最后通过HTTP/HTTPS客户端发送请求并处理响应
     *
     * @param context 上下文对象，包含API服务、应用等信息
     */
    public void afterConvert(Context context) {
        Map<String, Object> pathParams = buildPathParams(context);
        Map<String, String> headers = buildHeaders(context);
        String query = URLUtil.buildQuery(pathParams, StandardCharsets.UTF_8);
        ApiApp apiApp = context.getApiApp();
        String pushUrl = apiApp.getUrl();
        ApiService apiService = context.getApiService();
        String uri = apiService.getUri();
        uri = StrUtil.isBlank(uri) ? "" : uri;
        String url = pushUrl + uri + (StrUtil.isBlank(query) ? "" : "?" + query);
        // TODO 更改为 OkHttpUtil
        HttpRequest httpRequest = HttpUtil.createPost(url);
        if (Const.MessageFormat.FORM.equals(apiService.getMessageFormat())) {
            // 表单提交
            httpRequest.form(context.getTarget());
        } else {
            httpRequest.body(context.getReqMsg());
        }
        httpRequest.addHeaders(headers);
        log.info("Begin request full URL: {}, headers: {}, request body: {}", url, headers, context.getReqMsg());
        String rspMsg;
        try (HttpResponse httpResponse = httpRequest.execute()) {
            rspMsg = httpResponse.body();
        }
        log.info("End request response body: {}", rspMsg);
        context.setRspMsg(rspMsg);
    }

    public void afterReturn(Context context) {
    }

    /**
     * 构建路径参数
     * 根据给定的上下文对象构建路径参数，这包括从数据库获取规则映射，检查其是否存在，
     * 并将这些规则转换为路径参数格式这个方法主要用于API应用中，帮助生成特定的路径参数，
     * 以便在API请求中使用
     *
     * @param context 上下文对象，包含API应用相关信息
     * @return Map<String, Object> 返回构建的路径参数，如果无规则，则返回空地图
     */
    public Map<String, Object> buildPathParams(Context context) {
        ApiApp apiApp = context.getApiApp();
        List<RuleMapping> rules = ruleMapper.getMappingRulesByRuleCode(Const.RuleType.URL, apiApp.getAppCode());
        if (CollUtil.isEmpty(rules)) {
            log.info("没有找到路径映射规则：{}", apiApp.getAppCode());
            return MapUtil.empty();
        }
        LinkedHashMap<String, Object> pathParams = new LinkedHashMap<>(16);
        context.setQueryParams(pathParams);
        convertor.parseMappingRules(BeanUtil.beanToMap(context), pathParams, rules);
        return pathParams;
    }

    /**
     * 构建请求头参数
     * 根据给定的上下文对象构建请求头参数，这包括从数据库获取规则映射，检查其是否存在，
     * 并将这些规则转换为请求头格式这个方法主要用于API应用中，帮助生成特定的请求头，
     * 以便在API请求中使用
     *
     * @param context 上下文对象，包含API应用相关信息
     * @return Map<String, Object> 返回构建的请求头，如果无规则，则返回空
     */
    public Map<String, String> buildHeaders(Context context) {
        ApiApp apiApp = context.getApiApp();
        List<RuleMapping> rules = ruleMapper.getMappingRulesByRuleCode(Const.RuleType.RHD, apiApp.getAppCode());
        if (CollUtil.isEmpty(rules)) {
            log.info("没有找到请求头映射规则：{}", apiApp.getAppCode());
            return MapUtil.empty();
        }
        LinkedHashMap<String, Object> targetMap = new LinkedHashMap<>(16);
        LinkedHashMap<String, String> headers = new LinkedHashMap<>(16);
        convertor.parseMappingRules(BeanUtil.beanToMap(context), targetMap, rules);
        targetMap.forEach((k, v) -> headers.put(k, v != null ? v.toString() : null));
        context.setHeaders(headers);
        return headers;
    }
}
