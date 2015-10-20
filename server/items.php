<?php
include_once('utils/utils.php');

if (@$_GET['action'] == 'retrieve') {
	include_once('controllers/items/retrieve_items.php');
} else if (@$_GET['action'] == 'send') {
	include_once('controllers/items/send_item.php');
} else {
	die("You must specify an action to perform");
}