### 一、全局说明

- 针对所有规则
  - 条件字段（condition）：即满足条件才会执行该条规则，为空默认为满足条件
  - 排序字段（sort）：执行规则的顺序，也可以通过映射规则该配置定义转换后报文目标字段的顺序

- 完全自定义实现：可以继承**AbstractReceiveHandler**或**AbstractSendHandler**后重写**handle**方法

- 新增ApiService：接入方式（1-接入，2-推送）最好正确配置，规则查询界面有使用该字段

### 二、转换规则

#### 1、说明

- 定义转换后目标的层级，即以目标的层级来定义
- 每个层级需要配置一条转换规则，具体逻辑类似mybatis嵌套查询xml配置
- 每个子转换规则的源（source）都可以从父级转换规则的源（source）基础上取值，如果source是List，则必须从父级转换规则的源（source）基础上取值，不能直接从最外层params.开始取值
- 如果由xml格式切换为json格式时，因xml多一个节点，格式配置变更后需要将转换规则的source多余节点去除。
  - 例：params.items.item -> params.items

#### 2、源多层，目标一层

- <table style="border-collapse: collapse;">
  <tr>
    <td style="vertical-align: top; padding: 5px 10px 5px 5px; white-space: pre-wrap; font-family: monospace;">
      <pre>
  {
    "name": "John Doe",
    "age": 30,
    "address": {
      "street": "123 Main St",
      "city": "New York",
      "zipcode": "10001"
    },
  }
        </pre>
      </td>
      <td style="vertical-align: top; padding: 5px 10px 5px 5px; white-space: pre-wrap; font-family: monospace;">
        <pre>
  {
    "name": "John Doe",
    "age": 30,
    "street": "123 Main St",
    "city": "New York",
    "zipcode": "10001"
  }
        </pre>
      </td>
    </tr>
  </table>

- | 规则id | 父规则id |   源   | 目标 | 排序 |
    | :----: | :------: | :----: | :--: | :--: |
    |   1    |    0     | params | 不填 |  0   |

#### 3、源一层，目标多层

- <table style="border-collapse: collapse;">
  <tr>
    <td style="vertical-align: top; padding: 5px 10px 5px 5px; white-space: pre-wrap; font-family: monospace;">
      <pre>
  {
    "name": "John Doe",
    "age": 30,
    "street": "123 Main St",
    "city": "New York",
    "zipcode": "10001"
  }
        </pre>
      </td>
      <td style="vertical-align: top; padding: 5px 10px 5px 5px; white-space: pre-wrap; font-family: monospace;">
        <pre>
  {
    "name": "John Doe",
    "age": 30,
    "address": {
      "street": "123 Main St",
      "city": "New York",
      "zipcode": "10001"
    }
  }
        </pre>
      </td>
    </tr>
  </table>

- | 规则id | 父规则id |   源   |  目标   | 排序 |
  | :----: | :------: | :----: | :-----: | :--: |
  |   1    |    0     | params |  不填   |  0   |
  |   2    |    1     |  不填  | address |  1   |

#### 4、源数组，目标数组

- <table style="border-collapse: collapse;">
  <tr>
    <td style="vertical-align: top; padding: 5px 10px 5px 5px; white-space: pre-wrap; font-family: monospace;">
      <pre>
  {
      "studentId": "S001",
      "name": "张三",
      "age": 16,
      "province": "广东省",
      "city": "深圳市",
      "courses": [
          {
              "courseId": "C001",
              "courseName": "数学",
              "teacher": "李老师",
              "schedule": [
                  {
                      "day": "周一",
                      "time": "09:00-11:00"
                  },
                  {
                      "day": "周三",
                      "time": "14:00-16:00"
                  }
              ]
          },
          {
              "courseId": "C002",
              "courseName": "英语",
              "teacher": "王老师",
              "schedule": [
                  {
                      "day": "周二",
                      "time": "10:00-12:00"
                  },
                  {
                      "day": "周四",
                      "time": "15:00-17:00"
                  }
              ]
          }
      ]
  }
        </pre>
      </td>
      <td style="vertical-align: top; padding: 5px 10px 5px 5px; white-space: pre-wrap; font-family: monospace;">
        <pre>
  {
      "studentId": "S001",
      "name": "张三",
      "age": 16,
      "address": {
          "province": "广东省",
          "city": "深圳市"
      },    
      "myCourses": [
          {
              "courseId": "C001",
              "courseName": "数学",
              "teacher": "李老师",
              "schedule": [
                  {
                      "day": "周一",
                      "time": "09:00-11:00"
                  },
                  {
                      "day": "周三",
                      "time": "14:00-16:00"
                  }
              ]
          },
          {
              "courseId": "C002",
              "courseName": "英语",
              "teacher": "王老师",
              "mySchedule": [
                  {
                      "day": "周二",
                      "time": "10:00-12:00"
                  },
                  {
                      "day": "周四",
                      "time": "15:00-17:00"
                  }
              ]
          }
      ]
  }
        </pre>
      </td>
    </tr>
  </table>

