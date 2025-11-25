<?php
require_once 'db_connect.php';
require_once 'utils.php';

// Set response headers
header('Content-Type: application/json');

// Get database connection
$conn = getDbConnection();

// Handle different HTTP methods
$method = $_SERVER['REQUEST_METHOD'];

switch ($method) {
    case 'GET':
        // Get shipment items for a specific shipment
        getShipmentItems($conn);
        break;
    case 'POST':
        // Add new shipment items
        addShipmentItems($conn);
        break;
    case 'DELETE':
        // Delete a shipment item
        deleteShipmentItem($conn);
        break;
    default:
        http_response_code(405); // Method Not Allowed
        echo json_encode(['error' => 'Method not allowed']);
        break;
}

// Close the database connection
$conn->close();

/**
 * Get shipment items for a specific shipment
 */
function getShipmentItems($conn) {
    // Check if shipment_id is provided
    if (!isset($_GET['shipment_id'])) {
        http_response_code(400);
        echo json_encode(['error' => 'Shipment ID is required']);
        return;
    }
    
    $shipmentId = intval($_GET['shipment_id']);
    
    // Prepare and execute the query
    $query = "SELECT si.shipment_item_id, si.shipment_id, si.product_id, p.product_name, p.sku, 
              si.quantity_shipped, si.unit_price, si.total_value, si.batch_id, si.created_at
              FROM shipment_items si
              JOIN products p ON si.product_id = p.product_id
              WHERE si.shipment_id = ?
              ORDER BY si.created_at DESC";
    
    $stmt = $conn->prepare($query);
    $stmt->bind_param('i', $shipmentId);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $items = [];
    while ($row = $result->fetch_assoc()) {
        $items[] = $row;
    }
    
    echo json_encode(['items' => $items]);
}

/**
 * Add new shipment items
 */
function addShipmentItems($conn) {
    // Get JSON data from request body
    $data = json_decode(file_get_contents('php://input'), true);
    
    // Validate required fields
    if (!isset($data['shipment_id']) || !isset($data['items']) || empty($data['items'])) {
        http_response_code(400);
        echo json_encode(['error' => 'Shipment ID and items are required']);
        return;
    }
    
    $shipmentId = intval($data['shipment_id']);
    $storeId = isset($data['store_id']) ? intval($data['store_id']) : 1; // Default to store 1 if not provided
    $items = $data['items'];
    
    // Start transaction
    $conn->begin_transaction();
    
    try {
        // Check if shipment exists
        $checkQuery = "SELECT shipment_id FROM shipments WHERE shipment_id = ?";
        $checkStmt = $conn->prepare($checkQuery);
        $checkStmt->bind_param('i', $shipmentId);
        $checkStmt->execute();
        $checkResult = $checkStmt->get_result();
        
        if ($checkResult->num_rows === 0) {
            throw new Exception('Shipment not found');
        }
        
        // Prepare insert statement
        $insertQuery = "INSERT INTO shipment_items (store_id, shipment_id, product_id, quantity_shipped, 
                        unit_price, total_value, batch_id, created_at) 
                        VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        $insertStmt = $conn->prepare($insertQuery);
        
        $insertedItems = [];
        
        foreach ($items as $item) {
            // Validate required item fields
            if (!isset($item['product_id']) || !isset($item['quantity_shipped']) || !isset($item['unit_price'])) {
                throw new Exception('Product ID, quantity, and unit price are required for each item');
            }
            
            $productId = intval($item['product_id']);
            $quantityShipped = intval($item['quantity_shipped']);
            $unitPrice = floatval($item['unit_price']);
            $totalValue = $quantityShipped * $unitPrice;
            $batchId = isset($item['batch_id']) ? $item['batch_id'] : null;
            
            // Insert the item
            $insertStmt->bind_param('iiiddss', $storeId, $shipmentId, $productId, $quantityShipped, 
                                   $unitPrice, $totalValue, $batchId);
            $insertStmt->execute();
            
            // Get the inserted item ID
            $itemId = $conn->insert_id;
            
            // Get product details
            $productQuery = "SELECT product_name, sku FROM products WHERE product_id = ?";
            $productStmt = $conn->prepare($productQuery);
            $productStmt->bind_param('i', $productId);
            $productStmt->execute();
            $productResult = $productStmt->get_result();
            $product = $productResult->fetch_assoc();
            
            // Add to inserted items array
            $insertedItems[] = [
                'shipment_item_id' => $itemId,
                'shipment_id' => $shipmentId,
                'product_id' => $productId,
                'product_name' => $product['product_name'],
                'sku' => $product['sku'],
                'quantity_shipped' => $quantityShipped,
                'unit_price' => $unitPrice,
                'total_value' => $totalValue,
                'batch_id' => $batchId
            ];
        }
        
        // Commit transaction
        $conn->commit();
        
        // Return success response
        echo json_encode([
            'success' => true,
            'message' => 'Shipment items added successfully',
            'items' => $insertedItems
        ]);
        
    } catch (Exception $e) {
        // Rollback transaction on error
        $conn->rollback();
        
        http_response_code(500);
        echo json_encode(['error' => $e->getMessage()]);
    }
}

/**
 * Delete a shipment item
 */
function deleteShipmentItem($conn) {
    // Get JSON data from request body
    $data = json_decode(file_get_contents('php://input'), true);
    
    // Validate required fields
    if (!isset($data['shipment_item_id'])) {
        http_response_code(400);
        echo json_encode(['error' => 'Shipment item ID is required']);
        return;
    }
    
    $shipmentItemId = intval($data['shipment_item_id']);
    
    // Check if the shipment item exists
    $checkQuery = "SELECT si.shipment_item_id, s.status 
                  FROM shipment_items si
                  JOIN shipments s ON si.shipment_id = s.shipment_id
                  WHERE si.shipment_item_id = ?";
    $checkStmt = $conn->prepare($checkQuery);
    $checkStmt->bind_param('i', $shipmentItemId);
    $checkStmt->execute();
    $checkResult = $checkStmt->get_result();
    
    if ($checkResult->num_rows === 0) {
        http_response_code(404);
        echo json_encode(['error' => 'Shipment item not found']);
        return;
    }
    
    $shipmentData = $checkResult->fetch_assoc();
    
    // Check if the shipment is in a status that allows item deletion
    if ($shipmentData['status'] !== 'pending') {
        http_response_code(400);
        echo json_encode(['error' => 'Cannot delete items from shipments that are not in pending status']);
        return;
    }
    
    // Delete the shipment item
    $deleteQuery = "DELETE FROM shipment_items WHERE shipment_item_id = ?";
    $deleteStmt = $conn->prepare($deleteQuery);
    $deleteStmt->bind_param('i', $shipmentItemId);
    
    if ($deleteStmt->execute()) {
        echo json_encode([
            'success' => true,
            'message' => 'Shipment item deleted successfully'
        ]);
    } else {
        http_response_code(500);
        echo json_encode(['error' => 'Failed to delete shipment item']);
    }
}
?>