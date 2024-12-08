package org.demacia.domain;

import lombok.Data;

/**
 * @author hepenglin
 * @since 2024/12/8 7:42
 */
@Data
public class ApiApp {
    private Long id;
    private String appCode;
    private String appName;
    private String appKey;
    private String appSecret;
    private String signMethod;
    private Long validTime;
}
