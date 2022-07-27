ALTER TABLE `config_info`
  ADD COLUMN `encrypted_data_key` text CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '秘钥';

ALTER TABLE `config_info_beta`
  ADD COLUMN `encrypted_data_key` text CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '秘钥';

ALTER TABLE `his_config_info`
  ADD COLUMN `encrypted_data_key` text CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '秘钥';