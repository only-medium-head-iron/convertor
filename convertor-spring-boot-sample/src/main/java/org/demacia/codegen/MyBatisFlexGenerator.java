package org.demacia.codegen;

import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.mybatisflex.codegen.config.TableDefConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MyBatisFlexGenerator {
    
    private static final String BASE_PACKAGE = "org.demacia";
    
    public static void main(String[] args) {
        System.out.println("开始生成MyBatis-Flex基础代码...");
        
        // 配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://rm-wz9osma53ib32tf1npo.mysql.rds.aliyuncs.com:6603/wt_otms?allowMultiQueries=true");
        dataSource.setUsername("omsroot");
        dataSource.setPassword("MysqlSAsls90");
        
        // 创建全局配置
        GlobalConfig globalConfig = createGlobalConfig();
        
        // 创建生成器
        Generator generator = new Generator(dataSource, globalConfig);
        
        // 执行生成
        generator.generate();
        
        System.out.println("MyBatis-Flex基础代码生成完成！");
    }
    
    private static GlobalConfig createGlobalConfig() {
        GlobalConfig globalConfig = new GlobalConfig();
        
        // 1. 设置包配置
        globalConfig.getPackageConfig()
            .setSourceDir("src/main/java")
            .setBasePackage(BASE_PACKAGE)
            .setTableDefPackage(BASE_PACKAGE + ".entity.table")  // 设置TableDef包路径
            .setMapperXmlPath("src/main/resources/mapper");
        
        // 2. 设置表配置
        globalConfig.getTableConfig("app_config");  // 生成指定表
        globalConfig.setGenerateTable("app_config");
        // 3. 设置策略配置
        globalConfig.getStrategyConfig()
            .setLogicDeleteColumn("deleted")
            .setVersionColumn("version")
            .setTablePrefix("");  // 设置表前缀
        
        // 4. 设置TableDef配置
        TableDefConfig tableDefConfig = globalConfig.getTableDefConfig();
        tableDefConfig.setClassPrefix("");       // 类前缀
        tableDefConfig.setClassSuffix("TableDef"); // 类后缀
        tableDefConfig.setPropertiesNameStyle(TableDefConfig.NameStyle.UPPER_CASE); // 属性名大写
        
        // 5. 启用/禁用生成器
        globalConfig.enableEntity();
        globalConfig.enableMapper();
        globalConfig.enableService();
        globalConfig.enableServiceImpl();
        globalConfig.enableController();
        globalConfig.enableTableDef();     // 启用TableDef生成
        globalConfig.enableMapperXml();
        
        // 6. 设置Javadoc配置
        globalConfig.getJavadocConfig()
            .setAuthor("code-generator")
            .setSince("1.0");
        
        return globalConfig;
    }
}