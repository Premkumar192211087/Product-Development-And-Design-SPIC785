<?php
// Prevent any output before JSON response
ob_start();

// Error reporting (disable in production)
error_reporting(E_ALL);
ini_set('display_errors', 0); // Don't display errors to output
ini_set('log_errors', 1); // Log errors instead

// Set headers first
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST');
header('Access-Control-Allow-Headers: Content-Type');

// Clean any previous output
if (ob_get_level()) {
    ob_clean();
}

// DB connection
$host = "localhost";
$db   = "inventory_management";
$user = "root";
$pass = "";

try {
    $conn = new mysqli($host, $user, $pass, $db);
    if ($conn->connect_error) {
        throw new Exception("DB connection failed: " . $conn->connect_error);
    }
    error_log("Database connection successful");
} catch (Exception $e) {
    error_log("Database connection error: " . $e->getMessage());
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit();
}

// Utility: format currency (INR)
function formatINR($amount) {
    return '₹' . number_format((float)$amount, 2);
}

// Function to send JSON response and exit
function sendJsonResponse($data) {
    // Clean any output buffer
    if (ob_get_level()) {
        ob_clean();
    }
    echo json_encode($data);
    exit();
}

// POST: Mark shipment as delivered
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        $input = file_get_contents("php://input");
        $data = json_decode($input, true);
        
        if (json_last_error() !== JSON_ERROR_NONE) {
            throw new Exception("Invalid JSON input");
        }
        
        $shipment_id = intval($data['shipment_id'] ?? 0);

        if ($shipment_id > 0) {
            $stmt = $conn->prepare("UPDATE shipments SET status='delivered', actual_delivery_date=CURDATE() WHERE shipment_id=?");
            if (!$stmt) {
                throw new Exception("Prepare statement failed: " . $conn->error);
            }
            
            $stmt->bind_param('i', $shipment_id);
            if ($stmt->execute()) {
                $stmt->close();
                $conn->close();
                sendJsonResponse(["status" => "success", "message" => "Shipment marked as delivered"]);
            } else {
                throw new Exception("Update failed: " . $stmt->error);
            }
        } else {
            sendJsonResponse(["status" => "error", "message" => "Invalid shipment ID"]);
        }
    } catch (Exception $e) {
        error_log("POST error: " . $e->getMessage());
        sendJsonResponse(["status" => "error", "message" => "Update failed"]);
    }
}

