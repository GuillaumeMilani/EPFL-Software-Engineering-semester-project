<?php
include_once('models/items/log_users.php');

// Retrieve post data
$content = urldecode(file_get_contents('php://input'));
// decode the json
$data = json_decode($content, true);
//check if there is an error
//TODO error handling
if($data == null)
{
	http_response_code(400);
	echo "Error : json data not found";
}
else
{
	// extract decoded data
	$deviceID= $data['DeviceID'];
	$email= $data['Email'];

	// add the data into the db
	if(log_user($email,$deviceID))
	{
		http_response_code(201);
		echo "Ack";
	}
	else
	{
		http_response_code(500);
		echo "Error : database";
	}
}