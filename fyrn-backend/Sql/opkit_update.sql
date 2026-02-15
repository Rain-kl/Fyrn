use `fyrn`;

SET NAMES utf8mb4;

-- ----------------------------

INSERT IGNORE INTO oms_parameter
(param_code, param_name, kind_code, param_value, param_desc, enabled_flag)
VALUES (20010, 'deeplx 提供商', 'opkit', '["base_url","token"]', '配置 deeplx 参数', 1);
