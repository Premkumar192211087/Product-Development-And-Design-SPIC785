<?php
// Include database connection
include 'db_connect.php';

// Set headers for JSON response
header('Content-Type: application/json');

// Check if the request method is POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Get parameters from POST request
    $store_id = isset($_POST['store_id']) ? $_POST['store_id'] : null;
    $from_date = isset($_POST['from_date']) ? $_POST['from_date'] : null;
    $to_date = isset($_POST['to_date']) ? $_POST['to_date'] : null;

    // Validate required parameters
    if (!$store_id || !$from_date || !$to_date) {
        echo json_encode([
            'status' => 'error',
            'message' => 'Missing required parameters'
        ]);
        exit;
    }

    try {
        // Initialize response array
        $response = [
            'status' => 'success',
            'purchase_summary' => [],
            'vendor_distribution' => [],
            'purchase_status' => [],
            'recent_purchases' => []
        ];

        // 1. Get purchase summary (total amount and count)
        $summary_query = "SELECT 
                            COUNT(po_id) as total_orders, 
                            COALESCE(SUM(total_amount), 0) as total_purchase_amount 
                          FROM purchase_orders 
                          WHERE store_id = ? 
                            AND order_date BETWEEN ? AND ?";
        
        $stmt = $conn->prepare($summary_query);
        $stmt->bind_param("iss", $store_id, $from_date, $to_date);
        $stmt->execute();
        $result = $stmt->get_result();
        $summary = $result->fetch_assoc();
        
        $response['purchase_summary'] = [
            'total_orders' => (int)$summary['total_orders'],
            'total_purchase_amount' => (float)$summary['total_purchase_amount']
        ];

        // 2. Get vendor distribution
        $vendor_query = "SELECT 
                            v.vendor_name, 
                            COALESCE(SUM(po.total_amount), 0) as amount 
                         FROM purchase_orders po 
                         JOIN vendors v ON po.vendor_id = v.vendor_id 
                         WHERE po.store_id = ? 
                           AND po.order_date BETWEEN ? AND ? 
                         GROUP BY po.vendor_id 
                         ORDER BY amount DESC 
                         LIMIT 5";
        
        $stmt = $conn->prepare($vendor_query);
        $stmt->bind_param("iss", $store_id, $from_date, $to_date);
        $stmt->execute();
        $result = $stmt->get_result();
        
        $total_amount = $summary['total_purchase_amount'] > 0 ? $summary['total_purchase_amount'] : 1; // Avoid division by zero
        $vendor_distribution = [];
        
        while ($row = $result->fetch_assoc()) {
            $percentage = ($row['amount'] / $total_amount) * 100;
            $vendor_distribution[] = [
                'vendor_name' => $row['vendor_name'],
                'amount' => (float)$row['amount'],
                'percentage' => (float)$percentage
            ];
        }
        
        $response['vendor_distribution'] = $vendor_distribution;

        // 3. Get purchase status distribution
        $status_query = "SELECT 
                            status, 
                            COUNT(po_id) as count 
                         FROM purchase_orders 
                         WHERE store_id = ? 
                           AND order_date BETWEEN ? AND ? 
                         GROUP BY status";
        
        $stmt = $conn->prepare($status_query);
        $stmt->bind_param("iss", $store_id, $from_date, $to_date);
        $stmt->execute();
        $result = $stmt->get_result();
        
        $total_orders = $summary['total_orders'] > 0 ? $summary['total_orders'] : 1; // Avoid division by zero
        $purchase_status = [];
        
        while ($row = $result->fetch_assoc()) {
            $percentage = ($row['count'] / $total_orders) * 100;
            $purchase_status[] = [
                'status' => $row['status'],
                'count' => (int)$row['count'],
                'percentage' => (float)$percentage
            ];
        }
        
        $response['purchase_status'] = $purchase_status;

        // 4. Get recent purchase orders
        $recent_query = "SELECT 
                            po.po_id, 
                            v.vendor_name, 
                            po.order_date, 
                            po.total_amount, 
                            po.status 
                         FROM purchase_orders po 
                         JOIN vendors v ON po.vendor_id = v.vendor_id 
                         WHERE po.store_id = ? 
                           AND po.order_date BETWEEN ? AND ? 
                         ORDER BY po.order_date DESC 
                         LIMIT 10";
        
        $stmt = $conn->prepare($recent_query);
        $stmt->bind_param("iss", $store_id, $from_date, $to_date);
        $stmt->execute();
        $result = $stmt->get_result();
        
        $recent_purchases = [];
        
        while ($row = $result->fetch_assoc()) {
            // Get purchase items for this order
            $items_query = "SELECT 
                                p.product_name, 
                                poi.quantity_ordered as quantity, 
                                poi.unit_price 
                             FROM purchase_order_items poi 
                             JOIN products p ON poi.product_id = p.product_id 
                             WHERE poi.po_id = ? 
                             LIMIT 5";
            
            $items_stmt = $conn->prepare($items_query);
            $items_stmt->bind_param("i", $row['po_id']);
            $items_stmt->execute();
            $items_result = $items_stmt->get_result();
            
            $items = [];
            while ($item = $items_result->fetch_assoc()) {
                $items[] = [
                    'product_name' => $item['product_name'],
                    'quantity' => (int)$item['quantity'],
                    'unit_price' => (float)$item['unit_price']
                ];
            }
            
            $recent_purchases[] = [
                'po_id' => $row['po_id'],
                'vendor_name' => $row['vendor_name'],
                'order_date' => $row['order_date'],
                'total_amount' => (float)$row['total_amount'],
                'status' => $row['status'],
                'items' => $items
            ];
        }
        
        $response['recent_purchases'] = $recent_purchases;

        // Return the response as JSON
        echo json_encode($response);

    } catch (Exception $e) {
        echo json_encode([
            'status' => 'error',
            'message' => 'Database error: ' . $e->getMessage()
        ]);
    }
} else {
    // If not a POST request
    echo json_encode([
        'status' => 'error',
        'message' => 'Invalid request method'
    ]);
}
?>