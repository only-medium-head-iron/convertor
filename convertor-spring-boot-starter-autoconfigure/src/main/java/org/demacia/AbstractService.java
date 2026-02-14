package org.demacia;

import lombok.extern.slf4j.Slf4j;
import org.demacia.domain.App;
import org.demacia.domain.Context;
import org.demacia.domain.ContextHolder;
import org.demacia.domain.Log;
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
        App app = context.getApp();
        log.setAppName(app.getAppName());
        logMapper.recordLog(log);
    }
}