package org.demacia.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.demacia.domain.Api;

/**
 * @author hepenglin
 * @since 2024/12/8 8:54
 */
@Mapper
public interface ApiMapper {
    @Select("SELECT * FROM api_config WHERE app_id = #{appId} AND api_code = #{apiCode}")
    Api getApi(Long appId, String apiCode);

    @Select("SELECT * FROM api_config WHERE app_code = #{appCode} AND api_code = #{apiCode} and deleted = 0")
    Api selectByAppCodeAndApiCode(String appCode, String apiCode);
}
