<?php
include_once('models/users/retrieve_users.php');

function send_push_to($id)
{
	// retrieve registration id
	try {
		$user = get_user_by_id($id);
		if(checkToken($user['registrationToken']))
		{
			send_push(array($user['registrationToken']));
		}
	
	} catch (Exception $e) {
		//TODO decide what we do if the user doesn't have a registration id
	}
	//send message
}

// TODO look at testNotification.php
function send_push($registrationIds)
{

	if(!is_array($registrationIds))
	{
		echo format_array(array('error' => 'registration Ids not array'));
	}
	// prep the bundle
	$msg = array
	(
		'message' 	=> 'here is a message. message',
		'title'		=> 'This is a title. title',
		'subtitle'	=> 'This is a subtitle. subtitle',
		'tickerText'	=> 'Ticker text here...Ticker text here...Ticker text here',
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
	echo $result;
}