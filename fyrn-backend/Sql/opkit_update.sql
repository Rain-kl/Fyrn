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

-- ----------------------------

INSERT IGNORE INTO oms_parameter
(param_code, param_name, kind_code, param_value, param_desc, enabled_flag)
VALUES (20102, 'llm 提供商', 'opkit', '["http://127.0.0.1:1234/v1","","gpt-5"]',
        '配置 llm 参数：[baseUrl, apiKey, model]', 1);

