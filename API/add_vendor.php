<?php
// Include database connection
require_once 'db_connect.php';

// Set headers
header('Content-Type: application/json');

// Check if required fields are provided
if (!isset($_POST['store_id']) || empty($_POST['store_id']) ||
    !isset($_POST['name']) || empty($_POST['name']) ||
    !isset($_POST['email']) || empty($_POST['email']) ||
    !isset($_POST['phone']) || empty($_POST['phone'])) {
    echo json_encode(array('error' => true, 'message' => 'Required fields are missing'));
    exit;
}

$store_id = $_POST['store_id'];
$name = $_POST['name'];
$email = $_POST['email'];
$phone = $_POST['phone'];
$address = isset($_POST['address']) ? $_POST['address'] : '';
$contact_person = isset($_POST['contact_person']) ? $_POST['contact_person'] : '';
$status = 'active';
$created_at = date('Y-m-d H:i:s');

try {
    // Prepare and execute query
    $stmt = $conn->prepare("INSERT INTO vendors (store_id, name, email, phone, address, contact_person, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
    $stmt->bind_param("ssssssss", $store_id, $name, $email, $phone, $address, $contact_person, $status, $created_at);
    $result = $stmt->execute();
    
    if ($result) {
        $vendor_id = $stmt->insert_id;
        echo json_encode(array('error' => false, 'message' => 'Vendor added successfully', 'vendor_id' => $vendor_id));
    } else {
        echo json_encode(array('error' => true, 'message' => 'Failed to add vendor'));
    }
    
} catch (Exception $e) {
    echo json_encode(array('error' => true, 'message' => 'Database error: ' . $e->getMessage()));
}

// Close connection
$stmt->close();
$conn->close();
?>