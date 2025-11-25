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
if (!isset($data['user_id']) || !isset($data['current_password']) || !isset($data['new_password'])) {
    echo json_encode([
        'status' => 'error',
        'message' => 'Missing required fields'
    ]);
    exit;
}

$userId = $conn->real_escape_string($data['user_id']);
$currentPassword = $conn->real_escape_string($data['current_password']);
$newPassword = $conn->real_escape_string($data['new_password']);

// First verify the current password
$sql = "SELECT Password FROM user_login WHERE id = '$userId'";
$result = $conn->query($sql);

if ($result && $result->num_rows > 0) {
    $row = $result->fetch_assoc();
    $storedPassword = $row['Password'];
    
    // Check if current password matches
    // Note: In a production environment, you should use password_verify() for hashed passwords
    if ($storedPassword === $currentPassword) {
        // Update the password
        // Note: In a production environment, you should use password_hash() for password security
        $updateSql = "UPDATE user_login SET Password = '$newPassword' WHERE id = '$userId'";
        
        if ($conn->query($updateSql) === TRUE) {
            echo json_encode([
                'status' => 'success',
                'message' => 'Password changed successfully'
            ]);
        } else {
            echo json_encode([
                'status' => 'error',
                'message' => 'Error updating password: ' . $conn->error
            ]);
        }
    } else {
        echo json_encode([
            'status' => 'error',
            'message' => 'Current password is incorrect'
        ]);
    }
} else {
    echo json_encode([
        'status' => 'error',
        'message' => 'User not found'
    ]);
}

$conn->close();
?>