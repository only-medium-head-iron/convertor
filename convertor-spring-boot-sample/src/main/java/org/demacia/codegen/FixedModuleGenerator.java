package org.demacia.codegen;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class FixedModuleGenerator {

    private static final VelocityEngine velocityEngine = initVelocity();
    private static final String BASE_PACKAGE = "org.demacia";
    private static final String COMMON_PACKAGE = BASE_PACKAGE + ".common";
    private static final String DATABASE = "wt_otms";

    // 配置表前缀，以逗号分隔
    private static final String TABLE_PREFIXES = "tms_";

    public static void main(String[] args) throws Exception {
        System.out.println("开始生成动态字段代码...");

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://rm-wz9osma53ib32tf1npo.mysql.rds.aliyuncs.com:6603/wt_otms?allowMultiQueries=true");
        dataSource.setUsername("omsroot");
        dataSource.setPassword("MysqlSAsls90");

        List<String> tables = Arrays.asList("tms_scheduling_main", "app_config");

        for (String tableName : tables) {
            System.out.println("正在生成表: " + tableName);

            // 解析表名（去除前缀）
            String tableNameWithoutPrefix = removeTablePrefix(tableName);

            System.out.println("原始表名: " + tableName);
            System.out.println("去除前缀后表名: " + tableNameWithoutPrefix);

            // 获取模块名（使用去除前缀后的表名）
            String module = getModuleName(tableNameWithoutPrefix);
            String entityName = toCamelCase(tableNameWithoutPrefix, true);
            String entityVariable = toCamelCase(tableNameWithoutPrefix, false);

            // 获取表注释
            String tableComment = getTableComment(dataSource, DATABASE, tableName);

            // 获取表字段信息
            List<Map<String, Object>> columns = fetchColumnDetails(dataSource, DATABASE, tableName);

            // 生成所有代码
            generateAllCode(module, tableName, tableNameWithoutPrefix, entityName, entityVariable, tableComment, columns);
        }

        System.out.println("所有代码生成完成！");
    }

    /**
     * 移除表前缀
     */
    private static String removeTablePrefix(String tableName) {
        String[] prefixes = TABLE_PREFIXES.split(",");
        for (String prefix : prefixes) {
            if (tableName.startsWith(prefix)) {
                return tableName.substring(prefix.length());
            }
        }
        return tableName;
    }

    /**
     * 获取模块名（以去除前缀后的第一个下划线分隔作为模块名）
     */
    private static String getModuleName(String tableNameWithoutPrefix) {
        if (tableNameWithoutPrefix == null || tableNameWithoutPrefix.isEmpty()) {
            return "common";
        }

        int underscoreIndex = tableNameWithoutPrefix.indexOf('_');
        if (underscoreIndex > 0) {
            return tableNameWithoutPrefix.substring(0, underscoreIndex);
        }

        return "common";
    }

    /**
     * 获取表注释
     */
    private static String getTableComment(HikariDataSource dataSource, String database, String tableName) {
        String tableComment = tableName + "表"; // 默认值

        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES " +
                    "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, database);
                pstmt.setString(2, tableName);

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String rawComment = rs.getString("TABLE_COMMENT");
                    if (rawComment != null && !rawComment.trim().isEmpty()) {
                        tableComment = rawComment.trim();
                        System.out.println("获取到表注释: " + tableComment);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("获取表注释失败: " + e.getMessage());
        }

        // 去掉最后一个"主表"字
        if (tableComment.endsWith("主表")) {
            tableComment = tableComment.substring(0, tableComment.length() - 2);
        }

        // 去掉最后一个"表"字
        if (tableComment.endsWith("表")) {
            tableComment = tableComment.substring(0, tableComment.length() - 1);
        }
        return tableComment;
    }

    private static List<Map<String, Object>> fetchColumnDetails(HikariDataSource dataSource, String database, String tableName) {
        List<Map<String, Object>> columns = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, COLUMN_TYPE, " +
                    "COLUMN_COMMENT, IS_NULLABLE, COLUMN_KEY, EXTRA " +
                    "FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
                    "ORDER BY ORDINAL_POSITION";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, database);
                pstmt.setString(2, tableName);

                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Map<String, Object> column = new HashMap<>();
                    String columnName = rs.getString("COLUMN_NAME");
                    String dataType = rs.getString("DATA_TYPE").toLowerCase();
                    column.put("name", columnName);
                    column.put("propertyName", toCamelCase(columnName, false));
                    column.put("dataType", dataType);
                    column.put("charMaxLength", rs.getInt("CHARACTER_MAXIMUM_LENGTH"));
                    column.put("columnType", rs.getString("COLUMN_TYPE"));
                    column.put("comment", rs.getString("COLUMN_COMMENT"));
                    column.put("nullable", "YES".equals(rs.getString("IS_NULLABLE")));
                    column.put("isPrimary", "PRI".equals(rs.getString("COLUMN_KEY")));
                    column.put("autoIncrement", rs.getString("EXTRA").toLowerCase().contains("auto_increment"));

                    // 判断字段类型
                    String javaType = getJavaType(dataType, rs.getString("COLUMN_TYPE"));
                    column.put("javaTypeSimpleName", javaType.contains(".") ?
                            javaType.substring(javaType.lastIndexOf('.') + 1) : javaType);

                    // 判断是否为逻辑删除字段
                    column.put("isLogicDelete", isLogicDeleteField(columnName));

                    // 判断是否为时间戳字段
                    column.put("isTimestampField", isTimestampField(dataType));

                    // 判断是否为时间戳字段
                    column.put("isBaseEntityField", isBaseEntityField(columnName));

                    // 添加jdbcType映射
                    String jdbcType = getJdbcType(dataType);
                    column.put("jdbcType", jdbcType);

                    columns.add(column);
                }
            }
        } catch (Exception e) {
            System.err.println("获取字段信息失败: " + e);
        }

        return columns;
    }

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

    private static boolean isLogicDeleteField(String columnName) {
        return columnName.equalsIgnoreCase("deleted") ||
                columnName.equalsIgnoreCase("is_deleted") ||
                columnName.toLowerCase().endsWith("_deleted");
    }

    private static boolean isTimestampField(String dataType) {
        dataType = dataType.toLowerCase();
        return "datetime".equals(dataType) ||
                "timestamp".equals(dataType) ||
                "date".equals(dataType);
    }

    private static boolean isBaseEntityField(String columnName) {
        columnName = columnName.toLowerCase();
        return "create_by".equals(columnName) ||
                "create_time".equals(columnName) ||
                "update_by".equals(columnName) ||
                "update_time".equals(columnName) ||
                "deleted".equals(columnName);
    }

    private static String toCamelCase(String str, boolean firstUpper) {
        if (str == null || str.isEmpty()) {
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

    private static void generateAllCode(String module, String originalTableName, String tableNameWithoutPrefix,
                                        String entityName, String entityVariable, String tableComment,
                                        List<Map<String, Object>> columns) throws Exception {
        // 准备上下文数据
        Map<String, Object> context = new HashMap<>();
        context.put("module", module);
        context.put("originalTableName", originalTableName);  // 原始表名
        context.put("tableNameWithoutPrefix", tableNameWithoutPrefix);     // 去除前缀后的表名
        context.put("entityName", entityName);
        context.put("entityVariable", entityVariable);
        context.put("comment", tableComment);
        context.put("columns", columns);
        context.put("basePackage", BASE_PACKAGE);
        context.put("commonPackage", COMMON_PACKAGE);

        // 包路径
        String entityPackage = BASE_PACKAGE + ".entity." + module;
        String mapperPackage = BASE_PACKAGE + ".mapper." + module;
        String controllerPackage = BASE_PACKAGE + ".controller." + module;
        String requestPackage = controllerPackage + ".request";
        String responsePackage = controllerPackage + ".response";
        String servicePackage = BASE_PACKAGE + ".service." + module;
        String serviceImplPackage = servicePackage + ".impl";

        context.put("entityPackage", entityPackage);
        context.put("mapperPackage", mapperPackage);
        context.put("controllerPackage", controllerPackage);
        context.put("requestPackage", requestPackage);
        context.put("responsePackage", responsePackage);
        context.put("servicePackage", servicePackage);
        context.put("serviceImplPackage", serviceImplPackage);

        // 设置TableDef常量名（使用原始表名，因为数据库查询需要）
        String tableConstantName = tableNameWithoutPrefix.toUpperCase();
        context.put("tableConstantName", tableConstantName);

        // 计算需要导入的注解
        boolean hasNotBlank = false;
        boolean hasNotNull = false;
        boolean hasSize = false;
        boolean hasMinMax = false;
        boolean hasDateField = false;
        boolean hasLocalDateTime = false;
        boolean hasLocalDate = false;
        boolean hasBigDecimal = false;

        for (Map<String, Object> column : columns) {
            if (!(Boolean) column.get("nullable") && "String".equals(column.get("javaTypeSimpleName"))) {
                hasNotBlank = true;
            }
            if (!(Boolean) column.get("nullable") && !"String".equals(column.get("javaTypeSimpleName"))) {
                hasNotNull = true;
            }
            if ("varchar".equals(column.get("dataType")) && (Integer) column.get("charMaxLength") > 0) {
                hasSize = true;
            }
            if ("Integer".equals(column.get("javaTypeSimpleName")) || "Long".equals(column.get("javaTypeSimpleName"))) {
                hasMinMax = true;
            }
            if ("LocalDateTime".equals(column.get("javaTypeSimpleName")) || "LocalDate".equals(column.get("javaTypeSimpleName"))) {
                hasDateField = true;
            }
            if ("LocalDateTime".equals(column.get("javaTypeSimpleName"))) {
                hasLocalDateTime = true;
            }
            if ("LocalDate".equals(column.get("javaTypeSimpleName"))) {
                hasLocalDate = true;
            }
            if ("BigDecimal".equals(column.get("javaTypeSimpleName"))) {
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

        // 创建目录
        createDirectories(context);

        // 生成所有文件
        String[][] files = {
                {"entity/entity.java.vm", entityPackage, entityName + "PO"},
                {"mapper/mapper.java.vm", mapperPackage, entityName + "Mapper"},
                {"mapper/mapper.xml.vm", "src/main/resources/mapper/" + module, entityName + "Mapper"},
                {"controller/controller.java.vm", controllerPackage, entityName + "Controller"},
                {"controller/request/createReq.java.vm", requestPackage, entityName + "CreateReq"},
                {"controller/request/updateReq.java.vm", requestPackage, entityName + "UpdateReq"},
                {"controller/request/pageQuery.java.vm", requestPackage, entityName + "PageQuery"},
                {"controller/response/respVo.java.vm", responsePackage, entityName + "RespVO"},
                {"service/service.java.vm", servicePackage, entityName + "Service"},
                {"service/serviceImpl.java.vm", serviceImplPackage, entityName + "ServiceImpl"}
        };

        for (String[] fileInfo : files) {
            generateFile(fileInfo[0], fileInfo[1], fileInfo[2], context);
        }
    }

    private static void createDirectories(Map<String, Object> context) {
        // Java包目录
        String[] packages = {
                (String) context.get("entityPackage"),
                (String) context.get("mapperPackage"),
                (String) context.get("controllerPackage"),
                (String) context.get("requestPackage"),
                (String) context.get("responsePackage"),
                (String) context.get("servicePackage"),
                (String) context.get("serviceImplPackage"),
        };

        for (String pkg : packages) {
            String dirPath = "src/main/java/" + pkg.replace('.', '/');
            File dir = new File(dirPath);
            if (!dir.exists()) {
                boolean flag = dir.mkdirs();
                if (!flag) {
                    throw new RuntimeException("创建目录失败：" + dirPath);
                }
            }
        }

        // 资源目录
        String module = (String) context.get("module");
        String xmlDir = "src/main/resources/mapper/" + module;
        File xmlDirFile = new File(xmlDir);
        if (!xmlDirFile.exists()) {
            boolean flag = xmlDirFile.mkdirs();
            if (!flag) {
                throw new RuntimeException("创建目录失败：" + xmlDir);
            }
        }
    }

    private static void generateFile(String templateName, String packageOrPath, String fileName,
                                     Map<String, Object> data) throws Exception {
        VelocityContext context = new VelocityContext(data);

        Template template = velocityEngine.getTemplate("templates/" + templateName, "UTF-8");
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        String content = writer.toString();

        // 确定文件路径和扩展名
        File file = createFile(templateName, packageOrPath, fileName);
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(content);
            System.out.println("  ✓ 生成: " + file.getAbsolutePath());
        }
    }

    private static File createFile(String templateName, String packageOrPath, String fileName) {
        String filePath;
        String extension = ".java";

        if (templateName.endsWith(".xml.vm")) {
            filePath = packageOrPath;  // 已经是完整路径
            extension = ".xml";
        } else {
            filePath = "src/main/java/" + packageOrPath.replace('.', '/');
        }

        File dir = new File(filePath);
        if (!dir.exists()) {
            boolean flag = dir.mkdirs();
            if (!flag) {
                throw new RuntimeException("创建目录失败：" + filePath);
            }
        }

        return new File(dir, fileName + extension);
    }

    private static VelocityEngine initVelocity() {
        VelocityEngine ve = new VelocityEngine();
        Properties props = new Properties();
        props.setProperty("resource.loader", "class");
        props.setProperty("class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        props.setProperty("input.encoding", "UTF-8");
        props.setProperty("output.encoding", "UTF-8");
        ve.init(props);
        return ve;
    }
}