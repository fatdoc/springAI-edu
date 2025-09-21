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

 Date: 07/08/2025 09:10:44
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
  `category_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID，自增主键',
  `category_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称，如\"中药材\"、\"中成药\"',
  `parent_category_id` int UNSIGNED NULL DEFAULT NULL COMMENT '父分类ID，NULL表示顶级分类',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分类创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '分类更新时间，自动更新',
  PRIMARY KEY (`category_id`) USING BTREE,
  INDEX `parent_category_id`(`parent_category_id`) USING BTREE,
  CONSTRAINT `categories_ibfk_1` FOREIGN KEY (`parent_category_id`) REFERENCES `categories` (`category_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品分类表，支持多级分类结构' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of categories
-- ----------------------------
INSERT INTO `categories` VALUES (1, '中药材', NULL, '2025-07-29 15:08:38', '2025-07-29 15:08:38');
INSERT INTO `categories` VALUES (2, '中成药', NULL, '2025-07-29 15:08:38', '2025-07-29 15:08:38');
INSERT INTO `categories` VALUES (3, '保健品', NULL, '2025-07-29 15:08:38', '2025-07-29 15:08:38');
INSERT INTO `categories` VALUES (4, '根茎类', 1, '2025-07-29 15:08:38', '2025-07-29 15:08:38');
INSERT INTO `categories` VALUES (5, '叶花类', 1, '2025-07-29 15:08:38', '2025-07-29 15:08:38');
INSERT INTO `categories` VALUES (6, '内服药', 2, '2025-07-29 15:08:38', '2025-07-29 15:08:38');
INSERT INTO `categories` VALUES (7, '外用药', 2, '2025-07-29 15:08:38', '2025-07-29 15:08:38');

SET FOREIGN_KEY_CHECKS = 1;
