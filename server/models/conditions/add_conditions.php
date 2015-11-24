<?php

/**
* Add a condition into the database
* TODO : Better management of the type
*/
function add_condition($condition)
{
	global $pdo;
	
	$query = $pdo->prepare('INSERT INTO `tb_condition` (`ID`, `condition`) VALUES (NULL, :condition)');
	$query->bindParam(':condition',$condition['condition'], PDO::PARAM_STR);
	try {
		$query->execute();
		return $pdo->lastInsertId();
	} catch (Exception $e) {
		http_response_code(500);
		die("Error : database in condition insertion");
	}

}
/**
*	Add an item of type text into the database
*/
function add_metadata_position($condition_id,$latitude,$longitude,$radius)
{
	global $pdo;
	
	$condition_id = (int) $condition_id;
	$ID = add_metadata($condition_id);
	
	try {
		$query = $pdo->prepare('INSERT INTO `tb_metadata_position` 
				(`ID`, `latitude`, `longitude`, `radius`) VALUES 
				(:id, :latitude, :longitude, :radius)');
		$query->bindParam(':id',$id,PDO::PARAM_INT);
		$query->bindParam(':latitude',$latitude,PDO::PARAM_STR);
		$query->bindParam(':longitude',$longitude,PDO::PARAM_STR);
		$query->bindParam(':radius',$radius,PDO::PARAM_STR);
		
		return $query->execute();
	} catch (Exception $e) {
		http_response_code(500);
		die("Error : database");
	}
}

function add_metadata($condition_id) {
	global $pdo;
	
	try {
		$query = $pdo->prepare('INSERT INTO `tb_metadata` (`ID`, `condition`) VALUES (NULL, :condition)');
		$query->bindParam(':condition',$condition_id,PDO::PARAM_INT);
		$query->execute();
		return $query->lastInsertId();
	} catch (Exception $e) {
		http_response_code(500);
		die("Error : database");
	}
}