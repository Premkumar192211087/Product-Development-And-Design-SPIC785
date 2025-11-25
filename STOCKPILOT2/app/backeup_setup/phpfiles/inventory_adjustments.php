<?php
// Database connection details
define('DB_SERVER', 'localhost');
define('DB_USERNAME', 'root'); // Replace with your database username
define('DB_PASSWORD', '');     // Replace with your database password
define('DB_NAME', 'inventory_management');

// Establish database connection
$conn = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_NAME);

// Check connection
if ($conn->connect_error) {
    // In a real application, you might log this error and provide a generic message.
    die(json_encode(["error" => "Database connection failed: " . $conn->connect_error]));
}

// Get store_id dynamically from GET request
// Default to store_id 1 if not provided, or handle as per app's logic
$store_id = isset($_GET['store_id']) ? intval($_GET['store_id']) : 1;

// Get filter parameters from GET request
$filter_movement_type = isset($_GET['movement_type']) ? $_GET['movement_type'] : "All";
$filter_reference_type = isset($_GET['reference_type']) ? $_GET['reference_type'] : "All";
$filter_quick = isset($_GET['quick_filter']) ? $_GET['quick_filter'] : "All";
$search_product_name = isset($_GET['search_product']) ? $_GET['search_product'] : "";

// Build SQL query with correct column names from both tables
$sql = "SELECT sm.movement_id, p.product_name, sm.movement_type, sm.quantity, sm.reference_type, sm.timestamp as movement_date, sm.unit_price, sd.full_name
        FROM stock_movements sm
        JOIN products p ON sm.product_id = p.id
        LEFT JOIN staff_details sd ON sm.performed_by = sd.staff_id
        WHERE sm.store_id = ?";
$params = [$store_id];
$types = "i";

if ($filter_movement_type !== "All") {
    $sql .= " AND sm.movement_type = ?";
    $params[] = $filter_movement_type;
    $types .= "s";
}
if ($filter_reference_type !== "All") {
    $sql .= " AND sm.reference_type = ?";
    $params[] = $filter_reference_type;
    $types .= "s";
}
if ($search_product_name !== "") {
    $sql .= " AND p.product_name LIKE ?";
    $params[] = "%" . $search_product_name . "%";
    $types .= "s";
}

// Apply quick filters based on date or specific movement types for "In"/"Out" quick filters
switch ($filter_quick) {
    case "Today":
        $sql .= " AND DATE(sm.timestamp) = CURDATE()";
        break;
    case "This Week":
        $sql .= " AND YEARWEEK(sm.timestamp, 1) = YEARWEEK(CURDATE(), 1)";
        break;
    case "This Month":
        $sql .= " AND MONTH(sm.timestamp) = MONTH(CURDATE()) AND YEAR(sm.timestamp) = YEAR(CURDATE())";
        break;
    case "In":
        if ($filter_movement_type === "All") {
            $sql .= " AND sm.movement_type = 'In'";
        }
        break;
    case "Out":
        if ($filter_movement_type === "All") {
            $sql .= " AND sm.movement_type = 'Out'";
        }
        break;
}

$sql .= " ORDER BY sm.timestamp DESC";

$stmt = $conn->prepare($sql);
$adjustments = [];

if ($stmt) {
    // Dynamically bind parameters
    $stmt->bind_param($types, ...$params);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result->num_rows > 0) {
        while ($row = $result->fetch_assoc()) {
            // Format quantity with +/- based on movement type
            // CHANGE 'quantity_moved' to 'quantity' (or whatever your column is named)
            $quantity_prefix = ($row['movement_type'] === 'In' || $row['movement_type'] === 'Transfer') ? '+' : '-';
            $formatted_quantity = $quantity_prefix . abs($row['quantity']);
            
            $adjustments[] = [
                "movement_id" => $row['movement_id'],
                "product_name" => $row['product_name'],
                "movement_type" => $row['movement_type'],
                "quantity_moved" => (int)$row['quantity'], // Keep original for calculations
                "formatted_quantity" => $formatted_quantity,     // Formatted for display
                "reference_type" => $row['reference_type'],
                "movement_date" => $row['movement_date'], // ISO format preferred for apps
                "unit_price" => (float)$row['unit_price'],
                "performed_by" => $row['full_name'] ?? "Unknown Staff",
            ];
        }
    }
    $stmt->close();
} else {
    // Error handling for statement preparation
    header('Content-Type: application/json');
    echo json_encode(["error" => "Error preparing statement: " . $conn->error]);
    $conn->close();
    exit();
}

$conn->close();

// Output JSON response
header('Content-Type: application/json');
echo json_encode([
    "store_id" => $store_id,
    "store_name" => "", // Store name is typically fetched separately by the app if needed
    "filters" => [
        "movement_types" => ["All", "In", "Out", "Transfer", "Adjustment"],
        "reference_types" => ["All", "Purchase", "Sale", "Return", "Damage", "Expired", "Stock Count", "Manual"],
        "quick_filters" => ["All", "In", "Out", "Today", "This Week", "This Month"]
    ],
    "selected_filters" => [
        "movement_type" => $filter_movement_type,
        "reference_type" => $filter_reference_type,
        "quick_filter" => $filter_quick,
        "search_product" => $search_product_name
    ],
    "adjustments" => $adjustments
]);
?>