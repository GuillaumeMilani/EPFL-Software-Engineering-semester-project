<?php
include_once('models/users/retrieve_users.php');
include_once('functions.php');

function send_push_to($id,$type)
{
	// retrieve registration id
	try {
		$user = get_user_by_id($id);
		var_dump($user);
		if(checkToken($user['registrationToken']))
		{
			send_push(array($user['registrationToken']),$type);
		}
	
	} catch (Exception $e) {
		//TODO decide what we do if the user doesn't have a registration id
	}
	//send message
}

// TODO look at testNotification.php
function send_push($registrationIds,$type)
{

	if(!is_array($registrationIds))
	{
		echo format_array(array('error' => 'registration Ids not array'));
	}
	// prep the bundle
	$msg = array
	(
		'message' 	=> $type,
		'title'		=> 'New Item',
		'tickerText'	=> 'You have received a new item',
		'vibrate'	=> 1,
		'sound'		=> 1,
		'largeIcon'	=> 'large_icon',
		'smallIcon'	=> 'small_icon'
	);
	$fields = array
	(
		'registration_ids' 	=> $registrationIds,
		'data'			=> $msg
	);
	 
	$headers = array
	(
		'Authorization: key=' . API_ACCESS_KEY,
		'Content-Type: application/json'
	);
	 
	$ch = curl_init();
	curl_setopt( $ch,CURLOPT_URL, 'https://android.googleapis.com/gcm/send' );
	curl_setopt( $ch,CURLOPT_POST, true );
	curl_setopt( $ch,CURLOPT_HTTPHEADER, $headers );
	curl_setopt( $ch,CURLOPT_RETURNTRANSFER, true );
	curl_setopt( $ch,CURLOPT_SSL_VERIFYPEER, false );
	curl_setopt( $ch,CURLOPT_POSTFIELDS, json_encode( $fields ) );
	$result = curl_exec($ch );
	curl_close( $ch );
}