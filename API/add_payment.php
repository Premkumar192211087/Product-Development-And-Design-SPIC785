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
$required_fields = ['store_id', 'vendor_id', 'payment_date', 'payment_method', 'amount'];
foreach ($required_fields as $field) {
    if (!isset($data[$field]) || empty($data[$field])) {
        echo json_encode([
            "success" => false,
            "message" => "Missing required field: $field"
        ]);
        exit();
    }
}

// Extract and validate data
$store_id = intval($data['store_id']);
$vendor_id = intval($data['vendor_id']);
$payment_date = $data['payment_date'];
$payment_method = $data['payment_method'];
$reference = isset($data['reference']) ? $data['reference'] : '';
$amount = floatval($data['amount']);
$bill_id = isset($data['bill_id']) && !empty($data['bill_id']) ? intval($data['bill_id']) : null;
$notes = isset($data['notes']) ? $data['notes'] : '';

// Validate store_id and vendor_id
if ($store_id <= 0 || $vendor_id <= 0) {
    echo json_encode([
        "success" => false,
        "message" => "Invalid store_id or vendor_id"
    ]);
    exit();
}

// Validate amount
if ($amount <= 0) {
    echo json_encode([
        "success" => false,
        "message" => "Amount must be greater than zero"
    ]);
    exit();
}

// Generate a unique payment number
$year = date('Y');
$month = date('m');
$day = date('d');

// Get the last payment number for this store
$sql = "SELECT payment_number FROM payments_made WHERE store_id = ? ORDER BY payment_id DESC LIMIT 1";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $store_id);
$stmt->execute();
$result = $stmt->get_result();

$next_number = 1;
if ($row = $result->fetch_assoc()) {
    // Extract the numeric part of the last payment number
    $last_number = $row['payment_number'];
    $matches = [];
    if (preg_match('/PAY-(\d+)-(\d+)-(\d+)-(\d+)/', $last_number, $matches)) {
        $last_year = $matches[1];
        $last_month = $matches[2];
        $last_day = $matches[3];
        $last_seq = $matches[4];
        
        // If same day, increment sequence
        if ($last_year == $year && $last_month == $month && $last_day == $day) {
            $next_number = intval($last_seq) + 1;
        }
    }
}

// Format the new payment number
$payment_number = sprintf("PAY-%04d-%02d-%02d-%04d", $year, $month, $day, $next_number);

// Start transaction
$conn->begin_transaction();

try {
    // Insert the payment
    $sql = "INSERT INTO payments_made (payment_number, store_id, vendor_id, payment_date, payment_method, 
            reference, amount, bill_id, notes, created_at, updated_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
    
    $stmt = $conn->prepare($sql);
    
    if (!$stmt) {
        throw new Exception("Query preparation failed: " . $conn->error);
    }
    
    // Bind parameters
    $stmt->bind_param("siissdis", 
        $payment_number, 
        $store_id, 
        $vendor_id, 
        $payment_date, 
        $payment_method, 
        $reference, 
        $amount, 
        $bill_id, 
        $notes
    );
    
    // Execute the statement
    if (!$stmt->execute()) {
        throw new Exception("Payment insertion failed: " . $stmt->error);
    }
    
    $payment_id = $conn->insert_id;
    
    // If a bill is associated, update its balance_due
    if ($bill_id !== null) {
        // Get current bill details
        $sql = "SELECT total, balance_due FROM bills WHERE bill_id = ? AND store_id = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("ii", $bill_id, $store_id);
        $stmt->execute();
        $result = $stmt->get_result();
        
        if ($result->num_rows === 0) {
            throw new Exception("Bill not found or does not belong to the specified store");
        }
        
        $bill = $result->fetch_assoc();
        $current_balance = floatval($bill['balance_due']);
        
        // Calculate new balance
        $new_balance = $current_balance - $amount;
        if ($new_balance < 0) {
            $new_balance = 0; // Prevent negative balance
        }
        
        // Determine bill status based on new balance
        $status = ($new_balance <= 0) ? 'paid' : 'partially_paid';
        
        // Update bill
        $sql = "UPDATE bills SET balance_due = ?, status = ?, updated_at = NOW() WHERE bill_id = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("dsi", $new_balance, $status, $bill_id);
        
        if (!$stmt->execute()) {
            throw new Exception("Bill update failed: " . $stmt->error);
        }
    }
    
    // Commit the transaction
    $conn->commit();
    
    // Return success response
    echo json_encode([
        "success" => true,
        "message" => "Payment added successfully",
        "payment_id" => $payment_id,
        "payment_number" => $payment_number
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