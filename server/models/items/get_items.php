<?php
include('../users/retrieve_users.php');

// List of the fields to select in the DB for an item
const TO_BE_SELECTED = 'itm.`ID`, itm.`from`, itm.`to`, itm.`date`, cnd.`condition`, itm.`message`, "SIMPLETEXTITEM" as "type"';

/**
 * Execute a query to get items and replace the "from" and "to" by a JSON Object corresponding to users
 * @param $query a prepared query that contains at least SELECT `from`
 * @return array of items (indexed by column name)
 */
function get_items($query) {
	$query->execute();

	$ret = [];

	// Fill the "from" and "to" columns with the users data instead of their ID
	while ($data = $query->fetch()) {
		$data['from'] = get_user_by_id($data['from']);
		if (isset($data['to'])) {
			$data['to'] = get_user_by_id($data['to']);
		}
		$ret[] = $data;
	}

	return $ret;
}

/**
 * Get all items sent from $last_refresh to a recipient in an area given by latitude/longitude min/max
 * @param JSON_array $recipient
 * @param int $last_refresh unix datetime
 * @param float $latitude_min
 * @param float $latitude_max
 * @param float $longitude_min
 * @param float $longitude_max
 */
function get_item_with_location($recipient, $last_refresh, $latitude_min, $latitude_max, 
		$longitude_min, $longitude_max) {
	global $pdo;
	
	$res = $pdo->prepare('
			SELECT DISTINCT
				'.TO_BE_SELECTED.'
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
	
	$recipient_id = $recipient['ID'];
	$res->bindParam(':latitude_min', $latitude_min, PDO::PARAM_STR);
	$res->bindParam(':latitude_max', $latitude_max, PDO::PARAM_STR);
	$res->bindParam(':longitude_min', $longitude_min, PDO::PARAM_STR);
	$res->bindParam(':longitude_max', $longitude_max, PDO::PARAM_STR);
	$res->bindParam(':to', $recipient_id, PDO::PARAM_INT);
	$res->bindParam(':last_refresh', $last_refresh, PDO::PARAM_STR);
	
	return get_items($res);
}

/*
 * Retrieve all PRIVATE items sent to the specified recipient
 */
function get_all_private_items($recipient, $last_refresh) {
	global $pdo;
	
	$query = $pdo->prepare('
			SELECT
				'.TO_BE_SELECTED.'
    		FROM
				tb_item as itm, tb_item_text as txt, tb_condition as cnd
			WHERE
				itm.condition = cnd.ID
			AND	itm.to = :to
    		AND itm.date > :last_refresh
			AND itm.ID = txt.ID');
	
	$query->bindParam(':to', $recipient['ID'], PDO::PARAM_INT);
	$query->bindParam(':last_refresh', $last_refresh, PDO::PARAM_STR);
	
	return get_items($query);
}