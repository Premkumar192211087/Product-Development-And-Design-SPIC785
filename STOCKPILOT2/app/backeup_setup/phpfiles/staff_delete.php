<?php
include 'db.php';

$store_id = $_POST['store_id'] ?? null;

if ($store_id === null) {
    echo json_encode(["error" => "Missing store_id"]);
    exit();
}

$sql = "SELECT staff_id, full_name, email, phone, Role AS role, address FROM staff_details WHERE store_id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $store_id);
$stmt->execute();
$result = $stmt->get_result();

$staffList = array();
while ($row = $result->fetch_assoc()) {
    $staffList[] = $row;
}

echo json_encode($staffList);
$conn->close();
?>