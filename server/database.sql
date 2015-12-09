-- phpMyAdmin SQL Dump
-- version 4.5.0.2
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Dec 09, 2015 at 10:38 AM
-- Server version: 5.5.44-37.3-log
-- PHP Version: 5.6.16

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `japanimp_calamar`
--

-- --------------------------------------------------------

--
-- Table structure for table `tb_condition`
--

CREATE TABLE `tb_condition` (
  `ID` int(11) NOT NULL,
  `condition` varchar(512) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `tb_item`
--

CREATE TABLE `tb_item` (
  `ID` int(11) NOT NULL,
  `from` int(11) NOT NULL,
  `to` int(11) DEFAULT NULL,
  `date` mediumtext NOT NULL,
  `condition` int(11) DEFAULT NULL,
  `message` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `tb_item_file`
--

CREATE TABLE `tb_item_file` (
  `ID` int(11) NOT NULL,
  `data` blob NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `tb_item_image`
--

CREATE TABLE `tb_item_image` (
  `ID` int(11) NOT NULL,
  `data` mediumblob NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `tb_item_text`
--

CREATE TABLE `tb_item_text` (
  `ID` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `tb_metadata`
--

CREATE TABLE `tb_metadata` (
  `ID` int(11) NOT NULL,
  `condition` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `tb_metadata_position`
--

CREATE TABLE `tb_metadata_position` (
  `ID` int(11) NOT NULL,
  `latitude` float(10,6) NOT NULL,
  `longitude` float(10,6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `tb_recipient`
--

CREATE TABLE `tb_recipient` (
  `ID` int(11) NOT NULL,
  `name` varchar(45) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `tb_recipient_user`
--

CREATE TABLE `tb_recipient_user` (
  `ID` int(11) NOT NULL,
  `device_id` varchar(16) DEFAULT NULL,
  `email` varchar(45) NOT NULL,
  `registrationToken` varchar(152) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Stand-in structure for view `view_user`
--
CREATE TABLE `view_user` (
`ID` int(11)
,`name` varchar(45)
,`registrationToken` varchar(152)
);

-- --------------------------------------------------------

--
-- Structure for view `view_user`
--
DROP TABLE IF EXISTS `view_user`;

CREATE ALGORITHM=UNDEFINED DEFINER=`japanimp`@`localhost` SQL SECURITY DEFINER VIEW `view_user`  AS  select `rec`.`ID` AS `ID`,`rec`.`name` AS `name`,`usr`.`registrationToken` AS `registrationToken` from (`tb_recipient` `rec` join `tb_recipient_user` `usr`) where (`rec`.`ID` = `usr`.`ID`) ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `tb_condition`
--
ALTER TABLE `tb_condition`
  ADD PRIMARY KEY (`ID`);

--
-- Indexes for table `tb_item`
--
ALTER TABLE `tb_item`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `idx_from_item` (`from`),
  ADD KEY `idx_to_item` (`to`),
  ADD KEY `ct_item_condition` (`condition`);

--
-- Indexes for table `tb_item_file`
--
ALTER TABLE `tb_item_file`
  ADD PRIMARY KEY (`ID`);

--
-- Indexes for table `tb_item_image`
--
ALTER TABLE `tb_item_image`
  ADD PRIMARY KEY (`ID`);

--
-- Indexes for table `tb_item_text`
--
ALTER TABLE `tb_item_text`
  ADD PRIMARY KEY (`ID`);

--
-- Indexes for table `tb_metadata`
--
ALTER TABLE `tb_metadata`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `ct_metadata_condition` (`condition`);

--
-- Indexes for table `tb_metadata_position`
--
ALTER TABLE `tb_metadata_position`
  ADD PRIMARY KEY (`ID`);

--
-- Indexes for table `tb_recipient`
--
ALTER TABLE `tb_recipient`
  ADD PRIMARY KEY (`ID`),
  ADD UNIQUE KEY `idx_un_name_recipient` (`name`);

--
-- Indexes for table `tb_recipient_user`
--
ALTER TABLE `tb_recipient_user`
  ADD PRIMARY KEY (`ID`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `tb_condition`
--
ALTER TABLE `tb_condition`
  MODIFY `ID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=68;
--
-- AUTO_INCREMENT for table `tb_item`
--
ALTER TABLE `tb_item`
  MODIFY `ID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=66;
--
-- AUTO_INCREMENT for table `tb_metadata`
--
ALTER TABLE `tb_metadata`
  MODIFY `ID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;
--
-- AUTO_INCREMENT for table `tb_recipient`
--
ALTER TABLE `tb_recipient`
  MODIFY `ID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;
--
-- Constraints for dumped tables
--

--
-- Constraints for table `tb_item`
--
ALTER TABLE `tb_item`
  ADD CONSTRAINT `ct_from` FOREIGN KEY (`from`) REFERENCES `tb_recipient_user` (`ID`),
  ADD CONSTRAINT `ct_item_condition` FOREIGN KEY (`condition`) REFERENCES `tb_condition` (`ID`),
  ADD CONSTRAINT `ct_to` FOREIGN KEY (`to`) REFERENCES `tb_recipient` (`ID`);

--
-- Constraints for table `tb_item_file`
--
ALTER TABLE `tb_item_file`
  ADD CONSTRAINT `ct_id_item_file` FOREIGN KEY (`ID`) REFERENCES `tb_item` (`ID`);

--
-- Constraints for table `tb_item_image`
--
ALTER TABLE `tb_item_image`
  ADD CONSTRAINT `ct_id_image_file` FOREIGN KEY (`ID`) REFERENCES `tb_item` (`ID`);

--
-- Constraints for table `tb_item_text`
--
ALTER TABLE `tb_item_text`
  ADD CONSTRAINT `ct_id_item_text` FOREIGN KEY (`ID`) REFERENCES `tb_item` (`ID`);

--
-- Constraints for table `tb_metadata`
--
ALTER TABLE `tb_metadata`
  ADD CONSTRAINT `ct_metadata_condition` FOREIGN KEY (`condition`) REFERENCES `tb_condition` (`ID`);

--
-- Constraints for table `tb_metadata_position`
--
ALTER TABLE `tb_metadata_position`
  ADD CONSTRAINT `ct_id_metadata_position` FOREIGN KEY (`ID`) REFERENCES `tb_metadata` (`ID`);

--
-- Constraints for table `tb_recipient_user`
--
ALTER TABLE `tb_recipient_user`
  ADD CONSTRAINT `ct_id_recipient_user` FOREIGN KEY (`ID`) REFERENCES `tb_recipient` (`ID`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
