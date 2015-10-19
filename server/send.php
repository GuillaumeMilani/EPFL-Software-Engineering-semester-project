<?php
include_once('include/utils.inc.php');
include_once('models/add_items.php');

$content = urldecode(file_get_contents('php://input'));
//echo $content;
//parse_str($content,$fields);
//echo 'michel'.$fields['html'];
//$data = json_decode($fields['html'], true);
$data = json_decode($content, true);
if($data == null)
{
	echo "Error : json data not found";
}
else
{
	/*print_r($data);
	echo '<br/>';*/
	// extract data
	$item = $data['message'];
	$type = $data['type'];
	$from = $data['from'];
	$to = $data['to'];
	$date = $data['date'];
	
	/*echo 'item = '.$item.'<br/>';
	echo 'type = ';
	print_r($type);
	echo '<br/>';
	echo 'from= ';
	print_r($from);
	echo '<br/>';
	echo 'to= '.$to.'<br/>';
	echo 'date= '.$date.'<br/>';*/
	if(add_items($from['ID'],$to['ID'],$date,$type,$item))
	{
		echo "Ack";
	}
	else
	{
		echo "Error : database";
	}
}