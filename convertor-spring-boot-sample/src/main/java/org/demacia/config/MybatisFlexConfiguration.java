package org.demacia.config;

import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.audit.AuditManager;
import org.demacia.common.BaseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisFlexConfiguration {

    private static final Logger logger = LoggerFactory
            .getLogger("mybatis-flex-sql");

    public MybatisFlexConfiguration() {
        // 开启审计功能
        AuditManager.setAuditEnable(true);

        // 设置 SQL 审计收集器
        AuditManager.setMessageCollector(auditMessage ->
                logger.info("{}; 耗时{}ms", auditMessage.getFullSql()
                        , auditMessage.getElapsedTime())
        );

        // 设置新增和修改监听器
        MybatisInsertListener mybatisInsertListener = new MybatisInsertListener();
        MybatisUpdateListener mybatisUpdateListener = new MybatisUpdateListener();
        FlexGlobalConfig config = FlexGlobalConfig.getDefaultConfig();

        // 设置BaseEntity类启用
        config.registerInsertListener(mybatisInsertListener, BaseEntity.class);
        config.registerUpdateListener(mybatisUpdateListener, BaseEntity.class);
    }
}