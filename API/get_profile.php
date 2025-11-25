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
if (!isset($data['user_id']) || !isset($data['store_id'])) {
    echo json_encode([
        'status' => 'error',
        'message' => 'Missing required fields'
    ]);
    exit;
}

$userId = $conn->real_escape_string($data['user_id']);
$storeId = $conn->real_escape_string($data['store_id']);

// Query to get user profile information
$sql = "SELECT u.id, u.username, u.role, s.full_name, s.email, s.phone, s.address 
        FROM user_login u 
        JOIN staff_details s ON u.id = s.user_id 
        WHERE u.id = '$userId' AND u.store_id = '$storeId'";

$result = $conn->query($sql);

if ($result && $result->num_rows > 0) {
    $profile = $result->fetch_assoc();
    
    echo json_encode([
        'status' => 'success',
        'data' => [
            'id' => $profile['id'],
            'username' => $profile['username'],
            'role' => $profile['role'],
            'full_name' => $profile['full_name'],
            'email' => $profile['email'],
            'phone' => $profile['phone'],
            'address' => $profile['address']
        ]
    ]);
} else {
    echo json_encode([
        'status' => 'error',
        'message' => 'Profile not found'
    ]);
}

$conn->close();
?>