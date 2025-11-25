<?php
// Include database connection
require_once 'db.php';

// Set headers for JSON response
header('Content-Type: application/json');

// Function to get bill details
function get_bill_details($conn, $bill_id, $store_id) {
    // Validate inputs
    $bill_id = (int)$bill_id;
    $store_id = (int)$store_id;
    
    if ($bill_id <= 0 || $store_id <= 0) {
        return [
            'success' => false,
            'message' => 'Invalid bill ID or store ID provided'
        ];
    }
    
    try {
        // Get bill details
        $sql = "SELECT b.*, s.supplier_name, s.contact_person, s.email, s.phone, s.address, 
                po.po_number, po.order_date, po.expected_delivery_date 
                FROM bills b 
                LEFT JOIN suppliers s ON b.supplier_id = s.supplier_id 
                LEFT JOIN purchase_orders po ON b.po_id = po.po_id 
                WHERE b.bill_id = ? AND b.store_id = ?";
        
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("ii", $bill_id, $store_id);
        $stmt->execute();
        $result = $stmt->get_result();
        
        if ($result->num_rows === 0) {
            return [
                'success' => false,
                'message' => 'Bill not found or you do not have permission to view it'
            ];
        }
        
        $bill = $result->fetch_assoc();
        
        // Get bill items
        $items_sql = "SELECT bi.*, p.product_name, p.sku 
                      FROM bill_items bi 
                      JOIN products p ON bi.product_id = p.id 
                      WHERE bi.bill_id = ? AND bi.store_id = ?";
        
        $items_stmt = $conn->prepare($items_sql);
        $items_stmt->bind_param("ii", $bill_id, $store_id);
        $items_stmt->execute();
        $items_result = $items_stmt->get_result();
        
        $items = [];
        while ($item = $items_result->fetch_assoc()) {
            $items[] = $item;
        }
        
        // Get bill payments
        $payments_sql = "SELECT bp.*, pm.payment_method_name 
                         FROM bill_payments bp 
                         LEFT JOIN payment_methods pm ON bp.payment_method_id = pm.payment_method_id 
                         WHERE bp.bill_id = ? AND bp.store_id = ? 
                         ORDER BY bp.payment_date DESC";
        
        $payments_stmt = $conn->prepare($payments_sql);
        $payments_stmt->bind_param("ii", $bill_id, $store_id);
        $payments_stmt->execute();
        $payments_result = $payments_stmt->get_result();
        
        $payments = [];
        $total_paid = 0;
        while ($payment = $payments_result->fetch_assoc()) {
            $payments[] = $payment;
            $total_paid += $payment['amount'];
        }
        
        // Calculate remaining amount
        $bill['paid_amount'] = $total_paid;
        $bill['remaining_amount'] = $bill['total_amount'] - $total_paid;
        
        // Determine payment status
        if ($bill['remaining_amount'] <= 0) {
            $bill['payment_status'] = 'paid';
        } else if ($bill['paid_amount'] > 0) {
            $bill['payment_status'] = 'partial';
        } else {
            $bill['payment_status'] = 'unpaid';
        }
        
        return [
            'success' => true,
            'bill' => $bill,
            'items' => $items,
            'payments' => $payments
        ];
    } catch (Exception $e) {
        return [
            'success' => false,
            'message' => 'Error retrieving bill details: ' . $e->getMessage()
        ];
    }
}

// Handle the request
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    // Check if required parameters are provided
    if (isset($_GET['bill_id']) && isset($_GET['store_id'])) {
        $bill_id = (int)$_GET['bill_id'];
        $store_id = (int)$_GET['store_id'];
        
        $response = get_bill_details($conn, $bill_id, $store_id);
        
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
            'message' => 'Missing required parameters: bill_id, store_id'
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