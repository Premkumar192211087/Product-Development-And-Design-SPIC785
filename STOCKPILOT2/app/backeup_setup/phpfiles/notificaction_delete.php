<?php
// Database configuration
require_once 'db.php';

// Headers
header('Content-Type: application/json');

// Response array
$response = array();

// Check if required parameters are provided
if (!isset($_GET['notification_id']) || empty($_GET['notification_id']) || 
    !isset($_GET['store_id']) || empty($_GET['store_id'])) {
    $response['success'] = false;
    $response['message'] = 'Notification ID and Store ID are required';
    echo json_encode($response);
    exit;
}

// Get parameters
$notification_id = intval($_GET['notification_id']);
$store_id = intval($_GET['store_id']);

// Connect to database
try {
    $conn = new PDO("mysql:host=$db_host;dbname=$db_name", $db_user, $db_pass);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch(PDOException $e) {
    $response['success'] = false;
    $response['message'] = 'Database connection failed: ' . $e->getMessage();
    echo json_encode($response);
    exit;
}

try {
    // Delete notification
    $stmt = $conn->prepare("DELETE FROM notifications 
                           WHERE id = :notification_id AND store_id = :store_id");
    $stmt->bindParam(':notification_id', $notification_id, PDO::PARAM_INT);
    $stmt->bindParam(':store_id', $store_id, PDO::PARAM_INT);
    $stmt->execute();
    
    // Check if delete was successful
    if ($stmt->rowCount() > 0) {
        $response['success'] = true;
        $response['message'] = 'Notification deleted successfully';
    } else {
        $response['success'] = false;
        $response['message'] = 'No matching notification found';
    }
    
} catch(PDOException $e) {
    $response['success'] = false;
    $response['message'] = 'Query failed: ' . $e->getMessage();
}

// Close connection
$conn = null;

// Return JSON response
echo json_encode($response);
?>