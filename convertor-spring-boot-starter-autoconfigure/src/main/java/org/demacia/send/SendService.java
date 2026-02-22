package org.demacia.send;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.demacia.AbstractService;
import org.demacia.Convertor;
import org.demacia.constant.Const;
import org.demacia.domain.*;
import org.demacia.enums.ResultCode;
import org.demacia.exception.ConvertException;
import org.demacia.mapper.AppMapper;
import org.demacia.mapper.ApiMapper;
import org.demacia.mapper.RuleMapper;
import org.demacia.rule.RuleMapping;
import org.demacia.send.handler.DefaultSendHandler;
import org.demacia.util.JacksonUtil;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.demacia.enums.ResultCode.APP_NOT_FOUND;

/**
 * 统一API推送请求处理服务
 *
 * @author hepenglin
 * @since 2024-08-10 09:35
 */
@Slf4j
@Service
public class SendService extends AbstractService {

    private static final int DEFAULT_MAP_CAPACITY = 16;

    @Resource
    private AppMapper appMapper;

    @Resource
    private RuleMapper ruleMapper;

    @Resource
    private Convertor convertor;

    @Resource
    private ApiMapper apiMapper;

    /**
     * 处理统一API推送请求
     *
     * @param sendRequest 推送请求参数
     * @return 响应数据传输对象
     */
    public Rsp send(SendRequest sendRequest) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Rsp rsp = new Rsp();
        Context context = new Context();
        ContextHolder.set(context);

        try {
            initializeContext(context, sendRequest);
            processRequest(context);
            rsp = buildResponse(context);
        } catch (ConvertException e) {
            log.error("请求外部处理失败：{}", e.getMessage(), e);
            rsp.setCode(e.getCode());
            rsp.setMessage(e.getMessage());
            rsp.setMessageExternal(e.getMessage());
        } catch (Exception e) {
            log.error("请求外部处理失败：{}", e.getMessage(), e);
            rsp.setCode(ResultCode.FAILURE.getCode());
            rsp.setMessage(e.toString());
            rsp.setMessageExternal(ResultCode.FAILURE.getMessage());
        } finally {
            context.setRsp(rsp);
            stopWatch.stop();
            context.setCost(stopWatch.getTotalTimeMillis());
            log.info("处理耗时：{} 毫秒", stopWatch.getTotalTimeMillis());
            recordLogAndClearContext(context);
        }

