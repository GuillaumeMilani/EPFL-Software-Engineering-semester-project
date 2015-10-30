<?php
include_once('models/users/add_users.php');

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
	$email= $data['name'];
	
	$response = add_recipient($email,$deviceID);

	// add the data into the db
	if($response != -1)
	{
		http_response_code(201);
		echo '{"ID": '.$response.'}';
	}
	else
	{
		http_response_code(500);
		echo "Error : database";
	}
}