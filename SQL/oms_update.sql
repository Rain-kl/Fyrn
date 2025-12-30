use `fyrn`;

SET NAMES utf8mb4;

-- ----------------------------

INSERT IGNORE INTO oms_parameter
(param_code, param_name, kind_code, param_value, param_desc, enabled_flag)
VALUES (1001, '生料文件目录地址', 'mms', '/data/fyrn/um/', '生料文件存放目录', 1);

-- ----------------------------

INSERT IGNORE INTO oms_parameter
(param_code, param_name, kind_code, param_value, param_desc, enabled_flag)
VALUES (1002, '生料回收站地址', 'mms', '/tmp/rubbish', '处理完的生料存放目录, 为空则删除', 1);
