package org.demacia.mapper;

import org.demacia.domain.ApiService;

/**
 * @author hepenglin
 * @since 2024/12/8 8:54
 */
public interface ApiServiceMapper {
    ApiService getApiService(Long appId, String serviceCode);
}
