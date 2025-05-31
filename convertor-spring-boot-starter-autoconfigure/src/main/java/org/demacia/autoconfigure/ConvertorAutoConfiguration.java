package org.demacia.autoconfigure;

import org.demacia.Convertor;
import org.demacia.receive.ReceiveService;
import org.demacia.send.SendService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hepenglin
 * @since 2024/11/24 22:11
 **/
@MapperScan("org.demacia.mapper")
@Configuration
@EnableConfigurationProperties(ConvertorProperties.class)
public class ConvertorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SendService sendService() {
        return new SendService();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReceiveService receiveService() {
        return new ReceiveService();
    }

    @Bean
    @ConditionalOnMissingBean
    public Convertor convertor() {
        return new Convertor();
    }
}
