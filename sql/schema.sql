-- ========================================================
-- 中小型仓库管理系统 (WMS) 数据库脚本
-- 适配 MySQL 5.7 / 8.0+
-- 包含 9 张表及初始演示数据
-- ========================================================

CREATE DATABASE IF NOT EXISTS `wms_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `wms_db`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 员工表 (employee)
-- ----------------------------
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '员工ID',
  `account` VARCHAR(50) NOT NULL COMMENT '账号',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `name` VARCHAR(50) NOT NULL COMMENT '员工姓名',
  `role` VARCHAR(20) NOT NULL COMMENT '角色: 主管(SUPERVISOR) / 理货员(TALLYMAN)',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `status` TINYINT DEFAULT 1 COMMENT '状态 1:正常 0:停用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account` (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- ----------------------------
-- 2. 往来单位表 (partner)
-- ----------------------------
DROP TABLE IF EXISTS `partner`;
CREATE TABLE `partner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '单位ID',
  `name` VARCHAR(100) NOT NULL COMMENT '单位名称',
  `contact_person` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(30) DEFAULT NULL COMMENT '联系电话',
  `type` VARCHAR(20) NOT NULL COMMENT '单位类型: SUPPLIER(供应商) / CUSTOMER(客户)',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '联系地址',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='往来单位表';

-- ----------------------------
-- 3. 仓库表 (warehouse)
-- ----------------------------
DROP TABLE IF EXISTS `warehouse`;
CREATE TABLE `warehouse` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '仓库ID',
  `name` VARCHAR(100) NOT NULL COMMENT '仓库名称',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE(启用) / INACTIVE(停用)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库表';

-- ----------------------------
-- 4. 货品表 (goods)
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '货品ID',
  `sku_code` VARCHAR(50) NOT NULL COMMENT 'SKU编码',
  `name` VARCHAR(100) NOT NULL COMMENT '货品名称',
  `spec` VARCHAR(100) DEFAULT NULL COMMENT '规格型号',
  `safety_stock` INT DEFAULT 10 COMMENT '安全库存',
  `stock_quantity` INT DEFAULT 0 COMMENT '当前库存量',
  `warehouse_id` BIGINT NOT NULL COMMENT '存放仓库ID',
  `price` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '参考价格',
  `status` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '当前状态: NORMAL(正常) / WARNING(库存预警)',
  `version` INT DEFAULT 0 NOT NULL COMMENT '乐观锁版本号',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku` (`sku_code`),
  KEY `idx_warehouse` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='货品表';

-- ----------------------------
-- 5. 入库单表 (inbound_order)
-- ----------------------------
DROP TABLE IF EXISTS `inbound_order`;
CREATE TABLE `inbound_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '入库单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '入库单号',
  `partner_id` BIGINT NOT NULL COMMENT '供货单位(供应商)ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '接收仓库ID',
  `employee_id` BIGINT NOT NULL COMMENT '创建员工ID',
  `inbound_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inbound_no` (`order_no`),
  KEY `idx_inbound_wh` (`warehouse_id`),
  KEY `idx_inbound_emp` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单表';

-- ----------------------------
-- 6. 出库单表 (outbound_order)
-- ----------------------------
DROP TABLE IF EXISTS `outbound_order`;
CREATE TABLE `outbound_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '出库单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '出库单号',
  `partner_id` BIGINT NOT NULL COMMENT '客户单位ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '发出仓库ID',
  `employee_id` BIGINT NOT NULL COMMENT '创建员工ID',
  `outbound_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '出库时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_outbound_no` (`order_no`),
  KEY `idx_outbound_wh` (`warehouse_id`),
  KEY `idx_outbound_emp` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单表';

-- ----------------------------
-- 7. 单据明细表 (order_detail)
-- 注: 通过 'order_type' 区分归属入库/出库单据
-- ----------------------------
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_type` VARCHAR(20) NOT NULL COMMENT '单据类型: INBOUND(入库) / OUTBOUND(出库)',
  `order_id` BIGINT NOT NULL COMMENT '关联入库单/出库单ID',
  `goods_id` BIGINT NOT NULL COMMENT '货品ID',
  `quantity` INT NOT NULL COMMENT '数量',
  `price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '单价快照',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_ref` (`order_type`, `order_id`),
  KEY `idx_goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单据明细表';

-- ----------------------------
-- 8. 盘点单表 (check_order)
-- ----------------------------
DROP TABLE IF EXISTS `check_order`;
CREATE TABLE `check_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '盘点单ID',
  `check_no` VARCHAR(64) NOT NULL COMMENT '盘点单号',
  `warehouse_id` BIGINT NOT NULL COMMENT '盘点仓库ID',
  `employee_id` BIGINT NOT NULL COMMENT '发起员工ID',
  `check_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '盘点时间',
  `status` VARCHAR(20) DEFAULT 'IN_PROGRESS' COMMENT '状态: IN_PROGRESS(盘点中) / COMPLETED(已完成)',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_check_no` (`check_no`),
  KEY `idx_check_wh` (`warehouse_id`),
  KEY `idx_check_emp` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点单表';

-- ----------------------------
-- 9. 盘点明细表 (check_detail)
-- ----------------------------
DROP TABLE IF EXISTS `check_detail`;
CREATE TABLE `check_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '盘点明细ID',
  `check_id` BIGINT NOT NULL COMMENT '盘点单ID',
  `goods_id` BIGINT NOT NULL COMMENT '货品ID',
  `system_stock` INT NOT NULL COMMENT '系统库存',
  `actual_stock` INT NOT NULL COMMENT '实际库存',
  `difference_stock` INT NOT NULL COMMENT '盈亏数量(actual - system)',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '盈亏原因/备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_check_id` (`check_id`),
  KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点明细表';

-- ========================================================
-- 初始化初始演示数据
-- ========================================================

-- 1. 员工数据 (主管和理货员，初始密码均为 123456)
INSERT INTO `employee` (`id`, `account`, `password`, `name`, `role`, `phone`, `status`) VALUES
(1, 'admin', '123456', '张主管', 'SUPERVISOR', '13800138001', 1),
(2, 'worker1', '123456', '李理货', 'TALLYMAN', '13800138002', 1),
(3, 'worker2', '123456', '王理货', 'TALLYMAN', '13800138003', 1);

-- 2. 往来单位数据
INSERT INTO `partner` (`id`, `name`, `contact_person`, `contact_phone`, `type`, `address`) VALUES
(1, '华为电子元器件供应商', '陈经理', '13911112222', 'SUPPLIER', '深圳市南山区高新科技园'),
(2, '晨光文具日用品制造厂', '赵经理', '13933334444', 'SUPPLIER', '上海市松江区工业园'),
(3, '苏宁易购线下门店', '孙店长', '13755556666', 'CUSTOMER', '北京市海淀区中关村大街'),
(4, '永辉超市生鲜百货店', '周经理', '13777778888', 'CUSTOMER', '杭州市西湖区文三路');

-- 3. 仓库数据
INSERT INTO `warehouse` (`id`, `name`, `address`, `status`) VALUES
(1, '1号中心仓(电子仓)', '仓储物流园区A栋101', 'ACTIVE'),
(2, '2号综合仓(日用仓)', '仓储物流园区B栋202', 'ACTIVE');

-- 4. 货品数据 (部分货品库存低于安全库存以测试预警)
INSERT INTO `goods` (`id`, `sku_code`, `name`, `spec`, `safety_stock`, `stock_quantity`, `warehouse_id`, `price`, `status`, `version`) VALUES
(1, 'SKU-ELEC-001', '无线静音蓝牙鼠标', '黑晶双模版/Type-C充电', 20, 50, 1, 49.90, 'NORMAL', 0),
(2, 'SKU-ELEC-002', '机械键盘 87键', '青轴/RGB背光', 15, 8, 1, 199.00, 'WARNING', 0),
(3, 'SKU-ELEC-003', 'Type-C 快充数据线 1.5m', '编织耐用款/66W', 50, 120, 1, 15.50, 'NORMAL', 0),
(4, 'SKU-DAILY-001', '晨光A4多功能打印纸', '70g/500张/包', 30, 25, 2, 22.00, 'WARNING', 0),
(5, 'SKU-DAILY-002', '得力按动中性笔 0.5mm', '黑笔/12支装', 40, 100, 2, 18.80, 'NORMAL', 0);

-- 5. 入库单初始记录
INSERT INTO `inbound_order` (`id`, `order_no`, `partner_id`, `warehouse_id`, `employee_id`, `inbound_time`, `remark`) VALUES
(1, 'RK202501010001', 1, 1, 1, '2025-01-01 10:00:00', '元旦采购首批电子元器件');

-- 6. 出库单初始记录
INSERT INTO `outbound_order` (`id`, `order_no`, `partner_id`, `warehouse_id`, `employee_id`, `outbound_time`, `remark`) VALUES
(1, 'CK202501020001', 3, 1, 2, '2025-01-02 14:30:00', '门店紧急补货');

-- 7. 单据明细初始记录
INSERT INTO `order_detail` (`id`, `order_type`, `order_id`, `goods_id`, `quantity`, `price`) VALUES
(1, 'INBOUND', 1, 1, 50, 45.00),
(2, 'INBOUND', 1, 3, 120, 12.00),
(3, 'OUTBOUND', 1, 1, 10, 49.90);

-- 8. 盘点单初始记录
INSERT INTO `check_order` (`id`, `check_no`, `warehouse_id`, `employee_id`, `check_time`, `status`, `remark`) VALUES
(1, 'PD202501030001', 1, 1, '2025-01-03 09:00:00', 'COMPLETED', '2025年第一季度电子仓例行盘点');

-- 9. 盘点明细初始记录
INSERT INTO `check_detail` (`id`, `check_id`, `goods_id`, `system_stock`, `actual_stock`, `difference_stock`, `remark`) VALUES
(1, 1, 1, 40, 40, 0, '账实相符'),
(2, 1, 2, 10, 8, -2, '运输途中破损2件');

SET FOREIGN_KEY_CHECKS = 1;
