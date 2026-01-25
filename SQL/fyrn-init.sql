CREATE database if NOT EXISTS `fyrn` default character set utf8mb4 collate utf8mb4_unicode_ci;
use `fyrn`;

SET NAMES utf8mb4;

-- ----------------------------
-- Table structure for mms_novel
-- ----------------------------
DROP TABLE IF EXISTS `mms_novel`;
CREATE TABLE `mms_novel`
(
    `id`           BIGINT(20) NOT NULL COMMENT '主键',
    `novel_title`  VARCHAR(50) DEFAULT NULL COMMENT '小说名称',
    `novel_author` VARCHAR(50) DEFAULT NULL COMMENT '小说作者',
    `status`       TINYINT(4)  DEFAULT '2' COMMENT '2：正常，4：删除',

    `create_time`  DATETIME    DEFAULT NULL COMMENT '创建时间',
    `update_time`  DATETIME    DEFAULT NULL COMMENT '修改时间',
    `created_user` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `updated_user` VARCHAR(50) DEFAULT NULL COMMENT '修改人',

    PRIMARY KEY (`id`),

    KEY `idx_author_status_time` (`novel_author`, `status`),
    KEY `idx_title_status` (`novel_title`, `status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='本地小说表';

-- ----------------------------
-- Table structure for mms_novel_file
-- ----------------------------
DROP TABLE IF EXISTS `mms_novel_file`;
CREATE TABLE `mms_novel_file`
(
    `id`           BIGINT(20) NOT NULL COMMENT '主键',
    `novel_id`     BIGINT(20)   DEFAULT NULL COMMENT '小说ID, 关联mms_novel表的主键',
    `file_name`    VARCHAR(100) DEFAULT NULL COMMENT '文件名称',
    `file_path`    VARCHAR(200) DEFAULT NULL COMMENT '文件路径',
    `file_size`    BIGINT(20)   DEFAULT NULL COMMENT '文件大小，单位：字节',
    `word_count`   BIGINT(20)   DEFAULT NULL COMMENT '小说字数, 单位：字',
    `status`       TINYINT(4)   DEFAULT '2' COMMENT '2：正常，4：删除',

    `create_time`  DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_time`  DATETIME     DEFAULT NULL COMMENT '修改时间',
    `created_user` VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `updated_user` VARCHAR(50)  DEFAULT NULL COMMENT '修改人',

    PRIMARY KEY (`id`),

    KEY `idx_author_status_time` (`novel_id`, `status`),
    KEY `idx_title_status` (`file_name`, `status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='本地小说表';



-- ----------------------------
-- Table structure for mms_novel_file
-- ----------------------------
DROP TABLE IF EXISTS `mms_meta`;
CREATE TABLE `mms_meta`
(
    `id`           BIGINT(20)   NOT NULL COMMENT '主键',
    `pbn`          VARCHAR(20)  DEFAULT NULL COMMENT 'Platform Book Number',
    `title`        VARCHAR(100) NOT NULL COMMENT '小说名称',
    `author`       VARCHAR(50)  NOT NULL COMMENT '小说作者',
    `tag`          VARCHAR(300) DEFAULT NULL COMMENT '小说标签',
    `summary`      TEXT         DEFAULT NULL COMMENT '小说简介',
    `popularity`   BIGINT(20)   DEFAULT '0' COMMENT '小说人气值',
    `word_count`   BIGINT(20)   DEFAULT '0' COMMENT '小说字数, 单位：字',
    `status`       TINYINT(4)   DEFAULT '2' COMMENT '1: 连载中, 2：完结',
    `source`       VARCHAR(10)  DEFAULT NULL COMMENT '小说来源',
    `source_url`   VARCHAR(200) DEFAULT NULL COMMENT '小说来源链接',
    `post_time`    DATETIME     DEFAULT NULL COMMENT '小说发布时间',
    `edit_time`    DATETIME     DEFAULT NULL COMMENT '小说最后编辑时间',

    `create_time`  DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_time`  DATETIME     DEFAULT NULL COMMENT '修改时间',
    `created_user` VARCHAR(50)  DEFAULT NULL COMMENT '创建人',
    `updated_user` VARCHAR(50)  DEFAULT NULL COMMENT '修改人',

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pbn` (`pbn`),

    KEY `idx_author` (`author`),
    KEY `idx_title` (`title`),
    KEY `idx_title_author` (`title`, `author`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='小说元数据表';
