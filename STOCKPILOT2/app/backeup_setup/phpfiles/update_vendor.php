<?php
// Include database connection
require_once 'db.php';

// Set headers for JSON response
header('Content-Type: application/json');

// Function to update an existing vendor
function update_vendor($conn, $data) {
    // Validate required fields
    if (!isset($data['supplier_id']) || !isset($data['store_id'])) {
        return [
            'success' => false,
            'message' => 'Missing required fields: supplier_id, store_id'
        ];
    }
    
    // Sanitize inputs
    $supplier_id = (int)$data['supplier_id'];
    $store_id = (int)$data['store_id'];
    
    // Check if vendor exists and belongs to the store
    $check_sql = "SELECT * FROM suppliers WHERE supplier_id = ? AND store_id = ?";
    $check_stmt = $conn->prepare($check_sql);
    $check_stmt->bind_param("ii", $supplier_id, $store_id);
    $check_stmt->execute();
    $result = $check_stmt->get_result();
    
    if ($result->num_rows === 0) {
        return [
            'success' => false,
            'message' => 'Vendor not found or you do not have permission to update it'
        ];
    }
    
    // Get current vendor data for audit log
    $current_vendor = $result->fetch_assoc();
    $old_values = json_encode($current_vendor);
    
    // Begin transaction
    $conn->begin_transaction();
    
    try {
        // Build update query dynamically based on provided fields
        $update_fields = [];
        $types = "";
        $params = [];
        
        // Fields that can be updated
        $allowed_fields = [
            'supplier_name' => 's',
            'contact_person' => 's',
            'email' => 's',
            'phone' => 's',
            'address' => 's',
            'payment_terms' => 's',
            'tax_id' => 's',
            'notes' => 's',
            'status' => 's'
        ];
        
        foreach ($allowed_fields as $field => $type) {
            if (isset($data[$field])) {
                $update_fields[] = "{$field} = ?";
                $types .= $type;
                $params[] = $data[$field];
            }
        }
        
        // If no fields to update
        if (empty($update_fields)) {
            return [
                'success' => false,
                'message' => 'No fields to update provided'
            ];
        }
        
        // Add supplier_id and store_id to parameters
        $types .= "ii";
        $params[] = $supplier_id;
        $params[] = $store_id;
        
        // Prepare update query
        $sql = "UPDATE suppliers SET " . implode(", ", $update_fields) . " WHERE supplier_id = ? AND store_id = ?";
        
        $stmt = $conn->prepare($sql);
        
        // Bind parameters dynamically
        $bind_params = array($types);
        foreach ($params as $key => $value) {
            $bind_params[] = &$params[$key];
        }
        call_user_func_array(array($stmt, 'bind_param'), $bind_params);
        
        $stmt->execute();
        
        // Get updated vendor data for audit log
        $check_stmt->execute();
        $updated_vendor = $check_stmt->get_result()->fetch_assoc();
        $new_values = json_encode($updated_vendor);
        
        // Add audit log entry
        $audit_sql = "INSERT INTO audit_log (table_name, record_id, action, old_values, new_values, changed_by, store_id) 
                      VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        $table_name = 'suppliers';
        $action = 'UPDATE';
        $user_id = isset($data['user_id']) ? (int)$data['user_id'] : 0;
        
        $audit_stmt = $conn->prepare($audit_sql);
        $audit_stmt->bind_param("sisssii", $table_name, $supplier_id, $action, $old_values, $new_values, $user_id, $store_id);
        $audit_stmt->execute();
        
        // Commit transaction
        $conn->commit();
        
        return [
            'success' => true,
            'message' => 'Vendor updated successfully',
            'vendor' => $updated_vendor
        ];
    } catch (Exception $e) {
        // Rollback transaction on error
        $conn->rollback();
        
        return [
            'success' => false,
            'message' => 'Error updating vendor: ' . $e->getMessage()
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
    
    $response = update_vendor($conn, $input);
    
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