/*
 Navicat Premium Data Transfer

 Source Server         : 本机
 Source Server Type    : MySQL
 Source Server Version : 80032
 Source Host           : localhost:3306
 Source Schema         : springbootf1k262s1

 Target Server Type    : MySQL
 Target Server Version : 80032
 File Encoding         : 65001

 Date: 07/08/2025 08:30:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for products
-- ----------------------------
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products`  (
  `product_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID，自增主键',
  `product_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称，如\"长白山人参\"',
  `category_id` int UNSIGNED NULL DEFAULT NULL COMMENT '所属分类ID，关联categories表',
  `main_image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主图URL，用于商品列表展示',
  `other_image_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '其他图片URL列表，JSON数组格式：[\"url1\",\"url2\"]',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品描述信息，介绍商品特点和功效',
  `price` decimal(10, 2) NOT NULL COMMENT '当前售价，精确到分',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价，用于显示折扣信息',
  `stock_quantity` int NOT NULL DEFAULT 0 COMMENT '库存数量，默认0表示无库存',
  `is_active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否上架，1表示上架，0表示下架',
  `purchase_links` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '购买链接列表，JSON数组格式：[{platform:\"淘宝\",url:\"https://...\"}]',
  `product_details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品详细信息，JSON对象格式：{成分:\"人参、枸杞\",用法:\"每日3次\"}',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '商品创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '商品更新时间，自动更新',
  PRIMARY KEY (`product_id`) USING BTREE,
  INDEX `idx_products_category_id`(`category_id`) USING BTREE,
  INDEX `idx_products_name`(`product_name`) USING BTREE,
  CONSTRAINT `products_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品信息表，合并了产品、图片、链接和详细信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of products
-- ----------------------------
INSERT INTO `products` VALUES (1, '长白山人参', 4, 'http://example.com/images/ginseng.jpg', '[\"http://example.com/images/ginseng1.jpg\",\"http://example.com/images/ginseng2.jpg\"]', '优质长白山人参，补气养血，增强免疫力', 199.99, 299.99, 100, 1, '[{\"platform\":\"淘宝\",\"url\":\"https://taobao.com/ginseng\"},{\"platform\":\"京东\",\"url\":\"https://jd.com/ginseng\"}]', '{\"成分\":\"人参\",\"用法\":\"每日1-2次，每次3-5克，煎服\"}', '2025-07-29 15:08:51', '2025-07-29 15:08:51');
INSERT INTO `products` VALUES (2, '当归', 4, 'https://img95.699pic.com/photo/60019/9474.jpg_wh860.jpg', '[\"http://example.com/images/danggui1.jpg\"]', '优质当归，补血活血，调经止痛', 49.99, 69.99, 200, 1, '[{\"platform\":\"淘宝\",\"url\":\"https://taobao.com/danggui\"}]', '{\"成分\":\"当归\",\"用法\":\"每日2次，每次5-10克，煎服\"}', '2025-07-29 15:08:51', '2025-07-30 09:23:06');
INSERT INTO `products` VALUES (3, '菊花', 5, 'https://ts3.tc.mm.bing.net/th/id/OIP-C.-fO9JXGnZbYTlzFlGotyyAHaHa?rs=1&pid=ImgDetMain&o=7&rm=3', '[\"http://example.com/images/chrysanthemum1.jpg\"]', '清热解毒，平肝明目，适合泡茶', 29.99, 39.99, 300, 1, '[{\"platform\":\"京东\",\"url\":\"https://jd.com/chrysanthemum\"}]', '{\"成分\":\"菊花\",\"用法\":\"每日1-2次，每次3-5克，泡茶饮用\"}', '2025-07-29 15:08:51', '2025-07-31 15:11:34');
INSERT INTO `products` VALUES (4, '六味地黄丸', 6, 'http://example.com/images/liuwei.jpg', '[\"http://example.com/images/liuwei1.jpg\",\"http://example.com/images/liuwei2.jpg\"]', '滋阴补肾，治疗肾阴虚', 89.99, 109.99, 150, 1, '[{\"platform\":\"淘宝\",\"url\":\"https://taobao.com/liuwei\"}]', '{\"成分\":\"熟地黄、山药、山茱萸等\",\"用法\":\"每日2次，每次8粒，口服\"}', '2025-07-29 15:08:51', '2025-07-29 15:08:51');
INSERT INTO `products` VALUES (5, '云南白药喷雾', 7, 'http://example.com/images/yunnanbaiyao.jpg', '[\"http://example.com/images/yunnanbaiyao1.jpg\"]', '活血化瘀，消肿止痛，适合外伤', 59.99, 79.99, 80, 1, '[{\"platform\":\"京东\",\"url\":\"https://jd.com/yunnanbaiyao\"}]', '{\"成分\":\"三七、冰片等\",\"用法\":\"外用，喷于患处，每日2-3次\"}', '2025-07-29 15:08:51', '2025-07-29 15:08:51');
INSERT INTO `products` VALUES (6, '深海鱼油', 3, 'http://example.com/images/fishoil.jpg', '[\"http://example.com/images/fishoil1.jpg\"]', '富含Omega-3，保护心血管健康', 129.99, 159.99, 50, 1, '[{\"platform\":\"淘宝\",\"url\":\"https://taobao.com/fishoil\"}]', '{\"成分\":\"鱼油、维生素E\",\"用法\":\"每日1次，每次2粒，随餐服用\"}', '2025-07-29 15:08:51', '2025-07-29 15:08:51');

SET FOREIGN_KEY_CHECKS = 1;
