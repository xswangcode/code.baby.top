# Baby Middleware MQTT Starter 开发说明

## 1. 项目简介

Baby Middleware 是一套面向 Spring Boot 项目的中间件 Starter。

当前 MQTT Starter：

- Spring Boot：2.6.13
- Java：8
- Spring Integration MQTT：5.5.10
- Eclipse Paho：1.2.5
- Maven
- MQTT 协议：MQTT 3.x

模块：

```
baby-core
baby-mqtt-spring-boot-starter
baby-mqtt-test
```

其中：

- `baby-core`：公共基础能力
- `baby-mqtt-spring-boot-starter`：MQTT Starter
- `baby-mqtt-test`：MQTT Starter 外部集成测试项目

---

## 2. MQTT Starter 整体架构

整体消息链路：

```
                    MQTT Broker
                         │
                         ▼
                Eclipse Paho Client
                         │
                         ▼
          Spring Integration MQTT
                         │
                         ▼
       MqttPahoMessageDrivenChannelAdapter
                         │
                         ▼
              babyMqttInputChannel
                         │
                         ▼
              MqttHandlerRegistry
                         │
                         ▼
               MqttTopicMatcher
                    /          \
                 List           Trie
                    \          /
                         │
                         ▼
                    MqttHandler
                         │
                         ▼
                    业务代码
```

核心设计思想：

> MQTT 连接、订阅、消息接收、Topic 匹配和 Handler 管理由 Starter 统一负责，业务项目只需要实现 Handler。

---

## 3. Paho 和 Spring Integration 的关系

MQTT Starter 实际上同时使用了：

- Eclipse Paho
- Spring Integration MQTT

两者不是二选一。

### 3.1 Eclipse Paho

Paho 是底层 MQTT Client。

负责：

- MQTT Broker 连接
- MQTT 协议通信
- 发布消息
- 接收消息
- MQTT Keep Alive
- 自动重连等底层能力

### 3.2 Spring Integration MQTT

Spring Integration MQTT 在 Paho 之上进行了 Spring 化封装。

主要负责：

- Spring Bean 管理
- MQTT Inbound Adapter
- MQTT Outbound Handler
- MessageChannel
- 消息转换
- 生命周期管理
- 与 Spring Integration 消息体系整合

因此当前架构是：

```
业务代码
   │
   ▼
Baby MQTT Starter
   │
   ▼
Spring Integration MQTT
   │
   ▼
Eclipse Paho
   │
   ▼
MQTT Broker
```

业务项目不需要直接操作 Paho Client。

---

## 4. 项目结构

```
code.baby.top
│
├── dependency-management
│   └── pom.xml
│
├── baby-core
│   ├── pom.xml
│   └── src/main/java/baby/top/core
│       ├── common
│       │   ├── ComponentState.java
│       │   └── Lifecycle.java
│       │
│       ├── exception
│       │   ├── BabyErrorCode.java
│       │   └── BabyException.java
│       │
│       └── utils
│           └── BabyAssert.java
│
├── baby-mqtt-spring-boot-starter
│   ├── pom.xml
│   └── src/main/java/baby/top/mqtt
│       ├── autoconfigure
│       ├── client
│       ├── handler
│       ├── matcher
│       ├── properties
│       └── template
│
└── baby-mqtt-test
    ├── pom.xml
    └── src
```

---

## 5. Maven 引入

业务项目只需要引入：

```xml

<dependency>
    <groupId>baby.top</groupId>
    <artifactId>baby-mqtt-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

不需要业务项目自己引入：

- Paho
- Spring Integration MQTT

Starter 会自动提供这些依赖。

---

## 6. MQTT 基础配置

最简单的配置：

```yaml
baby:
  mqtt:
    enabled: true
    broker: tcp://192.168.0.250:1883
    username: admin
    password: public
    client-id: baby-mqtt-test
    auto-reconnect: true
    connection-timeout: 10
    keep-alive: 60
    matcher-type: trie

    subscriptions:
      - name: robotSleep
        topic: produce/+/robot_sleep_mode/+
        handler: testMqttHandler
