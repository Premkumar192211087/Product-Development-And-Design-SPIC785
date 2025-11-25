<?php
// delete_item.php

// Database configuration
$host = 'localhost';
$dbname = 'your_database_name';
$username = 'your_username';
$password = 'your_password';

// Create MySQLi connection
$conn = new mysqli($host, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Function to delete a product item
function deleteItem($conn, $productId, $storeId, $userId) {
    try {
        // Begin transaction
        $conn->autocommit(FALSE);
        
        // First, get the product details for audit log
        $stmt = $conn->prepare("SELECT * FROM products WHERE id = ? AND store_id = ?");
        $stmt->bind_param("ii", $productId, $storeId);
        $stmt->execute();
        $result = $stmt->get_result();
        $product = $result->fetch_assoc();
        
        if (!$product) {
            throw new Exception("Product not found or you don't have permission to delete it");
        }
        
        // Store old values for audit log
        $oldValues = json_encode($product);
        
        // Delete related records first (foreign key constraints)
        
        // 1. Delete from batch_details
        $stmt = $conn->prepare("DELETE FROM batch_details WHERE product_id = ? AND store_id = ?");
        $stmt->bind_param("ii", $productId, $storeId);
        $stmt->execute();
        
        // 2. Delete from inventory_alerts
        $stmt = $conn->prepare("DELETE FROM inventory_alerts WHERE product_id = ? AND store_id = ?");
        $stmt->bind_param("ii", $productId, $storeId);
        $stmt->execute();
        
        // 3. Delete from product_suppliers
        $stmt = $conn->prepare("DELETE FROM product_suppliers WHERE product_id = ?");
        $stmt->bind_param("i", $productId);
        $stmt->execute();
        
        // 4. Delete from purchase_order_items (if any pending orders)
        $stmt = $conn->prepare("DELETE FROM purchase_order_items WHERE product_id = ?");
        $stmt->bind_param("i", $productId);
        $stmt->execute();
        
        // 5. Delete from sales_movements
        $stmt = $conn->prepare("DELETE FROM sales_movements WHERE product_id = ? AND store_id = ?");
        $stmt->bind_param("ii", $productId, $storeId);
        $stmt->execute();
        
        // 6. Delete from return_items
        $stmt = $conn->prepare("DELETE FROM return_items WHERE product_id = ?");
        $stmt->bind_param("i", $productId);
        $stmt->execute();
        
        // 7. Delete from sales_items
        $stmt = $conn->prepare("DELETE FROM sales_items WHERE product_id = ?");
        $stmt->bind_param("i", $productId);
        $stmt->execute();
        
        // 8. Delete from shipments_items
        $stmt = $conn->prepare("DELETE FROM shipments_items WHERE product_id = ?");
        $stmt->bind_param("i", $productId);
        $stmt->execute();
        
        // Finally, delete the main product record
        $stmt = $conn->prepare("DELETE FROM products WHERE id = ? AND store_id = ?");
        $stmt->bind_param("ii", $productId, $storeId);
        $stmt->execute();
        
        if ($stmt->affected_rows === 0) {
            throw new Exception("Failed to delete product");
        }
        
        // Add audit log entry
        $stmt = $conn->prepare("INSERT INTO audit_log (table_name, record_id, action, old_values, changed_by, ip_address, user_agent) VALUES (?, ?, ?, ?, ?, ?, ?)");
        $tableName = 'products';
        $action = 'DELETE';
        $ipAddress = $_SERVER['REMOTE_ADDR'] ?? 'unknown';
        $userAgent = $_SERVER['HTTP_USER_AGENT'] ?? 'unknown';
        
        $stmt->bind_param("sisssss", $tableName, $productId, $action, $oldValues, $userId, $ipAddress, $userAgent);
        $stmt->execute();
        
        // Create notification for deletion
        $notificationTitle = "Product Deleted";
        $notificationMessage = "Product '{$product['product_name']}' has been deleted successfully";
        $notificationType = "deleted";
        $notificationStatus = "read";
        
        $stmt = $conn->prepare("INSERT INTO notifications (store_id, title, message, type, status) VALUES (?, ?, ?, ?, ?)");
        $stmt->bind_param("issss", $storeId, $notificationTitle, $notificationMessage, $notificationType, $notificationStatus);
        $stmt->execute();
        
        // Commit transaction
        $conn->commit();
        $conn->autocommit(TRUE);
        
        return [
            'success' => true,
            'message' => 'Product deleted successfully',
            'deleted_product' => $product['product_name']
        ];
        
    } catch (Exception $e) {
        // Rollback transaction on error
        $conn->rollback();
        $conn->autocommit(TRUE);
        
        return [
            'success' => false,
            'message' => 'Error deleting product: ' . $e->getMessage()
        ];
    }
}

// Handle AJAX request
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $productId = isset($_POST['product_id']) ? intval($_POST['product_id']) : 0;
    $storeId = isset($_POST['store_id']) ? intval($_POST['store_id']) : 0;
    $userId = isset($_POST['user_id']) ? intval($_POST['user_id']) : 0;

    if ($productId > 0 && $storeId > 0 && $userId > 0) {
        try {
            deleteItem($conn, $productId, $storeId, $userId);
            $conn->commit();
            echo json_encode(['status' => 'success', 'message' => 'Product deleted successfully']);
        } catch (Exception $e) {
            $conn->rollback();
            echo json_encode(['status' => 'error', 'message' => $e->getMessage()]);
        }
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Missing or invalid parameters']);
    }
} else {
    echo json_encode(['status' => 'error', 'message' => 'Invalid request method']);
}

// Close connection
$conn->close();
?>