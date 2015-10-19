<!DOCTYPE unspecified PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<title="Retrieve messages"></title>
	</head>
	<body>
		<?php 
			require_once(dirname(__FILE__).'/include/utils.inc.php');
		
			$postContent = file_get_contents("php://input");
		    if ($postContent == null)
		    	die("You must precise a user to fetch the messages");
		    
		    $userToFetch = json_decode($postContent, true);
			
		    $res = $pdo->prepare('SELECT *, "simpleText" as "type"
		    		FROM tb_item as itm, tb_item_text as txt 
		    		WHERE itm.ID = txt.ID
		    		AND itm.ID = :userID
		    		AND itm.date > :itmDate');
		    
		    $res->bindParam(':userID', $userToFetch['recipient']['ID'], PDO::PARAM_INT);
		    $res->bindParam(':itmDate', $userToFetch['lastRefresh'], PDO::PARAM_STR);
		    $res->execute();
		    
		    if (!($res = $res->fetchAll()))
		        die("No records found in the database");
		
		    echo json_encode($res);
		?>
	</body>
</html>