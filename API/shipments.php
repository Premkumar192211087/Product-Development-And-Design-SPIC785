<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE');
header('Access-Control-Allow-Headers: Content-Type');

$host = "localhost";
$db = "inventory_management";
$user = "root";
$pass = "";

try {
    $conn = new mysqli($host, $user, $pass, $db);
    if ($conn->connect_error) {
        throw new Exception("Database connection failed");
    }
} catch (Exception $e) {
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit();
}

// Filter Options
$validStatuses = ["pending", "shipped", "in_transit", "delivered"];
$validOrderTypes = ["purchase_order", "sales_order"];

// POST: Create new shipment or update existing
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $input = json_decode(file_get_contents("php://input"), true);
    
    if (json_last_error() !== JSON_ERROR_NONE) {
        echo json_encode(["status" => "error", "message" => "Invalid JSON input"]);
        exit();
    }
    
    // Check if it's an update operation (existing shipment_id)
    if (isset($input['shipment_id']) && $input['shipment_id'] > 0) {
        $shipment_id = intval($input['shipment_id']);
        
        $stmt = $conn->prepare("UPDATE shipments SET status = 'delivered', actual_delivery_date = CURDATE() WHERE shipment_id = ?");
        $stmt->bind_param('i', $shipment_id);
        
        if ($stmt->execute()) {
            echo json_encode(["status" => "success", "message" => "Shipment marked as delivered"]);
        } else {
            echo json_encode(["status" => "error", "message" => "Failed to update status"]);
        }
        $stmt->close();
    } else {
        // Create new shipment
        $store_id = intval($input['store_id'] ?? 0);
        $order_type = $input['order_type'] ?? '';
        $order_id = intval($input['order_id'] ?? 0);
        $carrier_name = $input['carrier_name'] ?? '';
        $tracking_number = $input['tracking_number'] ?? '';
        $shipping_method = $input['shipping_method'] ?? 'standard';
        $shipping_cost = floatval($input['shipping_cost'] ?? 0.00);
        $recipient_name = $input['recipient_name'] ?? '';
        $recipient_address = $input['recipient_address'] ?? '';
        $recipient_phone = $input['recipient_phone'] ?? '';
        $estimated_delivery_date = $input['estimated_delivery_date'] ?? null;
        $notes = $input['notes'] ?? '';
        $created_by = intval($input['created_by'] ?? 1);
        
        // Generate shipment number
        $shipment_number = 'SH-' . date('Y') . '-' . str_pad(rand(1, 999999), 6, '0', STR_PAD_LEFT);
        
        $stmt = $conn->prepare("INSERT INTO shipments (
            shipment_number, order_type, order_id, carrier_name, tracking_number, 
            shipping_method, shipping_cost, estimated_delivery_date, status, 
            recipient_name, recipient_address, recipient_phone, store_id, notes, created_by
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'pending', ?, ?, ?, ?, ?, ?)");
        
        $stmt->bind_param('ssisssdsssssi', 
            $shipment_number, $order_type, $order_id, $carrier_name, $tracking_number,
            $shipping_method, $shipping_cost, $estimated_delivery_date,
            $recipient_name, $recipient_address, $recipient_phone, $store_id, $notes, $created_by
        );
        
        if ($stmt->execute()) {
            echo json_encode([
                "status" => "success", 
                "message" => "Shipment created successfully",
                "shipment_id" => $conn->insert_id,
                "shipment_number" => $shipment_number
            ]);
        } else {
            echo json_encode(["status" => "error", "message" => "Failed to create shipment"]);
        }
        $stmt->close();
    }
    $conn->close();
    exit();
}

