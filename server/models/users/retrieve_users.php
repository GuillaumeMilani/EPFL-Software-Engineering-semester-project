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
	
	if($query->execute() == true && $query->rowCount() == 1) // if the query was correctly execute and we have only one ID returned
	{
		$result = $query->fetch(PDO::FETCH_ASSOC);
		return array('name' => $name,
		 'ID' => $result['ID'],
		 'type' => 'user');
	}
	else
	{
		throw new Exception("Query wasn't executed");
	}

}