<?php
header("Content-Type: application/json");
require 'db.php';

// Check if staff_id is provided
if (isset($_GET["staff_id"])) {
    $staff_id = intval($_GET["staff_id"]);
    $sql = "SELECT * FROM staff WHERE staff_id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $staff_id);
} else {
    $sql = "SELECT * FROM staff_details";
    $stmt = $conn->prepare($sql);
}

if (!$stmt) {
    echo json_encode(["success" => false, "message" => "SQL error: " . $conn->error]);
    exit();
}

$stmt->execute();
$result = $stmt->get_result();
$data = [];

while ($row = $result->fetch_assoc()) {
    $data[] = $row;
}

echo json_encode(["success" => true, "data" => $data]);
$stmt->close();
$conn->close();
?>
