# WarpExchange 项目

WarpExchange是一个基于Spring Boot的交易系统，包含交易引擎、API接口、用户界面等模块。

## 项目结构

- **trading-engine**: 交易引擎模块，负责处理交易逻辑
- **trading-api**: 交易API模块，提供RESTful接口
- **trading-sequencer**: 交易序列化模块
- **quotation**: 报价模块
- **push**: 推送服务模块
- **ui**: 用户界面模块
- **common**: 公共模块，包含共享组件和工具类
- **config**: 配置文件模块

## 技术栈

- Spring Boot
- Spring Cloud
- Redis
- MySQL
- Java 17+

## 运行项目

1. 安装Java 17+和Maven
2. 启动Redis和MySQL服务
3. 运行`mvn clean install`安装依赖
4. 运行`mvn spring-boot:run`启动项目

## 贡献

欢迎提交Issue和Pull Request来改进项目。