- | 规则id | 父规则id |    源    |    目标    | 排序 |
  | :----: | :------: | :------: | :--------: | :--: |
  |   1    |    0     |  params  |    不填    |  0   |
  |   2    |    1     |   不填   |  address   |  1   |
  |   3    |    1     | courses  | myCourses  |  2   |
  |   4    |    3     | schedule | mySchedule |  3   |
  |   ⋮    |    ⋮     |    ⋮     |     ⋮      |  ⋮   |

  （**注：其中第三条，courses节点也能配置成params.courses是因为courses节点的父节点不是List。如果父节点是List，如schedule节点，则源必须配置成schedule**）

#### 5、二次处理

- 对转换后的目标进行二次处理，配置一条平级结构的转换规则

  - ```json
    {
        "storage_location": "0005",
        "receipt_time": "2024-11-21 10:34:40",
        "origin_no": "PO7072224BLC0010000",
        "receipt_user_name": "万通wms",
        "detail_list": "[{\"stock_type\":\"OK\",\"ware_count\":2000,\"unit\":\"EA\",\"row_num\":\"20\",\"production_date\":\"2024-11-2 00:00:00\",\"expiry_date\":\"2025-5-1 00:00:00\",\"ware_code\":\"ZL0002\"},{\"stock_type\":\"OK\",\"ware_count\":500,\"unit\":\"EA\",\"row_num\":\"10\",\"production_date\":\"2024-11-1 00:00:00\",\"expiry_date\":\"2025-1-20 00:00:00\",\"ware_code\":\"ZL0001\"}]",
        "bill_no": "IB2024112100001239279",
        "receipt_user_id": "1",
        "uuid": "c28c48be-c45c-41b4-95a3-a803d5b8e60a",
        "dispatch_type": "10",
        "po_no": "PO7072224BLC0010000"
    }
    ```

  - | 规则id | 父规则 |                            源                             |    目标     | 排序 |
    | :----: | :----: | :-------------------------------------------------------: | :---------: | :--: |
    |   1    |   0    |                          params                           |    不填     |  0   |
    |   2    |   1    |                      outboundDetails                      | detail_list |  1   |
    |   3    |   1    | target.detail_list=JSONUtil.toJsonStr(target.detail_list) | detail_list |  2   |


### 三、映射规则

#### 1、校验表达式

- 对源字段进行表达式校验，例（下表格）：实际收货数量不大于零时，会返回报错：收货数量必须大于零（**注：是否必填和校验表达式只能二选一**）
  - |    源    |   目标    |         校验表达式          | 是否必填 |      报错信息      |
    | :------: | :-------: | :-------------------------: | :------: | :----------------: |
    | goodsQty | actualQty | actQty != nil && actQty > 0 |    否    | 收货数量必须大于零 |

#### 2、取值方式

- 如果需要写死常量，可以选择固定值取值，或者填写默认值，建议选择固定值取值方式

  - |      源       |  目标  | 默认值 |  取值方式   |
    | :-----------: | :----: | :----: | :---------: |
    |      200      | status |  不填  |  取固定值   |
    | 不填/没取到值 | status |  200   | Aviator取值 |
    | 不填/没取到值 | status |  200   |   Map取值   |

    默认值：当源不填或者取值方式为Aviator取值、Map取值时，没有取到值，会返回默认值

- 尽量使用MAP取值方式，**提高性能**，一对一映射的可以使用map取值方式

- 包含表达式的只能使用Aviator取值方式。如appApi.appCode、params.reqMsg

#### 3、接收请求

- 请求映射-REQ

  - 应用级别规则，rule_code一般为appCode
  - 适用于接收请求
