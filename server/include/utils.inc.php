<?php
	require_once(realpath(dirname(__FILE__).'/config.inc.php'));

    $pdo = new PDO('mysql:host='.MYSQL_HOST.';port='.MYSQL_PORT.';dbname='.MYSQL_DBNAME, MYSQL_USER, MYSQL_PASSWORD,array(PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION));
    $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
?>