```

---

## 7. 配置参数说明

配置前缀：

```
baby.mqtt
```

### 7.1 enabled

```yaml
enabled: true
```

是否启用 MQTT Starter。

默认：

```yaml
enabled: false
```

### true

自动创建 MQTT 相关 Bean，并建立 MQTT 连接。

### false

MQTT Starter 不启动。

适用于：

- 开发环境暂时关闭 MQTT
- 单元测试
- 不需要 MQTT 的服务

---

### 7.2 broker

```yaml
broker: tcp://192.168.0.250:1883
```

MQTT Broker 地址。

例如：

```
tcp://192.168.0.250:1883
```

TLS：

```
ssl://192.168.0.250:8883
```

该配置最终会交给 Paho MQTT Client。

---

### 7.3 username

MQTT 登录用户名。

```yaml
username: admin
```

如果 Broker 不需要用户名，可以不配置。

---

### 7.4 password

MQTT 登录密码。

```yaml
password: public
```

生产环境建议通过：

- 环境变量
- 配置中心
- Secret

提供，不建议直接提交到 Git。

---

### 7.5 client-id

MQTT Client ID。

```yaml
client-id: baby-mqtt-test
```

Broker 会通过 Client ID 区分客户端。

生产环境建议保证唯一。

例如：

```
baby-production-robot-01
baby-production-robot-02
```

---

### 7.6 auto-reconnect

是否自动重连。

```yaml
auto-reconnect: true
```

推荐生产环境开启：

```yaml
auto-reconnect: true
```

Broker 网络异常时，Paho 会尝试重新连接。

---

### 7.7 connection-timeout

MQTT 建立连接的超时时间。

```yaml
connection-timeout: 10
```

单位：

```
秒
```

例如：

```yaml
connection-timeout: 10
```

表示连接 Broker 最多等待 10 秒。

---

### 7.8 keep-alive

MQTT Keep Alive。

```yaml
keep-alive: 60
```

单位：

```
秒
```

用于维护 MQTT 长连接。

一般：

```
30
60
120
```

都比较常见。

---

### 7.9 matcher-type

Topic 匹配器类型。

```yaml
matcher-type: list
```

或者：

```yaml
matcher-type: trie
```

当前支持：

- list
- trie

默认：

```yaml
matcher-type: list
```

---

## 8. List Matcher

List Matcher 使用简单的 Topic 列表进行匹配。

适合：

- Topic 数量较少
- 系统规模较小
- 规则变化不频繁

例如：

```yaml
matcher-type: list
```

Topic：

```
produce/+/robot_sleep_mode/+
produce/+/alarm/+
produce/+/energy/+
```

收到消息后依次进行匹配。

优点：

- 实现简单
- 代码容易理解
- 调试方便

缺点：

- Topic 数量较大时需要遍历大量规则

---

## 9. Trie Matcher

Trie Matcher 使用 Topic 树结构进行匹配。

配置：

```yaml
matcher-type: trie
```

例如：

```
produce
  │
  ├── +
  │    │
  │    ├── robot_sleep_mode
  │    │       │
  │    │       └── +
  │    │
  │    ├── alarm
  │    │       │
  │    │       └── +
  │    │
  │    └── energy
  │            │
  │            └── +
```

适合：

- Topic 数量较多
- Topic 层级复杂
- 系统规模较大

生产环境推荐：

```yaml
matcher-type: trie
```

---

## 10. MqttHandler

业务项目处理 MQTT 消息，需要实现：

```java
public interface MqttHandler {

    String getName();

    void handle(
            String topic,
            String payload
    );
}
```

只有两个核心方法：

### getName()

返回 Handler 唯一名称。

### handle()

处理 MQTT 消息。

---

## 11. Handler 示例

例如创建：

```
TestMqttHandler
```

代码：

```java
package baby.top.test.handler;

import baby.top.mqtt.handler.MqttHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TestMqttHandler
        implements MqttHandler {

    @Override
    public String getName() {
        return "testMqttHandler";
    }

    @Override
    public void handle(
            String topic,
            String payload) {

        log.info(
                "收到 MQTT 消息，topic: {}, payload: {}",
                topic,
                payload
        );
    }
}
```

重点：

```java
@Component
```

必须让 Spring 扫描到 Handler。

---

## 12. Handler 不负责 Topic

当前设计中：

```java
MqttHandler
```

不包含：

```java
getTopic()
```

也就是说 Handler 只负责：

> 收到消息以后怎么处理。

Topic 负责：

> 什么消息应该交给哪个 Handler。

这样可以实现：

```
一个 Handler
    │
    ├── Topic A
    ├── Topic B
    └── Topic C
```

也可以：

```
Topic A
   │
   ├── Handler A
   └── Handler B
```

Handler 与 Topic 解耦。

---

## 13. subscriptions

配置：

```yaml
subscriptions:
  - name: robotSleep
    topic: produce/+/robot_sleep_mode/+
    handler: testMqttHandler
