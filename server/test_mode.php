<?php
include_once('utils/utils.php');

$action = @$_GET['action'];
global $pdo;

if ($action == 'enable') {

} else if ($action == 'disable') {

} else {
	http_response_code(404);
	die("Page not found. Specify a correct action to perform");
}