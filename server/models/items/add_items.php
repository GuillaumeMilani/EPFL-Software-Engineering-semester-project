<?php

/**
* Add an item into the database
* TODO : Better management of the type
*/
function add_items($from,$to,$date,$type,$type_data,$condition_id)
{
	global $pdo;
	
	$from = (int) $from;
	$to = (int) $to;
	$date = (int) $date;
	if ($to == -1) {
		$to = null;
	}
	
	$query = $pdo->prepare('INSERT INTO `tb_item` (`ID`, `from`, `to`, `date`, `condition`) VALUES (NULL, :from, :to, :date, :condition)');
	$query->bindParam(':to',$to,PDO::PARAM_INT);
	$query->bindParam(':from',$from,PDO::PARAM_INT);
	$query->bindParam(':date',$date, PDO::PARAM_INT);
	$query->bindParam(':condition', $condition_id, PDO::PARAM_INT);
	$query->execute();
	
	return add_items_text($pdo->lastInsertId(),$type_data);
}
/**
*	Add an item of type text into the database
*/
function add_items_text($ID,$text)
{
	global $pdo;
	
	$ID = (int) $ID;
	
	$query = $pdo->prepare('INSERT INTO `tb_item_text` (`ID`, `text`) VALUES (:id, :text)');
	$query->bindParam(':id',$ID,PDO::PARAM_INT);
	$query->bindParam(':text',$text,PDO::PARAM_STR);
	$query->execute();
	
	return $ID;
}