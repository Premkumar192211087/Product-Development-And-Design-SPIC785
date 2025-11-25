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
if (!isset($data['username']) || !isset($data['password']) || !isset($data['full_name']) || 
    !isset($data['email']) || !isset($data['phone']) || !isset($data['address']) || 
    !isset($data['role']) || !isset($data['store_id']) || !isset($data['created_by'])) {
    echo json_encode([
        'status' => 'error',
        'message' => 'Missing required fields'
    ]);
    exit;
}

$username = $conn->real_escape_string($data['username']);
$password = $conn->real_escape_string($data['password']);
$fullName = $conn->real_escape_string($data['full_name']);
$email = $conn->real_escape_string($data['email']);
$phone = $conn->real_escape_string($data['phone']);
$address = $conn->real_escape_string($data['address']);
$role = $conn->real_escape_string($data['role']);
$storeId = $conn->real_escape_string($data['store_id']);
$createdBy = $conn->real_escape_string($data['created_by']);

// Check if username already exists
$checkSql = "SELECT id FROM user_login WHERE username = '$username' AND store_id = '$storeId'";
$checkResult = $conn->query($checkSql);

if ($checkResult && $checkResult->num_rows > 0) {
    echo json_encode([
        'status' => 'error',
        'message' => 'Username already exists'
    ]);
    exit;
}

// Start transaction
$conn->begin_transaction();

try {
    // Insert into user_login table
    $userSql = "INSERT INTO user_login (username, Password, store_id, role) 
                VALUES ('$username', '$password', '$storeId', '$role')";
    
    if (!$conn->query($userSql)) {
        throw new Exception("Error creating user: " . $conn->error);
    }
    
    // Get the new user ID
    $userId = $conn->insert_id;
    
    // Insert into staff_details table
    $staffSql = "INSERT INTO staff_details (full_name, user_id, email, phone, role, store_id, address) 
                 VALUES ('$fullName', '$userId', '$email', '$phone', '$role', '$storeId', '$address')";
    
    if (!$conn->query($staffSql)) {
        throw new Exception("Error creating staff details: " . $conn->error);
    }
    
    // Commit transaction
    $conn->commit();
    
    echo json_encode([
        'status' => 'success',
        'message' => 'Staff added successfully',
        'data' => [
            'user_id' => $userId
        ]
    ]);
    
} catch (Exception $e) {
    // Rollback transaction on error
    $conn->rollback();
    
    echo json_encode([
        'status' => 'error',
        'message' => $e->getMessage()
    ]);
}

$conn->close();
?>