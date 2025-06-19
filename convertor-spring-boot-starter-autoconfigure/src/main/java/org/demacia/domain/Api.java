package org.demacia.domain;

import lombok.Data;

/**
 * @author hepenglin
 * @since 2024/12/8 7:58
 */
@Data
public class Api {

    private String apiCode;

    private String handler;

    private String httpMethod;

    private String messageFormat;

    private String uri;
}