```

每个 Subscription 包含：

| 参数      | 说明         |
|---------|------------|
| name    | 当前订阅规则名称   |
| topic   | MQTT Topic |
| handler | Handler 名称 |

其中真正决定消息分发关系的是：

```
topic
+
handler
```

---

## 14. Subscription 与 Handler 的关系

例如：

```yaml
subscriptions:
  - name: robotSleep
    topic: produce/+/robot_sleep_mode/+
    handler: testMqttHandler
```

表示：

```
produce/+/robot_sleep_mode/+
              │
              ▼
      testMqttHandler
```

收到：

```
produce/2310/robot_sleep_mode/BS10L_R01
```

会匹配：

```
testMqttHandler
```

---

## 15. MQTT + 通配符

MQTT 标准支持：

```
+
#
```

### 15.1 +

`+` 表示一个 Topic 层级。

例如：

```
produce/+/robot_sleep_mode/+
```

可以匹配：

```
produce/2310/robot_sleep_mode/BS10L_R01
produce/2310/robot_sleep_mode/BS10L_R02
produce/2311/robot_sleep_mode/BS20L_R01
```

但是不能匹配：

```
produce/2310/robot_sleep_mode
```

因为最后少了一层。

---

## 16.

`#` 表示后面的所有 Topic 层级。

例如：

```
produce/#
```

可以匹配：

```
produce/2310
produce/2310/robot
produce/2310/robot/sleep
produce/2310/robot/sleep/status
```

一般用于：

- 全量订阅
- 某个业务域全部消息监听

生产环境不建议随意使用：

```
#
```

避免一次订阅大量无关消息。

---

## 17. 一个 Adapter 统一处理所有 Topic

当前设计不是：

```
Topic A -> Adapter A
Topic B -> Adapter B
Topic C -> Adapter C
```

而是：

```
              一个 MQTT Adapter
                     │
       ┌─────────────┼─────────────┐
       ▼             ▼             ▼
    Topic A        Topic B        Topic C
       │             │             │
       └─────────────┼─────────────┘
                     ▼
              Handler Registry
```

这样做的好处：

- MQTT Client 数量少
- 连接管理统一
- 订阅管理统一
- 重连统一
- Topic 路由统一
- 业务 Handler 解耦

---

## 18. Handler Registry

```
MqttHandlerRegistry
``` 是 Handler 管理中心。

主要负责：

1. 注册 Handler
2. 注册 Topic 与 Handler 的关系
3. 根据 Topic 查找 Handler
4. 分发消息
5. 捕获 Handler 执行异常

核心结构：
```

handlerMap

testMqttHandler
│
▼
MqttHandler

matcher

produce/+/robot_sleep_mode/+
│
▼
testMqttHandler

```

---

## 19. Handler 注册过程

Spring 启动后：
```

Spring Bean
│
▼
MqttHandlerRegistry
│
▼
扫描 MqttHandler
│
▼
register(handler)

```

例如：

```java
@Component
public class TestMqttHandler
        implements MqttHandler {
}
```

Spring 会创建 Bean。

Starter 自动发现：

```
TestMqttHandler
```

然后注册：

```
testMqttHandler
        ↓
TestMqttHandler
```

---

## 20. Subscription 注册过程

Handler 注册完成后，根据配置注册 Topic：

```
subscriptions
      │
      ▼
topic
      +
handler
      │
      ▼
MqttHandlerRegistry
      │
      ▼
MqttTopicMatcher
```

例如：

```yaml
topic: produce/+/robot_sleep_mode/+
handler: testMqttHandler
```

最终：

```
produce/+/robot_sleep_mode/+
                │
                ▼
        testMqttHandler
```

---

## 21. 消息接收流程

收到：

```
produce/2310/robot_sleep_mode/BS10L_R01
```

完整流程：

```
MQTT Broker
    │
    ▼
Paho
    │
    ▼
MqttPahoMessageDrivenChannelAdapter
    │
    ▼
babyMqttInputChannel
    │
    ▼
babyMqttMessageHandler
    │
    ▼
MqttHandlerRegistry.dispatch()
    │
    ▼
MqttTopicMatcher.findHandlers()
    │
    ▼
testMqttHandler
    │
    ▼
handle(topic, payload)
```

---

## 22. Handler 异常处理

Handler 执行：

```java
handler.handle(topic, payload);
```

Starter 会捕获异常。

因此：

```text
Handler A 执行异常
```

