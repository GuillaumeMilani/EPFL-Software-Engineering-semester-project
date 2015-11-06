<?php

/**
* Add an item into the database
* better error handling
*/
function log_user($email,$deviceID)
{
	global $pdo;
	$email = strtolower($email);
	
	if(!checkEmail($email))
	{
		die("Wrong email");
	}
	
	if(!checkdeviceID($deviceID))
	{
		die("wrong device ID");
	}

	$query = $pdo->prepare('UPDATE `tb_recipient` SET `device_id` = :device WHERE `email` = :email');
	
	$query->bindParam(':device',$to,PDO::PARAM_STR);
	$query->bindParam(':email',$from,PDO::PARAM_STR);
	
	$query->execute();
	

}

function checkdeviceID($deviceID)
{
	return ctype_xdigit($deviceID) && strlen($deviceID) == 16;
}

function checkEmail($email)
{
	return filter_var($email, FILTER_VALIDATE_EMAIL)
}