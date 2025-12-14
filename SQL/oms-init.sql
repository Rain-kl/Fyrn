CREATE database if NOT EXISTS `fyrn` default character set utf8mb4 collate utf8mb4_unicode_ci;
use `fyrn`;

SET NAMES utf8mb4;

-- ----------------------------
-- Table structure for oms_parameter
-- ----------------------------
DROP TABLE IF EXISTS `oms_parameter`;
CREATE TABLE `oms_parameter`
(
    `param_code`     BIGINT(20) NOT NULL COMMENT '主键',
    `param_name`     VARCHAR(50)  DEFAULT NULL COMMENT '参数名称',
    `kind_code`      VARCHAR(200) DEFAULT NULL COMMENT '参数类型',
    `param_value`    VARCHAR(500) DEFAULT NULL COMMENT '参数值',
    `param_desc`     VARCHAR(200) DEFAULT NULL COMMENT '参数描述',
    `enabled_flag`   TINYINT(4)   DEFAULT '1' COMMENT '是否启用',
    `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`param_code`),
    KEY `idx_kind_code` (`kind_code`),
    KEY `idx_enabled_flag` (`enabled_flag`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
    COMMENT='系统参数表';