不会直接导致：

MQTT Adapter 停止

也不会影响其他 Handler：

```text

Topic
│
├── Handler A -> 异常
│
└── Handler B -> 正常执行
```

异常会记录日志。

## 23. 发布 MQTT 消息

业务代码可以使用：

```

BabyMqttTemplate

```

发布消息。

例如：

```java

@Autowired
private BabyMqttTemplate mqttTemplate;
```

发布：

```java
mqttTemplate.publish(
"produce/2310/robot_sleep_mode/BS10L_R01",
payload,
0,
        false
);
```

参数：

参数 说明
topic MQTT Topic
payload 消息内容
qos QoS
retained 是否 Retain

## 24. 发布 JSON 示例

例如：

```java
String topic =
        "produce/2310/robot_sleep_mode/BS10L_R01";

String payload =
        "{"
                + ""
deviceName":"BS10L_R01","
        +""SleepStatus":false,"
        +""time":1789380060000"
        +"}";

        mqttTemplate.

publish(
        topic,
        payload,
    0,
                false
);
```

## 25. 如何新增一个 MQTT 业务

假设现在增加：

```
机器人报警
```

Topic：

```
produce/+/robot_alarm/+
```

第一步：创建 Handler

```java

@Component
@Slf4j
public class RobotAlarmHandler
        implements MqttHandler {

    @Override
    public String getName() {
        return "robotAlarmHandler";
    }

    @Override
    public void handle(
            String topic,
            String payload) {

        log.info(
                "收到机器人报警，topic: {}, payload: {}",
                topic,
                payload
        );

        // 业务处理
    }

}
```

## 26. 第二步：增加配置

```yaml
subscriptions:
  - name: robotSleep
    topic: produce/+/robot_sleep_mode/+
    handler: testMqttHandler
  - name: robotAlarm
    topic: produce/+/robot_alarm/+
    handler: robotAlarmHandler
```

不需要：

新建 MQTT Client
新建 MQTT Adapter
新建 MQTT Connection
修改 Paho
修改 Matcher

只需要：

```
Handler + YAML
```

## 27. 多个 Topic 使用同一个 Handler

例如：

```yaml
subscriptions:
  - name: robotSleep
    topic: produce/+/robot_sleep_mode/+
    handler: robotHandler
  - name: robotStatus
    topic: produce/+/robot_status/+
    handler: robotHandler
  - name: robotAlarm
    topic: produce/+/robot_alarm/+
    handler: robotHandler
```

最终：

```
robot_sleep_mode
│
robot_status ───► robotHandler
│
robot_alarm
```

适合一个业务模块统一处理多个 Topic。

## 28. 一个 Topic 可以使用多个 Handler

Starter 支持一个 Topic 配置多个 Handler。

例如：

```yaml
subscriptions:

  - name: alarmRecord
    topic: produce/+/alarm/+
    handler: alarmRecordHandler

  - name: alarmPush
    topic: produce/+/alarm/+
    handler: alarmPushHandler
```

收到：

```
produce/2310/alarm/xxx
```

后，Matcher 会返回多个 Handler：

```
produce/2310/alarm/xxx
          │
          ├── alarmRecordHandler
          │
          └── alarmPushHandler
```

当前版本中，多个 Handler 默认按照匹配结果进行串行调用。

```
Handler A 执行
      ↓
Handler A 完成
      ↓
Handler B 执行
      ↓
Handler B 完成
```

每个 Handler 都会单独捕获异常。

因此 Handler A 执行异常不会阻止 Handler B 继续执行。

### 使用场景

一个 Topic 多 Handler 适合处理多个相互独立的业务，例如：

```
MQTT 报警消息
    │
    ├── 保存报警记录
    │
    └── 推送报警消息
```

对应：

```yaml
subscriptions:

  - name: alarmRecord
    topic: produce/+/alarm/+
    handler: alarmRecordHandler

  - name: alarmPush
    topic: produce/+/alarm/+
    handler: alarmPushHandler
```

### 使用建议

虽然 Starter 支持一个 Topic 对应多个 Handler，但不建议无限制地给同一个 Topic 配置大量 Handler。

通常推荐：

```
一个 Topic
    ↓
一个主要 Handler
```

只有存在明确且相互独立的业务处理需求时，再配置多个 Handler。

### 当前版本的执行模型

当前多个 Handler 是同步串行执行：

```
MQTT 消息
    ↓
MqttHandlerRegistry
    ↓
Handler A
    ↓
Handler B
    ↓
Handler C
```

