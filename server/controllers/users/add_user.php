<?php
include_once('models/users/add_users.php');
include_once('models/users/retrieve_users.php');
include_once('models/users/log_users.php');

$data= get_post_JSON();

// extract decoded data
$token= $data['token'];
$name= $data['name'];

try {
    $response = add_recipient($name,$token);
    http_response_code(201);
    echo format_array($response);
} catch (Exception $e) {
    http_response_code(500);
    echo format_array(array('error' => $e->getMessage()));
}