package org.demacia.mapper;

import org.demacia.domain.ApiApp;

/**
 * @author hepenglin
 * @since 2024/12/8 8:54
 */
public interface ApiAppMapper {

    ApiApp getApiApp(String appCode);
}
