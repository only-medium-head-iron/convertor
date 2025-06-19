package org.demacia.domain;

import lombok.Data;

/**
 * @author hepenglin
 * @since 2024/12/8 8:10
 */
@Data
public class SendRequest {
    private String appCode;
    private String apiCode;
    private String bizNo;
    private String requestBody;
    private boolean internalRetry;
}
