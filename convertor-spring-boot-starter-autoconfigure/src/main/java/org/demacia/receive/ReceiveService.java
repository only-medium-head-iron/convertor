package org.demacia.receive;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.demacia.AbstractService;
import org.demacia.Convertor;
import org.demacia.mapper.AppMapper;
import org.demacia.mapper.ApiMapper;
import org.demacia.mapper.RuleMapper;
import org.demacia.constant.Const;
import org.demacia.enums.ResultCode;
import org.demacia.domain.*;
import org.demacia.exception.ConvertException;
import org.demacia.rule.RuleMapping;
import org.demacia.util.JacksonUtil;
import org.demacia.util.MessageFormatter;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.*;

/**
 * 外部应用请求处理服务
 *
 * @author hepenglin
 * @since 2024-08-04 17:25
 */
@Slf4j
@Service
public class ReceiveService extends AbstractService {

    private static final String RSP_WRAPPER_KEY = "response";
    private static final int DEFAULT_MAP_CAPACITY = 16;

    @Resource
    private Convertor convertor;

    @Resource
    private RuleMapper ruleMapper;

    @Resource
    private AppMapper appMapper;

    @Resource
    private ApiMapper apiMapper;

    /**
     * 处理外部应用的请求
     *
     * @param receiveRequest 请求参数
     * @return 响应消息
     */
    public String receive(ReceiveRequest receiveRequest) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Rsp rsp = Rsp.success();
        Context context = new Context();
        ContextHolder.set(context);

        String rspMsg;

        try {
            initializeRequest(context, receiveRequest);
            processRequest(context);
            rsp.setSuccess(true);
            rspMsg = buildSuccessResponse(context, rsp);
        } catch (ConvertException e) {
            log.error("外部请求处理失败：{}", e.getMessage(), e);
            rsp.setCode(e.getCode());
            rsp.setMessage(e.toString());
            rsp.setMessageExternal(e.getMessage());
            rspMsg = buildErrorResponse(context, rsp);
        } catch (Exception e) {
            log.error("外部请求处理失败：{}", e.getMessage(), e);
            rsp.setCode(ResultCode.FAILURE.getCode());
            rsp.setMessage(e.toString());
            rsp.setMessageExternal("请求失败，请联系相关人员告知原因！");
            rspMsg = buildErrorResponse(context, rsp);
        } finally {
            recordLogAndClearContext(context);
        }

