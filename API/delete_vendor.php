<?php
// Include database connection
require_once 'db.php';

// Set headers for JSON response
header('Content-Type: application/json');

// Function to delete a vendor
function delete_vendor($conn, $supplier_id, $store_id, $user_id) {
    // Validate inputs
    $supplier_id = (int)$supplier_id;
    $store_id = (int)$store_id;
    $user_id = (int)$user_id;
    
    if ($supplier_id <= 0 || $store_id <= 0) {
        return [
            'success' => false,
            'message' => 'Invalid supplier ID or store ID provided'
        ];
    }
    
    // Begin transaction
    $conn->begin_transaction();
    
    try {
        // Check if vendor exists and belongs to the store
        $check_sql = "SELECT * FROM suppliers WHERE supplier_id = ? AND store_id = ?";
        $check_stmt = $conn->prepare($check_sql);
        $check_stmt->bind_param("ii", $supplier_id, $store_id);
        $check_stmt->execute();
        $result = $check_stmt->get_result();
        
        if ($result->num_rows === 0) {
            throw new Exception("Vendor not found or you do not have permission to delete it");
        }
        
        // Get vendor data for audit log and notification
        $vendor = $result->fetch_assoc();
        $old_values = json_encode($vendor);
        $vendor_name = $vendor['supplier_name'];
        
        // Check if vendor has associated purchase orders
        $check_po_sql = "SELECT COUNT(*) as po_count FROM purchase_orders WHERE supplier_id = ? AND store_id = ?";
        $check_po_stmt = $conn->prepare($check_po_sql);
        $check_po_stmt->bind_param("ii", $supplier_id, $store_id);
        $check_po_stmt->execute();
        $po_result = $check_po_stmt->get_result()->fetch_assoc();
        
        if ($po_result['po_count'] > 0) {
            throw new Exception("Cannot delete vendor with associated purchase orders. Please delete the purchase orders first or mark the vendor as inactive.");
        }
        
        // Check if vendor has associated product suppliers
        $check_ps_sql = "SELECT COUNT(*) as ps_count FROM product_suppliers WHERE supplier_id = ? AND store_id = ?";
        $check_ps_stmt = $conn->prepare($check_ps_sql);
        $check_ps_stmt->bind_param("ii", $supplier_id, $store_id);
        $check_ps_stmt->execute();
        $ps_result = $check_ps_stmt->get_result()->fetch_assoc();
        
        if ($ps_result['ps_count'] > 0) {
            throw new Exception("Cannot delete vendor with associated product suppliers. Please remove the product supplier relationships first or mark the vendor as inactive.");
        }
        
        // Delete the vendor
        $delete_sql = "DELETE FROM suppliers WHERE supplier_id = ? AND store_id = ?";
        $delete_stmt = $conn->prepare($delete_sql);
        $delete_stmt->bind_param("ii", $supplier_id, $store_id);
        $delete_stmt->execute();
        
        if ($delete_stmt->affected_rows === 0) {
            throw new Exception("Failed to delete vendor");
        }
        
        // Add audit log entry
        $audit_sql = "INSERT INTO audit_log (table_name, record_id, action, old_values, changed_by, store_id) 
                      VALUES (?, ?, ?, ?, ?, ?)";
        
        $table_name = 'suppliers';
        $action = 'DELETE';
        
        $audit_stmt = $conn->prepare($audit_sql);
        $audit_stmt->bind_param("sissii", $table_name, $supplier_id, $action, $old_values, $user_id, $store_id);
        $audit_stmt->execute();
        
        // Create notification for vendor deletion
        $notification_title = "Vendor Deleted";
        $notification_message = "Vendor '{$vendor_name}' has been deleted from your store";
        $notification_type = "vendor";
        $notification_status = "unread";
        
        $notification_sql = "INSERT INTO notifications (store_id, title, message, type, status) 
                             VALUES (?, ?, ?, ?, ?)";
        
        $notification_stmt = $conn->prepare($notification_sql);
        $notification_stmt->bind_param("issss", $store_id, $notification_title, $notification_message, $notification_type, $notification_status);
        $notification_stmt->execute();
        
        // Commit transaction
        $conn->commit();
        
        return [
            'success' => true,
            'message' => 'Vendor deleted successfully',
            'deleted_vendor' => $vendor_name
        ];
    } catch (Exception $e) {
        // Rollback transaction on error
        $conn->rollback();
        
        return [
            'success' => false,
            'message' => 'Error deleting vendor: ' . $e->getMessage()
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
    
    // Validate required parameters
    if (!isset($input['supplier_id']) || !isset($input['store_id'])) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Missing required parameters: supplier_id, store_id'
        ]);
        exit;
    }
    
    $supplier_id = (int)$input['supplier_id'];
    $store_id = (int)$input['store_id'];
    $user_id = isset($input['user_id']) ? (int)$input['user_id'] : 0;
    
    $response = delete_vendor($conn, $supplier_id, $store_id, $user_id);
    
    // Set appropriate HTTP status code
    if (!$response['success']) {
        http_response_code(400);
    }
    
    echo json_encode($response);
} else if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    // Handle GET request (for browser testing)
    if (isset($_GET['supplier_id']) && isset($_GET['store_id'])) {
        $supplier_id = (int)$_GET['supplier_id'];
        $store_id = (int)$_GET['store_id'];
        $user_id = isset($_GET['user_id']) ? (int)$_GET['user_id'] : 0;
        
        $response = delete_vendor($conn, $supplier_id, $store_id, $user_id);
        
        // Set appropriate HTTP status code
        if (!$response['success']) {
            http_response_code(400);
        }
        
        echo json_encode($response);
    } else {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Missing required parameters: supplier_id, store_id'
        ]);
    }
} else {
    // Method not allowed
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'message' => 'Method not allowed. Use POST or GET request.'
    ]);
}

// Close the database connection
$conn->close();
?>