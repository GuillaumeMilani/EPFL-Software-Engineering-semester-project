<?php
include_once('models/items/add_items.php');

// Retrieve post data
$content = urldecode(file_get_contents('php://input'));
// decode the json
$data = json_decode($content, true);
//check if there is an error
if($data == null)
{
	http_response_code(400);
	die("Error : json data not found");
}
else
{
	// extract decoded data
	if (isset($data['message']) && isset($data['type']) && isset($data['from']) && isset($data['to'])) {
			$item = $data['message'];
			$type = $data['type'];
			$from = $data['from'];
			$to = $data['to'];
			$date = $data['date'];
	} else {
		http_response_code(400);
		die("Error : json malformed");
	}

	// add the data into the db
	if(add_items($from['ID'],$to['ID'],$date,$type,$item))
	{
		http_response_code(201);
	}
	else
	{
		http_response_code(500);
		die("Error : database");
	}
}