        stopWatch.stop();
        log.info("处理外部请求耗时：{} 毫秒", stopWatch.getTotalTimeMillis());
        return rspMsg;
    }

    /**
     * 构建成功响应
     */
    private String buildSuccessResponse(Context context, Rsp rsp) {
        context.setRsp(rsp);
        try {
            Map<String, Object> parseResult = parseResponse(context, rsp);
            String messageFormat = Optional.ofNullable(context.getReq())
                    .map(Req::getFormat)
                    .orElse(Const.MsgFormat.JSON);

            String rspMsg = MessageFormatter.determineMsgFormat(parseResult, messageFormat);
            context.setRspMsg(rspMsg);
            return rspMsg;
        } catch (Exception e) {
            log.error("响应解析失败：{}", e.getMessage(), e);
            rsp.setSuccess(false);
            rsp.setMessage(e.toString());
            return JSONUtil.toJsonStr(DefaultResponse.failure(rsp.getCode(), rsp.getMessageExternal()));
        }
    }

    /**
     * 构建错误响应
     */
    private String buildErrorResponse(Context context, Rsp rsp) {
        context.setRsp(rsp);
        try {
            Map<String, Object> parseResult = parseResponse(context, rsp);
            String messageFormat = Optional.ofNullable(context.getReq())
                    .map(Req::getFormat)
                    .orElse(Const.MsgFormat.JSON);

            return MessageFormatter.determineMsgFormat(parseResult, messageFormat);
        } catch (Exception e) {
            log.error("错误响应解析失败：{}", e.getMessage(), e);
            return JSONUtil.toJsonStr(DefaultResponse.failure(rsp.getCode(), rsp.getMessageExternal()));
        }
    }

    /**
     * 初始化请求上下文
     */
    private void initializeRequest(Context context, ReceiveRequest receiveRequest) {
        context.setDirection(Const.Direction.RECEIVE);
        context.setRetryParams(JacksonUtil.toJson(receiveRequest));
        BeanUtil.copyProperties(receiveRequest, context);

        setApp(context, receiveRequest.getAppCode());
        setParams(context, receiveRequest.getReqMsg());
        setReq(context, receiveRequest.getAppCode());

        String apiCode = Optional.ofNullable(context.getReq())
                .map(Req::getApiCode)
                .orElseThrow(() -> new ConvertException("接口编码不能为空"));

        context.setRuleCode(receiveRequest.getAppCode() + StrUtil.DASHED + apiCode);
        setPre(context);
        setApi(context, apiCode);
    }

    /**
     * 处理请求
     */
    private void processRequest(Context context) {
        validateRequest(context);
        ReceiveHandler receiveHandler = determineWhichHandler(context);
        receiveHandler.handle(context);
    }

    /**
     * 根据上下文获取接收处理程序实例
     */
    private ReceiveHandler determineWhichHandler(Context context) {
        String handlerBeanName = Optional.ofNullable(context.getApi())
                .map(Api::getHandlerClass)
                .orElseThrow(() -> new ConvertException("API处理器配置不能为空"));

        try {
            return SpringUtil.getBean(StrUtil.lowerFirst(handlerBeanName));
        } catch (NoSuchBeanDefinitionException e) {
            throw new ConvertException("没有找到对应的处理器: " + handlerBeanName);
        }
    }

    /**
     * 设置请求对象Req
     */
    private void setReq(Context context, String appCode) {
        List<RuleMapping> rules = ruleMapper.getMappingRulesByRuleCode(Const.RuleType.REQ, appCode);
        if (CollUtil.isEmpty(rules)) {
            log.warn("没有找到请求映射规则：{}", appCode);
            return;
        }

        LinkedHashMap<String, Object> reqMap = new LinkedHashMap<>(DEFAULT_MAP_CAPACITY);
        convertor.parseMappingRules(BeanUtil.beanToMap(context), reqMap, rules);
        context.setReq(BeanUtil.toBean(reqMap, Req.class));
    }

    /**
     * 设置前置信息
     */
    private void setPre(Context context) {
        List<RuleMapping> rules = ruleMapper.getMappingRulesByRuleCode(Const.RuleType.PRE, context.getRuleCode());
        if (CollUtil.isEmpty(rules)) {
            log.warn("没有找到前置映射规则：{}", context.getRuleCode());
            return;
        }

        LinkedHashMap<String, Object> preMap = new LinkedHashMap<>(DEFAULT_MAP_CAPACITY);
        convertor.parseMappingRules(BeanUtil.beanToMap(context), preMap, rules);
        context.setPre(BeanUtil.toBean(preMap, Pre.class));
    }

    /**
     * 设置请求参数
     */
    private void setParams(Context context, String reqMsg) {
        Map<String, Object> params = parseRequestMessage(reqMsg);
        context.setParams(params);
    }

    /**
     * 解析请求消息
     */
    private Map<String, Object> parseRequestMessage(String reqMsg) {
        if (StrUtil.isBlank(reqMsg)) {
            return new HashMap<>();
        }

        try {
            if (JSONUtil.isTypeJSONObject(reqMsg)) {
                return JSONUtil.parseObj(reqMsg, true);
            } else if (JSONUtil.isTypeJSONArray(reqMsg)) {
                // TODO: 处理JSON数组场景
                log.debug("收到JSON数组格式的请求消息");
                return new HashMap<>();
            } else {
                return XmlUtil.xmlToMap(reqMsg);
            }
        } catch (Exception e) {
            log.error("请求报文解析失败: {}", e.getMessage(), e);
            throw new ConvertException("请求报文不是JSON或XML格式");
        }
    }

    /**
     * 设置API服务信息
     */
    private void setApi(Context context, String apiCode) {
        App app = Optional.ofNullable(context.getApp())
                .orElseThrow(() -> new ConvertException("应用信息不存在"));

        Api api;
        try {
            api = apiMapper.getApi(app.getId(), apiCode);
        } catch (Exception e) {
            log.error("查询API服务失败，appId: {}, apiCode: {}, 错误信息: {}", app.getId(), apiCode, e.getMessage(), e);
            throw new ConvertException("查询API服务失败");
        }

        if (api == null) {
            throw new ConvertException("API服务不存在: " + apiCode);
        }

        context.setApi(api);
    }

    /**
     * 设置应用信息
     */
    private void setApp(Context context, String appCode) {
        App app;
        try {
            app = appMapper.getApp(appCode);
        } catch (Exception e) {
            log.error("查询应用信息失败，appCode: {}, 错误信息: {}", appCode, e.getMessage(), e);
            throw new ConvertException("查询应用信息失败");
        }

        if (app == null) {
            throw new ConvertException("应用不存在: " + appCode);
        }

        context.setApp(app);
    }

    /**
     * 解析响应对象
     */
    public Map<String, Object> parseResponse(Context context, Rsp rsp) {
        List<RuleMapping> rules = ruleMapper.getMappingRulesByRuleCode(Const.RuleType.RSP, context.getRuleCode());
        Map<String, Object> rspMap = BeanUtil.beanToMap(rsp);

        if (CollUtil.isEmpty(rules)) {
            log.warn("没有找到对应的响应映射规则：{}，返回默认响应", context.getRuleCode());
            return buildDefaultResponse(context, rspMap);
        }

        Map<String, Object> contextMap = new HashMap<>(BeanUtil.beanToMap(context));
        contextMap.putAll(rspMap);

        return convertor.parseMappingRules(contextMap, rules);
    }

    /**
     * 构建默认响应
     */
    private Map<String, Object> buildDefaultResponse(Context context, Map<String, Object> rspMap) {
        Req req = context.getReq();
        if (req != null && Const.MsgFormat.XML.equals(req.getFormat())) {
            Map<String, Object> defaultRspMap = new LinkedHashMap<>();
            defaultRspMap.put(RSP_WRAPPER_KEY, rspMap);
            return defaultRspMap;
        }
        return rspMap;
    }

    /**
     * 验证请求的合法性
     */
    private void validateRequest(Context context) {
        if (context.isInternalRetry()) {
            return;
        }

        Req req = Optional.ofNullable(context.getReq())
                .orElseThrow(() -> new ConvertException("请求信息不存在"));
        App app = Optional.ofNullable(context.getApp())
                .orElseThrow(() -> new ConvertException("应用信息不存在"));

        validateRequestBasics(req, app);
        validateRequestSignature(req, app);
    }

    /**
     * 验证请求基础信息
     */
    private void validateRequestBasics(Req req, App app) {
//        RequestValidator.validateRepeatRequest(req.getReqId(), app.getValidTime());
//        RequestValidator.validateTimeExpired(req.getTimestamp(), app.getValidTime());
    }

    /**
     * 验证请求签名
     */
    private void validateRequestSignature(Req req, App app) {
        if (!app.getSignRequired()) {
            return;
        }

        String rcvSign = req.getRcvSign();
        String genSign = req.getGenSign();

        if (!StrUtil.equals(rcvSign, genSign)) {
            log.error("签名验证失败，rcvSign: {}, genSign: {}", rcvSign, genSign);
            throw new ConvertException("签名验证失败");
        }
    }
}