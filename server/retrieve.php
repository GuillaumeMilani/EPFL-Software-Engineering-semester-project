<?php 
	require_once(dirname(__FILE__).'/include/utils.inc.php');

	function getUser($ID) {
		global $pdo;
		$res = $pdo->prepare('SELECT *, "user" as "type"
			FROM view_user as usr
    		WHERE usr.ID = :userID');
		
		$res->bindParam(':userID', $ID, PDO::PARAM_INT);
		$res->execute();
		
		if (!($res = $res->fetchAll()))
			die("No records found in the database");
		
		return $res[0];
	}
	
	$postContent = urldecode(file_get_contents("php://input"));
    if ($postContent == null)
    	die("You must precise a user to fetch the messages");

    file_put_contents("log.txt", $postContent."\n");
    $userToFetch = json_decode($postContent, true);
	
    $res = $pdo->prepare('SELECT *, "simpleText" as "type"
    		FROM view_text_message as msg 
    		WHERE msg.to = :userID
    		AND msg.date > :itmDate');
    
    $res->bindParam(':userID', $userToFetch['recipient']['ID'], PDO::PARAM_INT);
    $res->bindParam(':itmDate', $userToFetch['lastRefresh'], PDO::PARAM_STR);
    $res->execute();
    
    if (!($res = $res->fetchAll()))
        die("No records found in the database");
    for ($i = 0; $i<sizeOf($res); $i++) {
    	$res[$i]['from'] = getUser($res[$i]['from']);
    	$res[$i]['to'] = getUser($res[$i]['to']);
    }
    
    echo json_encode($res, JSON_NUMERIC_CHECK);
    
    /*
    echo '
    [{
    "ID":1,
    "type":"simpleText",
    "message":"Hello Bob, it\'s Alice !",
    "from": {
        "name":"Alice",
        "ID":1,
        "type":"user"
    },
   "to": {
        "name":"Bob",
        "ID":2,
        "type":"user"
    },
   "date":1445198510
  },
  {
    "ID":2,
    "type":"simpleText",
    "message":"Hello Bob, it\'s Carol !",
    "from": {
        "name":"Carol",
        "ID":3,
        "type":"user"
    },
   "to": {
        "name":"Bob",
        "ID":2,
        "type":"user"
    },
   "date":1445198520
  }
]
    		'; */
?>