- 请求报文格式（format）：定义接收请求时请求报文格式`format`，支持json、xml，不配置默认为`json`（**注：推送请求报文格式由`ApiService`配置决定**，支持json、xml、表单提交）
  
  - 服务编码（apiCode）：**必须配置**，定义具体的接口，根据对方请求方式决定
- 其他字段定义见代码`Req`类
- 前置映射-PRE
  - 接口级别规则，rule_code一般为appCode + apiCode
  - 适用于接收请求
  - 其他字段定义见代码`Pre`类
  
- 转换映射-DTO
  - 具体报文转换逻辑
  - 通过rule_code与具体的转换规则的id对应，即每个转换规则都可以配置对应的映射

#### 4、推送请求

- 转换映射-DTO
  - 具体报文转换逻辑

  - 通过rule_code与具体的转换规则的id对应，即每个转换规则都可以配置对应的映射
- 请求头映射-RHD
  - 应用级别规则，rule_code一般为appCode
  - 适用于推送请求
  - 请求外部时需要放置请求头参数

- 地址栏映射-URL
  - 应用级别规则，rule_code一般为appCode
  - 适用于推送请求
  - 请求外部时需要拼接在地址栏的参数，即问号后面的参数。例：http://localhost:8080/api?method=deliveryOrder.create


#### 5、上下文取值

- 如果在转换过程中后面的映射规则需要用到前面映射规则目标字段值，可以将目标字段添加 “#” 号，后面映射规则取值时可以通过temp取出。（**注：目前temp只有一层结构，如果在List中这样处理，temp中存储的是最后一个值**）

  - |           源            |      目标       | 排序 |
    | :---------------------: | :-------------: | :--: |
    | IdUtil.fastSimpleUUID() | **outboundNo#** |  0   |
    |   **temp.outboundNo**   |      bizNo      |  1   |


### 四、合并规则

- 说明：对转换后的List进行合并，通过rule_code与具体的转换规则的id对应，即每个转换规则都可以配置对应的合并规则

- 简单合并逻辑配置：配置分组字段、合并字段、排序字段。（**注：以目标字段配置**）
  - | 合并处理器 |     分组字段     |       合并字段        |           排序字段           |
    | :--------: | :--------------: | :-------------------: | :--------------------------: |
    |    不填    | lineNo,goodsCode | actualQty,totalAmount | productDate desc,exprireDate |
  
  - | lineNo | goodsCode | actualQty | totalAmount | productDate | exprireDate |
    | ------ | --------- | --------- | ----------- | ----------- | ----------- |
    | 1      | wtbh-01   | 3         | 1.00        | 20250101    | 20260101    |
    | 1      | wtbh-01   | 2         | 3.00        | 20250102    | 20260102    |
    | 2      | wtbh-02   | 3         | 7.00        | 20250102    | 20260101    |
    | 2      | wtbh-02   | 4         | 2.00        | 20250102    | 20260102    |
  
    合并后变成
  
  - | lineNo | goodsCode | actualQty | totalAmount | productDate | exprireDate |
    | ------ | --------- | --------- | ----------- | ----------- | ----------- |
    | 1      | wtbh-01   | 5         | 4.00        | 20250102    | 20260102    |
    | 2      | wtbh-02   | 7         | 9.00        | 20250102    | 20260101    |
  
- 复杂合并逻辑配置：扩展接口`MergeProcessor`，使用时直接继承`AbstractMergeProcessor`即可实现自定义的合并逻辑
  - |      合并处理器      | 分组字段 | 合并字段 | 排序字段 |
    | :------------------: | :------: | :------: | :------: |
    | customMergeProcessor |   不填   |   不填   |    0     |

### 五、校验规则

- 说明：对转换后的对象进行校验

- 简单校验逻辑配置：配置校验表达式，校验转换后的目标对象是否满足条件，如不满足，则抛出异常，异常信息可自定义，不配置则抛出默认异常”不满足条件【XXX表达式】“
  - | 校验处理器 |                    校验表达式                    |     报错信息     |
    | :--------: | :----------------------------------------------: | :--------------: |
    |    不填    | packages != nil && CollUtil.isNotEmpty(packages) | 包裹信息不能为空 |
