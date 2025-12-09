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

