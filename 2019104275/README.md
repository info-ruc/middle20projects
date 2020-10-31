# 服务端
### 项目简介

该项目为一个微博系统的服务端，包括用户的注册、登录、修改用户信息等；微博的发布、删除、浏览、更新、点赞、评论；且包含将热门微博缓存至redis以提高响应速度；将热门微博转发至kafka供其他应用模块消费使用等。微博的图片、视频等存储至腾讯云的对象存储；用户注册及修改密码等使用邮箱验证。另外，本项目还采用filter进行网关的模拟，以进行请求合法性的校验；采用aop实现日志的打印等。

登录流程图如下所示：



### 技术栈

springboot、mybatis

核心代码-controller,注解RestController声明该类为controller且返回json对象。注解Autowired声明该属性以来ioc自动注入。PostMapping声明该方法为post方法处理器。

```
@RestController
public class MsgController {
		@Autowired
    private MsgService msgService;

    // 创建
    @PostMapping("/msg/msg")
    public RetModel createMsg(@RequestHeader(Const.userId) String userId, Msg msg) throws Exception {

        String msgUid = msgService.addMsg(userId, msg);
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("msgUuid", msgUid);
        return Ret.ok(map);
    }
}
```

核心代码-dao，持久化框架使用mybatis，该框架只需声明java接口，采用注解select、update、delete、insert等实现操作数据库，mybatis会根据注解的值(sql)去执行sql。

```
@Mapper
public interface MsgDao {
	@Select("select * from msg where uuid = #{msgUuid} and deleted = 0")
	public Msg getMsg(String msgUuid);
}
```



### 数据库
本项目的数据库采用mysql。
### 中间件
缓存redis：负责缓存热门微博，以提高响应速度。

核心代码如下：

```
@Configuration
public class RedisConfig {
 
    @Bean
    public RedisTemplate<Serializable, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Serializable, String> template = new RedisTemplate<Serializable, String>();
        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();
        // redis存取对象的关键配置
        template.setKeySerializer(new StringRedisSerializer());
        // ObjectRedisSerializer类为java对象的序列化和反序列化工具类
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
    
    
		@Autowired
    private RedisTemplate<Serializable, String> redisTemplate;
 
    @RequestMapping("/setTest")
    public String setPOJO(){
 
        User user = new User();
        user.setAge("18");
        user.setGender("男");
        user.setNickname("cherish");
        user.setPassword("123456");
        user.setUsername("admin");
        redisTemplate.opsForValue().set("user1", user.toString();
        return "存储对象";
    }
}
```



消息队列kafka：将热门微博转发至kafka，供其他模块消费使用(如数据分析、热门记录等)。

核心代码如下：

```
public Obejct kafkaTest(){
 
        int events = 100;
        Properties props = new Properties();
        //集群地址，多个服务器用"，"分隔
        props.put("bootstrap.servers", "127.0.0.1:9092");
        //key、value的序列化，此处以字符串为例，使用kafka已有的序列化类
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        //props.put("partitioner.class", "com.kafka.demo.Partitioner");//分区操作，此处未写
        props.put("request.required.acks", "1");
        //创建生产者
        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < events; i++){
            String msg = user.toString
            //写入名为"test-partition-1"的topic
            User user = new User();
        		user.setAge("18");
        		user.setGender("男");
        		user.setNickname("cherish");
        		user.setPassword("123456");
        		user.setUsername("admin");
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>("test-partition-1", "key-"+i, msg);
            producer.send(producerRecord);
            System.out.println("写入test-partition-1：" + msg);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        producer.close();
        return "kafka success"
    }
```



### 涉及接口
用户接口：describeUser、register、login、updateUserInfo、
微博接口：createVLog、describeVLog、describeVLogs、deleteVLog、likeVLog、commentVLog

### 运行
1.配置腾讯云ak、sk，对象存储地域、存储桶id、url前缀。

```
qcloud.secretId=xxx
qcloud.secretKey=xxx
qcloud.bucketName=pasd-1259423553
qcloud.region=ap-beijing
qcloud.url=https://pasd-1259423553.cos.ap-beijing.myqcloud.com/
```

2.配置mysql地址，用户名和密码。

```
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/pasd?useUnicode=true&characterEncoding=utf8&useSSL=true&autoReconnect=true&failOverReadOnly=false
spring.datasource.username=root
spring.datasource.password=root
```

3.配置邮箱、授权码。

```
mail.sk=xxx
mail.server=smtp.163.com
mail.username=xxx@163.com
mail.nickname=xxx
```

### cmd
nohup java -jar xxx.war