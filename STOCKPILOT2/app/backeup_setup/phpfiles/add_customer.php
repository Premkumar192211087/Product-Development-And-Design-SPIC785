<?php
require 'db.php'; // your DB connection file
header('Content-Type: application/json');

$response = ["status" => "error", "message" => "Invalid request"];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Get POST data and sanitize
    $store_id        = isset($_POST['store_id']) ? intval($_POST['store_id']) : null;
    $customer_name   = trim($_POST['customer_name'] ?? '');
    $email           = trim($_POST['email'] ?? '');
    $phone           = trim($_POST['phone'] ?? '');
    $address         = trim($_POST['address'] ?? '');
    $date_of_birth   = trim($_POST['date_of_birth'] ?? '');
    $loyalty_points  = intval($_POST['loyalty_points'] ?? 0);
    $status          = trim($_POST['status'] ?? 'active');
    $notes           = trim($_POST['notes'] ?? '');
    
    // Convert empty strings to NULL for optional fields
    $email = empty($email) ? null : $email;
    $phone = empty($phone) ? null : $phone;
    $address = empty($address) ? null : $address;
    $date_of_birth = empty($date_of_birth) ? null : $date_of_birth;
    $notes = empty($notes) ? null : $notes;
    
    // Validation
    if (!$store_id || empty($customer_name)) {
        $response['message'] = "Store ID and Customer Name are required.";
        echo json_encode($response);
        exit;
    }
    
    // Validate email format if provided
    if ($email && !filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $response['message'] = "Invalid email format.";
        echo json_encode($response);
        exit;
    }
    
    // Validate date format if provided
    if ($date_of_birth && !preg_match('/^\d{4}-\d{2}-\d{2}$/', $date_of_birth)) {
        $response['message'] = "Invalid date format. Use YYYY-MM-DD.";
        echo json_encode($response);
        exit;
    }
    
    try {
        // Insert into `customers` table
        $stmt = $conn->prepare("
            INSERT INTO customers 
            (store_id, customer_name, email, phone, address, date_of_birth, loyalty_points, status, notes) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ");
        
        $stmt->bind_param(
            "isssssiss",
            $store_id,
            $customer_name,
            $email,
            $phone,
            $address,
            $date_of_birth,
            $loyalty_points,
            $status,
            $notes
        );
        
        if ($stmt->execute()) {
            $customer_id = $stmt->insert_id;
            $avatar_letter = strtoupper(substr($customer_name, 0, 1));
            
            $response = [
                "status"        => "success",
                "message"       => "Customer added successfully",
                "customer_id"   => $customer_id,
                "avatar_letter" => $avatar_letter
            ];
        } else {
            $response['message'] = "Database error: " . $stmt->error;
        }
        
        $stmt->close();
        
    } catch (Exception $e) {
        $response['message'] = "Error: " . $e->getMessage();
    }
}

echo json_encode($response);
?>