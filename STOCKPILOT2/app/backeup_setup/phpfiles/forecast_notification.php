<?php
include 'db.php';

$store_id = $_POST['store_id'];

$result = mysqli_query($conn, "SELECT title, message, timestamp FROM notifications WHERE store_id = '$store_id' ORDER BY timestamp DESC");
$data = [];

while ($row = mysqli_fetch_assoc($result)) {
    $data[] = $row;
}

echo json_encode($data);
?>
