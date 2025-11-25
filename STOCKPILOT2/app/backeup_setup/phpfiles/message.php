<?php
// Include database connection
require_once 'db.php';

// Check if notification ID is provided
if (!isset($_GET['id']) || empty($_GET['id'])) {
    echo "Error: Notification ID is required";
    exit();
}

$notification_id = $_GET['id'];

// Prepare and execute the query to get notification details
$query = "SELECT * FROM notificatons WHERE id = ?";
$stmt = $conn->prepare($query);
$stmt->bind_param("i", $notification_id);
$stmt->execute();
$result = $stmt->get_result();

// Check if notification exists
if ($result->num_rows === 0) {
    echo "Error: Notification not found";
    exit();
}

// Fetch notification data
$notification = $result->fetch_assoc();

// Update notification status to 'read' if it's currently 'unread'
if ($notification['status'] == 'unread') {
    $update_query = "UPDATE notificatons SET status = 'read' WHERE id = ?";
    $update_stmt = $conn->prepare($update_query);
    $update_stmt->bind_param("i", $notification_id);
    $update_stmt->execute();
    $update_stmt->close();
}

// Format timestamp for better readability
$formatted_date = date('F j, Y, g:i a', strtotime($notification['timestamp']));

// Get related product information based on notification type
if ($notification['type'] == 'low_stock' || $notification['type'] == 'restock' || $notification['type'] == 'expired' || $notification['type'] == 'damaged') {
    // Get product information if available
    $product_query = "SELECT * FROM products WHERE product_name = ? AND store_id = ? LIMIT 1";
    $product_stmt = $conn->prepare($product_query);
    $product_stmt->bind_param("si", $notification['title'], $notification['store_id']);
    $product_stmt->execute();
    $product_result = $product_stmt->get_result();
    
    if ($product_result->num_rows > 0) {
        $product = $product_result->fetch_assoc();
    }
    $product_stmt->close();
}

// Close statement and connection
$stmt->close();
$conn->close();
?>