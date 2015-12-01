<?php

/**
* Add an item into the database
* better error handling
* What if there is a database error in the recipient user parts
*/
function add_recipient($name,$deviceID)
{
	global $pdo;
	$name = strtolower($name);
	
	if(checkEmail($name) == false)
	{
		 throw new Exception('Wrong name');
	}
	
	if(!checkdeviceID($deviceID))
	{
		 throw new Exception('Wrong device ID');
	}
	$query = $pdo->prepare('INSERT INTO `tb_recipient` (`name`) VALUES(NULL)');
	
	if($query->execute() == true)
	{
		return add_recipientUser($pdo->lastInsertId(),$name,$deviceID);
	}
	else
	{
		 throw new Exception("Query wasn't executed");
	}

}
/**
*	Add an item of type text into the database
*/
function add_recipientUser($ID,$name,$devID)
{
	global $pdo;
	
	$id = (int) $ID;
	
	$query = $pdo->prepare('INSERT INTO `tb_recipient_user` (`ID`,`device_ID`,`email`) VALUES(:id,:device,:email)');
	
	$query->bindParam(':id',$id,PDO::PARAM_INT);
	$query->bindParam(':device',$devID,PDO::PARAM_STR);
	$query->bindParam(':email',$name,PDO::PARAM_STR);
	return array('ID' => $id);
}