-- 角色配置表
CREATE TABLE `role_configs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `role_description` text COMMENT '角色描述',
  `system_prompt` text NOT NULL COMMENT '系统提示词',
  `llm_config_id` bigint(20) NOT NULL COMMENT '关联的大模型配置ID',
  `temperature` decimal(3,2) DEFAULT NULL COMMENT '温度参数(覆盖LLM默认值)',
  `max_tokens` int(11) DEFAULT NULL COMMENT '最大token数(覆盖LLM默认值)',
  `is_default` tinyint(1) DEFAULT 0 COMMENT '是否默认角色',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_llm_config_id` (`llm_config_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_role_configs_llm_config_id` FOREIGN KEY (`llm_config_id`) REFERENCES `llm_configs` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色配置表';
