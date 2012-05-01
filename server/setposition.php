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

// making query about record existence
$hasUser = false;
$username = $_GET['username'];
$sql_stmt = sprintf(
    "select\n" .
    "  *\n" .
    "from\n" .
    "  tbl_lastknown\n" .
    "where\n" .
    "  username = '%s'",
    $conn->real_escape_string($username));
$res = $conn->query($sql_stmt);
if(!$res) {
    exit(getErrJson($conn->errno, $conn->error));
}
$hasUser = ($res->num_rows != 0);

// either update or insert
$lat = is_numeric($_GET['latitude']) ? $_GET['latitude'] : null;
$lon = is_numeric($_GET['longitude']) ? $_GET['longitude'] : null;
$acc = is_numeric($_GET['accuracy']) ? $_GET['accuracy'] : null;
$time = time();
if($hasUser) {
    $sql_stmt = sprintf(
        "update\n" .
        "  tbl_lastknown\n" .
        "set\n" .
        "  latitude = %F,\n" .
        "  longitude = %F,\n" .
        "  accuracy = %F,\n" .
        "  epoch_time = %d\n" .
        "where\n" .
        "  username = '%s'",
        $lat,
        $lon,
        $acc,
        $time,
        $conn->real_escape_string($username));
} else {
    $sql_stmt = sprintf(
        "insert into \n" .
        "  tbl_lastknown (username, latitude, longitude, accuracy, epoch_time)\n" .
        "values (\n" .
        "  '%s',\n" .
        "  %F,\n" .
        "  %F,\n" .
        "  %F,\n" .
        "  %d)\n",
        $conn->real_escape_string($username),
        $lat,
        $lon,
        $acc,
        $time);
}
$res = $conn->query($sql_stmt);
if(!$res) {
    exit(getErrJson($conn->errno, $conn->error));
}

echo json_encode(array(
    'status' => 'SUCCESS',
    'new_user' => !$hasUser,
    'username' => $username,
    'latitude' => $lat,
    'longitude' => $lon,
    'accuracy' => $acc,
    'epoch_time' => $time));

$conn->close();
?>
