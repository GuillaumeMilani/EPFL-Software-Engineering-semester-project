<?php

/**
* Add an item into the database
* TODO : Better management of the type
*/
function add_items($from,$to,$date,$type,$message,$condition_id)
{
	global $pdo;
	
	$from = (int) $from;
	$to = (int) $to;
	$date = (int) $date;
	if ($to == -1) {
		$to = null;
	}
	
	$query = $pdo->prepare('INSERT INTO `tb_item` (`ID`, `from`, `to`, `date`, `condition`, `message`) VALUES (NULL, :from, :to, :date, :condition, :message)');
	$query->bindParam(':to',$to,PDO::PARAM_INT);
	$query->bindParam(':from',$from,PDO::PARAM_INT);
	$query->bindParam(':date',$date, PDO::PARAM_INT);
	$query->bindParam(':condition', $condition_id, PDO::PARAM_INT);
	$query->bindParam(':message', $message, PDO::PARAM_STR);
	$query->execute();
	$item_id = $pdo->lastInsertId();
	
	add_items_text($item_id);
	return $item_id;
}
/**
*	Add an item of type text into the database
*/
function add_items_text($ID)
{
	global $pdo;
	
	$ID = (int) $ID;
	
	$query = $pdo->prepare('INSERT INTO `tb_item_text` (`ID`) VALUES (:id)');
	$query->bindParam(':id',$ID,PDO::PARAM_INT);
	$query->execute();
	
	return $ID;
}