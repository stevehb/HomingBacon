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
$lat = is_numeric($_GET['lat']) ? $_GET['lat'] : null;
$lon = is_numeric($_GET['lon']) ? $_GET['lon'] : null;
if($hasUser) {
    $sql_stmt = sprintf(
        "update\n" .
        "  tbl_lastknown\n" .
        "set\n" .
        "  latitude = %F,\n" .
        "  longitude = %F,\n" .
        "  datetime = NOW()"
        "where\n" .
        "  username = '%s'",
        $lat,
        $lon,
        $conn->real_escape_string($username));
} else {
    $sql_stmt = sprintf(
        "insert into \n" .
        "  tbl_lastknown (username, latitude, longitude, datetime)\n" .
        "values (\n" .
        "  username = '%s',\n"
        "  latitude = %F,\n" .
        "  longitude = %F,\n" .
        "  datetime = NOW())",
        $conn->real_escape_string($username),
        $lat,
        $lon);
}

$conn->close();
?>
