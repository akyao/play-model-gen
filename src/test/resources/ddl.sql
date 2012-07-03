SET SESSION FOREIGN_KEY_CHECKS=0;

/* Drop Tables */

DROP TABLE IF EXISTS hoge_sub2;
DROP TABLE IF EXISTS hoge;
DROP TABLE IF EXISTS hoge_sub;




/* Create Tables */

CREATE TABLE hoge
(
	id bigint NOT NULL AUTO_INCREMENT,
	field_varchar varchar(255),
	field_bit1 bit(1),
	field_bit2 bit(1) DEFAULT false NOT NULL,
	field_bit3 bit(1) DEFAULT true NOT NULL,
	field_blog blob,
	field_boolean boolean,
	field_char char(8),
	field_date date,
	field_datetime datetime,
	field_decimal decimal(10,2),
	field_double double(10,2),
	hoge_sub_id bigint unsigned,
	PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARACTER SET utf8;


CREATE TABLE hoge_sub
(
	id bigint unsigned NOT NULL UNIQUE AUTO_INCREMENT,
	PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARACTER SET utf8;


CREATE TABLE hoge_sub2
(
	id bigint unsigned NOT NULL UNIQUE AUTO_INCREMENT,
	hoge_id bigint NOT NULL,
	PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARACTER SET utf8;



/* Create Foreign Keys */

ALTER TABLE hoge_sub2
	ADD FOREIGN KEY (hoge_id)
	REFERENCES hoge (id)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE hoge
	ADD FOREIGN KEY (hoge_sub_id)
	REFERENCES hoge_sub (id)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;



