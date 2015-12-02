<?php
include_once('models/items/add_items.php');
include_once('models/conditions/add_conditions.php');
include_once('utils/push.php');
global $pdo;

$data = get_post_JSON();
$return_array;

// Extract decoded data
if (isset($data['type']) && isset($data['from']) && isset($data['to']) && isset($data['condition'])) {
		$message = $data['message'];
		$type = $data['type'];
		$from = $data['from'];
		$to = $data['to'];
		$date = time();
		$condition = $data['condition'];
		
		$return_array = array("type" => $type, "from" => $from, "to" => $to, "date" => $date, "message" => $message);
		
		if ($type == "SIMPLETEXTITEM") {
		
		} else if ($type == "FILEITEM" && isset($data['data'])) {
			$item_data = $data['data'];
			$return_array['data'] = $item_data;
		} else {
			http_response_code(400);
			die("Error : json malformed");
		}
} else {
	http_response_code(400);
	die("Error : json malformed");
}

$condition_id = -1;

try {
	$pdo->beginTransaction();
	
	if ($condition['type'] == "true") {
		$condition_id = null;
	} else {
		// If metadata are specified, extract them from the JSON and store the condition as a String
		if (isset($condition['metadata'])) {
			$metadata = $condition['metadata'];
			unset($condition['metadata']);
		}
		$condition = json_encode($condition);
				
		$condition_id = add_condition($condition);
		
		foreach ($metadata as $data) {
			if ($data['type'] == "POSITIONCONDITION" && isset($data['latitude']) && isset($data['longitude'])) {
				add_metadata_position($condition_id, $data['latitude'], $data['longitude']);
			} else {
				$pdo->rollBack();
				http_response_code(400);
				die("Error : json malformed");
			}
		}			
	}
		
	// add the data into the db
	$item_id = add_items($from['ID'],$to['ID'],$date,$type,$message,$condition_id);
	
	if ($type == "SIMPLETEXTITEM") {
		add_items_text($item_id);
	} else if ($type == "FILEITEM") {
		add_items_file($item_id, $item_data);
	}
	
	$pdo->commit();
} catch (Exception $e) {
	http_response_code(500);
	die("Error : database in item creation ".$e->getMessage());
}

http_response_code(201);
$return_array['ID'] = $item_id;
$return_array['condition'] = json_decode($condition);
echo json_encode($return_array);
send_push_to($to,$type);
