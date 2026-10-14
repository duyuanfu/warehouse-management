# 基于微信小程序的中小型仓库管理系统 (WMS)

本项目为中小型仓库协同管理系统。
系统采用前后端分离架构，前端为微信小程序原生开发，后端基于 Java 17 + Spring Boot 3 + MyBatis-Plus，数据库采用 MySQL。系统实现了从员工登录、货品管理、入库单据、出库单据到库存盘点的完整业务流转，并引入乐观锁（version 字段）机制解决多管理员并发协同时的库存冲突问题。

---

## 一、 主管 (admin) 与 理货员 (user/worker) 的角色区别

系统严格按照需求区分【主管】与【理货员】角色，具体权限与职责区别如下：

| 权限 / 功能模块 | 主管 (SUPERVISOR / admin) | 理货员 (TALLYMAN / worker) | 区别说明 |
| :--- | :---: | :---: | :--- |
| **货品资料维护** | **新增、修改、删除** | **只读查看** | 只有主管可以录入新货品、修改规格和单价、删除零库存货品；理货员仅可查看货品档案与实时库存。 |
| **库存预警监控** | 支持一键筛选预警货品 | 支持一键筛选预警货品 | 双方均可实时掌握低于安全库存的货品。 |
| **采购入库操作** | 允许创建入库单 | 允许创建入库单 | 理货员作为一线库管员，日常负责执行进货核对与入库单录入。 |
| **销售出库操作** | 允许创建出库单 | 允许创建出库单 | 理货员负责根据订单发货出库，系统自动扣减库存并防超卖。 |
| **库存盘点操作** | 允许发起盘点与调账 | 允许实盘录入与调账 | 理货员现场清点实物，录入实盘数量，系统自动核算盈亏并更新库存。 |
| **单据责任溯源** | 记录主管操作单据 | 记录理货员操作单据 | 所有入库单、出库单、盘点单均记录经办员工ID，明确责任。 |

---

## 二、 数据库表设计（对应ER图 9 张表）

建表 SQL 及测试数据文件位于：`sql/schema.sql`。

| 序号 | 数据库表名 | 中文名称 | 核心字段说明 |
| :--- | :--- | :--- | :--- |
| 1 | `employee` | 员工表 | `id`, `account`, `password`, `name`, `role`(SUPERVISOR/TALLYMAN), `status` |
| 2 | `partner` | 往来单位表 | `id`, `name`, `contact_person`, `contact_phone`, `type`(SUPPLIER/CUSTOMER) |
| 3 | `warehouse` | 仓库表 | `id`, `name`, `address`, `status` |
| 4 | `goods` | 货品表 | `id`, `sku_code`, `name`, `spec`, `safety_stock`, `stock_quantity`, `warehouse_id`, `version`(乐观锁) |
| 5 | `inbound_order`| 入库单表 | `id`, `order_no`, `partner_id`, `warehouse_id`, `employee_id`, `inbound_time` |
| 6 | `outbound_order`| 出库单表 | `id`, `order_no`, `partner_id`, `warehouse_id`, `employee_id`, `outbound_time` |
| 7 | `order_detail`| 单据明细表 | `id`, `order_type`(INBOUND/OUTBOUND), `order_id`, `goods_id`, `quantity`, `price` |
| 8 | `check_order` | 盘点单表 | `id`, `check_no`, `warehouse_id`, `employee_id`, `check_time`, `status` |
| 9 | `check_detail`| 盘点明细表 | `id`, `check_id`, `goods_id`, `system_stock`, `actual_stock`, `difference_stock` |

---

## 三、 乐观锁协同机制

在货品表 (`goods`) 中设计有整数类型 `version` 字段。
在执行出库、入库、盘点调账等更新库存操作时，SQL 底层自动进行 CAS 版本比对：
```sql
UPDATE goods 
SET stock_quantity = stock_quantity + #{delta}, version = version + 1 
WHERE id = #{id} AND version = #{version}
```
当多名管理员同时修改同一货品库存时，后提交者会因为 version 不一致导致更新行数为 0，系统将拦截并提示“库存已被其他员工修改，请刷新后重试”，有效避免并发脏写。

---

## 四、 快速部署与运行指引

### 1. 数据库准备
1. 打开 MySQL 执行 `sql/schema.sql`；
2. 脚本会自动创建 `wms_db` 数据库及 9 张表，并预置演示数据。

### 2. 后端启动
1. 使用 IDEA 打开 `backend` Maven 项目；
2. 确认 `src/main/resources/application.yml` 中的 MySQL 账号密码；
3. 运行 `WarehouseApplication`，后端将在 `http://127.0.0.1:8080/api` 启动。

### 3. 小程序运行
1. 使用 **微信开发者工具** 导入 `miniapp` 目录；
2. AppID 选择测试号或填写个人开发者 AppID；
3. 勾选“不校验合法域名、web-view（业务域名）、TLS版本以及HTTPS证书”即可直接本地调试。

---

## 五、 测试账号

- **主管**：账号 `admin`，密码 `123456`
- **理货员**：账号 `worker1`，密码 `123456`