// PUT: Update shipment status or details
if ($_SERVER['REQUEST_METHOD'] === 'PUT') {
    $input = json_decode(file_get_contents("php://input"), true);
    
    if (json_last_error() !== JSON_ERROR_NONE) {
        echo json_encode(["status" => "error", "message" => "Invalid JSON input"]);
        exit();
    }
    
    $shipment_id = intval($input['shipment_id'] ?? 0);
    
    if ($shipment_id > 0) {
        $updateFields = [];
        $updateValues = [];
        $updateTypes = '';
        
        if (isset($input['status']) && in_array($input['status'], $validStatuses)) {
            $updateFields[] = "status = ?";
            $updateValues[] = $input['status'];
            $updateTypes .= 's';
            
            // Auto-set dates based on status
            if ($input['status'] === 'shipped' && !isset($input['ship_date'])) {
                $updateFields[] = "ship_date = CURDATE()";
            } elseif ($input['status'] === 'delivered') {
                $updateFields[] = "actual_delivery_date = CURDATE()";
            }
        }
        
        if (isset($input['tracking_number'])) {
            $updateFields[] = "tracking_number = ?";
            $updateValues[] = $input['tracking_number'];
            $updateTypes .= 's';
        }
        
        if (isset($input['carrier_name'])) {
            $updateFields[] = "carrier_name = ?";
            $updateValues[] = $input['carrier_name'];
            $updateTypes .= 's';
        }
        
        if (isset($input['shipping_cost'])) {
            $updateFields[] = "shipping_cost = ?";
            $updateValues[] = floatval($input['shipping_cost']);
            $updateTypes .= 'd';
        }
        
        if (isset($input['estimated_delivery_date'])) {
            $updateFields[] = "estimated_delivery_date = ?";
            $updateValues[] = $input['estimated_delivery_date'];
            $updateTypes .= 's';
        }
        
        if (!empty($updateFields)) {
            $updateFields[] = "updated_at = CURRENT_TIMESTAMP()";
            $updateValues[] = $shipment_id;
            $updateTypes .= 'i';
            
            $sql = "UPDATE shipments SET " . implode(', ', $updateFields) . " WHERE shipment_id = ?";
            $stmt = $conn->prepare($sql);
            $stmt->bind_param($updateTypes, ...$updateValues);
            
            if ($stmt->execute()) {
                echo json_encode(["status" => "success", "message" => "Shipment updated successfully"]);
            } else {
                echo json_encode(["status" => "error", "message" => "Failed to update shipment"]);
            }
            $stmt->close();
        } else {
            echo json_encode(["status" => "error", "message" => "No valid fields to update"]);
        }
    } else {
        echo json_encode(["status" => "error", "message" => "Invalid shipment ID"]);
    }
    $conn->close();
    exit();
}

// DELETE: Cancel/delete shipment
if ($_SERVER['REQUEST_METHOD'] === 'DELETE') {
    $shipment_id = intval($_GET['shipment_id'] ?? 0);
    
    if ($shipment_id > 0) {
        // Check if shipment can be deleted (only pending or shipped status)
        $stmt = $conn->prepare("SELECT status FROM shipments WHERE shipment_id = ?");
        $stmt->bind_param('i', $shipment_id);
        $stmt->execute();
        $result = $stmt->get_result();
        
        if ($result->num_rows > 0) {
            $row = $result->fetch_assoc();
            if (in_array($row['status'], ['pending', 'shipped'])) {
                $stmt->close();
                
                $deleteStmt = $conn->prepare("DELETE FROM shipments WHERE shipment_id = ?");
                $deleteStmt->bind_param('i', $shipment_id);
                
                if ($deleteStmt->execute()) {
                    echo json_encode(["status" => "success", "message" => "Shipment deleted successfully"]);
                } else {
                    echo json_encode(["status" => "error", "message" => "Failed to delete shipment"]);
                }
                $deleteStmt->close();
            } else {
                echo json_encode(["status" => "error", "message" => "Cannot delete shipment with status: " . $row['status']]);
            }
        } else {
            echo json_encode(["status" => "error", "message" => "Shipment not found"]);
        }
        $stmt->close();
    } else {
        echo json_encode(["status" => "error", "message" => "Invalid shipment ID"]);
    }
    $conn->close();
    exit();
}

// GET: Fetch shipments with enhanced filtering and statistics
$store_id = intval($_GET['store_id'] ?? 0);
$status = isset($_GET['status']) ? strtolower($_GET['status']) : '';
$order_type = isset($_GET['order_type']) ? strtolower($_GET['order_type']) : '';
$search = $_GET['search'] ?? '';

if ($store_id === 0) {
    echo json_encode(["status" => "error", "message" => "Missing or invalid store ID"]);
    exit();
}

// Get shipment statistics for the dashboard cards
$statsStmt = $conn->prepare("SELECT status, COUNT(*) as count FROM shipments WHERE store_id = ? GROUP BY status");
$statsStmt->bind_param('i', $store_id);
$statsStmt->execute();
$statsResult = $statsStmt->get_result();

