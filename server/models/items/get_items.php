<?php

function get_item_with_location($recipient, $last_refresh, $latitude, $longitude, $radius) {
	global $pdo;
	
	$res = $pdo->prepare('
			SELECT DISTINCT
				itm.`ID`, itm.`from`, itm.`to`, itm.`date`, cnd.`condition`, itm.`message`, "SIMPLETEXTITEM" as "type"
    		FROM 
				tb_item as itm, tb_item_text as txt, tb_condition as cnd, tb_metadata as mtd, tb_metadata_position as pos
			WHERE
				pos.latitude <= :latitude_max
			AND pos.latitude >= :latitude_min
			AND pos.longitude <= :longitude_max
			AND pos.longitude >= :longitude_min
			AND pos.ID = mtd.ID
			AND mtd.condition = cnd.ID
			AND itm.condition = cnd.ID
			AND	(itm.to = :to OR itm.to IS NULL)
    		AND itm.date > :last_refresh
			AND itm.ID = txt.ID');
	$kmToLatFactor = 1/110.574; 
	$kmToLongFactor = 1/(111.320); // *cos($latitude));
	$latMin = $latitude -  $kmToLatFactor * $radius; // $latitude - $radius;
	$latMax = $latitude + $kmToLatFactor * $radius;
	$longMax = $longitude +  $kmToLongFactor * $radius;
	$longMin = $longitude - $kmToLongFactor * $radius;
	$recipient_id = $recipient['ID'];
	$res->bindParam(':latitude_min', $latMin, PDO::PARAM_STR);
	$res->bindParam(':latitude_max', $latMax, PDO::PARAM_STR);
	$res->bindParam(':longitude_min', $longMin, PDO::PARAM_STR);
	$res->bindParam(':longitude_max', $longMax, PDO::PARAM_STR);
	$res->bindParam(':to', $recipient_id, PDO::PARAM_INT);
	$res->bindParam(':last_refresh', $last_refresh, PDO::PARAM_STR);
	$res->execute();
	
	$ret = [];
	
	// Fill the "from" and "to" columns with the users data instead of their ID
	while ($data = $res->fetch()) {
		$data['from'] = get_user($data['from']);
		if (isset($data['to'])) {
			$data['to'] = get_user($data['to']);
		}
		$ret[] = $data;
	}
	
	return $ret;
}

/**
 * Get a user from the DB using his ID
 * @param int $user_id
 */
function get_user($user_id) {
	global $pdo;
	$res = $pdo->prepare('SELECT *, "user" as "type"
			FROM view_user as usr
    		WHERE usr.ID = :user_id');

	$res->bindParam(':user_id', $user_id, PDO::PARAM_INT);
	$res->execute();

	if (!($res = $res->fetchAll())) {
		die();
	}

	return $res[0];
}

/**
 * Get items sent to a specific recipient from a given date
 * @param Recipient.User (JSON) $recipient
 * @param posix_time $last_refresh
 * @return array of items (indexed by column name)
 */
// DEPRECATED, UPDATE IF NEEDED
function get_items($recipient, $last_refresh, $get_content = false) {
	global $pdo;
	
	if ($get_content) {
		$table = "view_text_message";
	} else {
		$table = "tb_item";
	}
	$res = $pdo->prepare('SELECT *, "SIMPLETEXTITEM" as "type"
    		FROM '.$table.' as itm
    		WHERE (itm.to = :to OR itm.to = NULL)
    		AND itm.date > :last_refresh');
	
	$res->bindParam(':to', $recipient['ID'], PDO::PARAM_INT);
	$res->bindParam(':last_refresh', $last_refresh, PDO::PARAM_STR);
	$res->execute();
	
	$ret = [];
	
	// Fill the "from" and "to" columns with the users data instead of their ID
	while ($data = $res->fetch()) {
		$data['from'] = get_user($data['from']);
		if (isset($data['to'])) {
			$data['to'] = get_user($data['to']);
		}
		$ret[] = $data;
	}
	
	return $ret;
}

function get_items_with_content($recipient, $last_refresh) {
	return get_items($recipient, $last_refresh, true);
}