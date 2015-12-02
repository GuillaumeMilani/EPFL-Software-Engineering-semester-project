<?php
include_once('models/users/retrieve_users.php');

// Retrieve post data
$content = urldecode(file_get_contents('php://input'));
// decode the json
$data = json_decode($content, true);

if($data == null)
{
	http_response_code(400);
	echo format_array(array('error' => 'json data not found'));
}
else
{
	// extract decoded data
	$name= $data['name'];
	
	try {
	    $response = retrieve_user($name);
	    http_response_code(201);
	    echo format_array($response);
	} catch (Exception $e) {
	
	    echo format_array(array('error' => $e->getMessage()));
	    http_response_code(500);
	   
	}
}