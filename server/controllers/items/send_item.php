<?php
include_once('models/items/add_items.php');

// Retrieve post data
$content = urldecode(file_get_contents('php://input'));
// decode the json
$data = json_decode($content, true);
//check if there is an error
if($data == null)
{
	echo "Error : json data not found";
}
else
{
	// extract decoded data
	$item = $data['message'];
	$type = $data['type'];
	$from = $data['from'];
	$to = $data['to'];
	$date = $data['date'];

	// add the data into the db
	if(add_items($from['ID'],$to['ID'],$date,$type,$item))
	{
		echo "Ack";
	}
	else
	{
		echo "Error : database";
	}
}