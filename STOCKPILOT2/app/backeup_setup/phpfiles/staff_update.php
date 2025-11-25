<?php
header("Content-Type: application/json");

// Database connection
$host = "localhost";
$user = "root";
$password = "";
$database = "inventory_management";

$conn = new mysqli($host, $user, $password, $database);
if ($conn->connect_error) {
    echo json_encode(["status" => false, "message" => "Connection failed: " . $conn->connect_error]);
    exit();
}

// Get POST values (received via form-data from OkHttp in Java)
$staff_id = $_POST['staff_id'] ?? null;
$store_id = $_POST['store_id'] ?? null;
$full_name = $_POST['full_name'] ?? null;
$email = $_POST['email'] ?? null;
$phone = $_POST['phone'] ?? null;
$address = $_POST['address'] ?? null;

// Input Validation
if (!$staff_id || !$store_id || !$full_name || !$email || !$phone || !$address) {
    echo json_encode(["status" => false, "message" => "All fields are required."]);
    exit();
}

// Update staff profile
$stmt = $conn->prepare("UPDATE staff_details 
                        SET full_name = ?, email = ?, phone = ?, address = ? 
                        WHERE staff_id = ? AND store_id = ?");
$stmt->bind_param("ssssii", $full_name, $email, $phone, $address, $staff_id, $store_id);

if ($stmt->execute()) {
    echo json_encode(["status" => true, "message" => "Profile updated successfully."]);
} else {
    echo json_encode(["status" => false, "message" => "Update failed: " . $conn->error]);
}

$stmt->close();
$conn->close();
?>
