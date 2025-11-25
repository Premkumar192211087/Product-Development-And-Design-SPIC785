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
        SELECT b.*, v.name as vendor_name 
        FROM bills b 
        LEFT JOIN vendors v ON b.vendor_id = v.id 
        WHERE b.store_id = ? 
        ORDER BY b.created_at DESC
    ");
    $stmt->bind_param("s", $store_id);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $bills = array();
    
    while ($row = $result->fetch_assoc()) {
        $bills[] = array(
            'id' => $row['id'],
            'bill_number' => $row['bill_number'],
            'vendor_id' => $row['vendor_id'],
            'vendor_name' => $row['vendor_name'],
            'amount' => $row['amount'],
            'due_date' => $row['due_date'],
            'status' => $row['status'],
            'notes' => $row['notes'],
            'created_at' => $row['created_at']
        );
    }
    
    echo json_encode(array('error' => false, 'bills' => $bills));
    
} catch (Exception $e) {
    echo json_encode(array('error' => true, 'message' => 'Database error: ' . $e->getMessage()));
}

// Close connection
$stmt->close();
$conn->close();
?>