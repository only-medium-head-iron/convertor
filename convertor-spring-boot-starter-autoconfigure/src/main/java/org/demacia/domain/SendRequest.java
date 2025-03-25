package org.demacia.domain;

import lombok.Data;

/**
 * @author hepenglin
 * @since 2024/12/8 8:10
 */
@Data
public class SendRequest {
    private String appCode;
    private String serviceCode;
    private String bizNo;
    private String reqMsg;
    private boolean internalRetry;
}
