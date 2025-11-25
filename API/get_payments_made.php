<?php
// Include database connection
require_once 'db_connect.php';

// Set headers
header('Content-Type: application/json');

// Check if store_id is provided
if (!isset($_POST['store_id']) || empty($_POST['store_id'])) {
    echo json_encode(array('error' => true, 'message' => 'Store ID is required'));
    exit;
}

$store_id = $_POST['store_id'];

try {
    // Prepare and execute query
    $stmt = $conn->prepare("
        SELECT p.*, v.name as vendor_name 
        FROM payments_made p 
        LEFT JOIN vendors v ON p.vendor_id = v.id 
        WHERE p.store_id = ? 
        ORDER BY p.payment_date DESC
    ");
    $stmt->bind_param("s", $store_id);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $payments = array();
    
    while ($row = $result->fetch_assoc()) {
        $payments[] = array(
            'id' => $row['id'],
            'payment_number' => $row['payment_number'],
            'vendor_id' => $row['vendor_id'],
            'vendor_name' => $row['vendor_name'],
            'amount' => $row['amount'],
            'payment_date' => $row['payment_date'],
            'payment_method' => $row['payment_method'],
            'reference' => $row['reference'],
            'notes' => $row['notes'],
            'created_at' => $row['created_at']
        );
    }
    
    echo json_encode(array('error' => false, 'payments' => $payments));
    
} catch (Exception $e) {
    echo json_encode(array('error' => true, 'message' => 'Database error: ' . $e->getMessage()));
}

// Close connection
$stmt->close();
$conn->close();
?>