        return rsp;
    }

    /**
     * 构建响应对象
     */
    private Rsp buildResponse(Context context) {
        try {
            Map<String, Object> rspMap = parseResponse(context);
            return BeanUtil.toBean(rspMap, Rsp.class);
        } catch (Exception e) {
            log.error("响应解析失败：{}", e.getMessage(), e);
            Rsp rsp = new Rsp();
            rsp.setCode(ResultCode.FAILURE.getCode());
            rsp.setMessage(e.toString());
            rsp.setMessageExternal(ResultCode.FAILURE.getMessage());
            return rsp;
        }
    }

    /**
     * 初始化上下文
     */
    private void initializeContext(Context context, SendRequest sendRequest) {
        // 设置前置信息
        setPre(context, sendRequest);

        // 基础信息设置
        context.setDirection(Const.Direction.SEND);
        context.setRetryParams(JacksonUtil.toJson(sendRequest));
        context.setParams(BeanUtil.beanToMap(sendRequest));
        context.setInternalRetry(sendRequest.isInternalRetry());

        // 设置应用和API信息
        setApp(context, sendRequest.getAppCode());
        setApi(context, sendRequest.getApiCode());

        // 设置规则编码
        App app = context.getApp();
        Api api = context.getApi();
        context.setRuleCode(app.getAppCode() + StrUtil.DASHED + api.getApiCode());

        // 处理重试场景
        handleRetryScenario(context, sendRequest);
    }

    /**
     * 设置前置信息
     */
    private void setPre(Context context, SendRequest sendRequest) {
        Pre pre = new Pre();
        pre.setBizNo(sendRequest.getBizNo());
        context.setPre(pre);
    }

    /**
     * 处理重试场景
     */
    private void handleRetryScenario(Context context, SendRequest sendRequest) {
        if (context.isInternalRetry()) {
            String reqMsg = sendRequest.getRequestBody();
            context.setReqMsg(reqMsg);

            Optional.ofNullable(context.getApi())
                    .filter(api -> Const.MessageFormat.FORM.equals(api.getMessageFormat()))
                    .ifPresent(api -> context.setTarget(JSONUtil.parseObj(reqMsg)));
        }
    }

    /**
     * 处理发送请求
     */
    private void processRequest(Context context) {
        SendHandler sendHandler = determineWhichHandler(context);
        sendHandler.handle(context);
    }

    /**
     * 根据上下文获取发送处理器
     */
    private SendHandler determineWhichHandler(Context context) {
        String handlerBeanName = Optional.ofNullable(context.getApi())
                .map(Api::getHandlerClass)
                .orElseThrow(() -> new ConvertException("处理器配置不能为空"));

        try {
            return SpringUtil.getBean(StrUtil.lowerFirst(handlerBeanName));
        } catch (NoSuchBeanDefinitionException e) {
            return handleHandlerNotFound(context, handlerBeanName, e);
        }
    }

    /**
     * 处理处理器未找到的情况
     */
    private SendHandler handleHandlerNotFound(Context context, String handlerBeanName, NoSuchBeanDefinitionException e) {
        if (context.isInternalRetry()) {
            // TODO 重试临时添加，后续迁移需要删除
            log.warn("未找到处理器：{}，使用默认处理器", handlerBeanName);
            return SpringUtil.getBean(DefaultSendHandler.class);
        }

        log.error("处理器不存在：{}", handlerBeanName);
        throw new ConvertException("处理器不存在: " + handlerBeanName);
    }

    /**
     * 设置应用信息
     */
    private void setApp(Context context, String appCode) {
        App app;
        try {
            app = appMapper.selectByAppCode(appCode);
        } catch (Exception e) {
            log.error("查询应用信息失败，appCode: {}, 错误信息: {}", appCode, e.getMessage(), e);
            throw new ConvertException(ResultCode.APP_QUERY_ERROR);
        }

        if (app == null) {
            throw new ConvertException("应用不存在，请确认是否已配置！应用编码: {}", appCode);
        }

        context.setApp(app);
    }

    /**
     * 设置API服务信息
     */
    private void setApi(Context context, String apiCode) {
        App app = Optional.ofNullable(context.getApp())
                .orElseThrow(() -> new ConvertException(ResultCode.APP_NOT_FOUND));

        Api api;
        try {
            api = apiMapper.getApi(app.getId(), apiCode);
        } catch (Exception e) {
            log.error("查询接口配置失败，appId: {}, apiCode: {}, 错误信息: {}", app.getId(), apiCode, e.getMessage(), e);
            throw new ConvertException(ResultCode.API_QUERY_ERROR);
        }

        if (api == null) {
            throw new ConvertException("接口配置不存在，请确认是否已配置！接口编码: {}", apiCode);
        }

        context.setApi(api);
    }

    /**
     * 解析响应消息
     */
    public Map<String, Object> parseResponse(Context context) {
        // 获取响应映射规则
        List<RuleMapping> rules = getResponseRules(context);
        if (CollUtil.isEmpty(rules)) {
            log.debug("没有找到响应映射规则：{}，返回空响应", context.getRuleCode());
            return new HashMap<>(DEFAULT_MAP_CAPACITY);
        }

        // 解析响应报文
        Map<String, Object> rspMap = parseResponseMessage(context);

        // 应用映射规则
        return convertor.parseMappingRules(rspMap, rules);
    }

    /**
     * 获取响应映射规则
     */
    private List<RuleMapping> getResponseRules(Context context) {
        try {
            return ruleMapper.getMappingRulesByRuleCode(Const.RuleType.RSP, context.getRuleCode());
        } catch (Exception e) {
            log.error("查询响应映射规则失败，ruleCode: {}, 错误信息: {}", context.getRuleCode(), e.getMessage(), e);
            throw new ConvertException("查询响应映射规则失败");
        }
    }

    /**
     * 解析响应报文
     */
    private Map<String, Object> parseResponseMessage(Context context) {
        Api api = Optional.ofNullable(context.getApi())
                .orElseThrow(() -> new ConvertException("API信息不存在"));

        String rspMsg = Optional.ofNullable(context.getRspMsg())
                .orElseThrow(() -> new ConvertException("响应报文不能为空"));

        String messageFormat = api.getMessageFormat();

        try {
            if (Const.MessageFormat.XML.equals(messageFormat)) {
                return XmlUtil.xmlToMap(rspMsg);
            } else {
                return parseJsonResponse(rspMsg);
            }
        } catch (Exception e) {
            log.error("响应报文解析失败，format: {}, message: {}, 错误信息: {}",
                    messageFormat, rspMsg, e.getMessage(), e);
            throw new ConvertException("响应报文格式不正确");
        }
    }

    /**
     * 解析JSON响应
     */
    private Map<String, Object> parseJsonResponse(String rspMsg) {
        try {
            if (JSONUtil.isTypeJSONObject(rspMsg)) {
                return JSONUtil.parseObj(rspMsg);
            } else if (JSONUtil.isTypeJSONArray(rspMsg)) {
                // TODO: 处理JSON数组场景
                log.debug("收到JSON数组格式的响应消息");
                return new HashMap<>();
            } else {
                throw new ConvertException("响应报文不是有效的JSON格式");
            }
        } catch (Exception e) {
            log.error("JSON响应解析失败: {}", e.getMessage(), e);
            throw new ConvertException("响应报文JSON格式不正确");
        }
    }
}