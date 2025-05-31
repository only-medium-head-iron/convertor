package org.demacia.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.demacia.domain.Api;

/**
 * @author hepenglin
 * @since 2024/12/8 8:54
 */
@Mapper
public interface ApiMapper {
    Api getApi(Long appId, String apiCode);
}
