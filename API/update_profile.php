<?php
header('Content-Type: application/json');

// Database connection parameters
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "inventory_management";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die(json_encode([
        'status' => 'error',
        'message' => 'Connection failed: ' . $conn->connect_error
    ]));
}

// Get POST data
$data = json_decode(file_get_contents('php://input'), true);

// Validate required fields
if (!isset($data['user_id']) || !isset($data['full_name']) || !isset($data['email']) || 
    !isset($data['phone']) || !isset($data['address'])) {
    echo json_encode([
        'status' => 'error',
        'message' => 'Missing required fields'
    ]);
    exit;
}

$userId = $conn->real_escape_string($data['user_id']);
$fullName = $conn->real_escape_string($data['full_name']);
$email = $conn->real_escape_string($data['email']);
$phone = $conn->real_escape_string($data['phone']);
$address = $conn->real_escape_string($data['address']);

// Update staff details
$sql = "UPDATE staff_details 
        SET full_name = '$fullName', 
            email = '$email', 
            phone = '$phone', 
            address = '$address' 
        WHERE user_id = '$userId'";

if ($conn->query($sql) === TRUE) {
    echo json_encode([
        'status' => 'success',
        'message' => 'Profile updated successfully'
    ]);
} else {
    echo json_encode([
        'status' => 'error',
        'message' => 'Error updating profile: ' . $conn->error
    ]);
}

$conn->close();
?>