因此，如果某个 Handler 内部存在耗时操作，会影响后续 Handler 的执行时间。

例如：

```
Handler A
    ↓
查询数据库 2 秒
    ↓
Handler B 开始执行
```

所以 Handler 中`不建议`执行长时间`阻塞操作`。

## 29. 启动校验

MQTT Starter 启动时会检查配置。

例如：

subscriptions 为空

```
MQTT subscriptions 不能为空
```

Topic 为空

```
MQTT subscription topic 不能为空
```

Handler 为空

```
MQTT subscription handler 不能为空
```

Handler 不存在

```
未找到 MQTT Handler: xxx
```

这样可以避免项目启动成功以后才发现 MQTT 消息没有业务处理。

## 30. 常见错误：Handler 不存在

例如：

```yaml
subscriptions:

  - name: robotSleep
    topic: produce/+/robot_sleep_mode/+
    handler: robotSleepHandler
```

但是项目中没有：

```java
getName() {
    return "robotSleepHandler";
}
```

启动会失败：

```
未找到 MQTT Handler: robotSleepHandler
```

检查：

- Handler 是否实现 MqttHandler
- 是否添加 @Component
- getName() 是否正确
- YAML handler 是否拼写一致
- Handler 是否在 Spring 扫描范围内

## 31. 常见错误：ArrayStoreException

如果启动出现：

```
ArrayStoreException:
arraycopy: element type mismatch
```

重点检查：

```
BabyMqttInboundConfiguration
```

不要直接：

```java
subscriptions.toArray(
new String[subscriptions.size()]
        );
```

因为：

```
subscriptions
```

实际上是：

```
List<MqttSubscription>
```

必须转换成：

```
String[]
```

正确：

```java
String[] topics =
        new String[subscriptions.size()];

for(
int i = 0;
i <subscriptions.

size();

i++){

topics[i]=
        subscriptions.

get(i).

getTopic();

}
```

## 32. 常见错误：没有收到消息

按照以下顺序排查。

1. MQTT 是否开启

```yaml
baby:
mqtt:
enabled: true
```

2. Broker 是否正确

```yaml
broker: tcp://192.168.0.250:1883
```

3. Topic 是否正确

检查：

```yaml
subscriptions:

topic: produce/+/robot_sleep_mode/+
```

4. Handler 是否注册

检查启动日志。

5. Handler 名称是否一致

```yaml
handler: testMqttHandler
```

对应：

```java
return"testMqttHandler";
```

6. Matcher 是否匹配

检查：

```yaml
matcher-type: trie
```

以及 Topic 通配符。

## 33. 推荐生产配置

生产环境推荐：

```yaml
baby:
mqtt:
enabled: true

broker: tcp://192.168.0.250:1883

username: ${MQTT_USERNAME}
password: ${MQTT_PASSWORD}

client-id: baby-production

auto-reconnect: true

connection-timeout: 10

keep-alive: 60

matcher-type: trie

subscriptions:

  - name: robotSleep
    topic: produce/+/robot_sleep_mode/+
    handler: robotSleepHandler

  - name: robotAlarm
    topic: produce/+/robot_alarm/+
    handler: robotAlarmHandler

```

## 34. 为什么推荐 Trie

当 Topic 较少时：

```
list
```

完全够用。

当系统逐渐增加：

```
10 个 Topic
50 个 Topic
100 个 Topic
500 个 Topic
```

Topic 匹配规则越来越多。

Trie 可以按照 Topic 层级进行匹配：

```
produce
│
└── 2310
│
├── robot_sleep_mode
├── robot_alarm
├── robot_status
└── energy
```

避免每次消息到来都完整遍历所有规则。

因此：

开发测试：

```yaml
matcher-type: list
```

生产环境：

```yaml
matcher-type: trie
```

## 35. 当前设计不使用一个 Topic 一个 Adapter

不推荐：

```
Topic A -> MQTT Adapter A
Topic B -> MQTT Adapter B
Topic C -> MQTT Adapter C
```

因为随着业务增加：

```
Adapter 数量
Connection 管理
订阅管理
重连管理
生命周期管理
```

都会变得复杂。

当前设计：

```
一个 MQTT Adapter
│
▼
所有 Topic
│
▼
Registry
│
▼
Matcher
│
▼
Handler
```

扩展一个 MQTT 业务只需要增加：

```
Handler
+
Subscription
```

