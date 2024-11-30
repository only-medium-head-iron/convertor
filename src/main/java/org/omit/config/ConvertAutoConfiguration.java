package org.omit.config;

import org.omit.Convertor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author hepenglin
 * @since 2024/11/24 22:11
 **/
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties({Convertor.class})
@Import(Convertor.class)
public class ConvertAutoConfiguration {

}
