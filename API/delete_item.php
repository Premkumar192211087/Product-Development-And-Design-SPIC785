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
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Get JSON input
    $input = json_decode(file_get_contents('php://input'), true);
    
    // Validate required parameters
    if (!isset($input['product_id']) || !isset($input['store_id']) || !isset($input['user_id'])) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Missing required parameters: product_id, store_id, user_id'
        ]);
        exit;
    }
    
    $productId = (int)$input['product_id'];
    $storeId = (int)$input['store_id'];
    $userId = (int)$input['user_id'];
    
    // Validate parameters
    if ($productId <= 0 || $storeId <= 0 || $userId <= 0) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Invalid parameters provided'
        ]);
        exit;
    }
    
    // Delete the item
    $result = deleteItem($conn, $productId, $storeId, $userId);
    
    // Set appropriate HTTP status code
    if (!$result['success']) {
        http_response_code(500);
    }
    
    // Return JSON response
    header('Content-Type: application/json');
    echo json_encode($result);
    
} else {
    // Handle GET request or form submission
    if (isset($_GET['product_id']) && isset($_GET['store_id']) && isset($_GET['user_id'])) {
        $productId = (int)$_GET['product_id'];
        $storeId = (int)$_GET['store_id'];
        $userId = (int)$_GET['user_id'];
        
        if ($productId > 0 && $storeId > 0 && $userId > 0) {
            $result = deleteItem($conn, $productId, $storeId, $userId);
            
            if ($result['success']) {
                echo "<div style='color: green; padding: 20px; font-family: Arial;'>";
                echo "<h3>Success!</h3>";
                echo "<p>" . htmlspecialchars($result['message']) . "</p>";
                echo "<p>Product: " . htmlspecialchars($result['deleted_product']) . "</p>";
                echo "</div>";
            } else {
                echo "<div style='color: red; padding: 20px; font-family: Arial;'>";
                echo "<h3>Error!</h3>";
                echo "<p>" . htmlspecialchars($result['message']) . "</p>";
                echo "</div>";
            }
        } else {
            echo "<div style='color: red; padding: 20px; font-family: Arial;'>";
            echo "<h3>Error!</h3>";
            echo "<p>Invalid parameters provided</p>";
            echo "</div>";
        }
    } else {
        // Show usage instructions
        echo "<div style='padding: 20px; font-family: Arial;'>";
        echo "<h2>Delete Item API</h2>";
        echo "<h3>Usage:</h3>";
        echo "<p><strong>GET Request:</strong><br>";
        echo "delete_item.php?product_id=1&store_id=1&user_id=1</p>";
        echo "<p><strong>POST Request (JSON):</strong><br>";
        echo "Send JSON data: {\"product_id\": 1, \"store_id\": 1, \"user_id\": 1}</p>";
        echo "<h3>Parameters:</h3>";
        echo "<ul>";
        echo "<li><strong>product_id:</strong> ID of the product to delete</li>";
        echo "<li><strong>store_id:</strong> ID of the store (for permission check)</li>";
        echo "<li><strong>user_id:</strong> ID of the user performing the deletion (for audit log)</li>";
        echo "</ul>";
        echo "</div>";
    }
}

// Close connection
$conn->close();
?>