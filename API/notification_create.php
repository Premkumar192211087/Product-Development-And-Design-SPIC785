<?php
// Database configuration
require_once 'db.php';

// Headers
header('Content-Type: application/json');

// Response array
$response = array();

// Check if required parameters are provided
if (!isset($_POST['store_id']) || empty($_POST['store_id']) || 
    !isset($_POST['title']) || empty($_POST['title']) || 
    !isset($_POST['message']) || empty($_POST['message']) || 
    !isset($_POST['type']) || empty($_POST['type'])) {
    $response['success'] = false;
    $response['message'] = 'Store ID, title, message, and type are required';
    echo json_encode($response);
    exit;
}

// Get parameters
$store_id = intval($_POST['store_id']);
$title = $_POST['title'];
$message = $_POST['message'];
$type = $_POST['type'];
$status = 'unread'; // Default status for new notifications

// Optional parameters
$product_id = isset($_POST['product_id']) ? intval($_POST['product_id']) : null;
$quantity = isset($_POST['quantity']) ? intval($_POST['quantity']) : null;
$expiry_date = isset($_POST['expiry_date']) ? $_POST['expiry_date'] : null;

// Check connection
if ($conn->connect_error) {
    $response['success'] = false;
    $response['message'] = 'Database connection failed: ' . $conn->connect_error;
    echo json_encode($response);
    exit;
}

// Prepare and execute statement
$stmt = $conn->prepare("INSERT INTO notifications (store_id, title, message, type, status, product_id, quantity, expiry_date) 
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

if ($stmt === false) {
    $response['success'] = false;
    $response['message'] = 'Prepare failed: ' . $conn->error;
    echo json_encode($response);
    exit;
}

$stmt->bind_param("issssiii", $store_id, $title, $message, $type, $status, $product_id, $quantity, $expiry_date);

if ($stmt->execute()) {
    $notification_id = $stmt->insert_id;
    $response['success'] = true;
    $response['message'] = 'Notification created successfully';
    $response['notification_id'] = $notification_id;
} else {
    $response['success'] = false;
    $response['message'] = 'Failed to create notification: ' . $stmt->error;
}

// Clean up
$stmt->close();
$conn->close();

// Output JSON
echo json_encode($response);
?>