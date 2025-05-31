package org.demacia.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.demacia.domain.ApiApp;

/**
 * @author hepenglin
 * @since 2024/12/8 8:54
 */
@Mapper
public interface AppMapper {

    ApiApp getApiApp(String appCode);
}
