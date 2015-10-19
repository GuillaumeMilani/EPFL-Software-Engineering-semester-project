<?php
function add_items($from,$to,$date,$type,$type_data)
{
	global $pdo;
	
	$from = (int) $from;
	$to = (int) $to;
	$date = (int) $date;
	
	$query = $pdo->prepare('INSERT INTO `tb_item` (`ID`, `from`, `to`, `date`) VALUES (NULL, :from, :to, :date)');
	$query->bindParam(':to',$to,PDO::PARAM_INT);
	$query->bindParam(':from',$from,PDO::PARAM_INT);
	$query->bindParam(':date',$date, PDO::PARAM_INT);
	if($query->execute() == true)
	{
		return add_items_text($pdo->lastInsertId(),$type_data);
	}
	else
	{
		return false;
	}

}

function add_items_text($ID,$text)
{
	global $pdo;
	
	$id = (int) $id;
	
	$query = $pdo->prepare('INSERT INTO `tb_item_text` (`ID`, `text`) VALUES (:id, :text)');
	$query->bindParam(':id',$ID,PDO::PARAM_INT);
	$query->bindParam(':text',$text,PDO::PARAM_STR);
	return $query->execute();
}