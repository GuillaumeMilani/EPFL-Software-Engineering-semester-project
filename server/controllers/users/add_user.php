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
	echo format_array(array('error' => 'json data not found'));
}
else
{
	// extract decoded data
	$deviceID= $data['deviceID'];
	$name= $data['name'];
	
	try {
	    $response = add_recipient($name,$deviceID);
	    http_response_code(201);
	    echo format_array($response);
	} catch (Exception $e) {
	    http_response_code(500);
	    echo format_array(array('error' => $e->getMessage()));
	}
}