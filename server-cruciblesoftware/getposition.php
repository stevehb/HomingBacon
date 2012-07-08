<?php
header("Content-Type: application/json; charset=utf-8");
ini_set('display_errors', 0);

function getErrJson($errNo, $errMsg) {
    $status = array('status' => 'ERROR');
    $msg = array('msg' => 'error(' . $errNo . ') ' . $errMsg);
    return json_encode($status + $msg);
}


// create connection
require 'db_login_config.php';
$conn = new mysqli($db_host, $db_username, $db_password, $db_dbname);
if($conn->connect_errno) {
    exit(getErrJson($conn->connect_errno, $conn->connect_error));
}

// making the query
$username = $_GET['username'];
$sql_stmt = sprintf(
    "select\n" .
    "  latitude,\n" .
    "  longitude,\n" .
    "  accuracy,\n" .
    "  epoch_time\n" .
    "from\n" .
    "  tbl_lastknown\n" .
    "where\n" .
    "  username = '%s'",
    $conn->real_escape_string($username));
$res = $conn->query($sql_stmt);
if(!$res) {
    exit(getErrJson($conn->errno, $conn->error));
}

// handle no data
if($res->num_rows == 0) {
    exit(json_encode(array('status' => 'NODATA')));
}

// return the first row
$row = $res->fetch_assoc();
echo json_encode(array(
    'status' => 'SUCCESS',
    'username' => $username,
    'latitude' => $row['latitude'],
    'longitude' => $row['longitude'],
    'accuracy' => $row['accuracy'],
    'epoch_time' => $row['epoch_time']));
$conn->close();
?>
