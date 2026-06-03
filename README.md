# 入职练习项目

这个项目用 Docker 启动即可，会同时拉起：

- Spring Boot 应用
- MySQL
- Redis
- RocketMQ NameServer
- RocketMQ Broker

## 启动

先打包：

```powershell
mvn clean package
```

再启动 Docker：

```powershell
docker compose -p onboarding-demo up -d --build
```

查看容器状态：

```powershell
docker compose -p onboarding-demo ps
```

应用启动成功后访问：

```text
http://localhost:8080
```

## 过一遍接口

创建订单：

```powershell
Invoke-RestMethod -Method Post http://localhost:8080/api/orders `
  -ContentType 'application/json' `
  -Body '{"productName":"Spring Boot Book","amount":99.90}'
```

查询订单：

```powershell
Invoke-RestMethod http://localhost:8080/api/orders/1
```

查询最近创建的订单：

```powershell
Invoke-RestMethod http://localhost:8080/api/orders?limit=10
```

支付订单：

```powershell
Invoke-RestMethod -Method Post http://localhost:8080/api/orders/1/pay
```

再查一次订单，状态应该变成 `PAID`：

```powershell
Invoke-RestMethod http://localhost:8080/api/orders/1
```

## 看日志

```powershell
docker compose -p onboarding-demo logs -f app
```

可以重点看这些日志：

- `request in/request out`：拦截器
- `method cost`：AOP
- `order cache hit`：Redis 缓存
- `RocketMQ send result: SEND_OK`：RocketMQ 生产者
- `RocketMQ consumed message`：RocketMQ 消费者

## 看数据

查看 MySQL 里的订单：

```powershell
docker exec onboarding-mysql mysql -uroot -proot onboarding -e "SELECT id, order_no, product_name, amount, status, created_at, updated_at FROM demo_order;"
```

查看 Redis 里的订单缓存：

```powershell
docker exec onboarding-redis redis-cli HGETALL cache:order:1
```

## 停止

只停止容器：

```powershell
docker compose -p onboarding-demo down
```

停止并清掉 MySQL 数据：

```powershell
docker compose -p onboarding-demo down -v
```
