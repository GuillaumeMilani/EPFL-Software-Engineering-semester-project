<?php

/**
* Add an item into the database
* better error handling
*/
function add_recipient($email,$deviceID)
{
	global $pdo;
	$email = strtolower($email);
	
	if(checkEmail($email) == false)
	{
		die("Wrong email");
	}
	
	if(!checkdeviceID($deviceID))
	{
		die("wrong device ID");
	}
	$query = $pdo->prepare('INSERT INTO `tb_recipient` (`name`) VALUES(NULL)');
	
	if($query->execute() == true)
	{
		return add_recipientUser($pdo->lastInsertId(),$email,$deviceID);
	}
	else
	{
		return -1;
	}

}
/**
*	Add an item of type text into the database
*/
function add_recipientUser($ID,$email,$devID)
{
	global $pdo;
	
	$id = (int) $ID;
	
	$query = $pdo->prepare('INSERT INTO `tb_recipient_user` (`ID`,`device_ID`,`email`) VALUES(:id,:device,:email)');
	
	$query->bindParam(':id',$id,PDO::PARAM_INT);
	$query->bindParam(':device',$devID,PDO::PARAM_STR);
	$query->bindParam(':email',$email,PDO::PARAM_STR);
	if($query->execute() == true)
	{
		return $id;
	}
	else
	{
		return -1;
	}
}

function checkdeviceID($deviceID)
{
	return ctype_xdigit($deviceID) && strlen($deviceID) == 16;
}

function checkEmail($email)
{
	return filter_var($email, FILTER_VALIDATE_EMAIL);
}