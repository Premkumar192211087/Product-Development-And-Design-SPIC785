<?php
// Database configuration
require_once 'db.php';

// Headers
header('Content-Type: application/json');

// Response array
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

// Get user preferences for thresholds
$prefs_stmt = $conn->prepare("SELECT preference_name, preference_value FROM user_preferences WHERE store_id = ?");
$prefs_stmt->bind_param("i", $store_id);
$prefs_stmt->execute();
$prefs_result = $prefs_stmt->get_result();

$preferences = array();
while ($row = $prefs_result->fetch_assoc()) {
    $preferences[$row['preference_name']] = $row['preference_value'];
}

// Default values if preferences not set
$low_stock_threshold = isset($preferences['low_stock_threshold']) ? intval($preferences['low_stock_threshold']) : 10;
$expiry_days_threshold = isset($preferences['expiry_days_threshold']) ? intval($preferences['expiry_days_threshold']) : 7;

// Array to store generated notifications
$generated_notifications = array();

// 1. Check for low stock items
if (isset($preferences['low_stock_notifications_enabled']) && $preferences['low_stock_notifications_enabled'] == 'true') {
    $low_stock_sql = $conn->prepare("SELECT p.id, p.product_name, p.quantity 
                                  FROM products p 
                                  WHERE p.store_id = ? AND p.quantity <= ? 
                                  AND NOT EXISTS (
                                      SELECT 1 FROM notifications n 
                                      WHERE n.store_id = p.store_id 
                                      AND n.product_id = p.id 
                                      AND n.type = 'low_stock' 
                                      AND n.status = 'unread' 
                                      AND n.timestamp > DATE_SUB(NOW(), INTERVAL 1 DAY)
                                  )");
    
    $low_stock_sql->bind_param("ii", $store_id, $low_stock_threshold);
    $low_stock_sql->execute();
    $low_stock_result = $low_stock_sql->get_result();
    
    while ($row = $low_stock_result->fetch_assoc()) {
        $product_id = $row['id'];
        $product_name = $row['product_name'];
        $quantity = $row['quantity'];
        
        $title = "Low Stock Alert";
        $message = "$product_name is running low with only $quantity units remaining. The minimum threshold is set to $low_stock_threshold units. Please consider restocking soon.";
        
        // Insert notification
        $insert_stmt = $conn->prepare("INSERT INTO notifications (store_id, title, message, type, status, product_id, quantity) 
                                    VALUES (?, ?, ?, 'low_stock', 'unread', ?, ?)");
        $insert_stmt->bind_param("issii", $store_id, $title, $message, $product_id, $quantity);
        
        if ($insert_stmt->execute()) {
            $generated_notifications[] = array(
                'type' => 'low_stock',
                'product_name' => $product_name,
                'quantity' => $quantity,
                'threshold' => $low_stock_threshold
            );
        }
        
        $insert_stmt->close();
    }
    
    $low_stock_sql->close();
}

// 2. Check for expiring items
if (isset($preferences['expiry_notifications_enabled']) && $preferences['expiry_notifications_enabled'] == 'true') {
    $expiry_date = date('Y-m-d', strtotime("+$expiry_days_threshold days"));
    
    $expiry_sql = $conn->prepare("SELECT b.id, p.id as product_id, p.product_name, b.exp_date 
                                FROM batch_details b 
                                JOIN products p ON b.product_id = p.id 
                                WHERE p.store_id = ? 
                                AND b.exp_date <= ? 
                                AND b.exp_date >= CURDATE() 
                                AND NOT EXISTS (
                                    SELECT 1 FROM notifications n 
                                    WHERE n.store_id = p.store_id 
                                    AND n.product_id = p.id 
                                    AND n.type = 'expired' 
                                    AND n.status = 'unread' 
                                    AND n.timestamp > DATE_SUB(NOW(), INTERVAL 1 DAY)
                                )");
    
    $expiry_sql->bind_param("is", $store_id, $expiry_date);
    $expiry_sql->execute();
    $expiry_result = $expiry_sql->get_result();
    
    while ($row = $expiry_result->fetch_assoc()) {
        $batch_id = $row['id'];
        $product_id = $row['product_id'];
        $product_name = $row['product_name'];
        $exp_date = $row['exp_date'];
        
        // Calculate days until expiry
        $today = new DateTime();
        $expiry = new DateTime($exp_date);
        $days_until_expiry = $today->diff($expiry)->days;
        
        $title = "Expiry Alert";
        $message = "$product_name will expire in $days_until_expiry days (on $exp_date). Please take appropriate action.";
        
        // Insert notification
        $insert_stmt = $conn->prepare("INSERT INTO notifications (store_id, title, message, type, status, product_id, expiry_date) 
                                    VALUES (?, ?, ?, 'expired', 'unread', ?, ?)");
        $insert_stmt->bind_param("issis", $store_id, $title, $message, $product_id, $exp_date);
        
        if ($insert_stmt->execute()) {
            $generated_notifications[] = array(
                'type' => 'expired',
                'product_name' => $product_name,
                'expiry_date' => $exp_date,
                'days_until_expiry' => $days_until_expiry
            );
        }
        
        $insert_stmt->close();
    }
    
    $expiry_sql->close();
}

// Prepare response
$response['success'] = true;
$response['notifications_generated'] = count($generated_notifications);
$response['notifications'] = $generated_notifications;

// Close connection
$conn->close();

// Output JSON
echo json_encode($response);
?>