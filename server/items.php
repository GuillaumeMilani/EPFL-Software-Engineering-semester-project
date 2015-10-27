<?php
include_once('utils/utils.php');

$action = @$_GET['action'];

if ($action == 'retrieve') {
	include_once('controllers/items/retrieve_items.php');
} else if ($action == 'send') {
	include_once('controllers/items/send_item.php');
} else {
	http_response_code(404);
	die("Page not found. Specify a correct action to perform");
}