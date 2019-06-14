SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `log_recharge`;
CREATE TABLE `log_recharge` (
  `lid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `order_id` varchar(255),
  `player_id` int(11) unsigned NOT NULL,
  `account_id` varchar(255) NOT NULL,
  `platform` varchar(32) NOT NULL,
  `channel` varchar(32) NOT NULL,
  `server_id` varchar(32) NOT NULL DEFAULT '0',
  `shop_id` varchar(32) NOT NULL,
  `shop_name` varchar(255) NOT NULL,
  `type` int(1) NOT NULL,
  `enumber` int(11) unsigned DEFAULT '0',
  `number` int(11) unsigned DEFAULT '0',
  `price` decimal(10,3) NOT NULL,
  `is_first` tinyint(1) NOT NULL DEFAULT b'0',
  `ip` varchar(255),
  `operator` varchar(128) NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`lid`),
  KEY `player_id` (`player_id`),
  KEY `create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `log_curr`;
CREATE TABLE `log_curr` (
  `lid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `player_id` int(11) unsigned NOT NULL,
  `type` int(11) NOT NULL,
  `way` int(11) NOT NULL,
  `number` int(11) NOT NULL,
  `enumber` int(11) unsigned DEFAULT '0',
  `arg1` varchar(128) NOT NULL DEFAULT '1',
  `ip` varchar(255),
  `operator` varchar(128) NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`lid`),
  KEY `player_id` (`player_id`),
  KEY `create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `log_curr_club`;
CREATE TABLE `log_curr_club` (
  `lid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `club_id` int(11) unsigned NOT NULL,
  `type` int(11) NOT NULL,
  `way` int(11) NOT NULL,
  `number` int(11) NOT NULL,
  `enumber` int(11) unsigned DEFAULT '0',
  `arg1` varchar(128) NOT NULL DEFAULT '1',
  `ip` varchar(255),
  `operator` varchar(128) NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`lid`),
  KEY `club_id` (`club_id`),
  KEY `create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `log_user`;
CREATE TABLE `log_user` (
  `lid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `player_id` int(11) unsigned NOT NULL,
  `account_id` varchar(255) NOT NULL,
  `platform` varchar(32) NOT NULL,
  `channel` varchar(32) NOT NULL,
  `device_id` varchar(255) DEFAULT NULL,
  `server_id` varchar(32) NOT NULL DEFAULT '0',
  `arg1` varchar(128) NOT NULL DEFAULT '1',
  `ip` varchar(255),
  `operator` varchar(128) NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`lid`),
  KEY `player_id` (`player_id`),
  KEY `create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `log_device`;
CREATE TABLE `log_device` (
  `lid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `player_id` int(11) unsigned NOT NULL,
  `os` varchar(255) ,
  `os_version` varchar(255) ,
  `device_model` varchar(255) ,
  `device_name` varchar(255) ,
  `device_type` varchar(255) ,
  `device_guid` varchar(255) ,
  `sms` varchar(255),
  `gdn` varchar(255),
  `gms` varchar(255),
  `pf` varchar(255),
  `idfa` varchar(255) DEFAULT NULL,
  `system_language` varchar(255),
  `ip` varchar(255),
  `operator` varchar(128) NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`lid`),
  UNIQUE KEY `device_index` (`device_guid`),
  KEY `player_id` (`player_id`),
  KEY `create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `log_login`;
CREATE TABLE `log_login` (
  `lid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `player_id` int(11) unsigned NOT NULL,
  `account_id` varchar(255) NOT NULL,
  `device_id` varchar(255) DEFAULT NULL,
  `platform` varchar(32) NOT NULL,
  `channel` varchar(32) NOT NULL,
  `server_id` varchar(32) NOT NULL DEFAULT '0',
  `type` int(11) NOT NULL,
  `arg1` varchar(128) NOT NULL DEFAULT '1',
  `ip` varchar(255),
  `operator` varchar(128) NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`lid`),
  KEY `player_id` (`player_id`),
  KEY `create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `log`;
CREATE TABLE `log` (
  `lid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `player_id` int(11) unsigned zerofill NOT NULL,
  `type` int(11) NOT NULL,
  `arg1` varchar(128) NOT NULL DEFAULT '1',
  `ip` varchar(255),
  `operator` varchar(128) NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`lid`),
  KEY `player_id` (`player_id`),
  KEY `create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `log_online`;
CREATE TABLE `log_online` (
  `lid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `server_id` varchar(32) NOT NULL DEFAULT '0',
  `online_player_num` int NOT NULL DEFAULT '0',
  `max_player_num` int NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`lid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `log_room`;
CREATE TABLE `log_room` (
  `lid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `room_id` int(11) unsigned NOT NULL,
  `creator_id` int(11) unsigned NOT NULL,
  `game_type` int(11) NOT NULL,
  `room_type` int(11) NOT NULL,
  `room_charge` int(11) NOT NULL DEFAULT '0',
  `operator` varchar(128) NOT NULL,
  `arg1` varchar(128) NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`lid`),
  KEY `room_id` (`room_id`),
  KEY `create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `log_chain_card`;
CREATE TABLE `log_chain_card` (
  `lid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `player_id` int(11) unsigned NOT NULL,
  `card_id` int(11) unsigned NOT NULL,
  `create_id` int(11) unsigned NOT NULL,
  `chain_token` int(11) NOT NULL,
  `chain_state` int(11) NOT NULL,
  `create_time` datetime NOT NULL,
  `trans_hash` varchar(128) NOT NULL DEFAULT '',
  PRIMARY KEY (`lid`),
  KEY `player_id` (`player_id`),
  KEY `card_id` (`card_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
