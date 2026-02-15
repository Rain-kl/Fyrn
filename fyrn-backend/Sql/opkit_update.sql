use `fyrn`;

SET NAMES utf8mb4;

-- ----------------------------

INSERT IGNORE INTO oms_parameter
(param_code, param_name, kind_code, param_value, param_desc, enabled_flag)
VALUES (20100, '翻译引擎', 'opkit', 'deeplx', '配置默认翻译引擎', 1);

-- ----------------------------

INSERT IGNORE INTO oms_parameter
(param_code, param_name, kind_code, param_value, param_desc, enabled_flag)
VALUES (20101, 'deeplx 提供商', 'opkit', '["base_url","token"]', '配置 deeplx 参数', 1);
