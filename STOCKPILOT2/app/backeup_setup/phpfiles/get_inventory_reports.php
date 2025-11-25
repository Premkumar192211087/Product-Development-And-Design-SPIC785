<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

// DB connection
$host = "localhost";
$user = "root";
$pass = "";
$db = "inventory_management";

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    echo json_encode([
        'status' => 'error',
        'error' => 'Database connection failed'
    ]);
    exit;
}

// Get parameters
$store_id = isset($_POST['store_id']) ? (int)$_POST['store_id'] : null;
$category = isset($_POST['category']) ? $_POST['category'] : null;

if (!$store_id) {
    echo json_encode([
        'status' => 'error',
        'error' => 'Store ID is required'
    ]);
    exit;
}

try {
    // Build category filter
    $categoryFilter = "";
    if ($category && $category !== "All") {
        $categoryFilter = " AND p.category = '" . $conn->real_escape_string($category) . "'";
    }

    // 1. Get inventory summary
    $summaryQuery = "
        SELECT
            COUNT(DISTINCT p.product_id) as total_products,
            SUM(CASE WHEN p.quantity <= p.min_stock_level THEN 1 ELSE 0 END) as low_stock,
            SUM(CASE WHEN p.expiry_date < CURDATE() THEN 1 ELSE 0 END) as expired
        FROM products p
        WHERE p.store_id = $store_id $categoryFilter
    ";

    $summaryResult = $conn->query($summaryQuery);
    $summary = $summaryResult->fetch_assoc();

    // 2. Get stock movements
    $movementsQuery = "
        SELECT
            sm.movement_id,
            p.product_name,
            p.category,
            sm.movement_type,
            sm.quantity,
            sm.reference_type,
            CONCAT(u.first_name, ' ', u.last_name) as performed_by,
            sm.timestamp
        FROM stock_movements sm
        INNER JOIN products p ON sm.product_id = p.product_id
        LEFT JOIN users u ON sm.user_id = u.user_id
        WHERE p.store_id = $store_id $categoryFilter
        ORDER BY sm.timestamp DESC
        LIMIT 50
    ";

    $movementsResult = $conn->query($movementsQuery);
    $movements = [];
    while ($row = $movementsResult->fetch_assoc()) {
        $movements[] = $row;
    }

    // 3. Get low stock alerts
    $alertsQuery = "
        SELECT
            p.product_id,
            p.product_name,
            p.category,
            p.quantity as current_stock,
            p.min_stock_level,
            p.reorder_quantity,
            CASE
                WHEN p.quantity = 0 THEN 'Out of Stock'
                WHEN p.quantity <= p.min_stock_level THEN 'Low Stock'
                ELSE 'In Stock'
            END as status
        FROM products p
        WHERE p.store_id = $store_id
        AND p.quantity <= p.min_stock_level
        $categoryFilter
        ORDER BY p.quantity ASC
        LIMIT 20
    ";

    $alertsResult = $conn->query($alertsQuery);
    $alerts = [];
    while ($row = $alertsResult->fetch_assoc()) {
        $alerts[] = $row;
    }

    // 4. Get all categories for filter
    $categoriesQuery = "
        SELECT DISTINCT category
        FROM products
        WHERE store_id = $store_id
        AND category IS NOT NULL
        AND category != ''
        ORDER BY category
    ";

    $categoriesResult = $conn->query($categoriesQuery);
    $categories = [];
    while ($row = $categoriesResult->fetch_assoc()) {
        $categories[] = $row['category'];
    }

    // Build response
    $response = [
        'status' => 'success',
        'inventory_summary' => [
            'total_products' => (int)$summary['total_products'],
            'low_stock' => (int)$summary['low_stock'],
            'expired' => (int)$summary['expired']
        ],
        'stock_movements' => $movements,
        'low_stock_alerts' => $alerts,
        'categories' => $categories
    ];

    echo json_encode($response);

} catch (Exception $e) {
    echo json_encode([
        'status' => 'error',
        'error' => 'Server error: ' . $e->getMessage()
    ]);
}

$conn->close();
?>