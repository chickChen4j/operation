


### 运维管理平台使用说明

本项目需要先本地install打包并放入本地.m2仓库对应目录,引入之后可以访问页面对项目进行运维管理操作


##### 配置步骤

1. 引入运维管理平台的jar包
  
    ```xml

    <dependency>
      <groupId>com.chickChen</groupId>
      <artifactId>operation</artifactId>
      <version>0.0.1-RELEASE</version>
    </dependency>

    ```

2. 客户端配置
   
    ```yml

      operation:
        statViewServlet:
          loginUsername: f171477 # 配置用户名,该账户为管理员账号
          loginPassword: Spring01 # 配置登录密码 当为空时,默认密码为Spring01
        prefix: operation # 后端路径的前缀 默认为operation
        table: 
          prefix: operation # 系统生成的四张表的前缀 默认为operation
        xml:
          name: operation # 客户端读取resources目录下xml文件的名称,当为空时,默认为operation
        deny: 127.0.0.1,127.0.0.2 # 访问ip黑名单
        allow: 127.0.0.1,127.0.0.2 # 访问ip白名单
        export:
          url: <downloadCenter> #下载中心地址
      
    ```   

    > 注: 该项目需要定义${spring.profiles.active},默认为dev  

##### xml配置文件

 **1.介绍** 

   在resources目录下,创建名称为${operation.xml.name}(默认为operation)的xml格式文件,jar包会读取该文件,并根据其创建对应接口.

 **2.标签**

| 标签名               | 描述                                                           |
| -------------------- | -------------------------------------------------------------- |
| OperationApplication | 配置文件的根级标签                                             |
| api                  | 单个api对应一个或一组接口,即单个api标签包含单个或多个sql类标签 |
| insert               | sql类标签,包含单个insert语句                                   |
| update               | sql类标签,包含单个update语句                                   |
| delete               | sql类标签,包含单个delete语句                                   |
| select               | sql类标签,包含单个select语句                                   |

 **3.属性**

| 属性名        | 对应标签                    | 描述                                                                                                                                                                                         |
| ------------- | --------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| id            | api                         | api的唯一标识,同时作为接口地址的一部分.比如restful请求时,请求地址${operation.prefix}/rest/${id}.如果id重复,默认读取最后一个api                                                               |
| type          | api                         | api的类型,目前有两种:restful和base                                                                                                                                                           |
| bean          | api                         | 在type为restful时必填,为base时不填,值为该restful接口所对应资源类的全限定名                                                                                                                   |
| primaryKey    | api                         | 在type为restful时必填, 值为该restful接口所对应资源类的主键名                                                                                                                                 |
| url           | api                         | 在type为base时可填,url匹配规则为${operation.prefix}/${id}/${url}                                                                                                                             |
| parameterType | insert,update,select,delete | 参数类型,对应sql中传入的值,默认为api中定义的bean类型                                                                                                                                         |
| resultType    | select                      | 返回值类型,默认为api中定义的bean类型                                                                                                                                                         |
| type          | select                      | 有one,list,page三类,one代表根据主键查询,list代表全量查询,page代表分页查询.默认为list.其中list和page用于前端页面展示,list为前端分页,page为后端分页.one用于update和delete前的校验.推荐使用page |
> 注:当api为restful类型时,如果配置重复,则选第一个,比如出现多个insert,则默认执行第一个

**4.示例**
   
 - restful风格api
  
```xml
<OperationApplication>
  <api id="status" groupId="订单" type="restful" bean="com.efivestar.bpm.dto.StatusDTO"
    primaryKey="id">
    <insert>
      insert into
      process_status
      (status,process_ticket_id)
      values
      ( #{status},#{processTicketId})
    </insert>
    <delete>
      delete from process_status where id = #{id}
    </delete>
    <update>
      update process_status
      <set>
        <if test="status!=null">status=#{status},</if>
        <if test="responder!=null">responder=#{responder},</if>
        <if test="suggestion!=null">suggestion=#{suggestion},</if>
        <if test="suggestionTime!=null">suggestion_time=#{suggestionTime},</if>
        <if test="approvalPlatform!=null">approval_platform=#{approvalPlatform}</if>
      </set>
      where id=#{id}
    </update>
    <select type="one">
      select * from process_status
      where id = #{id}
    </select>
    <select type="list">
      select * from process_status
      <where>
        <if test="id!=null">
          id = #{id}
        </if>
        <if test="responder!=null">
          AND responder = #{responder}
        </if>
        <if test="status!=null">
          AND status=#{status}
        </if>
        <if test="suggestion!=null and suggestion !=''">
          AND suggestion like "%${suggestion}%"
        </if>
        <if test="approvalPlatform!=null">
          AND approvalPlatform=#{approvalPlatform}
        </if>
      </where>
    </select>
    <select type="page">
      select * from process_status
      <where>
        <if test="id!=null">
          id = #{id}
        </if>
        <if test="responder!=null">
          AND responder = #{responder}
        </if>
        <if test="status!=null">
          AND status=#{status}
        </if>
        <if test="suggestion!=null and suggestion !=''">
          AND suggestion like "%${suggestion}%"
        </if>
        <if test="approvalPlatform!=null">
          AND approvalPlatform=#{approvalPlatform}
        </if>
      </where>
      <if test="sortName!=null">
        order by ${sortName} ${sortOrder}
      </if>
      limit #{number},#{size}
    </select>
  </api>
</OperationApplication>

```

