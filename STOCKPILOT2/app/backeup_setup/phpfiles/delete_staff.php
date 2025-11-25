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
if (!isset($data['staff_id']) || !isset($data['deleted_by'])) {
    echo json_encode([
        'status' => 'error',
        'message' => 'Missing required fields'
    ]);
    exit;
}

$staffId = $conn->real_escape_string($data['staff_id']);
$deletedBy = $conn->real_escape_string($data['deleted_by']);

// Get user_id from staff_details
$getUserSql = "SELECT user_id FROM staff_details WHERE staff_id = '$staffId'";
$userResult = $conn->query($getUserSql);

if (!$userResult || $userResult->num_rows == 0) {
    echo json_encode([
        'status' => 'error',
        'message' => 'Staff not found'
    ]);
    exit;
}

$userData = $userResult->fetch_assoc();
$userId = $userData['user_id'];

// Start transaction
$conn->begin_transaction();

try {
    // Delete from staff_details
    $deleteStaffSql = "DELETE FROM staff_details WHERE staff_id = '$staffId'";
    if (!$conn->query($deleteStaffSql)) {
        throw new Exception("Error deleting staff details: " . $conn->error);
    }
    
    // Delete from user_login
    $deleteUserSql = "DELETE FROM user_login WHERE id = '$userId'";
    if (!$conn->query($deleteUserSql)) {
        throw new Exception("Error deleting user login: " . $conn->error);
    }
    
    // Commit transaction
    $conn->commit();
    
    echo json_encode([
        'status' => 'success',
        'message' => 'Staff deleted successfully'
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