## 36. 核心类职责

    类	职责
    BabyMqttProperties	MQTT 配置
    MqttSubscription	Topic 订阅配置
    MqttHandler	业务消息处理接口
    MqttHandlerRegistry	Handler 注册与消息分发
    MqttTopicMatcher	Topic 匹配接口
    ListMqttTopicMatcher	List 类型 Topic 匹配
    TrieMqttTopicMatcher	Trie 类型 Topic 匹配
    BabyMqttInboundConfiguration	MQTT 入站消息配置
    BabyMqttClient	MQTT Client 能力
    BabyMqttTemplate	MQTT 消息发布
    BabyMqttAutoConfiguration	Starter 自动配置

## 37. 代码职责边界

    Starter 负责

```
MQTT 连接
MQTT 重连
MQTT 订阅
MQTT 消息接收
Topic 匹配
Handler 注册
消息分发
MQTT 发布
```

业务项目负责

```
Handler
业务参数解析
业务校验
数据库处理
业务 Service
业务异常处理
```

## 38. 新开发人员接入流程

拿到一个新的 MQTT Topic：

```
produce/+/xxx/+
```

只需要：

1. 创建 Handler

```java

@Component
public class XxxHandler
        implements MqttHandler {

    @Override
    public String getName() {
        return "xxxHandler";
    }

    @Override
    public void handle(
            String topic,
            String payload) {

        // 业务处理
    }

}
```

2. 配置 Subscription

```yaml
subscriptions:

name: xxx
topic: produce/+/xxx/+
handler: xxxHandler
```

3. 启动项目

Starter 自动完成：

```
Handler 注册
↓
Subscription 注册
↓
Topic Matcher 注册
↓
MQTT Adapter 订阅
↓
开始接收消息
```

## 39. 快速接入清单

开发一个新的 MQTT 业务，只检查以下内容：

引入 baby-mqtt-spring-boot-starter
baby.mqtt.enabled=true
配置 broker
配置 username/password
配置唯一 client-id
创建 MqttHandler
添加 @Component
实现 getName()
实现 handle()
增加 subscriptions
handler 名称与 getName() 保持一致
检查 Topic 通配符
生产环境使用 trie

## 40. 设计原则

当前 MQTT Starter 遵循以下原则：

原则一：业务与 MQTT 基础设施解耦

业务代码不直接操作 Paho。

原则二：Handler 与 Topic 解耦

Handler 不负责定义 Topic。

原则三：统一 Adapter

整个 Starter 使用统一 MQTT Inbound Adapter。

原则四：统一 Registry

所有 Handler 通过 Registry 管理。

原则五：Matcher 可替换

支持：

```
List
Trie
```

未来可以继续扩展其他匹配算法。

原则六：配置驱动

Topic 与 Handler 的关系通过 YAML 配置完成。

原则七：自动配置

业务项目引入 Starter 后，无需手动创建 MQTT 基础 Bean。

## 41. 最终使用示例

业务项目最终只需要：

Java

```java

@Component
@Slf4j
public class RobotSleepHandler
        implements MqttHandler {

    @Override
    public String getName() {
        return "robotSleepHandler";
    }

    @Override
    public void handle(
            String topic,
            String payload) {

        log.info(
                "机器人休眠消息，topic: {}, payload: {}",
                topic,
                payload
        );

        // TODO: 解析 payload
        // TODO: 执行业务逻辑
    }

}
```

YAML

```yaml
baby:
mqtt:
enabled: true
broker: tcp://192.168.0.250:1883
username: admin
password: public
client-id: baby-mqtt-test
auto-reconnect: true
connection-timeout: 10
keep-alive: 60
matcher-type: trie

subscriptions:
  - name: robotSleep
    topic: produce/+/robot_sleep_mode/+
    handler: robotSleepHandler

```

完成以上配置后，Starter 自动完成：

```
Spring Boot 启动
↓
BabyMqttAutoConfiguration
↓
创建 MQTT Client
↓
创建 Inbound Adapter
↓
注册 MqttHandler
↓
注册 Subscription
↓
注册 Topic Matcher
↓
连接 MQTT Broker
↓
订阅 Topic
↓
接收 MQTT 消息
↓
匹配 Handler
↓
执行业务代码
```

## 42. 一句话总结

Baby MQTT Starter 的核心思想是：一个 MQTT 连接统一接收消息，通过 Registry + Topic Matcher 将消息分发到业务
Handler，业务开发人员只需要实现 Handler 和配置 Topic，不需要关心 MQTT 底层连接和订阅管理。
