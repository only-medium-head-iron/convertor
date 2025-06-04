package org.demacia.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.demacia.domain.App;

/**
 * @author hepenglin
 * @since 2024/12/8 8:54
 */
@Mapper
public interface AppMapper {
    App getApp(String appCode);
}
