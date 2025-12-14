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
    `id`            BIGINT UNSIGNED NOT NULL,
    `job_id`        VARCHAR(64)     NOT NULL COMMENT '业务任务ID(对外返回)',
    `task_type`     VARCHAR(64)     NOT NULL COMMENT '任务类型，如 file_batch',
    `status`        TINYINT         NOT NULL COMMENT '0=queued,1=running,2=success,3=failed,4=canceled',
    `message`       VARCHAR(1024)   NULL COMMENT '状态说明/失败摘要',
    `created_user`  VARCHAR(64)     NULL COMMENT '触发人',
    `started_time`  DATETIME(3)     NULL COMMENT '任务开始时间',
    `finished_time` DATETIME(3)     NULL COMMENT '任务结束时间',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_job_id (job_id),
    KEY idx_status_updated (status, update_time),
    KEY idx_task_type_created (task_type, create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='任务表';