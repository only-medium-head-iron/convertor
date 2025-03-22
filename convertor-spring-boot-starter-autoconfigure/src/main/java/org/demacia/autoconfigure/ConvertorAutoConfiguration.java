package org.demacia.autoconfigure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author hepenglin
 * @since 2024/11/24 22:11
 **/
@Configuration
@EnableConfigurationProperties(ConvertorProperties.class)
public class ConvertorAutoConfiguration {

}
