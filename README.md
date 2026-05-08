# 订单管理系统后端

基于 Spring Boot 的订单管理系统后端服务。

## 🚀 技术栈

- **Java 17**
- **Spring Boot 3.2.4**
- **MySQL 8.0+**
- **MyBatis-Plus 3.5.6**
- **PageHelper 1.4.7**
- **Swagger/OpenAPI 3.0**

## 📋 功能特性

### 订单管理
- ✅ 创建订单（支持多商品明细）
- ✅ 更新订单信息
- ✅ 删除订单（软删除）
- ✅ 查询订单详情
- ✅ 分页查询订单列表
- ✅ 订单状态管理（pending/processing/completed/cancelled）

### 高级功能
- ✅ 自动订单号生成（ORD-yyyyMMdd-xxxx）
- ✅ 自动计算商品小计和订单总额
- ✅ 状态流转验证
- ✅ 批量操作支持
- ✅ 订单统计信息

### 技术特性
- ✅ RESTful API 设计
- ✅ 完整的异常处理
- ✅ 参数验证（JSR-303）
- ✅ 事务管理
- ✅ 软删除机制
- ✅ 跨域支持

## 🏗️ 项目结构

```
order-management-backend/
├── src/main/java/com/demo/order/
│   ├── OrderManagementApplication.java      # 应用启动类
│   ├── config/                             # 配置类
│   │   └── WebConfig.java                  # Web配置
│   ├── controller/                         # 控制器层
│   │   └── OrderController.java            # 订单控制器
│   ├── service/                           # 业务逻辑层
│   │   ├── OrderService.java               # 订单服务接口
│   │   └── impl/OrderServiceImpl.java      # 订单服务实现
│   ├── mapper/                            # 数据访问层
│   │   ├── OrderHeaderMapper.java          # 订单主表Mapper
│   │   └── OrderDetailMapper.java          # 订单明细表Mapper
│   ├── entity/                            # 实体类
│   │   ├── OrderHeader.java               # 订单主表实体
│   │   └── OrderDetail.java               # 订单明细表实体
│   ├── dto/                               # 数据传输对象
│   │   ├── request/                       # 请求DTO
│   │   │   ├── OrderCreateRequest.java    # 创建订单请求
│   │   │   ├── OrderUpdateRequest.java    # 更新订单请求
│   │   │   └── OrderQueryRequest.java     # 查询订单请求
│   │   └── response/                      # 响应DTO
│   │       ├── OrderResponse.java         # 订单响应
│   │       ├── OrderDetailResponse.java   # 订单详情响应
│   │       └── PageResponse.java          # 分页响应
│   ├── common/                            # 通用组件
│   │   ├── constants/                     # 常量
│   │   │   └── OrderStatus.java           # 订单状态枚举
│   │   ├── exception/                     # 异常处理
│   │   │   ├── BusinessException.java     # 业务异常
│   │   │   └── GlobalExceptionHandler.java # 全局异常处理器
│   │   └── utils/                         # 工具类
│   │       └── OrderNoGenerator.java      # 订单号生成器
├── src/main/resources/
│   ├── application.yml                    # 主配置文件
│   ├── application-dev.yml                # 开发环境配置
│   └── application-prod.yml               # 生产环境配置（待补充）
└── pom.xml                                # Maven配置
```

## 📊 数据库设计

### 订单主表 (order_header)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| order_no | VARCHAR(32) | 订单编号，唯一 |
| customer_name | VARCHAR(100) | 客户姓名 |
| phone | VARCHAR(20) | 联系电话 |
| address | VARCHAR(500) | 收货地址 |
| remark | TEXT | 备注 |
| status | VARCHAR(20) | 订单状态 |
| total_items | INT | 商品总数 |
| total_amount | DECIMAL(15,2) | 总金额 |
| order_time | DATETIME | 下单时间 |
| update_time | DATETIME | 最后更新时间 |
| deleted | TINYINT(1) | 软删除标记 |
| delete_time | DATETIME | 删除时间 |

### 订单明细表 (order_detail)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| order_id | BIGINT | 订单ID，外键 |
| product_name | VARCHAR(200) | 商品名称 |
| quantity | INT | 数量，≥1 |
| unit_price | DECIMAL(10,2) | 单价，≥0 |
| subtotal | DECIMAL(15,2) | 小计 = 数量×单价 |
| sort_order | INT | 排序序号 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

