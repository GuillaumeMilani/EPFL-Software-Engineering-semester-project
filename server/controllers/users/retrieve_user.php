<?php
include_once('models/users/retrieve_users.php');

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
	$email= $data['name'];
	
	$response = retrieve_user($email);

	// add the data into the db
	if($response != -1)
	{
		http_response_code(201);
		// TODO : optimization using json_encode
		echo '{"user": {
      				"name":"'.$email.'",
     				 "ID":'.$response.',
     				 "type":"user"
 			      }}';
 		
	}
	else
	{
		http_response_code(500);
		echo "Error : database";
	}
}