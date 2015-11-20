<?php
include_once('utils/utils.php');

$action = @$_GET['action'];

if ($action == 'retrieve') {
	include_once('controllers/conditions/retrieve_conditions.php');
} else if ($action == 'add') {
	include_once('controllers/items/add_conditions.php');
} else {
	http_response_code(404);
	die("Page not found. Specify a correct action to perform");
}