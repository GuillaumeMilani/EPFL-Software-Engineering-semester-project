<?php

/**
* Add an item into the database
* better error handling
* What if there is a database error in the recipient user parts
*/
function add_recipient($name,$token)
{
	global $pdo;
	$name = strtolower($name);
	
	if(checkEmail($name) == false)
	{
		 throw new Exception('Wrong name :'.$name);
	}
	
	if(!checktoken($token))
	{
		throw new Exception('Wrong token');
	}
	
	// check if user already exist
	try {
		$response = retrieve_user($name);
		$id = $response['user']['ID'];
		//update token if user exist
		log_user_token($name,$token);
		
		return array('ID' => $id);
	} catch (Exception $e) {
	    // user doesn't exist
	    // continue
	}
	
	
	//TODO check how to do for group
	$query = $pdo->prepare('INSERT INTO `tb_recipient` (`name`) VALUES(:name)');
	$query->bindParam(':name', $name, PDO::PARAM_STR);
	
	if($query->execute() == true)
	{
		return add_recipientUser($pdo->lastInsertId(),$name,$token);
	}
	else
	{
		 throw new Exception("Query wasn't executed into top table");
	}

}
/**
*	Add an item of type text into the database
*/
function add_recipientUser($ID,$name,$token)
{
	global $pdo;
	
	$id = (int) $ID;
	
	$query = $pdo->prepare('INSERT INTO `tb_recipient_user` (`ID`,`registrationToken`,`email`) VALUES(:id,:token,:email)');
	
	$query->bindParam(':id',$id,PDO::PARAM_INT);
	$query->bindParam(':token',$token,PDO::PARAM_STR);
	$query->bindParam(':email',$name,PDO::PARAM_STR);
	$query->execute();
	
	return array('ID' => $id);
}