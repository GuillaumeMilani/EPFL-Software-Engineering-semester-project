<?php
include_once('models/items/add_conditions.php');
global $pdo;

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
try {
	$pdo->beginTransaction();
	$condition_id = add_condition($condition);
	
	foreach ($metadata as $data) {
		if ($data['type'] == "position" && isset($data['latitude']) && isset($data['longitude'])) {
			add_metadata_position($condition_id, $data['latitude'], $data['longitude']);
		} else {
			$pdo->rollBack();
			http_response_code(400);
			die("Error : json malformed");
		}
	}
	$pdo->commit();
} catch (Exception $e) {
	$pdo->rollBack();
	http_response_code(500);
	die("Server error");
}

echo json_encode(array("ID" => $condition_id));