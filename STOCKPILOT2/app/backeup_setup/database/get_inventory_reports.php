<?php
// Database connection
require_once 'db_connect.php';

// Set headers for JSON response
header('Content-Type: application/json');

// Check if store_id is provided
if (!isset($_POST['store_id']) || empty($_POST['store_id'])) {
    echo json_encode(['success' => false, 'message' => 'Store ID is required']);
    exit;
}

$store_id = $_POST['store_id'];
$category_filter = isset($_POST['category']) ? $_POST['category'] : null;

// Initialize response array
$response = [
    'success' => true,
    'inventory_summary' => [
        'total_products' => 0,
        'low_stock' => 0,
        'expired' => 0
    ],
    'stock_movements' => [],
    'low_stock_alerts' => []
];

// Build category filter condition
$category_condition = '';
if ($category_filter && $category_filter !== 'All') {
    $category_condition = " AND category = '$category_filter'";
}

// Get inventory summary
// Total products
$query = "SELECT COUNT(*) as total FROM products WHERE store_id = ? AND status = 'active'";
$stmt = $conn->prepare($query);
$stmt->bind_param('i', $store_id);
$stmt->execute();
$result = $stmt->get_result();
if ($row = $result->fetch_assoc()) {
    $response['inventory_summary']['total_products'] = intval($row['total']);
}

// Low stock products
$query = "SELECT COUNT(*) as low_stock 
          FROM products p 
          JOIN inventory_alerts ia ON p.id = ia.product_id AND p.store_id = ia.store_id
          WHERE p.store_id = ? AND p.status = 'active' AND p.quantity <= ia.min_stock_level";
$stmt = $conn->prepare($query);
$stmt->bind_param('i', $store_id);
$stmt->execute();
$result = $stmt->get_result();
if ($row = $result->fetch_assoc()) {
    $response['inventory_summary']['low_stock'] = intval($row['low_stock']);
}

// Expired products (using batch_details table)
$current_date = date('Y-m-d');
$query = "SELECT COUNT(DISTINCT product_id) as expired 
          FROM batch_details 
          WHERE store_id = ? AND exp_date < '$current_date'";
$stmt = $conn->prepare($query);
$stmt->bind_param('i', $store_id);
$stmt->execute();
$result = $stmt->get_result();
if ($row = $result->fetch_assoc()) {
    $response['inventory_summary']['expired'] = intval($row['expired']);
}

// Get recent stock movements
$query = "SELECT 
            sm.movement_id,
            sm.movement_type,
            sm.quantity,
            sm.reference_type,
            sm.timestamp,
            p.product_name,
            p.category,
            u.username as performed_by_user
          FROM stock_movements sm
          JOIN products p ON sm.product_id = p.id
          JOIN user_login u ON sm.performed_by = u.id
          WHERE sm.store_id = ? $category_condition
          ORDER BY sm.timestamp DESC
          LIMIT 10";

$stmt = $conn->prepare($query);
$stmt->bind_param('i', $store_id);
$stmt->execute();
$result = $stmt->get_result();

while ($row = $result->fetch_assoc()) {
    $response['stock_movements'][] = [
        'movement_id' => $row['movement_id'],
        'product_name' => $row['product_name'],
        'category' => $row['category'],
        'movement_type' => $row['movement_type'],
        'quantity' => intval($row['quantity']),
        'reference_type' => $row['reference_type'],
        'performed_by' => $row['performed_by_user'],
        'timestamp' => $row['timestamp']
    ];
}

// Get low stock alerts
$query = "SELECT 
            p.id as product_id,
            p.product_name,
            p.quantity as current_stock,
            p.category,
            ia.min_stock_level,
            ia.reorder_quantity
          FROM products p
          JOIN inventory_alerts ia ON p.id = ia.product_id AND p.store_id = ia.store_id
          WHERE p.store_id = ? AND p.status = 'active' 
            AND p.quantity <= ia.min_stock_level $category_condition
          ORDER BY (p.quantity / ia.min_stock_level) ASC
          LIMIT 10";

$stmt = $conn->prepare($query);
$stmt->bind_param('i', $store_id);
$stmt->execute();
$result = $stmt->get_result();

while ($row = $result->fetch_assoc()) {
    $response['low_stock_alerts'][] = [
        'product_id' => $row['product_id'],
        'product_name' => $row['product_name'],
        'category' => $row['category'],
        'current_stock' => intval($row['current_stock']),
        'min_stock_level' => intval($row['min_stock_level']),
        'reorder_quantity' => intval($row['reorder_quantity']),
        'status' => ($row['current_stock'] == 0) ? 'Out of Stock' : 'Low Stock'
    ];
}

// Get all categories for filter
$query = "SELECT DISTINCT category FROM products WHERE store_id = ? ORDER BY category";
$stmt = $conn->prepare($query);
$stmt->bind_param('i', $store_id);
$stmt->execute();
$result = $stmt->get_result();

$response['categories'] = [];
while ($row = $result->fetch_assoc()) {
    $response['categories'][] = $row['category'];
}

// Return the response
echo json_encode($response);
$conn->close();