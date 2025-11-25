<?php
// Include database connection
require_once 'db.php';

// Set headers for JSON response
header('Content-Type: application/json');

// Function to get vendor details
function get_vendor_details($conn, $supplier_id, $store_id) {
    // Validate inputs
    $supplier_id = (int)$supplier_id;
    $store_id = (int)$store_id;
    
    if ($supplier_id <= 0 || $store_id <= 0) {
        return [
            'success' => false,
            'message' => 'Invalid supplier ID or store ID provided'
        ];
    }
    
    try {
        // Get vendor details
        $sql = "SELECT * FROM suppliers WHERE supplier_id = ? AND store_id = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("ii", $supplier_id, $store_id);
        $stmt->execute();
        $result = $stmt->get_result();
        
        if ($result->num_rows === 0) {
            return [
                'success' => false,
                'message' => 'Vendor not found or you do not have permission to view it'
            ];
        }
        
        $vendor = $result->fetch_assoc();
        
        // Get associated products
        $products_sql = "SELECT ps.*, p.product_name, p.sku 
                         FROM product_suppliers ps 
                         JOIN products p ON ps.product_id = p.id 
                         WHERE ps.supplier_id = ? AND ps.store_id = ?";
        
        $products_stmt = $conn->prepare($products_sql);
        $products_stmt->bind_param("ii", $supplier_id, $store_id);
        $products_stmt->execute();
        $products_result = $products_stmt->get_result();
        
        $products = [];
        while ($product = $products_result->fetch_assoc()) {
            $products[] = $product;
        }
        
        // Get purchase order history
        $po_sql = "SELECT po.*, 
                   (SELECT COUNT(*) FROM purchase_order_items WHERE po_id = po.po_id) as item_count 
                   FROM purchase_orders po 
                   WHERE po.supplier_id = ? AND po.store_id = ? 
                   ORDER BY po.order_date DESC";
        
        $po_stmt = $conn->prepare($po_sql);
        $po_stmt->bind_param("ii", $supplier_id, $store_id);
        $po_stmt->execute();
        $po_result = $po_stmt->get_result();
        
        $purchase_orders = [];
        while ($po = $po_result->fetch_assoc()) {
            $purchase_orders[] = $po;
        }
        
        return [
            'success' => true,
            'vendor' => $vendor,
            'products' => $products,
            'purchase_orders' => $purchase_orders
        ];
    } catch (Exception $e) {
        return [
            'success' => false,
            'message' => 'Error retrieving vendor details: ' . $e->getMessage()
        ];
    }
}

// Handle the request
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    // Check if required parameters are provided
    if (isset($_GET['supplier_id']) && isset($_GET['store_id'])) {
        $supplier_id = (int)$_GET['supplier_id'];
        $store_id = (int)$_GET['store_id'];
        
        $response = get_vendor_details($conn, $supplier_id, $store_id);
        
        // Set appropriate HTTP status code
        if (!$response['success']) {
            http_response_code(404);
        }
        
        echo json_encode($response);
    } else {
        // Return error if required parameters are not provided
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Missing required parameters: supplier_id, store_id'
        ]);
    }
} else {
    // Method not allowed
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'message' => 'Method not allowed. Use GET request.'
    ]);
}

// Close the database connection
$conn->close();
?>