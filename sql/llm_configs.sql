-- 大模型配置表
CREATE TABLE `llm_configs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `llm_type` varchar(50) NOT NULL COMMENT 'LLM类型：chatgpt,deepseek,doubao,qwen,ollama',
  `api_key` varchar(500) NOT NULL COMMENT 'API密钥',
  `base_url` varchar(500) NOT NULL COMMENT '基础URL',
  `model_name` varchar(100) NOT NULL COMMENT '模型名称',
  `temperature` decimal(3,2) DEFAULT 0.7 COMMENT '温度参数',
  `max_tokens` int(11) DEFAULT 2000 COMMENT '最大token数',
  `timeout` int(11) DEFAULT 30000 COMMENT '超时时间(毫秒)',
  `is_default` tinyint(1) DEFAULT 0 COMMENT '是否默认配置',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_llm_type` (`llm_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='大模型配置表';
