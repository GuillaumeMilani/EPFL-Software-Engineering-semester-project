<?php
include_once('models/items/add_conditions.php');

$data = get_post_JSON();

// extract decoded data
if (isset($data['metadata'])) {
		$metadata = $data['metadata'];
		unset($data['metadata']);
		$condition = json_encode($data);
} else {
	http_response_code(400);
	die("Error : json malformed");
}
$condition_id = add_condition($condition);

foreach ($metadata as $data) {
	if ($data['type'] == "position" && isset($data['latitude']) && isset($data['longitude']) && isset($data['radius'])) {
		if (!add_metadata_position($condition_id, $data['latitude'], $data['longitude'], $data['radius'])) {
			http_response_code(500);
			die("Error : database");
		}
	} else {
		http_response_code(400);
		die("Error : json malformed");
	}
}

echo json_encode(array("ID" => $condition_id));