// GET: Fetch full shipment detail
try {
    $shipment_id = isset($_GET['shipment_id']) ? intval($_GET['shipment_id']) : 0;
    error_log("Processing shipment_id: " . $shipment_id);
    
    if ($shipment_id <= 0) {
        error_log("Invalid shipment ID provided: " . $shipment_id);
        sendJsonResponse(["status" => "error", "message" => "Invalid shipment ID"]);
    }

    // Fetch shipment
    error_log("Starting shipment query for ID: " . $shipment_id);
    $stmt = $conn->prepare("SELECT shipment_id, shipment_number, order_type, order_id, carrier_name, tracking_number, shipping_method, shipping_cost, ship_date, estimated_delivery_date, actual_delivery_date, status, recipient_name, recipient_phone, recipient_address, notes FROM shipments WHERE shipment_id = ?");
    
    if (!$stmt) {
        error_log("Prepare statement failed: " . $conn->error);
        throw new Exception("Prepare statement failed: " . $conn->error);
    }
    
    $stmt->bind_param('i', $shipment_id);
    if (!$stmt->execute()) {
        error_log("Query execution failed: " . $stmt->error);
        throw new Exception("Query execution failed: " . $stmt->error);
    }
    
    $result = $stmt->get_result();
    if ($result->num_rows === 0) {
        error_log("No shipment found with ID: " . $shipment_id);
        $stmt->close();
        $conn->close();
        sendJsonResponse(["status" => "error", "message" => "Shipment not found"]);
    }
    
    $shipment = $result->fetch_assoc();
    $stmt->close();
    error_log("Shipment found: " . $shipment['shipment_number']);

    // Build display-friendly order info
    $orderLabelMap = [
        'purchase_order' => 'Purchase Order',
        'sales_order'    => 'Sales Order'
    ];
    $orderTypeRaw = $shipment['order_type'];
    $orderLabel = $orderLabelMap[$orderTypeRaw] ?? ucfirst(str_replace('_', ' ', $orderTypeRaw));
    $orderInfo = sprintf("%s #%s", $orderLabel, $shipment['order_id']);

    // Fetch shipment items
    error_log("Starting items query for shipment ID: " . $shipment_id);
    $stmt = $conn->prepare(
        "SELECT si.product_id, p.product_name, p.sku, p.image_url, si.quantity_shipped, si.unit_price, si.total_value, si.batch_id
         FROM shipment_items si
         JOIN products p ON si.product_id = p.product_id AND si.store_id = p.store_id
         WHERE si.shipment_id = ?"
    );
    
    if (!$stmt) {
        error_log("Prepare statement failed for items: " . $conn->error);
        throw new Exception("Prepare statement failed for items: " . $conn->error);
    }
    
    $stmt->bind_param('i', $shipment_id);
    if (!$stmt->execute()) {
        error_log("Items query execution failed: " . $stmt->error);
        throw new Exception("Items query execution failed: " . $stmt->error);
    }
    
    $result_items = $stmt->get_result();
    error_log("Items query returned " . $result_items->num_rows . " rows");

    $items = [];
    $totalItems = 0;
    $totalValue = 0.0;
    
    while ($row = $result_items->fetch_assoc()) {
        $items[] = [
            'product_id'       => (int)$row['product_id'],
            'product_name'     => $row['product_name'] ?? '',
            'sku'              => $row['sku'] ?? '',
            'image_url'        => $row['image_url'] ?? '',
            'quantity_shipped' => (int)$row['quantity_shipped'],
            'unit_price'       => formatINR($row['unit_price']),
            'total_value'      => formatINR($row['total_value']),
            'batch_id'         => $row['batch_id'] ?? ''
        ];
        $totalItems += (int)$row['quantity_shipped'];
        $totalValue += (float)$row['total_value'];
    }
    $stmt->close();

    // Build response
    $response = [
        'status'       => 'success',
        'shipment'     => [
            'shipment_id'             => (int)$shipment['shipment_id'],
            'shipment_number'         => $shipment['shipment_number'] ?? '',
            'order_info'              => $orderInfo,
            'carrier_name'            => $shipment['carrier_name'] ?? '',
            'tracking_number'         => $shipment['tracking_number'] ?? '',
            'shipping_method'         => $shipment['shipping_method'] ?? '',
            'shipping_cost'           => formatINR($shipment['shipping_cost']),
            'ship_date'               => $shipment['ship_date'] ?? '',
            'estimated_delivery_date' => $shipment['estimated_delivery_date'] ?? '',
            'actual_delivery_date'    => $shipment['actual_delivery_date'] ?? '',
            'status'                  => $shipment['status'] ?? '',
            'recipient_name'          => $shipment['recipient_name'] ?? '',
            'recipient_phone'         => $shipment['recipient_phone'] ?? '',
            'recipient_address'       => $shipment['recipient_address'] ?? '',
            'notes'                   => $shipment['notes'] ?? ''
        ],
        'items'        => $items,
        'total_items'  => $totalItems,
        'total_value'  => formatINR($totalValue)
    ];

    $conn->close();
    error_log("Successfully built response for shipment " . $shipment_id);
    sendJsonResponse($response);

} catch (Exception $e) {
    error_log("GET error in detailed_shipments.php: " . $e->getMessage());
    error_log("Stack trace: " . $e->getTraceAsString());
    if (isset($conn)) {
        $conn->close();
    }
    sendJsonResponse(["status" => "error", "message" => "Failed to fetch shipment details"]);
}
?>