<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Content-Type');

// Enable error reporting for debugging
error_reporting(E_ALL);
ini_set('display_errors', 1);

require 'db.php'; // assumes $conn is defined

// Log the request
error_log("get_shipment_items.php called with parameters: " . print_r($_GET, true));

if (!isset($_GET['shipment_id'])) {
    http_response_code(400);
    echo json_encode([
        "status" => "error", 
        "message" => "Missing shipment_id parameter"
    ]);
    exit;
}

$shipment_id = intval($_GET['shipment_id']);

if ($shipment_id <= 0) {
    http_response_code(400);
    echo json_encode([
        "status" => "error", 
        "message" => "Invalid shipment_id. Must be a positive integer."
    ]);
    exit;
}

error_log("Processing shipment_id: " . $shipment_id);

// First check if shipment exists
$check_sql = "SELECT shipment_id FROM shipments WHERE shipment_id = ?";
$check_stmt = $conn->prepare($check_sql);
if (!$check_stmt) {
    error_log("Prepare failed for check query: " . $conn->error);
    http_response_code(500);
    echo json_encode([
        "status" => "error",
        "message" => "Database prepare error"
    ]);
    exit;
}

$check_stmt->bind_param("i", $shipment_id);
$check_stmt->execute();
$check_result = $check_stmt->get_result();

if ($check_result->num_rows == 0) {
    http_response_code(404);
    echo json_encode([
        "status" => "error",
        "message" => "Shipment not found"
    ]);
    exit;
}
$check_stmt->close();

// Main query to get shipment items
$sql = "
    SELECT 
        si.shipment_item_id,
        si.product_id,
        p.product_name,
        p.sku,
        si.quantity_shipped,
        si.unit_price,
        si.total_value,
        si.batch_id
    FROM 
        shipment_items si
    JOIN 
        products p ON si.product_id = p.id
    WHERE 
        si.shipment_id = ?
    ORDER BY 
        si.shipment_item_id ASC
";

$stmt = $conn->prepare($sql);
if (!$stmt) {
    error_log("Prepare failed for main query: " . $conn->error);
    http_response_code(500);
    echo json_encode([
        "status" => "error",
        "message" => "Database prepare error"
    ]);
    exit;
}

$stmt->bind_param("i", $shipment_id);
$stmt->execute();
$result = $stmt->get_result();

if (!$result) {
    error_log("Query execution failed: " . $conn->error);
    http_response_code(500);
    echo json_encode([
        "status" => "error",
        "message" => "Query execution failed"
    ]);
    exit;
}

$items = [];
while ($row = $result->fetch_assoc()) {
    $items[] = [
        "shipment_item_id" => (int)$row['shipment_item_id'],
        "product_id" => (int)$row['product_id'],
        "product_name" => $row['product_name'] ?? '',
        "sku" => $row['sku'] ?? '',
        "quantity_shipped" => (int)$row['quantity_shipped'],
        "unit_price" => (float)$row['unit_price'],
        "total_value" => (float)$row['total_value'],
        "batch_id" => $row['batch_id'] ?? null
    ];
}

$stmt->close();

error_log("Found " . count($items) . " items for shipment_id: " . $shipment_id);

// Return the items array directly (as your Android code expects)
echo json_encode($items);
?>