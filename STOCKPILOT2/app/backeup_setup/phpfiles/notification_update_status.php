<?php
// Database configuration
require_once 'db.php';

// Headers
header('Content-Type: application/json');

// Response array
$response = array();

// Check if required parameters are provided
if (!isset($_POST['notification_id']) || empty($_POST['notification_id']) || 
    !isset($_POST['store_id']) || empty($_POST['store_id']) || 
    !isset($_POST['status']) || empty($_POST['status'])) {
    $response['success'] = false;
    $response['message'] = 'Notification ID, Store ID, and Status are required';
    echo json_encode($response);
    exit;
}

// Get parameters
$notification_id = intval($_POST['notification_id']);
$store_id = intval($_POST['store_id']);
$status = $_POST['status']; // Expected values: 'read', 'unread'

// Validate status value
if ($status !== 'read' && $status !== 'unread') {
    $response['success'] = false;
    $response['message'] = 'Invalid status value. Must be "read" or "unread".';
    echo json_encode($response);
    exit;
}

// Check connection
if ($conn->connect_error) {
    $response['success'] = false;
    $response['message'] = 'Database connection failed: ' . $conn->connect_error;
    echo json_encode($response);
    exit;
}

// Prepare and execute statement
$stmt = $conn->prepare("UPDATE notifications 
                        SET status = ? 
                        WHERE id = ? AND store_id = ?");

if ($stmt === false) {
    $response['success'] = false;
    $response['message'] = 'Prepare failed: ' . $conn->error;
    echo json_encode($response);
    exit;
}

$stmt->bind_param("sii", $status, $notification_id, $store_id);

if ($stmt->execute()) {
    if ($stmt->affected_rows > 0) {
        $response['success'] = true;
        $response['message'] = 'Notification status updated successfully';
    } else {
        $response['success'] = false;
        $response['message'] = 'No matching notification found or status already set to ' . $status;
    }
} else {
    $response['success'] = false;
    $response['message'] = 'Failed to update notification status: ' . $stmt->error;
}

// Clean up
$stmt->close();
$conn->close();

// Output JSON
echo json_encode($response);
?>