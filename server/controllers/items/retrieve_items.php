<?php
include_once('models/items/get_items.php');

$params_array = get_post_JSON();

if (isset($params_array['latitudeMin']) 
		&& isset($params_array['latitudeMax']) 
		&& isset($params_array['longitudeMin']) 
		&& isset($params_array['longitudeMax'])
		&& isset($params_array['recipient'])
		&& isset($params_array['lastRefresh'])) {
	try {
		$items = get_item_with_location(
				$params_array['recipient'],
				$params_array['lastRefresh'],
				$params_array['latitudeMin'],
				$params_array['latitudeMax'],
				$params_array['longitudeMin'],
				$params_array['longitudeMax']);
	} catch (Exception $e) {
		die($e->getMessage());
	}
} else if (isset($params_array['recipient']) && isset($params_array['lastRefresh'])) {
	$items = get_all_private_items($params_array['recipient'], $params_array['lastRefresh']);
} else {
	// Malformed request
	http_response_code(400);
}
echo json_encode($items, JSON_NUMERIC_CHECK);

// We don't use the views for the moment (would contain only an "echo")
// include_once('views/items/retrieve.php');