package org.demacia;

import lombok.extern.slf4j.Slf4j;


/**
 * @author hepenglin
 * @since 2024-08-08 17:52
 **/
@Slf4j
public abstract class AbstractService {

//    @Resource
//    private AccessLogService accessLogService;
//
//    public void recordLogAndClearContext(Context context) {
//        if (!context.isDirectCall()) {
//            return;
//        }
//        try {
//            accessLogService.createLog(context);
//        } catch (Exception e) {
//            log.error("接口日志落库失败：{}", e.getMessage(), e);
//        } finally {
//            ContextHolder.clear();
//        }
//    }
}