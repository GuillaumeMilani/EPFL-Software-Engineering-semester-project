<?php
include('models/users/retrieve_users.php');

global $types;
// List of item types with some of their characteristics
$types = array(
	array('name' => '"SIMPLETEXTITEM"', 'table' => 'tb_item_text', 'fields' => ''),
	array('name' => '"IMAGEITEM"', 'table' => 'tb_item_image', 'fields' => ', type.`data`'),
	array('name' => '"FILEITEM"', 'table' => 'tb_item_file', 'fields' => ', type.`data`'),
);

// List of the fields to select in the DB for a localized item
define('LOCATION_WHERE','
		    AND pos.latitude <= :latitude_max
			AND pos.latitude >= :latitude_min
			AND pos.longitude <= :longitude_max
			AND pos.longitude >= :longitude_min
			AND pos.ID = mtd.ID
			AND mtd.condition = cnd.ID');

// List of the tables to select a localized item
define('LOCATION_FROM',', tb_metadata as mtd, tb_metadata_position as pos');

/**
 * Execute a query to get items and replace the "from" and "to" by a JSON Object corresponding to users
 * @param $query a prepared query that contains at least SELECT `from`
 * @return array of items (indexed by column name)
 */
function get_items($recipient, $last_refresh, $type, $location_params = '') {
	global $pdo;
	
	$location_where = '';
	$location_from = '';
	$to = 'itm.to = :to';

	if ($location_params != '') {
		$location_where = LOCATION_WHERE;
		$location_from = LOCATION_FROM;
		$to = '(itm.to = :to OR itm.to IS NULL)';
	}
	
	$query = $pdo->prepare('
			SELECT
				itm.`ID`, itm.`from`, itm.`to`, itm.`date`, cnd.`condition`, itm.`message` '.$type['fields'].', '.$type['name'].' as "type"
    		FROM
				tb_item as itm, '.$type['table'].' as type, tb_condition as cnd '.$location_from.'
			WHERE
				itm.ID = type.ID
			AND	itm.condition = cnd.ID
			AND	'.$to.'
    		AND itm.date > :last_refresh
			'.$location_where);
	
	$id = $recipient['ID'];
	
	$query->bindParam(':to', $id, PDO::PARAM_INT);
	$query->bindParam(':last_refresh', $last_refresh, PDO::PARAM_STR);
	
	if (isset($location_params)) {
		$query->bindParam(':latitude_min', $location_params['latitude_min'], PDO::PARAM_STR);
		$query->bindParam(':latitude_max', $location_params['latitude_max'], PDO::PARAM_STR);
		$query->bindParam(':longitude_min', $location_params['longitude_min'], PDO::PARAM_STR);
		$query->bindParam(':longitude_max', $location_params['longitude_max'], PDO::PARAM_STR);
	}
	
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
	global $types;
	
	$location_params = array(
			'latitude_min' => $latitude_min,
			'latitude_max' => $latitude_max,
			'longitude_min' => $longitude_min,
			'longitude_max' => $longitude_max
	);
	
	$ret = array();
	
	foreach ($types as $type) {
		$ret = array_merge($ret,get_items($recipient, $last_refresh, $type, $location_params));
	}
	
	return $ret;
}

/**
 * Retrieve all PRIVATE items sent to the specified recipient
 * @param int $recipient
 * @param float $last_refresh
 * @return array of items
 */
function get_all_private_items($recipient, $last_refresh) {	
	$ret = array();
	
	foreach (TYPES as $type) {
		$ret = array_merge($ret,get_items($recipient, $last_refresh, $type));
	}
	
	return $ret;
}