- 复杂校验逻辑配置：扩展接口`ValidateProcessor`，使用时直接继承`AbstractValidateProcessor`即可实现自定义的校验逻辑（**注：合并处理器和校验表达式可以同时配置，会先执行校验表达式校验**）
  - |       校验处理器        |                    校验表达式                    |     报错信息     |
    | :---------------------: | :----------------------------------------------: | :--------------: |
    | customValidateProcessor | packages != nil && CollUtil.isNotEmpty(packages) | 包裹信息不能为空 |

### 六、任务编排

- ```java
  public abstract class AbstractReceiveHandler implements ReceiveHandler {
  
      @Resource
      private Convertor convertor;
  
      @Resource
      protected RcvMapperStep rcvMapperStep;
  
      @Resource
      protected OwnerStep ownerStep;
  
      @Resource
      protected WarehouseStep warehouseStep;
  
      private LinkedList<Step> steps;
  
      /**
       * 重写handle方法，用于处理数据转换的整个流程
       *
       * @param context 上下文对象，包含需要处理的数据和规则ID
       * @return 转换后的对象
       */
      @Override
      public Object handle(Context context) {
          beforeConvert(context);
          Object object = convertor.convert(context.getRuleId(), BeanUtil.beanToMap(context));
          afterConvert(object);
          return object;
      }
  
      /**
       * 初始化步骤链，将步骤添加到链表中，以确定步骤的顺序
       */
      @PostConstruct
      public void init() {
          this.steps = CollUtil.newLinkedList(rcvMapperStep, ownerStep, warehouseStep);
      }
  
      /**
       * 在转换过程开始之前执行一系列步骤
       *
       * @param context 转换上下文，包含可能需要传递给各个步骤的必要信息
       */
      public void beforeConvert(Context context) {
          for (Step step : steps) {
              step.run(context);
          }
      }
  ```

- 需要不同的步骤，可以继承**AbstractReceiveHandler**或**AbstractSendHandler**后重写**init**和**beforeConvert**方法进行自定义编排任务

- 后续有需要可以扩展为表存储来进行任务动态编排

### 七、取上下文

- ```java 
  // 在请求完成之前都可以通过ContextHolder获取上下文
  Context context = ContextHolder.get()
  ```

### 八、操作界面

- 接入方式：可以通过配置类全限定名来自动展开映射规则配置模板，不用再考虑层级结构
- 推送方式：可以将对方需要的报文配置成首条转换规则的模板，查询时自动按层级展开，不用再考虑层级结构

### 时间转换

- 毫秒值转**LocalDateTime**：`LocalDateTimeUtil.of(product_date)`

- 字符串转**LocalDateTime**：`LocalDateTimeUtil.of(string_to_date(orderConfirmTime,'yyyy-MM-dd HH:mm:ss'))`

- 字符串转**Date**：`string_to_date(product_date, 'yyyy-MM-dd : HH:mm:ss')`

- 字符串转毫秒值：`getTime(string_to_date(params.timestamp, 'yyyy-MM-dd HH:mm:ss'))`

- **Date**转字符串：`date_to_string(sysdate(),'yyyy-MM-dd HH:mm:ss')`
- **LocalDate**转字符串：`LocalDateTimeUtil.format(product_date,'yyyy-MM-dd HH:mm:ss')`

### 注意事项
- 推送场景：原入库单与出库单定义的DTO（OutboundHeaderPushReqDTO）字段类型不一致：
  - 入库实际表生产日期、到期日期类型是LocalDate，出库为LocalDateTime。转换json后变为数组: [2019, 11, 30]，source配置为：`StrUtil.join("-", productDate) + ' 00:00:00'`
- 推送场景：原入库单与出库单定义的DTO（InboundDataPushReqDTO）层级结构不一致：入库单多了一层，所以在配置入库出库都适用的规则时，需要注意添加一层
- 重试场景：双汇ERP重试需要多配置一条重试规则（因为签名在请求体中）
  - 参考：`select * from cim_rule_convert where rule_code = 'SHERP-erp.callback'`
  - 其他推送的重试只需要在现有`转换规则-条件`处配置：`internalRetry == false`
- 新增Handler：如有新增接收 DTO，需要新增字典：`select * from system_dict_type where name = '类全限定名' and type = 'cim_class_name';`

### 常见报错

- 没有找到属性：通常为表达式中存在函数
  - 入参为null，如果该字段非必填，可以通过配置条件（condition）：xxx != nil 来跳过该条规则
  - 如果是Java中函数，一般为参数类型不正确，或者是参数个数不正确，因为java中是根据参数类型和个数寻找重载方法