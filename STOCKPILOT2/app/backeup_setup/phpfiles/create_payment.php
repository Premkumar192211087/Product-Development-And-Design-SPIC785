<?php
// Include database connection
require_once 'db.php';

// Set headers for JSON response
header('Content-Type: application/json');

// Function to create a new payment
function create_payment($conn, $data) {
    // Validate required fields
    if (!isset($data['store_id']) || !isset($data['bill_id']) || !isset($data['amount']) || 
        !isset($data['payment_date']) || !isset($data['payment_method_id']) || !isset($data['created_by'])) {
        return [
            'success' => false,
            'message' => 'Missing required fields: store_id, bill_id, amount, payment_date, payment_method_id, created_by'
        ];
    }
    
    // Sanitize inputs
    $store_id = (int)$data['store_id'];
    $bill_id = (int)$data['bill_id'];
    $amount = (float)$data['amount'];
    $payment_date = $conn->real_escape_string($data['payment_date']);
    $payment_method_id = (int)$data['payment_method_id'];
    $created_by = (int)$data['created_by'];
    $reference = isset($data['reference']) ? $conn->real_escape_string($data['reference']) : null;
    $notes = isset($data['notes']) ? $conn->real_escape_string($data['notes']) : null;
    
    // Validate amount
    if ($amount <= 0) {
        return [
            'success' => false,
            'message' => 'Payment amount must be greater than zero'
        ];
    }
    
    // Begin transaction
    $conn->begin_transaction();
    
    try {
        // Check if bill exists and get bill details
        $check_bill_sql = "SELECT b.*, s.supplier_name, 
                           (SELECT SUM(amount) FROM bill_payments WHERE bill_id = b.bill_id) as paid_amount 
                           FROM bills b 
                           JOIN suppliers s ON b.supplier_id = s.supplier_id 
                           WHERE b.bill_id = ? AND b.store_id = ?";
        $check_bill_stmt = $conn->prepare($check_bill_sql);
        $check_bill_stmt->bind_param("ii", $bill_id, $store_id);
        $check_bill_stmt->execute();
        $bill_result = $check_bill_stmt->get_result();
        
        if ($bill_result->num_rows === 0) {
            return [
                'success' => false,
                'message' => 'Bill not found or you do not have permission to add payment'
            ];
        }
        
        $bill = $bill_result->fetch_assoc();
        $paid_amount = $bill['paid_amount'] ?? 0;
        $remaining_amount = $bill['total_amount'] - $paid_amount;
        
        // Check if payment amount exceeds remaining amount
        if ($amount > $remaining_amount) {
            return [
                'success' => false,
                'message' => 'Payment amount exceeds remaining bill amount. Maximum allowed: ' . $remaining_amount
            ];
        }
        
        // Check if payment method exists
        $check_payment_method_sql = "SELECT * FROM payment_methods WHERE payment_method_id = ?";
        $check_payment_method_stmt = $conn->prepare($check_payment_method_sql);
        $check_payment_method_stmt->bind_param("i", $payment_method_id);
        $check_payment_method_stmt->execute();
        $payment_method_result = $check_payment_method_stmt->get_result();
        
        if ($payment_method_result->num_rows === 0) {
            return [
                'success' => false,
                'message' => 'Invalid payment method'
            ];
        }
        
        $payment_method = $payment_method_result->fetch_assoc();
        
        // Insert payment
        $sql = "INSERT INTO bill_payments (store_id, bill_id, amount, payment_date, payment_method_id, 
                                         reference, notes, created_by, created_at) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("iidsiss", $store_id, $bill_id, $amount, $payment_date, $payment_method_id, 
                         $reference, $notes, $created_by);
        $stmt->execute();
        
        if ($stmt->affected_rows === 0) {
            throw new Exception("Failed to create payment");
        }
        
        $payment_id = $stmt->insert_id;
        
        // Update bill status if fully paid
        $new_paid_amount = $paid_amount + $amount;
        $new_status = null;
        
        if ($new_paid_amount >= $bill['total_amount']) {
            $new_status = 'paid';
        } else if ($new_paid_amount > 0) {
            $new_status = 'partial';
        }
        
        if ($new_status) {
            $update_bill_sql = "UPDATE bills SET status = ? WHERE bill_id = ? AND store_id = ?";
            $update_bill_stmt = $conn->prepare($update_bill_sql);
            $update_bill_stmt->bind_param("sii", $new_status, $bill_id, $store_id);
            $update_bill_stmt->execute();
        }
        
        // Create notification for new payment
        $notification_title = "New Payment Added";
        $notification_message = "A payment of {$amount} has been added for bill {$bill['bill_number']} ({$bill['supplier_name']})";
        $notification_type = "payment";
        $notification_status = "unread";
        
        $notification_sql = "INSERT INTO notifications (store_id, title, message, type, status) 
                             VALUES (?, ?, ?, ?, ?)";
        
        $notification_stmt = $conn->prepare($notification_sql);
        $notification_stmt->bind_param("issss", $store_id, $notification_title, $notification_message, 
                                      $notification_type, $notification_status);
        $notification_stmt->execute();
        
        // Log the payment in audit log
        $log_sql = "INSERT INTO audit_log (store_id, user_id, action, entity_type, entity_id, details, created_at) 
                    VALUES (?, ?, 'create', 'payment', ?, ?, NOW())";
        
        $details = json_encode([
            'bill_id' => $bill_id,
            'bill_number' => $bill['bill_number'],
            'amount' => $amount,
            'payment_method' => $payment_method['payment_method_name'],
            'payment_date' => $payment_date
        ]);
        
        $log_stmt = $conn->prepare($log_sql);
        $log_stmt->bind_param("iiss", $store_id, $created_by, $payment_id, $details);
        $log_stmt->execute();
        
        // Commit transaction
        $conn->commit();
        
        return [
            'success' => true,
            'message' => 'Payment created successfully',
            'payment_id' => $payment_id,
            'bill_status' => $new_status ?? $bill['status'],
            'remaining_amount' => $bill['total_amount'] - $new_paid_amount
        ];
    } catch (Exception $e) {
        // Rollback transaction on error
        $conn->rollback();
        
        return [
            'success' => false,
            'message' => 'Error creating payment: ' . $e->getMessage()
        ];
    }
}

// Handle the request
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Get JSON input
    $input = json_decode(file_get_contents('php://input'), true);
    
    if ($input === null) {
        // Try to get form data if JSON is not provided
        $input = $_POST;
    }
    
    $response = create_payment($conn, $input);
    
    // Set appropriate HTTP status code
    if (!$response['success']) {
        http_response_code(400);
    }
    
    echo json_encode($response);
} else {
    // Method not allowed
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'message' => 'Method not allowed. Use POST request.'
    ]);
}

// Close the database connection
$conn->close();
?>