$stats = [
    "pending" => 0,
    "shipped" => 0,
    "in_transit" => 0,
    "delivered" => 0
];

while ($row = $statsResult->fetch_assoc()) {
    $stats[$row['status']] = intval($row['count']);
}
$statsStmt->close();

// Build main query for shipments list
$sql = "SELECT 
    s.*,
    CASE 
        WHEN s.order_type = 'purchase_order' THEN CONCAT('PO-', s.order_id)
        WHEN s.order_type = 'sales_order' THEN CONCAT('SO-', s.order_id)
        ELSE CONCAT('ORD-', s.order_id)
    END as order_number,
    DATEDIFF(s.estimated_delivery_date, CURDATE()) as days_until_delivery
    FROM shipments s 
    WHERE s.store_id = ?";

$params = [$store_id];
$types = 'i';

if ($status !== '' && in_array($status, $validStatuses)) {
    $sql .= " AND s.status = ?";
    $params[] = $status;
    $types .= 's';
}

if ($order_type !== '' && in_array($order_type, $validOrderTypes)) {
    $sql .= " AND s.order_type = ?";
    $params[] = $order_type;
    $types .= 's';
}

if ($search !== '') {
    $sql .= " AND (s.shipment_number LIKE ? OR s.tracking_number LIKE ? OR s.recipient_name LIKE ? OR s.carrier_name LIKE ?)";
    $searchTerm = "%$search%";
    $params = array_merge($params, [$searchTerm, $searchTerm, $searchTerm, $searchTerm]);
    $types .= 'ssss';
}

$sql .= " ORDER BY s.created_at DESC";

$stmt = $conn->prepare($sql);
$stmt->bind_param($types, ...$params);
$stmt->execute();
$result = $stmt->get_result();

$shipments = [];
while ($row = $result->fetch_assoc()) {
    $shipments[] = [
        "shipment_id" => intval($row['shipment_id']),
        "shipment_number" => $row['shipment_number'],
        "order_type" => $row['order_type'],
        "order_id" => intval($row['order_id']),
        "order_number" => $row['order_number'],
        "carrier_name" => $row['carrier_name'],
        "tracking_number" => $row['tracking_number'],
        "shipping_method" => $row['shipping_method'],
        "shipping_cost" => floatval($row['shipping_cost']),
        "ship_date" => $row['ship_date'],
        "estimated_delivery_date" => $row['estimated_delivery_date'],
        "actual_delivery_date" => $row['actual_delivery_date'],
        "status" => $row['status'],
        "recipient_name" => $row['recipient_name'],
        "recipient_address" => $row['recipient_address'],
        "recipient_phone" => $row['recipient_phone'],
        "notes" => $row['notes'],
        "created_at" => $row['created_at'],
        "updated_at" => $row['updated_at'],
        "days_until_delivery" => $row['days_until_delivery'],
        "formatted_shipping_cost" => '$' . number_format($row['shipping_cost'], 2),
        "status_color" => getStatusColor($row['status']),
        "can_track" => !empty($row['tracking_number']),
        "is_overdue" => ($row['estimated_delivery_date'] && $row['estimated_delivery_date'] < date('Y-m-d') && $row['status'] !== 'delivered')
    ];
}
$stmt->close();

// Get store name for header
$storeStmt = $conn->prepare("SELECT store_name FROM stores WHERE store_id = ?");
$storeStmt->bind_param('i', $store_id);
$storeStmt->execute();
$storeResult = $storeStmt->get_result();

$storeName = "Store";
if ($storeResult->num_rows > 0) {
    $storeRow = $storeResult->fetch_assoc();
    $storeName = $storeRow['store_name'];
}
$storeStmt->close();

// Final JSON Output
echo json_encode([
    "status" => "success",
    "store_name" => $storeName,
    "statistics" => $stats,
    "filters" => [
        "status" => $validStatuses,
        "order_type" => $validOrderTypes
    ],
    "data" => $shipments,
    "total_count" => count($shipments),
    "has_data" => count($shipments) > 0
]);

$conn->close();

// Helper function for status colors
function getStatusColor($status) {
    switch($status) {
        case 'pending': return '#FF9800';
        case 'shipped': return '#2196F3';
        case 'in_transit': return '#9C27B0';
        case 'delivered': return '#4CAF50';
        default: return '#666666';
    }
}
?>