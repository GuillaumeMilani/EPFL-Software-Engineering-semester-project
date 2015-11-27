<?php
include_once('models/items/add_items.php');
include_once('models/conditions/add_conditions.php');
include_once('utils/push.php');
global $pdo;

$data = get_post_JSON();

// extract decoded data
if (isset($data['message']) && isset($data['type']) && isset($data['from']) && isset($data['to']) && isset($data['condition'])) {
		$message = $data['message'];
		$type = $data['type'];
		$from = $data['from'];
		$to = $data['to'];
		$date = time();
		$condition = $data['condition'];
} else {
	http_response_code(400);
	die("Error : json malformed");
}

$condition_id = -1;
if ($condition['type'] == "true") {
	$condition_id = null;
} else {
	if (isset($condition['metadata'])) {
			$metadata = $condition['metadata'];
			unset($condition['metadata']);
			$condition = json_encode($condition);
	}
	
	$condition_id = add_condition($condition);
	
	foreach ($metadata as $data)
	{
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
}

$result;
try {
	// add the data into the db
	$result = add_items($from['ID'],$to['ID'],$date,$type,$message,$condition_id);
} catch (Exception $e) {
	http_response_code(500);
	die("Error : database in item creation ".$e->getMessage());
}
	http_response_code(201);
	echo json_encode(array("ID" => $result, "type" => $type, "from" => $from, "to" => $to, "date" => $date, "message" => $message, "condition" => json_decode($condition), "message" => $message));
	send_push_to($to);