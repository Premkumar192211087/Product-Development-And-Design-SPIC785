<?php
include 'db.php';

// Get the staff_id from the request
$staff_id = isset($_POST['staff_id']) ? $_POST['staff_id'] : null;

if ($staff_id === null) {
    echo json_encode(["success" => false, "message" => "Missing staff_id"]);
    exit();
}

// Step 1: Get user_id from staff_details
$getUserIdSql = "SELECT user_id FROM staff_details WHERE staff_id = ?";
$stmt = $conn->prepare($getUserIdSql);
$stmt->bind_param("i", $staff_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode(["success" => false, "message" => "Staff not found"]);
    exit();
}

$row = $result->fetch_assoc();
$user_id = $row['user_id'];

// Step 2: Delete from staff_details
$deleteStaffSql = "DELETE FROM staff_details WHERE staff_id = ?";
$stmt = $conn->prepare($deleteStaffSql);
$stmt->bind_param("i", $staff_id);
$stmt->execute();

// Step 3: Delete from user_login
$deleteLoginSql = "DELETE FROM user_login WHERE id = ?";
$stmt = $conn->prepare($deleteLoginSql);
$stmt->bind_param("i", $user_id);
$stmt->execute();

echo json_encode(["success" => true, "message" => "Staff deleted successfully"]);
$conn->close();
?>