- base风格api
  
```xml
<OperationApplication>
  <api id="findCoupon" groupId="优惠券" url="/select" type="base">
      <select type="list">
        select * from coupon
        <where>
          <if test="coupon_money!=null">coupon_money = #{coupon_money}</if>
          <if test="coupon_code!=null">AND coupon_code = #{coupon_code}</if>
          <if test="coupon_id!=null">AND coupon_id = #{coupon_id}</if>
        </where>
      </select>
    </api>
</OperationApplication>

```

**5.注解**

  如果需要对表单和接口做更详尽的配置,可以使用@Describe和@Condition,需要在指定bean的属性上使用注解,故只针对rest风格API.

 ```java
 public class SqlDTO {

  /**
   * 主键
   */
  private Long id;

  /**
   * 类型(insert,update,delete,select四种)
   */
  @Describe(alias = "SQL类型", required = true, desc = "请选择SQL类型")
  private SqlType type;

  /**
   * 具体的sql语句
   */
  @Describe(alias = "SQL语句", desc = "请填写SQL语句", required = true, condition = {
      @Condition(type = SqlType.INSERT, value = "value!=null&&value!=''")})
  private String value;


  /**
   * 参数类型
   */
  @Describe(alias = "参数类型", desc = "请填写全限定类名")
  private String parameterType;


  /**
   * 查询的类型(one,list,page三种)
   */
  @Describe(alias = "查询方式", required = true, desc = "请选择查询类型")
  private SelectType selectType;

  /**
   * 返回值类型
   */
  @Describe(alias = "返回类型", desc = "请填写返回类型")
  private String resultType;

  /**
   * 外键(api与sql是一对多关系)
   */
  @Describe(alias = "API", desc = "请选择对应的API", required = true, relate = "api",relateField="name")
  private Long apiId;
}

 ```
 - @Describe中,desc表示新增弹窗中表单水印信息;alias是表单对应的文本;required表示表单信息是否必须，用于前端校验;relate用于外键字段,值为关联的apiName;relateField表示外键关联展示的字段,需要先指定relate,默认取关联api的primaryKey值;condition用于后端校验,@Condition中type是SqlType的枚举值,表示用于增删改查接口,value为SpEL表达式,用于后端接口对参数进行校验.

##### 操作方法

1. 访问客户端项目地址下的/admin-console进入登录页面,使用域账号密码进行登录,登录成功后进入主界面.
   
2. 当配置了select语句,界面上显示分页查询结果的表格, 在表单中输入查询条件,点击查询按钮,表格中显示查询结果.
   
3. 当配置了insert语句,界面上显示新增按钮,点击新增按钮,输入相应的参数,点击确定,完成新增操作.
   
4. 当配置了update语句,表格中显示操作列,点击对应编辑按钮,修改对应参数,点击确定,完成编辑操作.
   
5. 当配置了delete语句,表格中显示操作列,点击对应删除按钮,即可删除该行数据.
   
6. 用户在运维管理平台的操作会被记录在operation_record表中,下载任务信息会被记录在operation_export_task表中.
   
7. 用户还可以通过配置界面动态创建api和sql的配置信息,并记录在operation_api_config和operation_sql_config表中,配置项参考xml配置.(xml配置的信息无法在配置界面读取到)
   
> 注: 如果项目中有权限拦截,需要忽略/admin-console/** ,/xml/config,/favicon.ico
>  ,${operation.prefix}/** (默认为/operation/**)


            




