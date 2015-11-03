<?php
include_once('models/items/get_items.php');

/**
 * TODO: Check params
 */
$post_content = urldecode(file_get_contents("php://input"));
if ($post_content == null)
	http_response_code(400);
	die("You must precise a user to fetch the messages");

$params_array = json_decode($post_content, true);

$items = get_items($params_array['recipient'], $params_array['lastRefresh']);

echo json_encode($items, JSON_NUMERIC_CHECK);

// We don't use the views for the moment (would contain only an "echo")
// include_once('views/items/retrieve.php');