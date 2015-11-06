<?php

/**
* Retrieve a user from the database
* TODO better error handling
*/
function retrieve_user($email)
{
	global $pdo;
	
	if(checkEmail($email) == false)
	{
		die("Wrong email");
	}
	
	$query = $pdo->prepare('SELECT `ID`,`email` FROM `tb_recipient_user` WHERE `email` = :email');
	$query->bindParam(':email',$email,PDO::PARAM_STR);
	
	if($query->execute() == true && $query->rowCount() == 1) // if the query was correctly execute and we have only one ID returned
	{
		$result = $query->fetch(PDO::FETCH_ASSOC);
		return $result['ID'];
	}
	else
	{
		return -1;
	}

}

function checkEmail($email)
{
	return filter_var($email, FILTER_VALIDATE_EMAIL);
}