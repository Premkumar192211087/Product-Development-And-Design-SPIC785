<?php
// Set content type to JSON
header('Content-Type: application/json');

// Enable error reporting for debugging
ini_set('display_errors', 1);
error_reporting(E_ALL);

// Database connection parameters
$host = "localhost";
$user = "root";
$password = "";
$database = "inventory_management";

// Create database connection
$conn = new mysqli($host, $user, $password, $database);

// Check connection
if ($conn->connect_error) {
    echo json_encode([
        "success" => false,
        "message" => "Database connection failed: " . $conn->connect_error
    ]);
    exit();
}

// Check if the request method is POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode([
        "success" => false,
        "message" => "Invalid request method. Only POST is allowed."
    ]);
    exit();
}

// Get JSON data from the request body
$json_data = file_get_contents('php://input');
$data = json_decode($json_data, true);

// Check if JSON data is valid
if ($data === null) {
    echo json_encode([
        "success" => false,
        "message" => "Invalid JSON data"
    ]);
    exit();
}

// Check if required fields are present
if (!isset($data['payment_id']) || !isset($data['store_id'])) {
    echo json_encode([
        "success" => false,
        "message" => "Missing required fields: payment_id and store_id"
    ]);
    exit();
}

// Extract and validate data
$payment_id = intval($data['payment_id']);
$store_id = intval($data['store_id']);

// Validate parameters
if ($payment_id <= 0 || $store_id <= 0) {
    echo json_encode([
        "success" => false,
        "message" => "Invalid payment_id or store_id"
    ]);
    exit();
}

// Start transaction
$conn->begin_transaction();

try {
    // First, get payment details to check if it exists and belongs to the store
    // Also check if it's associated with a bill that needs to be updated
    $sql = "SELECT p.*, b.bill_id, b.balance_due, b.total, b.status 
            FROM payments_made p
            LEFT JOIN bills b ON p.bill_id = b.bill_id
            WHERE p.payment_id = ? AND p.store_id = ?";
    
    $stmt = $conn->prepare($sql);
    
    if (!$stmt) {
        throw new Exception("Query preparation failed: " . $conn->error);
    }
    
    // Bind parameters
    $stmt->bind_param("ii", $payment_id, $store_id);
    
    // Execute the statement
    if (!$stmt->execute()) {
        throw new Exception("Query execution failed: " . $stmt->error);
    }
    
    $result = $stmt->get_result();
    
    // Check if payment exists and belongs to the store
    if ($result->num_rows === 0) {
        throw new Exception("Payment not found or does not belong to the specified store");
    }
    
    $payment = $result->fetch_assoc();
    $bill_id = $payment['bill_id'];
    $payment_amount = floatval($payment['amount']);
    
    // If payment is associated with a bill, update the bill's balance_due and status
    if ($bill_id !== null) {
        $current_balance_due = floatval($payment['balance_due']);
        $bill_total = floatval($payment['total']);
        
        // Calculate new balance due by adding back the payment amount
        $new_balance_due = $current_balance_due + $payment_amount;
        
        // Ensure balance_due doesn't exceed the total
        if ($new_balance_due > $bill_total) {
            $new_balance_due = $bill_total;
        }
        
        // Determine new status based on the new balance due
        $new_status = 'unpaid';
        if ($new_balance_due <= 0) {
            $new_status = 'paid';
        } else if ($new_balance_due < $bill_total) {
            $new_status = 'partially_paid';
        }
        
        // Update the bill
        $sql = "UPDATE bills SET balance_due = ?, status = ?, updated_at = NOW() WHERE bill_id = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("dsi", $new_balance_due, $new_status, $bill_id);
        
        if (!$stmt->execute()) {
            throw new Exception("Bill update failed: " . $stmt->error);
        }
    }
    
    // Delete the payment
    $sql = "DELETE FROM payments_made WHERE payment_id = ? AND store_id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ii", $payment_id, $store_id);
    
    if (!$stmt->execute()) {
        throw new Exception("Payment deletion failed: " . $stmt->error);
    }
    
    // Check if any rows were affected
    if ($stmt->affected_rows === 0) {
        throw new Exception("Payment not found or already deleted");
    }
    
    // Commit the transaction
    $conn->commit();
    
    // Return success response
    echo json_encode([
        "success" => true,
        "message" => "Payment deleted successfully"
    ]);
    
} catch (Exception $e) {
    // Rollback the transaction on error
    $conn->rollback();
    
    echo json_encode([
        "success" => false,
        "message" => $e->getMessage()
    ]);
}

// Close connection
$conn->close();