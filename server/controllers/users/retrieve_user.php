<?php
include_once('models/users/retrieve_users.php');
include_once('utils/push.php');

$data= get_post_JSON();

// extract decoded data
$name= $data['name'];

try {
    $response = retrieve_user($name);
    http_response_code(201);
    echo format_array($response);
	//send push to the user retrieved
	send_push_to($response['user']['ID'],"RETRIEVE");
} catch (Exception $e) {

    echo format_array(array('error' => $e->getMessage()));
    http_response_code(500);
	   
}