-- 建数据库
create database if not exists damai_ai character set utf8mb4;
-- 创建表
CREATE TABLE `d_chat_type_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `type` int NOT NULL COMMENT '会话类型，详见ChatType枚举',
  `chat_id` varchar(225) NOT NULL COMMENT '会话id',
  `title` varchar(512) DEFAULT NULL COMMENT '标题',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `edit_time` datetime DEFAULT NULL COMMENT '编辑时间',
  `status` tinyint(1) DEFAULT '1' COMMENT '1:正常 0:删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb3 COMMENT='会话历史表';

-- AI调用追踪表
CREATE TABLE `d_ai_trace` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `trace_id` varchar(64) NOT NULL COMMENT '追踪ID',
  `conversation_id` varchar(225) DEFAULT NULL COMMENT '会话ID',
  `model_name` varchar(64) DEFAULT NULL COMMENT '模型名称',
  `request_type` varchar(32) DEFAULT NULL COMMENT '请求类型: CHAT, RAG, FUNCTION_CALL',
  `prompt_tokens` int DEFAULT NULL COMMENT '输入Token数',
  `completion_tokens` int DEFAULT NULL COMMENT '输出Token数',
  `total_tokens` int DEFAULT NULL COMMENT '总Token数',
  `latency_ms` bigint DEFAULT NULL COMMENT '响应延迟（毫秒）',
  `estimated_cost` decimal(10,6) DEFAULT NULL COMMENT '预估费用（元）',
  `user_input` text DEFAULT NULL COMMENT '用户输入（截断）',
  `ai_output` text DEFAULT NULL COMMENT 'AI输出（截断）',
  `success` tinyint(1) DEFAULT 1 COMMENT '是否成功',
  `error_message` text DEFAULT NULL COMMENT '错误信息',
  `metadata` json DEFAULT NULL COMMENT '附加元数据',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `edit_time` datetime DEFAULT NULL COMMENT '编辑时间',
  `status` tinyint(1) DEFAULT '1' COMMENT '1:正常 0:删除',
  PRIMARY KEY (`id`),
  KEY `idx_trace_id` (`trace_id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AI调用追踪表';