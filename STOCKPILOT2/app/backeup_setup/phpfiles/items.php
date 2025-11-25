<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

// Database credentials
$host = "localhost";
$db = "inventory_management";
$user = "root";
$pass = "";

try {
    // Connect to database with error handling
    $conn = new mysqli($host, $user, $pass, $db);
    
    if ($conn->connect_error) {
        throw new Exception('Database connection failed: ' . $conn->connect_error);
    }

    // Set charset for security
    $conn->set_charset("utf8");

    // Get and validate POST parameters
    $store_id = isset($_POST['store_id']) ? intval($_POST['store_id']) : 0;
    $status = isset($_POST['status']) ? trim($_POST['status']) : '';
    $sort_by = isset($_POST['sort_by']) ? trim($_POST['sort_by']) : 'product_name';
    $sort_order = (isset($_POST['sort_order']) && strtoupper(trim($_POST['sort_order'])) === 'DESC') ? 'DESC' : 'ASC';
    $limit = isset($_POST['limit']) ? intval($_POST['limit']) : 100;
    $offset = isset($_POST['offset']) ? intval($_POST['offset']) : 0;

    // Validate required parameters
    if ($store_id <= 0) {
        throw new Exception('Valid store_id is required');
    }

    if ($limit <= 0 || $limit > 1000) {
        $limit = 100; // Default limit with max cap
    }

    if ($offset < 0) {
        $offset = 0;
    }

    // Allowed columns for sorting (security measure)
    $allowed_sort_columns = ['product_name', 'price', 'quantity', 'sku', 'id'];
    if (!in_array($sort_by, $allowed_sort_columns)) {
        $sort_by = 'product_name';
    }

    // Prepare base query using prepared statements for security
    $sql = "SELECT id, product_name, sku, quantity, price, image_url, status, created_at
            FROM products
            WHERE store_id = ?";

    $params = [$store_id];
    $types = "i";

    // Add status filter if provided and not "All"
    if (!empty($status) && strtolower($status) !== 'all') {
        $sql .= " AND status = ?";
        $params[] = $status;
        $types .= "s";
    }

    // Add sorting
    $sql .= " ORDER BY $sort_by $sort_order";

    // Add pagination
    $sql .= " LIMIT ? OFFSET ?";
    $params[] = $limit;
    $params[] = $offset;
    $types .= "ii";

    // Prepare and execute statement
    $stmt = $conn->prepare($sql);
    if (!$stmt) {
        throw new Exception('Prepare failed: ' . $conn->error);
    }

    $stmt->bind_param($types, ...$params);

    if (!$stmt->execute()) {
        throw new Exception('Execute failed: ' . $stmt->error);
    }

    $result = $stmt->get_result();

    // Fetch results
    $items = [];
    while ($row = $result->fetch_assoc()) {
        $items[] = [
            'id' => (int)$row['id'],
            'product_name' => htmlspecialchars($row['product_name'], ENT_QUOTES, 'UTF-8'),
            'sku' => htmlspecialchars($row['sku'], ENT_QUOTES, 'UTF-8'),
            'quantity' => (int)$row['quantity'],
            'price' => number_format((float)$row['price'], 2, '.', ''),
            'image_url' => !empty($row['image_url']) ? htmlspecialchars($row['image_url'], ENT_QUOTES, 'UTF-8') : 'default_image.jpg',
            'status' => htmlspecialchars($row['status'], ENT_QUOTES, 'UTF-8'),
            'created_at' => $row['created_at']
        ];
    }

    // Get total count for pagination
    $count_sql = "SELECT COUNT(*) as total FROM products WHERE store_id = ?";
    $count_params = [$store_id];
    $count_types = "i";

    if (!empty($status) && strtolower($status) !== 'all') {
        $count_sql .= " AND status = ?";
        $count_params[] = $status;
        $count_types .= "s";
    }

    $count_stmt = $conn->prepare($count_sql);
    $count_stmt->bind_param($count_types, ...$count_params);
    $count_stmt->execute();
    $count_result = $count_stmt->get_result();
    $total_count = $count_result->fetch_assoc()['total'];

    // Return successful response
    echo json_encode([
        'status' => 'success',
        'items' => $items,
        'pagination' => [
            'total' => (int)$total_count,
            'limit' => $limit,
            'offset' => $offset,
            'has_more' => ($offset + $limit) < $total_count
        ],
        'filters' => [
            'store_id' => $store_id,
            'status' => $status,
            'sort_by' => $sort_by,
            'sort_order' => $sort_order
        ]
    ]);

} catch (Exception $e) {
    // Return error response
    http_response_code(400);
    echo json_encode([
        'status' => 'error',
        'message' => $e->getMessage()
    ]);
} finally {
    // Clean up resources
    if (isset($stmt)) {
        $stmt->close();
    }
    if (isset($count_stmt)) {
        $count_stmt->close();
    }
    if (isset($conn)) {
        $conn->close();
    }
}
?>
