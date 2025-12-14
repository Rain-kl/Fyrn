use `fyrn`;

SET NAMES utf8mb4;

-- ----------------------------

DELETE
FROM `oms_parameter`
WHERE param_code = 1001;

INSERT INTO `oms_parameter` (param_code, param_name, kind_code, param_value, param_desc, enabled_flag)
VALUES (1001, '生料文件目录地址', 'mms', '~/data/fyrn/um/', '生料文件存放目录', 1);