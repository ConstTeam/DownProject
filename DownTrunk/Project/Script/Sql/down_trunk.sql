/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50639
Source Host           : localhost:3306
Source Database       : x_trunk

Target Server Type    : MYSQL
Target Server Version : 50639
File Encoding         : 65001

Date: 2018-04-20 16:04:55
*/

SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `anti_addi_info`;
CREATE TABLE `anti_addi_info` (
  `player_id` int(11) unsigned NOT NULL,
  `online_time` bigint(20) NOT NULL DEFAULT '0',
  `login_time` datetime NOT NULL,
  `logout_time` datetime NOT NULL,
  PRIMARY KEY (`player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `display_items`;
CREATE TABLE `display_items` (
  `items` varchar(60) NOT NULL,
  `open` smallint(6) NOT NULL DEFAULT '0',
  PRIMARY KEY (`items`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `recharge_info`;
CREATE TABLE `recharge_info` (
  `lid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `order_id` varchar(255) NOT NULL COMMENT '充值订单号',
  `platform` varchar(32) DEFAULT NULL,
  `channel` varchar(32) DEFAULT NULL,
  `account_id` varchar(255) NOT NULL,
  `player_id` int(11) unsigned zerofill NOT NULL,
  `price` varchar(32) NOT NULL DEFAULT '0',
  `diamond` int(11) unsigned DEFAULT '0',
  `shop_id` varchar(32) NOT NULL DEFAULT '0',
  `is_succ` tinyint(1) DEFAULT '0',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`lid`),
  UNIQUE KEY `uk_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `recharge_info_error`;
CREATE TABLE `recharge_info_error` (
  `lid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `order_id` varchar(255) NOT NULL COMMENT '充值订单号',
  `platform` varchar(32) DEFAULT NULL,
  `channel` varchar(32) DEFAULT NULL,
  `account_id` varchar(255) NOT NULL,
  `player_id` int(11) unsigned zerofill NOT NULL,
  `price` varchar(32) NOT NULL DEFAULT '0',
  `diamond` int(11) unsigned DEFAULT '0',
  `shop_id` varchar(32) NOT NULL DEFAULT '0',
  `is_succ` tinyint(1) DEFAULT '0',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`lid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `recharge_info_pre`;
CREATE TABLE `recharge_info_pre` (
  `lid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `order_id` varchar(255) NOT NULL COMMENT '充值订单号',
  `platform` varchar(32) DEFAULT NULL,
  `channel` varchar(32) DEFAULT NULL,
  `account_id` varchar(255) NOT NULL,
  `player_id` int(11) unsigned zerofill NOT NULL,
  `price` varchar(32) NOT NULL DEFAULT '0',
  `diamond` int(11) unsigned DEFAULT '0',
  `shop_id` varchar(32) NOT NULL DEFAULT '0',
  `is_succ` tinyint(1) DEFAULT '0',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`lid`),
  UNIQUE KEY `uk_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
-- ----------------------------
-- Table structure for clear_time
-- ----------------------------
DROP TABLE IF EXISTS `server_clear_time`;
CREATE TABLE `server_clear_time` (
  `server_id` varchar(32) NOT NULL DEFAULT '0',
  `type` varchar(32) NOT NULL,
  `clear_time` datetime NOT NULL,
  PRIMARY KEY (`server_id`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for player_info
-- ----------------------------
DROP TABLE IF EXISTS `player_account_info`;
CREATE TABLE `player_account_info` (
  `player_lid` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `player_id` int(11) unsigned NOT NULL,
  `platform_id` varchar(255) NOT NULL,
  `platform` varchar(32) NOT NULL DEFAULT '1',
  `channel` varchar(32) NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`player_lid`),
  UNIQUE KEY `player_id_index` (`player_id`),
  KEY `platform_id_index` (`platform_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `player_info`;
CREATE TABLE `player_info` (
  `player_id` int(11) unsigned NOT NULL,
  `nickname` varchar(255) NOT NULL,
  `icon` varchar(255) NOT NULL,
  `gold` int(11) unsigned DEFAULT '0',
  `diamond` int(11) unsigned DEFAULT '0',
  `last_login_time` varchar(64) DEFAULT NULL,
  `is_anti_addi` tinyint(1) NOT NULL DEFAULT '0',
  `anti_addi_state` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `server_list`;
CREATE TABLE `server_list` (
  `server_id` varchar(32) NOT NULL,
  `type` varchar(32) NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `player_use_count`;
CREATE TABLE `player_use_count` (
  `player_id` int(11) unsigned NOT NULL,
  `use_count_type` int(11) NOT NULL,
  `use_count_value` int(11) NOT NULL,
  PRIMARY KEY (`player_id`, `use_count_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `player_clear_time`;
CREATE TABLE `player_clear_time` (
  `player_id` int(11) unsigned NOT NULL,
  `clear_type` varchar(32) NOT NULL,
  `clear_time` datetime NOT NULL,
  PRIMARY KEY (`player_id`, `clear_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `player_role`;
CREATE TABLE `player_role` (
  `player_id` int(11) unsigned NOT NULL,
  `role` int(11) NOT NULL,
  PRIMARY KEY (`player_id`, `role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `player_scene`;
CREATE TABLE `player_scene` (
  `player_id` int(11) unsigned NOT NULL,
  `scene` int(11) NOT NULL,
  PRIMARY KEY (`player_id`, `scene`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
