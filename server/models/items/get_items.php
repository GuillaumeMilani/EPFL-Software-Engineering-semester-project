<?php
function get_user($user_id) {
	global $pdo;
	$res = $pdo->prepare('SELECT *, "user" as "type"
			FROM view_user as usr
    		WHERE usr.ID = :user_id');

	$res->bindParam(':user_id', $user_id, PDO::PARAM_INT);
	$res->execute();

	if (!($res = $res->fetchAll()))
		die("No records found in the database");

	return $res[0];
}

function get_items($recipient, $last_refresh) {
	global $pdo;
	
	$res = $pdo->prepare('SELECT *, "simpleText" as "type"
    		FROM view_text_message as msg
    		WHERE msg.to = :to
    		AND msg.date > :last_refresh');
	
	$res->bindParam(':to', $recipient['ID'], PDO::PARAM_INT);
	$res->bindParam(':last_refresh', $last_refresh, PDO::PARAM_STR);
	$res->execute();
	
	$ret = [];
	
	while ($data = $res->fetch()) {
		$data['from'] = get_user($data['from']);
		$data['to'] = get_user($data['to']);
		$ret[] = $data;
	}
	return $ret;
}