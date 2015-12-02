<?php

/**
* Add a condition into the database
* TODO : Better management of the type
*/
function add_condition($condition)
{
	global $pdo;
	
	$query = $pdo->prepare('INSERT INTO `tb_condition` (`ID`, `condition`) VALUES (NULL, :condition)');
	$query->bindParam(':condition',$condition, PDO::PARAM_STR);
	try {
		$query->execute();
		return $pdo->lastInsertId();
	} catch (Exception $e) {
		http_response_code(500);
		die("Error : database in add condition ".$e->getMessage());
	}

}
/**
*	Add an item of type text into the database
*/
function add_metadata_position($condition_id,$latitude,$longitude)
{
	global $pdo;
	
	$condition_id = (int) $condition_id;
	$ID = add_metadata($condition_id);
	
	try {
		$query = $pdo->prepare('INSERT INTO `tb_metadata_position` 
				(`ID`, `latitude`, `longitude`) VALUES 
				(:id, :latitude, :longitude)');
		$query->bindParam(':id',$ID,PDO::PARAM_INT);
		$query->bindParam(':latitude',$latitude,PDO::PARAM_STR);
		$query->bindParam(':longitude',$longitude,PDO::PARAM_STR);
		
		return $query->execute();
	} catch (Exception $e) {
		http_response_code(500);
		die("Error : database in add metadata position ".$e->getMessage());
	}
}

function add_metadata($condition_id) {
	global $pdo;
	
	try {
		$query = $pdo->prepare('INSERT INTO `tb_metadata` (`ID`, `condition`) VALUES (NULL, :condition)');
		$query->bindParam(':condition',$condition_id,PDO::PARAM_INT);
		$query->execute();
		return $pdo->lastInsertId();
	} catch (Exception $e) {
		http_response_code(500);
		die("Error : database in add metadata ".$e->getMessage());
	}
}