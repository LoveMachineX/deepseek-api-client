# DeepSeek API Java 端口


## 功能
API 请求处理
JSON 响应解析

## 使用方法
```java
String response = DeepSeekAIClient.getAIResponse("Hello");

## 项目结构
deepseek-api-client/
├── src/
│   ├── main/java/com/yourcompany/deepseek/   # 主代码
│   └── test/java/com/yourcompany/deepseek/   # 单元测试
├── pom.xml                                  # Maven 依赖管理
└── README.md                                # 项目说明
