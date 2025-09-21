-- 用户配置表
CREATE TABLE `d_user_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `description` text COMMENT '角色描述',
  `api_key` varchar(500) NOT NULL COMMENT 'API Key',
  `base_url` varchar(200) DEFAULT 'https://api.openai.com/v1' COMMENT 'Base URL',
  `model` varchar(50) DEFAULT 'gpt-3.5-turbo' COMMENT '模型名称',
  `temperature` decimal(3,2) DEFAULT 0.70 COMMENT '温度参数',
  `max_tokens` int DEFAULT 2000 COMMENT '最大Token数',
  `system_prompt` text COMMENT '系统提示词',
  `enabled` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `edit_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户配置表'; 