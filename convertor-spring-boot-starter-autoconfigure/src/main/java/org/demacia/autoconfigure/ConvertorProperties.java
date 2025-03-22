package org.demacia.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "convertor.config")
public class ConvertorProperties {

    private String rulePath;
}
