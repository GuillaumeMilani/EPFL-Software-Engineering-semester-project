<?php

/**
* Take an array and format it to send it back to the client
*/
function format_array($array)
{
	if(!is_array($array))
	{
		 throw new Exception('Not an array');
	}
		
	return json_encode($array);
}

/**
* Check if the string is an email
*/
function checkEmail($email)
{
	return filter_var($email, FILTER_VALIDATE_EMAIL);
}


/**
* Check if the value is an 16 length hexadecimal 
*
*/
function checkdeviceID($deviceID)
{
	return ctype_xdigit($deviceID) && strlen($deviceID) == 16;
}

/**
*Check if the value is an 152 length string
*/
function checkToken($token)
{
	return (strlen($token) == 152);
}