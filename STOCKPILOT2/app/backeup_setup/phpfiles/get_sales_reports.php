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
$from_date = isset($_POST['from_date']) ? $_POST['from_date'] : null;
$to_date = isset($_POST['to_date']) ? $_POST['to_date'] : null;

// Initialize response array
$response = [
    'success' => true,
    'total_sales' => 0,
    'total_orders' => 0,
    'payment_methods' => [],
    'recent_sales' => []
];

// Build date filter condition
$date_condition = '';
if ($from_date && $to_date) {
    $date_condition = " AND DATE(sale_date) BETWEEN '$from_date' AND '$to_date'";
} elseif ($from_date) {
    $date_condition = " AND DATE(sale_date) >= '$from_date'";
} elseif ($to_date) {
    $date_condition = " AND DATE(sale_date) <= '$to_date'";
}

// Get total sales amount and count
$query = "SELECT 
            COUNT(*) as total_orders, 
            SUM(final_amount) as total_sales 
          FROM sales 
          WHERE store_id = ? $date_condition";

$stmt = $conn->prepare($query);
$stmt->bind_param('i', $store_id);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    $response['total_sales'] = floatval($row['total_sales']) ?: 0;
    $response['total_orders'] = intval($row['total_orders']) ?: 0;
}

// Get payment methods breakdown
$query = "SELECT 
            payment_method, 
            COUNT(*) as count, 
            SUM(final_amount) as total 
          FROM sales 
          WHERE store_id = ? $date_condition 
          GROUP BY payment_method";

$stmt = $conn->prepare($query);
$stmt->bind_param('i', $store_id);
$stmt->execute();
$result = $stmt->get_result();

while ($row = $result->fetch_assoc()) {
    $response['payment_methods'][] = [
        'method' => $row['payment_method'],
        'count' => intval($row['count']),
        'total' => floatval($row['total'])
    ];
}

// Get recent sales
$query = "SELECT 
            s.sale_id, 
            s.invoice_number, 
            s.sale_date, 
            s.final_amount, 
            s.payment_method,
            c.customer_name
          FROM sales s
          LEFT JOIN customers c ON s.customer_id = c.customer_id
          WHERE s.store_id = ? $date_condition
          ORDER BY s.sale_date DESC 
          LIMIT 10";

$stmt = $conn->prepare($query);
$stmt->bind_param('i', $store_id);
$stmt->execute();
$result = $stmt->get_result();

while ($row = $result->fetch_assoc()) {
    // Get sale items
    $items_query = "SELECT 
                    p.product_name, 
                    si.quantity, 
                    si.unit_price, 
                    si.total_price
                  FROM sale_items si
                  JOIN products p ON si.product_id = p.id
                  WHERE si.sale_id = ? AND si.store_id = ?";
    
    $items_stmt = $conn->prepare($items_query);
    $items_stmt->bind_param('ii', $row['sale_id'], $store_id);
    $items_stmt->execute();
    $items_result = $items_stmt->get_result();
    
    $items = [];
    while ($item = $items_result->fetch_assoc()) {
        $items[] = [
            'product_name' => $item['product_name'],
            'quantity' => intval($item['quantity']),
            'unit_price' => floatval($item['unit_price']),
            'total_price' => floatval($item['total_price'])
        ];
    }
    
    $response['recent_sales'][] = [
        'sale_id' => $row['sale_id'],
        'invoice_number' => $row['invoice_number'],
        'customer_name' => $row['customer_name'] ?: 'Walk-in Customer',
        'date' => $row['sale_date'],
        'amount' => floatval($row['final_amount']),
        'payment_method' => $row['payment_method'],
        'items' => $items
    ];
}

// Return the response
echo json_encode($response);
$conn->close();