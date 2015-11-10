<?php
	require_once('config.php');

    global $pdo;
    try {
	    $pdo = new PDO('mysql:host='.MYSQL_HOST.';port='.MYSQL_PORT.';dbname='.MYSQL_DBNAME, MYSQL_USER, MYSQL_PASSWORD,array(PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION));
	    $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
    } catch (Exception $e) {
    	http_response_code(500);
    	die("Database error : ".$e->getMessage());
    }
