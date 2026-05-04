# WarpExchange 项目

WarpExchange是一个基于Spring Boot的交易系统，包含交易引擎、API接口、用户界面等模块。

## 项目结构
- **内存优先**: 100%内存交易，微秒级响应
- **微服务设计**: 7个独立服务，易于扩展
- **事件驱动**: 严格序列化，保证一致性
- **异步处理**: 多线程架构，高并发支持
- **trading-engine**: 交易引擎模块，负责处理交易逻辑
- **trading-api**: 交易API模块，提供RESTful接口
- **trading-sequencer**: 交易序列化模块
- **quotation**: 报价模块
- **push**: 推送服务模块
- **ui**: 用户界面模块
- **common**: 公共模块，包含共享组件和工具类
- **config**: 配置文件模块

## 技术栈

- **后端**: Java 17 + Spring Boot 3.x
- **前端**: Vue.js + Bootstrap 5
- **通信**: WebSocket + Kafka
- **缓存**: Redis + Lua脚本
- **存储**: MySQL + 连接池

## 运行项目

1. 安装Java 17+和Maven
2. 启动Redis和MySQL服务
3. 运行`mvn clean install`安装依赖
4. 运行`mvn spring-boot:run`启动项目

## 贡献
- 学习的这位老师的项目：https://github.com/michaelliao
- 欢迎提交Issue和Pull Request来改进项目。

## docker
镜像
- confluentinc/cp-zookeeper
- confluentinc/cp-kafka
- 可以到这个博主这里下载有视频教程：https://github.com/wys03/DockerTarBuilder
<img width="1912" height="1076" alt="image" src="https://github.com/user-attachments/assets/fb4774fa-d8d6-40eb-9b2c-a3109095ff63" />


项目架构
该项目包含 7 个微服务：

服务	端口	描述
ui	8000	前端用户界面
trading-api	8001	交易 API
trading-engine	8002	交易引擎
trading-sequencer	8003	交易序列器
quotation	8004	报价服务
push	8005	推送服务
config	8888	Spring Cloud 配置中心
启动步骤
第一步：启动依赖的基础服务（Docker）
bash
cd e:/warpexchange/build
docker-compose up -d
这将启动：

MySQL 8.0 (端口 3306)
Redis 6.2 (端口 6379)
Kafka 7.4.0 (端口 9092)
Zookeeper (端口 2181)
注意：首次启动会自动执行 build/sql/schema.sql 初始化数据库。

第二步：构建项目
bash
cd e:/warpexchange/build
mvn clean package -DskipTests
第三步：启动各个微服务

模块	Main Class	端口
config	com.itranswarp.warpconfig.WarpConfigApplication	8888
trading-sequencer	com.itranswarp.sequencer.TradingSequencerApplication	8003
trading-engine	com.itranswarp.engine.TradingEngineApplication	8002
trading-api	com.itranswarp.exchange.TradingApiApplication	8001
quotation	com.itranswarp.quotation.QuotationApplication	8004
push	com.itranswarp.push.PushApplication	8005
ui	com.itranswarp.ui.UIApplication	8000

环境要求
Java 17
Maven 3.6+
Docker (用于运行 MySQL, Redis, Kafka 等)
访问地址
UI 界面：http://localhost:8000
API 文档：http://localhost:8001/swagger-ui.html

