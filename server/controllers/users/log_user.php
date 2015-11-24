<?php
include_once('models/users/log_users.php');

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
	$token= $data['token'];
	$name= $data['name'];
	
	try {
	    $response = log_user_token($name,$token);
	    http_response_code(201);
	    echo format_array($response);
	} catch (Exception $e) {
	    http_response_code(500);
	    echo format_array(array('error' => $e->getMessage()));
	}
}