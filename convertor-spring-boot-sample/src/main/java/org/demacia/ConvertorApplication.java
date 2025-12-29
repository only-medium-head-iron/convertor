package org.demacia;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author hepenglin
 * @since 2025/3/25 15:38
 **/
@MapperScan("org.demacia.mapper")
@SpringBootApplication
public class ConvertorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConvertorApplication.class, args);
    }
}
