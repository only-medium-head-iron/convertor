package org.omit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author hepenglin
 * @since 2024/11/20 11:25
 **/
@Getter
@AllArgsConstructor
public enum MethodEnum {

    /**
     * 调度单更新接口
     */
    SCHEDULE_UPDATE_RESULT("schedule.update", "scheduleUpdateHandler"),

    /**
     * 调度单取消接口
     */
    SCHEDULE_CANCEL("schedule.cancel", "调度单取消接口");

    private final String method;

    private final String handler;
}
