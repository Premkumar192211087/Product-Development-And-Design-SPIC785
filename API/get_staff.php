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
if (!isset($data['store_id'])) {
    echo json_encode([
        'status' => 'error',
        'message' => 'Missing store_id'
    ]);
    exit;
}

$storeId = $conn->real_escape_string($data['store_id']);

// Query to get all staff members for the store
$sql = "SELECT s.staff_id, s.full_name, s.email, s.phone, s.role, s.address, u.id, u.username 
        FROM staff_details s 
        JOIN user_login u ON s.user_id = u.id 
        WHERE s.store_id = '$storeId'";

$result = $conn->query($sql);

if ($result) {
    $staffList = [];
    
    while ($row = $result->fetch_assoc()) {
        $staffList[] = [
            'staffId' => (int)$row['staff_id'],
            'fullName' => $row['full_name'],
            'email' => $row['email'],
            'phone' => $row['phone'],
            'role' => $row['role'],
            'address' => $row['address'],
            'id' => (int)$row['id'],
            'username' => $row['username']
        ];
    }
    
    echo json_encode([
        'status' => 'success',
        'data' => $staffList
    ]);
} else {
    echo json_encode([
        'status' => 'error',
        'message' => 'Error retrieving staff: ' . $conn->error
    ]);
}

$conn->close();
?>