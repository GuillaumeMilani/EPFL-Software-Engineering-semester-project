<?php

/**
* Add an item into the database
* better error handling
*/
function log_user_token($name,$token)
{
	global $pdo;
	$name= strtolower($name);
	
	if(!checkEmail($name))
	{
		throw new Exception('Wrong email');
	}
	
	if(!checktoken($token))
	{
		throw new Exception('Wrong token');
	}

	$query = $pdo->prepare('UPDATE `tb_recipient_user` SET `registrationToken` = :token WHERE `email` = :email');
	
	$query->bindParam(':token',$token,PDO::PARAM_STR);
	$query->bindParam(':email',$name,PDO::PARAM_STR);
	
	//TODO need to know what to do if the email dosn't exist
	
	if($query->execute())
	{
		return array('status' => 'Ack');
	}
	else
	{
		throw new Exception("Query error");
	}
}