## 🔧 快速开始

### 1. 环境要求
- JDK 17+
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库初始化
```sql
-- 创建数据库
CREATE DATABASE order_management_demo 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_general_ci;

-- 使用数据库
USE order_management_demo;

-- 执行数据库脚本
-- 见 database_schema.sql 文件
```

### 3. 配置修改
修改 `src/main/resources/application.yml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/order_management_demo?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

### 4. 编译运行
```bash
# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/order-management-backend-1.0.0.jar
```

### 5. 访问API文档
项目启动后，访问以下地址：
- Swagger UI: http://localhost:8080/swagger-ui.html
- API文档: http://localhost:8080/api-docs

## 📚 API 接口

### 订单管理接口

#### 1. 创建订单
```http
POST /api/orders
Content-Type: application/json

{
  "customerName": "张三",
  "phone": "13800138000",
  "address": "北京市朝阳区",
  "remark": "请尽快发货",
  "items": [
    {
      "productName": "iPhone 15 Pro",
      "quantity": 1,
      "unitPrice": 8999.00
    },
    {
      "productName": "AirPods Pro",
      "quantity": 2,
      "unitPrice": 1499.00
    }
  ]
}
```

#### 2. 查询订单列表
```http
GET /api/orders?page=1&size=10&status=pending&customerName=张三
```

#### 3. 获取订单详情
```http
GET /api/orders/{id}
```

#### 4. 更新订单
```http
PUT /api/orders/{id}
Content-Type: application/json

{
  "customerName": "李四",
  "phone": "13900139000",
  "items": [...]
}
```

#### 5. 删除订单（软删除）
```http
DELETE /api/orders/{id}
```

#### 6. 变更订单状态
```http
PATCH /api/orders/{id}/status?newStatus=processing
```

#### 7. 获取订单统计
```http
GET /api/orders/statistics
```

## 🧪 测试

### 单元测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=OrderServiceTest
```

### API测试
使用 Postman 或 curl 进行API测试：
```bash
# 健康检查
curl http://localhost:8080/api/orders/health

# 创建订单
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "测试用户",
    "items": [
      {
        "productName": "测试商品",
        "quantity": 1,
        "unitPrice": 100.00
      }
    ]
  }'
```

## 🔧 配置说明

### 应用配置
```yaml
app:
  order:
    prefix: ORD-          # 订单号前缀
    date-format: yyyyMMdd # 日期格式
    sequence-length: 4    # 序列号长度
```

### 数据库配置
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/order_management_demo?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root123
```

### MyBatis-Plus配置
```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      logic-delete-field: deleted      # 逻辑删除字段
      logic-delete-value: 1           # 删除值
      logic-not-delete-value: 0       # 未删除值
```

## 🚀 部署

### 1. 生产环境配置
创建 `src/main/resources/application-prod.yml`：
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

logging:
  level:
    com.demo.order: INFO
    org.springframework: WARN

server:
  port: ${SERVER_PORT:8080}
```

### 2. Docker部署
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/order-management-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 3. 环境变量
```bash
export DATABASE_URL=jdbc:mysql://mysql-host:3306/order_management_demo
export DATABASE_USERNAME=root
export DATABASE_PASSWORD=your_password
export SERVER_PORT=8080
```

## 📈 监控与日志

### 健康检查
- `GET /api/orders/health` - 服务健康状态
- `GET /actuator/health` - Spring Boot Actuator（需添加依赖）

### 日志配置
日志文件位置：`logs/order-service.log`
日志级别可通过 `application.yml` 配置。

## 🐛 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查数据库服务是否运行
   - 验证连接配置是否正确
   - 检查网络连接

2. **端口冲突**
   - 修改 `server.port` 配置
   - 检查是否有其他服务占用8080端口

3. **依赖下载失败**
   - 检查网络连接
   - 尝试使用阿里云Maven镜像
   - 清理本地Maven仓库：`mvn clean install -U`

### 日志查看
```bash
# 查看应用日志
tail -f logs/order-service.log

# 查看错误日志
grep ERROR logs/order-service.log
```

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

- 项目地址：[https://github.com/CharJay/order-service-demo](https://github.com/CharJay/order-service-demo)
- 问题反馈：[GitHub Issues](https://github.com/CharJay/order-service-demo/issues)

---

**Happy Coding!** 🚀