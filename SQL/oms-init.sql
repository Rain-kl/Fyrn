CREATE database if NOT EXISTS `fyrn` default character set utf8mb4 collate utf8mb4_unicode_ci;
use `fyrn`;

SET NAMES utf8mb4;

-- ----------------------------
-- Table structure for oms_parameter
-- ----------------------------
DROP TABLE IF EXISTS `oms_parameter`;
CREATE TABLE `oms_parameter`
(
    `param_code`   BIGINT(20) NOT NULL COMMENT '主键',
    `param_name`   VARCHAR(50)  DEFAULT NULL COMMENT '参数名称',
    `kind_code`    VARCHAR(200) DEFAULT NULL COMMENT '参数类型',
    `param_value`  VARCHAR(500) DEFAULT NULL COMMENT '参数值',
    `param_desc`   VARCHAR(200) DEFAULT NULL COMMENT '参数描述',
    `enabled_flag` TINYINT(4)   DEFAULT '1' COMMENT '是否启用',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`param_code`),
    KEY `idx_kind_code` (`kind_code`),
    KEY `idx_enabled_flag` (`enabled_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='系统参数表';


-- ----------------------------
-- Table structure for oms_job
-- ----------------------------
DROP TABLE IF EXISTS `oms_job`;
CREATE TABLE oms_job
(
    `job_id`        VARCHAR(64)   NOT NULL COMMENT '业务任务ID',
    `task_type`     VARCHAR(64)   NOT NULL COMMENT '任务类型，如 file_batch',
    `status`        TINYINT       NOT NULL COMMENT '0=queued,1=running,2=success,3=failed,4=canceled',
    `message`       VARCHAR(4096) NULL COMMENT '执行消息',
    `created_user`  VARCHAR(64)   NULL COMMENT '触发人',
    `started_time`  DATETIME(3)   NULL COMMENT '任务开始时间',
    `finished_time` DATETIME(3)   NULL COMMENT '任务结束时间',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (job_id),
    KEY idx_status_updated (status, update_time),
    KEY idx_task_type_created (task_type, create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='任务表';


-- ----------------------------
-- Table structure for oms_user
-- ----------------------------
DROP TABLE IF EXISTS `oms_user`;
CREATE TABLE `oms_user`
(
    `user_id`         VARCHAR(64)  NOT NULL COMMENT '用户ID',
    `username`        VARCHAR(64)  NOT NULL COMMENT '登录用户名',
    `password`        VARCHAR(255) NOT NULL COMMENT '登录密码(加密)',
    `nickname`        VARCHAR(64)  NULL COMMENT '用户昵称',
    `email`           VARCHAR(128) NULL COMMENT '邮箱',
    `phone`           VARCHAR(32)  NULL COMMENT '手机号',
    `last_login_ip`   VARCHAR(64)  NULL COMMENT '最后登录IP',
    `last_login_time` DATETIME(3)  NULL COMMENT '最后登录时间',
    `created_user`    VARCHAR(64)  NULL COMMENT '创建人',
    `status`          TINYINT  DEFAULT 1 COMMENT '用户状态：0=禁用，1=正常，2=锁定',
    `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`user_id`),
    UNIQUE KEY uk_username (`username`),
    UNIQUE KEY uk_email (`email`),
    KEY idx_status_updated (`status`, `update_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统用户表';
