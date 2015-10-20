--
-- Contenu de la table `tb_item`
--

INSERT INTO `tb_item` (`ID`, `from`, `to`, `date`) VALUES
(1, 1, 2, '1445261987'),
(2, 2, 1, '1445262015');

--
-- Contenu de la table `tb_item_text`
--

INSERT INTO `tb_item_text` (`ID`, `text`) VALUES
(1, 'Hello Jane,\r\nhow are you ?!'),
(2, 'Hello John, I''m fine and you ?');

--
-- Contenu de la table `tb_recipient`
--

INSERT INTO `tb_recipient` (`ID`, `name`) VALUES
(2, 'Jane Doe'),
(1, 'John Doe');

--
-- Contenu de la table `tb_recipient_user`
--

INSERT INTO `tb_recipient_user` (`ID`) VALUES
(1),
(2);