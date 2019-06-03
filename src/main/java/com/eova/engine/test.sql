SELECT
	`chenzi`.`member`.`id` AS `id`,
	`chenzi`.`member`.`mobilephone` AS `mobilephone`,
	`chenzi`.`member`.`card_no` AS `card_no`,
	`chenzi`.`member`.`card_type` AS `card_type`,
	`chenzi`.`award`.`status` AS `status`,
	`chenzi`.`award`.`memo` AS `memo`,
	`chenzi`.`award`.`create_time` AS `create_time`
FROM
	(
		`chenzi`.`award`
		JOIN `chenzi`.`member`
	)
WHERE
	(
		`chenzi`.`award`.`uid` = `chenzi`.`member`.`id`
	)
	
	
SELECT
	`a`.`id` AS `id`,
	`a`.`memo` AS `memo`,
	`m`.`name` AS `name`
FROM
	`chenzi`.`award` `a`
JOIN `chenzi`.`member` `m`
WHERE
	(`a`.`uid` = `m`.`id`)

UPDATE `v_award` SET `memo`='111111' WHERE (`id`='11')



SELECT
	`a`.`id` AS `id`,
	`a`.`memo` AS `memo`,
	`m`.`name` AS `name`
FROM
	(
		`chenzi`.`award` `a`
		LEFT JOIN `chenzi`.`member` `m` ON ((`a`.`uid` = `m`.`id`))
	)
	
CREATE TABLE `user_info` (
  `id` int(11) NOT NULL,
  `rid` int(11) DEFAULT '0' COMMENT '冗余角色ID',
  `status` int(2) DEFAULT '0' COMMENT '状态',
  `nickname` varchar(30) DEFAULT '' COMMENT '昵称',
  `mobile` varchar(11) DEFAULT NULL COMMENT '联系手机',
  `province` int(11) DEFAULT NULL COMMENT '省',
  `city` int(11) DEFAULT NULL COMMENT '市',
  `region` int(11) DEFAULT NULL COMMENT '区',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_nickname` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户详细信息';

