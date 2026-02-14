package org.demacia;

import lombok.extern.slf4j.Slf4j;
import org.demacia.domain.*;
import org.demacia.mapper.LogMapper;

import javax.annotation.Resource;


/**
 * @author hepenglin
 * @since 2024-08-08 17:52
 **/
@Slf4j
public abstract class AbstractService {

    @Resource
    private LogMapper logMapper;

    public void recordLogAndClearContext(Context context) {
        try {
            createLog(context);
        } catch (Exception e) {
            log.error("接口日志落库失败：{}", e.getMessage(), e);
        } finally {
            ContextHolder.clear();
        }
    }

    private void createLog(Context context) {
        Log log = new Log();
//        log.setBizNo();

        App app = context.getApp();
        if (app != null) {
            log.setAppCode(app.getAppCode());
            log.setAppName(app.getAppName());
        }

        Api api = context.getApi();
        if (api != null) {
            log.setApiCode(api.getApiCode());
            log.setApiName(api.getApiName());
        }

        log.setRequestMessage(context.getReqMsg());
        log.setResponseMessage(context.getRspMsg());
        log.setRetryParams(context.getRetryParams());

        Rsp rsp = context.getRsp();
        if (rsp != null) {
            log.setRequestResult(rsp.isSuccess());
            log.setErrorMessage(rsp.getMessage());
        }

        log.setCost(context.getCost());
        logMapper.recordLog(log);
    }
}