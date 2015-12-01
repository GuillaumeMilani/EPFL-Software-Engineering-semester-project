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

/**
 * Get a user from the DB using his ID
 * @param int $user_id
 */
function get_user_by_id($user_id) {
	global $pdo;
	$res = $pdo->prepare('SELECT *, "user" as "type"
			FROM view_user as usr
    		WHERE usr.ID = :user_id');

	$res->bindParam(':user_id', $user_id, PDO::PARAM_INT);
	$res->execute();

	if (!($res = $res->fetch(PDO::FETCH_ASSOC))) {
		throw new RuntimeException("No user where found for ID ".$user_id);
	}

	return $res;
}