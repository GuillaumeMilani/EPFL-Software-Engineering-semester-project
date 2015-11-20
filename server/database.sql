-- -----------------------------------------------------
-- Table `tb_recipient`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_recipient` (
  `ID` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  PRIMARY KEY (`ID`)
)
ENGINE = InnoDB;

CREATE UNIQUE INDEX `idx_un_name_recipient` ON `tb_recipient` (`name` ASC)  ;


-- -----------------------------------------------------
-- Table `tb_recipient_user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_recipient_user` (
  `ID` INT NOT NULL,
  PRIMARY KEY (`ID`) ,
  CONSTRAINT `ct_id_recipient_user`
    FOREIGN KEY (`ID`)
    REFERENCES `tb_recipient` (`ID`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT
)
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `tb_condition`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_condition` (
    `ID` INT NOT NULL,
    `condition` VARCHAR(512),
    `value` TINYINT(1) NOT NULL,
    PRIMARY KEY (`ID`)
)
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `tb_metadata`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_metadata` (
    `ID` INT NOT NULL,
    `condition` INT NOT NULL,
    PRIMARY KEY (`ID`),
    CONSTRAINT `ct_metadata_condition`
      FOREIGN KEY (`condition`)
      REFERENCES `tb_condition` (`ID`)
      ON DELETE RESTRICT
      ON UPDATE RESTRICT
)
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `tb_metadata_position`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_metadata_position` (
  `ID` INT NOT NULL,
  `latitude` FLOAT( 10, 6 ) NOT NULL ,
  `longitude` FLOAT( 10, 6 ) NOT NULL,
  `radius` FLOAT NOT NULL,
  PRIMARY KEY (`ID`),
  CONSTRAINT `ct_id_metadata_position`
    FOREIGN KEY (`ID`)
    REFERENCES `tb_metadata` (`ID`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT
)
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `tb_item`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_item` (
  `ID` INT NOT NULL AUTO_INCREMENT,
  `from` INT NULL,
  `to` INT NOT NULL,
  `date` MEDIUMTEXT NOT NULL,
  `condition` INT NULL,
  PRIMARY KEY (`ID`) ,
  CONSTRAINT `ct_from`
    FOREIGN KEY (`from`)
    REFERENCES `tb_recipient_user` (`ID`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `ct_to`
    FOREIGN KEY (`to`)
    REFERENCES `tb_recipient` (`ID`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `ct_item_condition`
    FOREIGN KEY (`condition`)
    REFERENCES `tb_condition` (`ID`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT
)
ENGINE = InnoDB;

CREATE INDEX `idx_from_item` ON `tb_item` (`from` ASC)  ;
CREATE INDEX `idx_to_item` ON `tb_item` (`to` ASC)  ;


-- -----------------------------------------------------
-- Table `tb_item_text`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `tb_item_text` (
  `ID` INT NOT NULL,
  `text` LONGTEXT NULL,
  PRIMARY KEY (`ID`) ,
  CONSTRAINT `ct_id_item_text`
    FOREIGN KEY (`ID`)
    REFERENCES `tb_item` (`ID`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT
)
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Placeholder table for view `view_text_message`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `view_text_message` (`ID` INT, `from` INT, `to` INT, `date` INT, `text` INT);

-- -----------------------------------------------------
-- Placeholder table for view `mydb`.`view_user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `view_user` (`ID` INT, `name` INT);

-- -----------------------------------------------------
-- View `view_text_message`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `view_text_message`;

CREATE OR REPLACE VIEW `view_text_message` AS
SELECT 
    itm.ID, itm.from, itm.to, itm.date, txt.text
FROM tb_item itm, tb_item_text txt WHERE itm.id = txt.id;

-- -----------------------------------------------------
-- View `view_user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `view_user`;

CREATE OR REPLACE VIEW `view_user` AS
SELECT rec.ID, rec.name FROM tb_recipient rec, tb_recipient_user usr WHERE rec.id = usr.id;