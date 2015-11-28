<?php

/**
* Retrieve a user from the database
* TODO change when we will use the group function
*/
function retrieve_user($name)
{
	global $pdo;
	$name= strtolower($name);
	
	if(checkEmail($name) == false)
	{
		throw new Exception('Wrong email');
	}
	
	$query = $pdo->prepare('SELECT `ID`,`email` FROM `tb_recipient_user` WHERE `email` = :email');
	$query->bindParam(':email',$name,PDO::PARAM_STR);
	
	if($query->execute() == true) // if the query was correctly execute and we have only one ID returned
	{
		if($query->rowCount() == 1)
		{
		$result = $query->fetch(PDO::FETCH_ASSOC);
		$responseData = array('name' => $name,
		 'ID' => $result['ID'],
		 'type' => 'user');
		return array('user' => $responseData);
		}
		else
		{
			throw new Exception("User not found");
		}
	}
	else
	{
		throw new Exception("Query wasn't executed");
	}

}