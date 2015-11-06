<?php
include_once('include/utils.inc.php');

if (@$_GET['action'] == 'add') { // add a new user
	include_once('controllers/users/add_user.php');
}else if (@$_GET['action'] == 'retrieve') { // retrieve an existing user
	include_once('controllers/users/retrieve_user.php');
}else if (@$_GET['action'] == 'log') { // will be useful when we will implement multiple device connection
	include_once('controllers/users/log_user.php');
} else if (@$_GET['action'] == 'disconnect') { // not really useful
	include_once('controllers/users/disconnect_user.php');
}else {
	http_response_code(400);
	echo "wrong url";
	die("You must specify an action to perform");
}