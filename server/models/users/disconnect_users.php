<?php

/**
* Add an item into the database
* better error handling
*/
function disconnect_user($email)
{
	global $pdo;
	
	if(!checkEmail($email))
	{
		die("Wrong email");
	}
	

	$query = $pdo->prepare('UPDATE `tb_recipient` SET `device_id` = NULL WHERE `email` = :email');
	
	$query->bindParam(':email',$from,PDO::PARAM_STR);
	
	$query->execute();
	

}

function checkEmail($email)
{
	return filter_var($email, FILTER_VALIDATE_EMAIL)
}