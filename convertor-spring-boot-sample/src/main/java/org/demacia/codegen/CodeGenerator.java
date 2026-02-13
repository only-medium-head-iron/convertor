package org.demacia.codegen;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CodeGenerator {

    private static final Logger logger = LoggerFactory.getLogger(CodeGenerator.class);
    private static final VelocityEngine VELOCITY_ENGINE = initVelocityEngine();

    // 配置常量
    private static final String BASE_PACKAGE = "org.demacia";
    private static final String COMMON_PACKAGE = BASE_PACKAGE + ".common";
    private static final String DATABASE = "wt_otms";
    private static final String TABLE_PREFIXES = "tms,oms,mdm";

    // 数据库配置
    private static final String JDBC_URL = "jdbc:mysql://rm-wz9osma53ib32tf1npo.mysql.rds.aliyuncs.com:6603/" + DATABASE + "?allowMultiQueries=true";
    private static final String USERNAME = "omsroot";
    private static final String PASSWORD = "MysqlSAsls90";

    // 需要生成的表列表
    private static final List<String> TABLES = Arrays.asList(
            "app_config"
    );

    public static void main(String[] args) {
        logger.info("开始生成动态字段代码...");

        try (HikariDataSource dataSource = createDataSource()) {
            for (String tableName : TABLES) {
                try {
                    processTable(dataSource, tableName);
                } catch (Exception e) {
                    logger.error("处理表 {} 时发生错误: {}", tableName, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error("代码生成过程发生错误: {}", e.getMessage(), e);
        }

        logger.info("所有代码生成完成！");
    }

    /**
     * 处理单个表
     */
    private static void processTable(HikariDataSource dataSource, String tableName) throws Exception {
        logger.info("正在生成表: {}", tableName);

        // 解析表名
        String tableNameWithoutPrefix = removeTablePrefix(tableName);
        logger.debug("原始表名: {}, 去除前缀后: {}", tableName, tableNameWithoutPrefix);

        // 获取表信息
        Map<String, Object> context = buildContext(dataSource, tableName, tableNameWithoutPrefix);

        // 生成所有代码
        generateAllCode(context);
    }

    /**
     * 构建表信息
     */
    private static Map<String, Object> buildContext(HikariDataSource dataSource,
                                                    String originalTableName,
                                                    String tableNameWithoutPrefix) throws SQLException {
        Map<String, Object> context = new HashMap<>();

        context.put("originalTableName", originalTableName);
        context.put("tableNameWithoutPrefix", tableNameWithoutPrefix);

        // 获取模块名
        String module = getModuleName(tableNameWithoutPrefix);
        context.put("module", module);

        // 实体名
        String entityName = toCamelCase(tableNameWithoutPrefix, true);
        context.put("entityName", entityName);
        context.put("entityVariable", toCamelCase(tableNameWithoutPrefix, false));

        // 请求映射URL
        String requestMappingURL = "/" + module + "/" + getUrlPath(tableNameWithoutPrefix);
        context.put("requestMappingURL", requestMappingURL);

        // 获取表注释
        String tableComment = getTableComment(dataSource, originalTableName);
        context.put("comment", tableComment);

        // 获取表字段信息
        List<Map<String, Object>> columns = fetchColumnDetails(dataSource, originalTableName);
        context.put("columns", columns);

        // 设置TableDef常量名
        context.put("tableConstantName", tableNameWithoutPrefix.toUpperCase());

        // 计算需要导入的注解和类型
        calculateImportFlags(context, columns);

        // 设置包路径
        setPackages(context, module);

        return context;
    }

    /**
     * 计算需要导入的注解和类型标志
     */
    private static void calculateImportFlags(Map<String, Object> context, List<Map<String, Object>> columns) {
        boolean hasNotBlank = false;
        boolean hasNotNull = false;
        boolean hasSize = false;
        boolean hasMinMax = false;
        boolean hasDateField = false;
        boolean hasLocalDateTime = false;
        boolean hasLocalDate = false;
        boolean hasBigDecimal = false;

        for (Map<String, Object> column : columns) {
            String javaTypeSimpleName = (String) column.get("javaTypeSimpleName");
            boolean nullable = (Boolean) column.get("nullable");
            String dataType = (String) column.get("dataType");

            if (!nullable && "String".equals(javaTypeSimpleName)) {
                hasNotBlank = true;
            }
            if (!nullable && !"String".equals(javaTypeSimpleName)) {
                hasNotNull = true;
            }
            if ("varchar".equals(dataType) && (Integer) column.get("charMaxLength") > 0) {
                hasSize = true;
            }
            if ("Integer".equals(javaTypeSimpleName) || "Long".equals(javaTypeSimpleName)) {
                hasMinMax = true;
            }
            if ("LocalDateTime".equals(javaTypeSimpleName) || "LocalDate".equals(javaTypeSimpleName)) {
                hasDateField = true;
            }
            if ("LocalDateTime".equals(javaTypeSimpleName)) {
                hasLocalDateTime = true;
            }
            if ("LocalDate".equals(javaTypeSimpleName)) {
                hasLocalDate = true;
            }
            if ("BigDecimal".equals(javaTypeSimpleName)) {
                hasBigDecimal = true;
            }
        }

        context.put("hasNotBlank", hasNotBlank);
        context.put("hasNotNull", hasNotNull);
        context.put("hasSize", hasSize);
        context.put("hasMinMax", hasMinMax);
        context.put("hasDateField", hasDateField);
        context.put("hasLocalDateTime", hasLocalDateTime);
        context.put("hasLocalDate", hasLocalDate);
        context.put("hasBigDecimal", hasBigDecimal);
    }

    /**
     * 设置包路径
     */
    private static void setPackages(Map<String, Object> context, String module) {
        context.put("basePackage", BASE_PACKAGE);
        context.put("commonPackage", COMMON_PACKAGE);
        context.put("entityPackage", BASE_PACKAGE + ".entity." + module);
        context.put("mapperPackage", BASE_PACKAGE + ".mapper." + module);
        context.put("controllerPackage", BASE_PACKAGE + ".controller." + module);
        context.put("requestPackage", BASE_PACKAGE + ".controller." + module + ".request");
        context.put("responsePackage", BASE_PACKAGE + ".controller." + module + ".response");
        context.put("servicePackage", BASE_PACKAGE + ".service." + module);
        context.put("serviceImplPackage", BASE_PACKAGE + ".service." + module + ".impl");
    }

    /**
     * 创建数据源
     */
    private static HikariDataSource createDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(JDBC_URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);

        return dataSource;
    }

    /**
     * 获取URL路径
     */
    private static String getUrlPath(String tableNameWithoutPrefix) {
        if (isEmpty(tableNameWithoutPrefix)) {
            return "";
        }

        int firstUnderscoreIndex = tableNameWithoutPrefix.indexOf('_');
        if (firstUnderscoreIndex > 0) {
            String urlPart = tableNameWithoutPrefix.substring(firstUnderscoreIndex + 1);
            return convertToKebabCase(urlPart);
        }

        return convertToKebabCase(tableNameWithoutPrefix);
    }

    /**
     * 移除表前缀
     */
    private static String removeTablePrefix(String tableName) {
        String[] prefixes = TABLE_PREFIXES.split(",");
        for (String prefix : prefixes) {
            String prefixWithUnderscore = prefix.trim() + "_"; // 自动添加下划线
            if (tableName.startsWith(prefixWithUnderscore)) {
                return tableName.substring(prefixWithUnderscore.length());
            }
            // 同时也检查原始前缀（不带下划线）的情况，以防万一
            if (tableName.startsWith(prefix.trim())) {
                return tableName.substring(prefix.trim().length());
            }
        }
        return tableName;
    }

    /**
     * 获取模块名
     */
    private static String getModuleName(String tableNameWithoutPrefix) {
        if (isEmpty(tableNameWithoutPrefix)) {
            return "common";
        }

        // 如果没有下划线，使用整个名称作为模块名
        int underscoreIndex = tableNameWithoutPrefix.indexOf('_');
        if (underscoreIndex > 0) {
            // 有下划线，取第一个下划线前的部分
            return tableNameWithoutPrefix.substring(0, underscoreIndex);
        }

        // 没有下划线，使用整个名称作为模块名
        return tableNameWithoutPrefix;
    }

    /**
     * 获取表注释
     */
    private static String getTableComment(HikariDataSource dataSource, String tableName) throws SQLException {
        String defaultComment = tableName + "表";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES " +
                             "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?")) {

            pstmt.setString(1, DATABASE);
            pstmt.setString(2, tableName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String rawComment = rs.getString("TABLE_COMMENT");
                    if (isNotBlank(rawComment)) {
                        defaultComment = rawComment.trim();
                        logger.debug("获取到表 {} 的注释: {}", tableName, defaultComment);
                    }
                }
            }
        }

        // 清理注释
        if (defaultComment.endsWith("主表")) {
            defaultComment = defaultComment.substring(0, defaultComment.length() - 2);
        }
        if (defaultComment.endsWith("表")) {
            defaultComment = defaultComment.substring(0, defaultComment.length() - 1);
        }

        return defaultComment;
    }

    /**
     * 获取字段详细信息
     */
    private static List<Map<String, Object>> fetchColumnDetails(HikariDataSource dataSource, String tableName) throws SQLException {
        List<Map<String, Object>> columns = new ArrayList<>();

        String sql = "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, COLUMN_TYPE, " +
                "COLUMN_COMMENT, IS_NULLABLE, COLUMN_KEY, EXTRA " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
                "ORDER BY ORDINAL_POSITION";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, DATABASE);
            pstmt.setString(2, tableName);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> column = new HashMap<>();

                    String columnName = rs.getString("COLUMN_NAME");
                    String dataType = rs.getString("DATA_TYPE").toLowerCase();
                    String columnType = rs.getString("COLUMN_TYPE");

                    column.put("name", columnName);
                    column.put("propertyName", toCamelCase(columnName, false));
                    column.put("dataType", dataType);
                    column.put("charMaxLength", rs.getInt("CHARACTER_MAXIMUM_LENGTH"));
                    column.put("columnType", columnType);
                    column.put("comment", rs.getString("COLUMN_COMMENT"));
                    column.put("nullable", "YES".equals(rs.getString("IS_NULLABLE")));
                    column.put("isPrimary", "PRI".equals(rs.getString("COLUMN_KEY")));
                    column.put("autoIncrement", rs.getString("EXTRA").toLowerCase().contains("auto_increment"));

                    // 设置Java类型
                    String javaType = getJavaType(dataType, columnType);
                    column.put("javaTypeSimpleName", javaType.contains(".") ?
                            javaType.substring(javaType.lastIndexOf('.') + 1) : javaType);

                    // 设置特殊字段标识
                    column.put("isLogicDelete", isLogicDeleteField(columnName));
                    column.put("isTimestampField", isTimestampField(dataType));
                    column.put("isBaseEntityField", isBaseEntityField(columnName));

                    // 设置JDBC类型
                    column.put("jdbcType", getJdbcType(dataType));

                    columns.add(column);
                }
            }
        }

        return columns;
    }

    /**
     * 获取Java类型
     */
    private static String getJavaType(String dataType, String columnType) {
        dataType = dataType.toLowerCase();
        switch (dataType) {
            case "bigint":
                return "Long";
            case "int":
            case "integer":
            case "smallint":
                return "Integer";
            case "tinyint":
                if (columnType != null && columnType.toLowerCase().contains("tinyint(1)")) {
                    return "Boolean";
                }
                return "Integer";
            case "datetime":
            case "timestamp":
                return "LocalDateTime";
            case "date":
                return "LocalDate";
            case "bit":
                return "Boolean";
            case "decimal":
            case "numeric":
                return "java.math.BigDecimal";
            case "float":
                return "Float";
            case "double":
                return "Double";
            default:
                return "String";
        }
    }

    /**
     * 获取JDBC类型
     */
    private static String getJdbcType(String dataType) {
        dataType = dataType.toLowerCase();
        switch (dataType) {
            case "bigint":
                return "BIGINT";
            case "int":
            case "integer":
                return "INTEGER";
            case "tinyint":
                return "TINYINT";
            case "smallint":
                return "SMALLINT";
            case "char":
                return "CHAR";
            case "datetime":
            case "timestamp":
                return "TIMESTAMP";
            case "date":
                return "DATE";
            case "bit":
                return "BIT";
            case "decimal":
            case "numeric":
                return "DECIMAL";
            case "float":
                return "FLOAT";
            case "double":
                return "DOUBLE";
            default:
                return "VARCHAR";
        }
    }

    /**
     * 判断是否为逻辑删除字段
     */
    private static boolean isLogicDeleteField(String columnName) {
        String lowerName = columnName.toLowerCase();
        return "deleted".equals(lowerName);
    }

    /**
     * 判断是否为时间戳字段
     */
    private static boolean isTimestampField(String dataType) {
        dataType = dataType.toLowerCase();
        return "datetime".equals(dataType) ||
                "timestamp".equals(dataType) ||
                "date".equals(dataType);
    }

    /**
     * 判断是否为基类字段
     */
    private static boolean isBaseEntityField(String columnName) {
        String lowerName = columnName.toLowerCase();
        return "create_by".equals(lowerName) ||
                "create_name".equals(lowerName) ||
                "create_time".equals(lowerName) ||
                "update_by".equals(lowerName) ||
                "update_name".equals(lowerName) ||
                "update_time".equals(lowerName) ||
                "deleted".equals(lowerName);
    }

    /**
     * 转换为驼峰命名
     */
    private static String toCamelCase(String str, boolean firstUpper) {
        if (isEmpty(str)) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        String[] parts = str.split("_");

        for (int i = 0; i < parts.length; i++) {
            if (i == 0 && !firstUpper) {
                result.append(parts[i]);
            } else {
                if (!parts[i].isEmpty()) {
                    result.append(Character.toUpperCase(parts[i].charAt(0)));
                    if (parts[i].length() > 1) {
                        result.append(parts[i].substring(1).toLowerCase());
                    }
                }
            }
        }
        return result.toString();
    }

    /**
     * 转换为kebab-case格式
     */
    private static String convertToKebabCase(String str) {
        if (isEmpty(str)) {
            return str;
        }

        return str.toLowerCase().replace('_', '-');
    }

    /**
     * 判断字符串是否为空
     */
    private static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否不为空
     */
    private static boolean isNotBlank(String str) {
        return !isEmpty(str);
    }

    /**
     * 生成所有代码
     */
    private static void generateAllCode(Map<String, Object> context) throws Exception {
        logger.debug("开始生成代码，表信息: {}", context);

        // 创建目录
        createDirectories(context);

        // 定义需要生成的文件
        List<Object[]> filesToGenerate = Arrays.asList(
                new Object[]{"entity/entity.java.vm", context.get("entityPackage"), context.get("entityName") + "PO"},
                new Object[]{"mapper/mapper.java.vm", context.get("mapperPackage"), context.get("entityName") + "Mapper"},
                new Object[]{"mapper/mapper.xml.vm", "src/main/resources/mapper/" + context.get("module"), context.get("entityName") + "Mapper"},
                new Object[]{"controller/controller.java.vm", context.get("controllerPackage"), context.get("entityName") + "Controller"},
                new Object[]{"controller/request/createReq.java.vm", context.get("requestPackage"), context.get("entityName") + "CreateReq"},
                new Object[]{"controller/request/updateReq.java.vm", context.get("requestPackage"), context.get("entityName") + "UpdateReq"},
                new Object[]{"controller/request/pageQuery.java.vm", context.get("requestPackage"), context.get("entityName") + "PageQuery"},
                new Object[]{"controller/response/respVO.java.vm", context.get("responsePackage"), context.get("entityName") + "RespVO"},
                new Object[]{"service/service.java.vm", context.get("servicePackage"), context.get("entityName") + "Service"},
                new Object[]{"service/serviceImpl.java.vm", context.get("serviceImplPackage"), context.get("entityName") + "ServiceImpl"}
        );

        // 生成文件
        for (Object[] fileInfo : filesToGenerate) {
            String templateName = (String) fileInfo[0];
            String packageOrPath = (String) fileInfo[1];
            String fileName = (String) fileInfo[2];

            generateFile(templateName, packageOrPath, fileName, context);
        }
    }

    /**
     * 创建目录
     */
    private static void createDirectories(Map<String, Object> context) {
        // Java包目录
        String[] packageKeys = {"entityPackage", "mapperPackage", "controllerPackage",
                "requestPackage", "responsePackage", "servicePackage", "serviceImplPackage"};

        for (String key : packageKeys) {
            String pkg = (String) context.get(key);
            String dirPath = "src/main/java/" + pkg.replace('.', '/');
            createDirectory(dirPath);
        }

        // 资源目录
        String module = (String) context.get("module");
        String xmlDir = "src/main/resources/mapper/" + module;
        createDirectory(xmlDir);
    }

    /**
     * 创建目录
     */
    private static void createDirectory(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("创建目录失败: " + dirPath);
        }
    }

    /**
     * 生成文件
     */
    private static void generateFile(String templateName, String packageOrPath, String fileName,
                                     Map<String, Object> data) throws Exception {
        logger.debug("生成文件: template={}, package={}, fileName={}", templateName, packageOrPath, fileName);

        // 调试：检查模板变量
        logger.debug("模板变量检查:");
        logger.debug("  comment: {}", data.get("comment"));
        logger.debug("  entityName: {}", data.get("entityName"));
        logger.debug("  columns size: {}", data.get("columns") != null ? ((List<?>) data.get("columns")).size() : 0);

        VelocityContext context = new VelocityContext(data);

        // 确保模板路径正确
        String templatePath = "templates/" + templateName;
        logger.debug("加载模板: {}", templatePath);

        try {
            Template template = VELOCITY_ENGINE.getTemplate(templatePath, "UTF-8");

            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            String content = writer.toString();

            // 检查生成的内容
            if (content.trim().isEmpty()) {
                logger.warn("生成的内容为空: {}", fileName);
            }

            // 确定文件路径和扩展名
            File file = createFile(templateName, packageOrPath, fileName);

            // 确保目录存在
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(content);
                logger.info("  ✓ 生成: {}", file.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.error("生成文件失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 创建文件对象
     */
    private static File createFile(String templateName, String packageOrPath, String fileName) {
        String filePath;
        String extension = ".java";

        if (templateName.endsWith(".xml.vm")) {
            filePath = packageOrPath;
            extension = ".xml";
        } else {
            filePath = "src/main/java/" + packageOrPath.replace('.', '/');
        }

        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return new File(dir, fileName + extension);
    }

    /**
     * 初始化Velocity引擎
     */
    private static VelocityEngine initVelocityEngine() {
        VelocityEngine ve = new VelocityEngine();
        Properties props = new Properties();
        props.setProperty("resource.loader", "class");
        props.setProperty("class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        props.setProperty("input.encoding", "UTF-8");
        props.setProperty("output.encoding", "UTF-8");
        props.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
        props.setProperty("runtime.log.logsystem.log4j.category", "velocity");
        props.setProperty("runtime.log.error.stacktrace", "true");
        props.setProperty("runtime.log.warn.stacktrace", "true");
        props.setProperty("runtime.log.info.stacktrace", "false");
        props.setProperty("runtime.log.invalid.reference", "true");

        ve.init(props);
        return ve;
    }
}