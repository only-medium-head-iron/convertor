package org.demacia.mapper;

import org.demacia.domain.Log;

/**
 * @author hepenglin
 * @since 2024/12/8 9:28
 */
public interface LogMapper {
    void recordLog(Log log);
}
