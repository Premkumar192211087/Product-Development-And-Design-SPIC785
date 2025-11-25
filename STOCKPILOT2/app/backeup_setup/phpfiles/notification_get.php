<?php
// Database configuration
require_once 'db.php';

// Set headers
header('Content-Type: application/json');

// Initialize response array
$response = array();

// Check if store_id is provided
if (!isset($_GET['store_id']) || empty($_GET['store_id'])) {
    $response['success'] = false;
    $response['message'] = 'Store ID is required';
    echo json_encode($response);
    exit;
}

// Get store_id
$store_id = intval($_GET['store_id']);

// Check connection
if ($conn->connect_error) {
    $response['success'] = false;
    $response['message'] = 'Database connection failed: ' . $conn->connect_error;
    echo json_encode($response);
    exit;
}

// Prepare and execute statement
$stmt = $conn->prepare("SELECT id, store_id, title, message, type, status, timestamp
                        FROM notifications
                        WHERE store_id = ?
                        ORDER BY timestamp DESC");

if ($stmt === false) {
    $response['success'] = false;
    $response['message'] = 'Prepare failed: ' . $conn->error;
    echo json_encode($response);
    exit;
}

$stmt->bind_param("i", $store_id);
$stmt->execute();

$result = $stmt->get_result();
$notifications = [];

while ($row = $result->fetch_assoc()) {
    $notifications[] = $row;
}

$response['success'] = true;
$response['notifications'] = $notifications;

// Clean up
$stmt->close();
$conn->close();

// Output JSON
echo json_encode($response);
?>
