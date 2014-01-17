-- VAR TBLNAME DumbCoin

-- ACT CREATE_TABLE
CREATE TABLE IF NOT EXISTS `{TBLNAME}` (
    `Id` INT(255) NOT NULL AUTO_INCREMENT,
    `Username` VARCHAR(32) NOT NULL,
    `Balance` DOUBLE(64,2) NOT NULL,
    UNIQUE KEY `Username` (`Username`),
    PRIMARY KEY `Id` (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

-- ACT UPDATE_BALANCE_MOD
INSERT INTO `{TBLNAME}` (`Username`, `Balance`) VALUES (?, ?)
    ON DUPLICATE KEY UPDATE `Balance` = `Balance` + ?

-- ACT UPDATE_BALANCE_SET
INSERT INTO `{TBLNAME}` (`Username`, `Balance`) VALUES (?, ?)
    ON DUPLICATE KEY UPDATE `Balance` = ?

-- ACT GET_BALANCE
SELECT `Username`,`Balance` FROM `{TBLNAME}` WHERE `Username` = ?

-- ACT GET_ALL_BALANCES
SELECT `Username`,`Balance` FROM `{TBLNAME}` ORDER BY `Balance` DESC