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
                                       `status` tinyint DEFAULT '1' COMMENT '1:正常 0:删除',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='会话历史表';