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
import org.demacia.validate.RequestValidator;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hepenglin
 * @since 2024-08-04 17:25
 **/
@Slf4j
@Service
public class ReceiveService extends AbstractService {

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
     * 该方法根据应用代码、路径参数和请求消息来处理外部服务的请求，并返回相应的响应消息
     *
     * @param receiveRequest 需要的参数
     * @return 响应消息，返回给外部应用的响应数据
     */
    public String receive(ReceiveRequest receiveRequest) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String rspMsg = "";
        Rsp rsp = new Rsp();
        rsp.setCode(ResultCode.SUCCESS.getCode());
        rsp.setMessage(ResultCode.SUCCESS.getMessage());
        Context context = new Context();
        ContextHolder.set(context);
        try {
            initContext(context, receiveRequest);
            validateRequest(context);
            ReceiveHandler receiveHandler = determineWhichHandler(context);
            receiveHandler.handle(context);
            rsp.setSuccess(true);
        } catch (ConvertException e) {
            log.error("外部请求处理失败：{}", e.getMessage(), e);
            rsp.setCode(e.getCode());
            rsp.setMessage(e.toString());
            rsp.setMessageForExternal(e.getMessage());
        } catch (Exception e) {
            log.error("外部请求处理失败：{}", e.getMessage(), e);
            rsp.setCode(ResultCode.FAILURE.getCode());
            rsp.setMessage(e.toString());
            rsp.setMessageForExternal("系统异常");
        } finally {
            context.setRsp(rsp);
            try {
                Map<String, Object> parseResult = parseRsp(context, rsp);
                Req req = context.getReq();
                String messageFormat = req != null ? req.getFormat() : Const.MsgFormat.JSON;
                rspMsg = MessageFormatter.determineMsgFormat(parseResult, messageFormat);
                context.setRspMsg(rspMsg);
            } catch (Exception e) {
                log.error("响应解析失败：{}", e.getMessage(), e);
                rsp.setSuccess(false);
                rsp.setMessage(e.toString());
            } finally {
                recordLogAndClearContext(context);
            }
        }
        stopWatch.stop();
        log.info("处理外部请求耗时：{} ms", stopWatch.getTotalTimeMillis());
        return rspMsg;
    }

    /**
     * 根据上下文获取接收处理程序实例
     * 此方法通过Spring上下文获取指定名称的Bean作为接收处理程序
     * 如果无法找到对应的Bean，则抛出异常提示应用处理器错误
     *
     * @param context 上下文对象
     * @return 返回获取到的接收处理程序实例
     */
    private ReceiveHandler determineWhichHandler(Context context) {
        Api api = context.getApi();
        String handlerBeanName = api.getHandler();
        ReceiveHandler receiveHandler;
        try {
            receiveHandler = SpringUtil.getBean(StrUtil.lowerFirst(handlerBeanName));
        } catch (NoSuchBeanDefinitionException e) {
            throw new ConvertException("没有找到对应的处理器");
        }
        return receiveHandler;
    }

    /**
     * 初始化上下文对象
     * 该方法主要用于设置和验证API调用的上下文环境，包括路径参数、请求消息、应用信息、服务信息等
     *
     * @param context        上下文对象，用于承载API调用的相关信息
     * @param receiveRequest 请求参数，包含请求头、路径参数、请求体
     */
    public void initContext(Context context, ReceiveRequest receiveRequest) {
        context.setCallType(Const.CallType.RECEIVE);
        context.setRetryParams(JacksonUtil.toJson(receiveRequest));
        String reqMsg = receiveRequest.getReqMsg();
        String appCode = receiveRequest.getAppCode();
        BeanUtil.copyProperties(receiveRequest, context);
        setApp(context, appCode);
        setParams(context, reqMsg);
        setReq(context, appCode);
        String apiCode = context.getReq().getApiCode();
        if (StrUtil.isBlank(apiCode)) {
            throw new ConvertException("serviceCode服务编码不能为空");
        }
        context.setRuleCode(appCode + StrUtil.DASHED + apiCode);
        setPre(context);
        setApi(context, apiCode);
    }

    /**
     * 根据应用代码设置请求对象Req
     * 该方法通过应用代码查询相关的规则映射，将这些规则映射应用到当前上下文对象的请求实体中
     *
     * @param context 上下文对象，包含请求和响应等信息
     * @param appCode 应用代码，用于查询规则映射
     */
    private void setReq(Context context, String appCode) {
        List<RuleMapping> reqRules = ruleMapper.getMappingRulesByRuleCode(Const.RuleType.REQ, appCode);
        if (CollUtil.isEmpty(reqRules)) {
            log.warn("没有找到请求映射规则：{}", appCode);
            return;
        }
        LinkedHashMap<String, Object> reqMap = new LinkedHashMap<>(16);
        convertor.parseMappingRules(BeanUtil.beanToMap(context), reqMap, reqRules);
        Req req = BeanUtil.toBean(reqMap, Req.class);
        context.setReq(req);
    }

    /**
     * 根据服务码设置订单信息
     *
     * @param context 上下文对象，用于传递请求过程中的数据
     */
    private void setPre(Context context) {
        List<RuleMapping> preRules = ruleMapper.getMappingRulesByRuleCode(Const.RuleType.PRE, context.getRuleCode());
        if (CollUtil.isEmpty(preRules)) {
            log.warn("没有找到前置映射规则：{}", context.getRuleCode());
            return;
        }
        LinkedHashMap<String, Object> preMap = new LinkedHashMap<>(16);
        convertor.parseMappingRules(BeanUtil.beanToMap(context), preMap, preRules);
        Pre pre = BeanUtil.toBean(preMap, Pre.class);
        context.setPre(pre);
    }

    /**
     * 设置请求参数
     * 根据请求消息的格式（JSON或XML），将其解析为参数Map，并设置到上下文对象中
     *
     * @param context 上下文对象，用于承载解析后的请求参数
     * @param reqMsg  请求消息，可以是JSON或XML格式
     * @throws ConvertException 如果请求消息既不是JSON也不是XML格式，则抛出转换错误的服务异常
     */
    private void setParams(Context context, String reqMsg) {
        Map<String, Object> params;
        try {
            // TODO 如果是JSON数组，JSONArray jsonArray = JSONUtil.parseArray(reqMsg); 待测试
            if (JSONUtil.isTypeJSONObject(reqMsg)) {
                params = JSONUtil.parseObj(reqMsg, true);
            } else if (JSONUtil.isTypeJSONArray(reqMsg)) {
//                JSONArray jsonArray = JSONUtil.parseArray(reqMsg, true);
                params = new HashMap<>();
            } else {
                params = XmlUtil.xmlToMap(reqMsg);
            }
        } catch (Exception e) {
            log.error("请求报文解析失败，既不是有效的JSON也不是XML格式: {}", e.getMessage(), e);
            throw new ConvertException("请求报文不是JSON或XML格式");
        }
        context.setParams(params);
    }

    /**
     * 设置API服务信息到上下文对象中
     *
     * @param context     上下文对象，包含API应用相关信息
     * @param serviceCode 服务代码，用于标识特定的服务
     *                    <p>
     *                    通过上下文对象获取API应用ID和服务代码，查询对应的API服务信息
     *                    如果服务不存在，则抛出异常提示服务不存在
     *                    否则将查询到的服务信息设置到上下文对象中，供后续处理使用
     */
    private void setApi(Context context, String serviceCode) {
        App app = context.getApp();
        Api api = apiMapper.getApi(app.getId(), serviceCode);
        if (null == api) {
            throw new ConvertException("");
        }
        context.setApi(api);
    }

    /**
     * 根据应用代码设置API应用信息到上下文中
     *
     * @param context 上下文对象，用于存储API应用信息
     * @param appCode 应用代码，用于唯一标识一个应用
     */
    private void setApp(Context context, String appCode) {
        App app = appMapper.getApp(appCode);
        if (null == app) {
            throw new ConvertException("");
        }
        context.setApp(app);
    }

    /**
     * 解析响应对象
     *
     * @param context 上下文对象，包含请求相关信息
     * @param rsp     响应对象，包含需要解析的数据
     * @return 解析后的响应参数，以Map形式返回
     */
    public Map<String, Object> parseRsp(Context context, Rsp rsp) {
        List<RuleMapping> rules = ruleMapper.getMappingRulesByRuleCode(Const.RuleType.RSP, context.getRuleCode());
        Map<String, Object> rspMap = BeanUtil.beanToMap(rsp);
        if (CollUtil.isEmpty(rules)) {
            log.warn("没有找到对应的响应映射规则：{}，返回默认响应", context.getRuleCode());
            // 返回默认响应
            Req req = context.getReq();
            if (req != null && Const.MsgFormat.XML.equals(req.getFormat())) {
                Map<String, Object> defaultRspMap = new LinkedHashMap<>();
                defaultRspMap.put("response", rspMap);
                return defaultRspMap;
            }
            return rspMap;
        }
        Map<String, Object> rspParams;
        Map<String, Object> contextMap = BeanUtil.beanToMap(context);
        contextMap.putAll(rspMap);
        rspParams = convertor.parseMappingRules(contextMap, rules);
        return rspParams;
    }

    /**
     * 验证请求的合法性
     *
     * @param context 请求上下文，包含请求信息和应用信息
     */
    private void validateRequest(Context context) {
        if (context.isInternalRetry()) {
            return;
        }
        Req req = context.getReq();
        App app = context.getApp();
        RequestValidator.validateRepeatRequest(req.getReqId(), app.getValidTime());
        RequestValidator.validateTimeExpired(req.getTimestamp(), app.getValidTime());
        // 是否要签名验证
        String signMethod = app.getSignMethod();
        if (Const.SignMethod.NOT_REQUIRED.equals(signMethod)) {
            return;
        }
        String rcvSign = req.getRcvSign();
        String genSign = req.getGenSign();
        if (!StrUtil.equals(rcvSign, genSign)) {
            log.error("签名验证失败，rcvSign: {}, genSign: {}, pathParams: {}, headers: {}, reqMsg: {}",
                    rcvSign, genSign, context.getQueryParams(), context.getHeaders(), context.getReqMsg());
            throw new ConvertException("");
        }
    }
}
