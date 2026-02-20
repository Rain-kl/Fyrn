CREATE database if NOT EXISTS `fyrn` default character set utf8mb4 collate utf8mb4_unicode_ci;
use `fyrn`;

SET NAMES utf8mb4;

-- ----------------------------
-- Table structure for oms_parameter
-- ----------------------------
DROP TABLE IF EXISTS `wb_task`;

CREATE TABLE `wb_task`
(
    `id`            VARCHAR(64)  NOT NULL COMMENT '任务ID',
    `name`          VARCHAR(100) NOT NULL COMMENT '任务名称',
    `description`   TEXT COMMENT '任务描述',
    `type`          TINYINT      NOT NULL DEFAULT 0 COMMENT '任务类型 0-普通 1-里程碑 2-子任务',
    `tag`           VARCHAR(100) COMMENT '任务分类',
    `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '任务状态 0-未开始 1-进行中 2-已完成 3-已取消',
    `progress`      TINYINT      NOT NULL DEFAULT 0 COMMENT '任务进度 0-100',
    `priority`      TINYINT      NOT NULL DEFAULT 1 COMMENT '优先级 1-低 2-中 3-高 4-紧急',
    `parent_id`     VARCHAR(64)           DEFAULT NULL COMMENT '父任务ID',
    `start_time`    DATETIME              DEFAULT NULL COMMENT '任务开始时间',
    `deadline`      DATETIME              DEFAULT NULL COMMENT '任务截止时间',
    `complete_time` DATETIME              DEFAULT NULL COMMENT '任务完成时间',

    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),

    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`),
    KEY `idx_deadline` (`deadline`),
    KEY `idx_create_time` (`create_time`)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='工作台任务表';
