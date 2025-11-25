<?php
header('Content-Type: application/json');
require_once 'db_connect.php';
require_once 'send_fcm.php';

// Get data from request
$data = json_decode(file_get_contents("php://input"), true);
$store_id = isset($data['store_id']) ? intval($data['store_id']) : 0;
$user_id = isset($data['user_id']) ? intval($data['user_id']) : 0;

if ($store_id === 0 || $user_id === 0) {
    echo json_encode(['success' => false, 'message' => 'Store ID and User ID are required']);
    exit;
}

try {
    // Check for low stock items
    $stmt = $conn->prepare("SELECT p.product_id, p.product_name, p.current_stock, p.low_stock_threshold
                           FROM products p
                           WHERE p.store_id = ? AND p.current_stock <= p.low_stock_threshold
                           AND p.current_stock > 0 AND p.status = 'active'
                           LIMIT 10");
    $stmt->bind_param("i", $store_id);
    $stmt->execute();
    $result = $stmt->get_result();

    $low_stock_items = [];
    while ($row = $result->fetch_assoc()) {
        $low_stock_items[] = $row;
    }

    // Check for expiring items (within 7 days)
    $stmt = $conn->prepare("SELECT p.product_id, p.product_name, p.expiry_date
                           FROM products p
                           WHERE p.store_id = ? AND p.expiry_date IS NOT NULL
                           AND p.expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
                           AND p.current_stock > 0 AND p.status = 'active'
                           LIMIT 10");
    $stmt->bind_param("i", $store_id);
    $stmt->execute();
    $result = $stmt->get_result();

    $expiring_items = [];
    while ($row = $result->fetch_assoc()) {
        $expiring_items[] = $row;
    }

    // Create notifications for low stock items
    foreach ($low_stock_items as $item) {
        $title = "Low Stock Alert";
        $message = "{$item['product_name']} is running low (Stock: {$item['current_stock']})";
        $type = "low_stock";

        // Check if this notification already exists
        $check = $conn->prepare("SELECT id FROM notifications
                               WHERE store_id = ? AND type = ? AND reference_id = ?
                               AND created_at > DATE_SUB(NOW(), INTERVAL 1 DAY)");
        $check->bind_param("isi", $store_id, $type, $item['product_id']);
        $check->execute();
        $exists = $check->get_result()->num_rows > 0;

        if (!$exists) {
            // Insert notification
            $insert = $conn->prepare("INSERT INTO notifications
                                    (store_id, user_id, title, message, type, reference_id, status, created_at)
                                    VALUES (?, ?, ?, ?, ?, ?, 'unread', NOW())");
            $insert->bind_param("iisssi", $store_id, $user_id, $title, $message, $type, $item['product_id']);
            $insert->execute();

            // Send FCM notification
            sendFcmNotification($conn, $store_id, $title, $message, [
                'type' => $type,
                'product_id' => (string)$item['product_id']
            ]);
        }
    }

    // Create notifications for expiring items
    foreach ($expiring_items as $item) {
        $expiry_date = new DateTime($item['expiry_date']);
        $today = new DateTime();
        $days_left = $today->diff($expiry_date)->days;

        $title = "Expiry Alert";
        $message = "{$item['product_name']} expires in {$days_left} days";
        $type = "expiry";

        // Check if this notification already exists
        $check = $conn->prepare("SELECT id FROM notifications
                               WHERE store_id = ? AND type = ? AND reference_id = ?
                               AND created_at > DATE_SUB(NOW(), INTERVAL 1 DAY)");
        $check->bind_param("isi", $store_id, $type, $item['product_id']);
        $check->execute();
        $exists = $check->get_result()->num_rows > 0;

        if (!$exists) {
            // Insert notification
            $insert = $conn->prepare("INSERT INTO notifications
                                    (store_id, user_id, title, message, type, reference_id, status, created_at)
                                    VALUES (?, ?, ?, ?, ?, ?, 'unread', NOW())");
            $insert->bind_param("iisssi", $store_id, $user_id, $title, $message, $type, $item['product_id']);
            $insert->execute();

            // Send FCM notification
            sendFcmNotification($conn, $store_id, $title, $message, [
                'type' => $type,
                'product_id' => (string)$item['product_id']
            ]);
        }
    }

    echo json_encode(['success' => true, 'message' => 'Notifications checked successfully']);
} catch (Exception $e) {
    echo json_encode(['success' => false, 'message' => 'Error: ' . $e->getMessage()]);
}
?>