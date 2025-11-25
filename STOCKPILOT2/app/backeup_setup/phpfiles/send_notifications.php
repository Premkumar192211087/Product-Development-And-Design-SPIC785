<?php
header('Content-Type: application/json');
$conn = new mysqli("localhost", "root", "", "inventory_management");

if ($conn->connect_error) {
    echo json_encode(['status' => 'error', 'message' => 'Connection failed: ' . $conn->connect_error]);
    exit;
}

// Get store ID from request
$data = json_decode(file_get_contents("php://input"), true);
$store_id = isset($data['store_id']) ? intval($data['store_id']) : 0;

if ($store_id === 0) {
    echo json_encode(['status' => 'error', 'message' => 'Store ID is required']);
    exit;
}

// Array to hold all notifications
$notifications = [];

// 1. Check for low stock items (below threshold)
$stmt = $conn->prepare("
    SELECT p.product_id, p.product_name, p.current_stock, p.reorder_level
    FROM products p
    WHERE p.store_id = ? AND p.current_stock <= p.reorder_level AND p.current_stock > 0
");
$stmt->bind_param("i", $store_id);
$stmt->execute();
$result = $stmt->get_result();

while ($row = $result->fetch_assoc()) {
    $notifications[] = [
        'type' => 'low_stock',
        'title' => 'Low Stock Alert',
        'message' => "Product '{$row['product_name']}' is low on stock ({$row['current_stock']} remaining)",
        'product_id' => $row['product_id']
    ];
}
$stmt->close();

// 2. Check for out of stock items
$stmt = $conn->prepare("
    SELECT p.product_id, p.product_name
    FROM products p
    WHERE p.store_id = ? AND p.current_stock = 0
");
$stmt->bind_param("i", $store_id);
$stmt->execute();
$result = $stmt->get_result();

while ($row = $result->fetch_assoc()) {
    $notifications[] = [
        'type' => 'low_stock',
        'title' => 'Out of Stock',
        'message' => "Product '{$row['product_name']}' is out of stock",
        'product_id' => $row['product_id']
    ];
}
$stmt->close();

// 3. Check for pending purchase orders
$stmt = $conn->prepare("
    SELECT po.po_id, po.po_number, po.expected_delivery_date, s.supplier_name
    FROM purchase_orders po
    JOIN suppliers s ON po.supplier_id = s.supplier_id
    WHERE po.store_id = ? AND po.status = 'pending'
    AND po.expected_delivery_date <= DATE_ADD(CURDATE(), INTERVAL 3 DAY)
");
$stmt->bind_param("i", $store_id);
$stmt->execute();
$result = $stmt->get_result();

while ($row = $result->fetch_assoc()) {
    $notifications[] = [
        'type' => 'pending_po',
        'title' => 'Pending Purchase Order',
        'message' => "PO #{$row['po_number']} from {$row['supplier_name']} is expected by {$row['expected_delivery_date']}",
        'po_id' => $row['po_id']
    ];
}
$stmt->close();

// 4. Check for expiring items (within 7 days)
$stmt = $conn->prepare("
    SELECT p.product_id, p.product_name, p.expiry_date
    FROM products p
    WHERE p.store_id = ? AND p.expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
");
$stmt->bind_param("i", $store_id);
$stmt->execute();
$result = $stmt->get_result();

while ($row = $result->fetch_assoc()) {
    $notifications[] = [
        'type' => 'expiring_soon',
        'title' => 'Expiring Soon',
        'message' => "Product '{$row['product_name']}' is expiring on {$row['expiry_date']}",
        'product_id' => $row['product_id']
    ];
}
$stmt->close();

// 5. Check for expired items
$stmt = $conn->prepare("
    SELECT p.product_id, p.product_name, p.expiry_date
    FROM products p
    WHERE p.store_id = ? AND p.expiry_date < CURDATE()
");
$stmt->bind_param("i", $store_id);
$stmt->execute();
$result = $stmt->get_result();

while ($row = $result->fetch_assoc()) {
    $notifications[] = [
        'type' => 'expired',
        'title' => 'Expired Item',
        'message' => "Product '{$row['product_name']}' expired on {$row['expiry_date']}",
        'product_id' => $row['product_id']
    ];
}
$stmt->close();

echo json_encode(['status' => 'success', 'notifications' => $notifications]);

$conn->close();
?>