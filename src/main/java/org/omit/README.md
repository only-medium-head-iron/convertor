### 时间转换
1、毫秒值转LocalDateTime：`LocalDateTimeUtil.of(product_date)`

2、字符串转LocalDateTime：`LocalDateTimeUtil.of(string_to_date(orderConfirmTime,'yyyy-MM-dd HH:mm:ss'))`

3、字符串转Date：`string_to_date(product_date, 'yyyy-MM-dd : HH:mm:ss')`

4、字符串转毫秒值：`getTime(string_to_date(params.timestamp, 'yyyy-MM-dd HH:mm:ss'))`

5、日期转字符串：`date_to_string(sysdate(),'yyyy-MM-dd HH:mm:ss')`

### 取值方式
1、尽量使用MAP取值方式

### 注意事项
1、入库单回执或推送下游场景与出库单DTO类型不一致：
入库实际表生产日期、到期日期类型是LocalDate，转换json后变为数组: [2019, 11, 30]，
source配置为：`StrUtil.join("-", productDate) + ' 00:00:00'`

2、双汇ERP重试需要多配置一条重试规则（因为签名在请求体中），参考：`select * from cim_rule_convert where rule_id = 'SHERP-erp.callback'`
    其他推送的只需要在现有`转换规则条件`处配置：`internalRetry == false`


3、新增接收方式处理类 DTO，需要新增字典：`delete from system_dict_type where name = '类全限定名' and type = 'cim_class_name';`