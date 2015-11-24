<?php
include_once('utils/utils.php');
include_once('utils/functions.php');

if (@$_GET['action'] == 'add') { // add a new user
	include_once('controllers/users/add_user.php');
}else if (@$_GET['action'] == 'retrieve') { // retrieve an existing user
	include_once('controllers/users/retrieve_user.php');
}else if (@$_GET['action'] == 'log') { // will be useful when we will implement multiple device connection. now is used for receiving the token
	include_once('controllers/users/log_user.php');
}else {
	http_response_code(400);
	echo format_array(array('error' => 'wrong url'));
}