<?php
include_once('include/utils.inc.php');

if (@$_GET['action'] == 'add') {
	include_once('controllers/users/add_user.php');
} else if (@$_GET['action'] == 'log') {
	include_once('controllers/users/log_user.php');
} else if (@$_GET['action'] == 'disconnect') {
	include_once('controllers/users/disconnect_user.php');
}else {
	http_response_code(400);
	echo "wrong url";
	die("You must specify an action to perform");
}