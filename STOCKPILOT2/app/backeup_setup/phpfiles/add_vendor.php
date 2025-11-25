<?php
// Include database connection
require_once 'db.php';

// Set headers for JSON response
header('Content-Type: application/json');

// Function to add a new vendor
function add_vendor($conn, $data) {
    // Validate required fields
    if (!isset($data['store_id']) || !isset($data['supplier_name']) || !isset($data['contact_person'])) {
        return [
            'success' => false,
            'message' => 'Missing required fields: store_id, supplier_name, contact_person'
        ];
    }
    
    // Sanitize inputs
    $store_id = (int)$data['store_id'];
    $supplier_name = $conn->real_escape_string($data['supplier_name']);
    $contact_person = $conn->real_escape_string($data['contact_person']);
    $email = isset($data['email']) ? $conn->real_escape_string($data['email']) : null;
    $phone = isset($data['phone']) ? $conn->real_escape_string($data['phone']) : null;
    $address = isset($data['address']) ? $conn->real_escape_string($data['address']) : null;
    $payment_terms = isset($data['payment_terms']) ? $conn->real_escape_string($data['payment_terms']) : null;
    $tax_id = isset($data['tax_id']) ? $conn->real_escape_string($data['tax_id']) : null;
    $notes = isset($data['notes']) ? $conn->real_escape_string($data['notes']) : null;
    
    // Begin transaction
    $conn->begin_transaction();
    
    try {
        // Insert new vendor
        $sql = "INSERT INTO suppliers (store_id, supplier_name, contact_person, email, phone, address, payment_terms, tax_id, notes) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("issssssss", $store_id, $supplier_name, $contact_person, $email, $phone, $address, $payment_terms, $tax_id, $notes);
        $stmt->execute();
        
        if ($stmt->affected_rows > 0) {
            $vendor_id = $stmt->insert_id;
            
            // Create notification for new vendor
            $notification_title = "New Vendor Added";
            $notification_message = "New vendor '{$supplier_name}' has been added to your store";
            $notification_type = "vendor";
            $notification_status = "unread";
            
            $sql = "INSERT INTO notifications (store_id, title, message, type, status) 
                    VALUES (?, ?, ?, ?, ?)";
            
            $stmt = $conn->prepare($sql);
            $stmt->bind_param("issss", $store_id, $notification_title, $notification_message, $notification_type, $notification_status);
            $stmt->execute();
            
            // Commit transaction
            $conn->commit();
            
            return [
                'success' => true,
                'message' => 'Vendor added successfully',
                'vendor_id' => $vendor_id,
                'vendor_name' => $supplier_name
            ];
        } else {
            // Rollback transaction
            $conn->rollback();
            
            return [
                'success' => false,
                'message' => 'Failed to add vendor'
            ];
        }
    } catch (Exception $e) {
        // Rollback transaction on error
        $conn->rollback();
        
        return [
            'success' => false,
            'message' => 'Error adding vendor: ' . $e->getMessage()
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
    
    $response = add_vendor($conn, $input);
    
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