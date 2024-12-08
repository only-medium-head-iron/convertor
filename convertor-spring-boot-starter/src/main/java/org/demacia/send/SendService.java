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
import org.demacia.mapper.ApiAppMapper;
import org.demacia.mapper.ApiServiceMapper;
import org.demacia.mapper.RuleMapper;
import org.demacia.rule.RuleMapping;
import org.demacia.send.handler.DefaultSendHandler;
import org.demacia.util.JsonUtil;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author hepenglin
 * @since 2024-08-10 09:35
 **/
@Slf4j
@Service
public class SendService extends AbstractService {

    @Resource
    private ApiAppMapper apiAppMapper;

    @Resource
    private RuleMapper ruleMapper;

    @Resource
    private Convertor convertor;

    @Resource
    private ApiServiceMapper apiServiceMapper;

    /**
     * 处理统一API推送请求
     * 该方法负责初始化上下文，根据上下文获取处理程序，并使用该处理程序处理上下文
     * 如果处理过程中抛出异常，则会记录错误并设置响应码和响应信息
     *
     * @param sendRequest 推送请求数据传输对象，包含推送所需的信息
     */
    public Rsp handle(SendRequest sendRequest) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Rsp rsp = new Rsp();
        Context context = new Context();
        ContextHolder.set(context);
        try {
            initContext(context, sendRequest);
            SendHandler sendHandler = getHandler(context);
            sendHandler.handle(context);
            Map<String, Object> rspMap = parseRsp(context);
            rsp = BeanUtil.toBean(rspMap, Rsp.class);
        } catch (ConvertException e) {
            log.error("请求失败：{}", e.getMessage(), e);
            rsp.setCode(e.getCode());
            rsp.setMessage(e.getMessage());
            rsp.setOuterMessage(e.getMessage());
        } catch (Exception e) {
            log.error("请求失败：{}", e.getMessage(), e);
            rsp.setCode(ResultCode.FAILURE.getCode());
            rsp.setMessage(e.toString());
            rsp.setOuterMessage(ResultCode.FAILURE.getMessage());
        } finally {
            context.setRsp(rsp);
            recordLogAndClearContext(context);
        }
        stopWatch.stop();
        log.info("处理耗时：{} ms", stopWatch.getTotalTimeMillis());
        return rsp;
    }

    /**
     * 根据上下文获取发送处理器
     * 此方法旨在通过Spring容器获取特定的发送处理器Bean
     * 如果无法找到对应的Bean，则抛出异常，指示应用程序处理逻辑出错
     *
     * @param context 上下文对象，用于获取API服务信息
     * @return SendHandler 返回对应的发送处理器实例
     */
    private SendHandler getHandler(Context context) {
        ApiService apiService = context.getApiService();
        SendHandler sendHandler;
        try {
            String handlerBeanName = StrUtil.lowerFirst(apiService.getHandler());
            sendHandler = SpringUtil.getBean(handlerBeanName);
        } catch (NoSuchBeanDefinitionException e) {
            if (context.isInternalRetry()) {
                // TODO 重试临时添加，后续迁移需要删除
                return SpringUtil.getBean(DefaultSendHandler.class);
            }
            log.error("没有找到对应的处理器：{}", apiService.getHandler());
            throw new ConvertException("");
        }
        return sendHandler;
    }

    /**
     * 初始化上下文
     * 此方法用于初始化上下文对象，设置请求参数、API应用和API服务信息
     *
     * @param context     上下文对象，用于存储请求参数、API应用和API服务信息
     * @param sendRequest 推送请求数据传输对象，包含推送所需的信息
     */
    private void initContext(Context context, SendRequest sendRequest) {
        Pre pre = new Pre();
        pre.setBizNo(sendRequest.getBizNo());
        context.setPre(pre);
        context.setCallType(Const.CallType.SEND);
        context.setRetryParams(JsonUtil.toJson(sendRequest));
        Map<String, Object> params = BeanUtil.beanToMap(sendRequest);
        context.setParams(params);
        ApiApp apiApp = apiAppMapper.getApiApp(sendRequest.getAppCode());
        if (null == apiApp) {
            throw new ConvertException("");
        }
        context.setApiApp(apiApp);
        // 对应接口处理
        ApiService apiService = apiServiceMapper.getApiService(apiApp.getId(), sendRequest.getServiceCode());
        if (null == apiService) {
            throw new ConvertException("");
        }
        context.setApiService(apiService);
        context.setRuleCode(apiApp.getAppCode() + StrUtil.DASHED + apiService.getServiceCode());
        context.setInternalRetry(sendRequest.isInternalRetry());
        if (context.isInternalRetry()) {
            String reqMsg = sendRequest.getReqMsg();
            context.setReqMsg(reqMsg);
            if (Const.MessageFormat.FORM.equals(apiService.getMessageFormat())) {
                context.setTarget(JSONUtil.parseObj(reqMsg));
            }
        }
    }

    /**
     * 解析响应消息
     * 此方法用于解析响应消息，根据API服务的消息类型（XML或JSON）进行解析
     *
     * @param context 上下文对象，用于获取API服务的消息类型
     * @return Map<String, Object> 解析后的响应消息，以键值对的形式返回
     */
    public Map<String, Object> parseRsp(Context context) {
        List<RuleMapping> rules = ruleMapper.getMappingRulesByRuleCode(Const.RuleType.RSP, context.getRuleCode());
        if (CollUtil.isEmpty(rules)) {
            log.warn("没有找到响应映射规则：{}", context.getRuleCode());
            return new HashMap<>(16);
        }
        ApiService apiService = context.getApiService();
        String messageFormat = apiService.getMessageFormat();
        Map<String, Object> rspMap;
        String rspMsg = context.getRspMsg();
        try {
            if (Const.MessageFormat.XML.equals(messageFormat)) {
                rspMap = XmlUtil.xmlToMap(rspMsg);
            } else {
                rspMap = JSONUtil.parseObj(rspMsg);
            }
        } catch (Exception e) {
            log.error("响应报文格式不正确：{}", e.getMessage(), e);
            throw new ConvertException("响应报文格式不正确");
        }
        return convertor.parseMappingRules(rspMap, rules);
    }
}
