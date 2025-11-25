<?php
// DB connection
$host = "localhost";
$user = "root";
$pass = "";
$db = "inventory_management";
$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    die(json_encode(['error' => 'Database connection failed']));
}

// Set content type to JSON
header('Content-Type: application/json');

// Check if request method is POST or PUT
if (!in_array($_SERVER['REQUEST_METHOD'], ['POST', 'PUT'])) {
    echo json_encode(['success' => false, 'error' => 'Only POST or PUT method allowed']);
    exit;
}

// Get JSON input
$input = json_decode(file_get_contents('php://input'), true);

// Validate required fields
if (!isset($input['product_id']) || empty($input['product_id'])) {
    echo json_encode(['success' => false, 'error' => 'Product ID is required']);
    exit;
}

$product_id = intval($input['product_id']);

try {
    // Start transaction
    $conn->autocommit(FALSE);
    
    // First, get current product data
    $current_product = $conn->prepare("
        SELECT p.*, ps.unit_cost as purchase_price, ia.min_stock_level, ia.max_stock_level, ia.reorder_quantity
        FROM products p
        LEFT JOIN product_suppliers ps ON p.id = ps.product_id AND ps.is_primary = 1
        LEFT JOIN inventory_alerts ia ON p.id = ia.product_id
        WHERE p.id = ?
    ");
    $current_product->bind_param("i", $product_id);
    $current_product->execute();
    $current_data = $current_product->get_result()->fetch_assoc();
    
    if (!$current_data) {
        throw new Exception("Product not found");
    }
    
    // Prepare update fields
    $update_fields = array();
    $update_values = array();
    $update_types = "";
    
    // Check each field for updates
    if (isset($input['product_name']) && $input['product_name'] !== $current_data['product_name']) {
        $update_fields[] = "product_name = ?";
        $update_values[] = $conn->real_escape_string($input['product_name']);
        $update_types .= "s";
    }
    
    if (isset($input['price']) && floatval($input['price']) !== floatval($current_data['price'])) {
        if (floatval($input['price']) <= 0) {
            throw new Exception("Price must be greater than 0");
        }
        $update_fields[] = "price = ?";
        $update_values[] = floatval($input['price']);
        $update_types .= "d";
    }
    
    if (isset($input['category']) && $input['category'] !== $current_data['category']) {
        $update_fields[] = "category = ?";
        $update_values[] = $conn->real_escape_string($input['category']);
        $update_types .= "s";
    }
    
    if (isset($input['sku']) && $input['sku'] !== $current_data['sku']) {
        // Check if new SKU already exists
        $sku_check = $conn->prepare("SELECT id FROM products WHERE sku = ? AND store_id = ? AND id != ?");
        $sku_check->bind_param("sii", $input['sku'], $current_data['store_id'], $product_id);
        $sku_check->execute();
        if ($sku_check->get_result()->num_rows > 0) {
            throw new Exception("SKU already exists in this store");
        }
        
        $update_fields[] = "sku = ?";
        $update_values[] = $conn->real_escape_string($input['sku']);
        $update_types .= "s";
    }
    
    if (isset($input['image_url']) && $input['image_url'] !== $current_data['image_url']) {
        $update_fields[] = "image_url = ?";
        $update_values[] = $conn->real_escape_string($input['image_url']);
        $update_types .= "s";
    }
    
    // Handle quantity update separately (for stock movement tracking)
    $quantity_changed = false;
    $old_quantity = intval($current_data['quantity']);
    $new_quantity = $old_quantity;
    
    if (isset($input['quantity']) && intval($input['quantity']) !== $old_quantity) {
        if (intval($input['quantity']) < 0) {
            throw new Exception("Quantity cannot be negative");
        }
        $new_quantity = intval($input['quantity']);
        $quantity_changed = true;
        $update_fields[] = "quantity = ?";
        $update_values[] = $new_quantity;
        $update_types .= "i";
    }
    
    // Update product if there are changes
    if (!empty($update_fields)) {
        $sql = "UPDATE products SET " . implode(", ", $update_fields) . " WHERE id = ?";
        $update_values[] = $product_id;
        $update_types .= "i";
        
        $stmt = $conn->prepare($sql);
        $stmt->bind_param($update_types, ...$update_values);
        
        if (!$stmt->execute()) {
            throw new Exception("Failed to update product: " . $stmt->error);
        }
    }
    
    // Update inventory alerts if provided
    $alert_updates = array();
    $alert_values = array();
    $alert_types = "";
    
    if (isset($input['min_stock_level']) && intval($input['min_stock_level']) !== intval($current_data['min_stock_level'])) {
        $alert_updates[] = "min_stock_level = ?";
        $alert_values[] = intval($input['min_stock_level']);
        $alert_types .= "i";
    }
    
    if (isset($input['max_stock_level']) && intval($input['max_stock_level']) !== intval($current_data['max_stock_level'])) {
        $alert_updates[] = "max_stock_level = ?";
        $alert_values[] = intval($input['max_stock_level']);
        $alert_types .= "i";
    }
    
    if (isset($input['reorder_quantity']) && intval($input['reorder_quantity']) !== intval($current_data['reorder_quantity'])) {
        $alert_updates[] = "reorder_quantity = ?";
        $alert_values[] = intval($input['reorder_quantity']);
        $alert_types .= "i";
    }
    
    if (!empty($alert_updates)) {
        $alert_updates[] = "updated_at = NOW()";
        $alert_sql = "UPDATE inventory_alerts SET " . implode(", ", $alert_updates) . " WHERE product_id = ?";
        $alert_values[] = $product_id;
        $alert_types .= "i";
        
        $alert_stmt = $conn->prepare($alert_sql);
        $alert_stmt->bind_param($alert_types, ...$alert_values);
        $alert_stmt->execute();
    }
    
    // Update supplier information if provided
    if (isset($input['purchase_price']) && floatval($input['purchase_price']) !== floatval($current_data['purchase_price'])) {
        $purchase_price = floatval($input['purchase_price']);
        
        // Check if supplier relationship exists
        $check_supplier = $conn->prepare("SELECT ps_id FROM product_suppliers WHERE product_id = ? AND is_primary = 1");
        $check_supplier->bind_param("i", $product_id);
        $check_supplier->execute();
        $supplier_exists = $check_supplier->get_result()->num_rows > 0;
        
        if ($supplier_exists) {
            // Update existing supplier cost
            $update_supplier = $conn->prepare("UPDATE product_suppliers SET unit_cost = ? WHERE product_id = ? AND is_primary = 1");
            $update_supplier->bind_param("di", $purchase_price, $product_id);
            $update_supplier->execute();
        } elseif (isset($input['supplier_id'])) {
            // Create new supplier relationship
            $insert_supplier = $conn->prepare("
                INSERT INTO product_suppliers (product_id, supplier_id, unit_cost, is_primary, status, created_at) 
                VALUES (?, ?, ?, 1, 'active', NOW())
            ");
            $supplier_id = intval($input['supplier_id']);
            $insert_supplier->bind_param("iid", $product_id, $supplier_id, $purchase_price);
            $insert_supplier->execute();
        }
    }
    
    // Log stock movement if quantity changed
    if ($quantity_changed) {
        $quantity_diff = $new_quantity - $old_quantity;
        $movement_type = $quantity_diff > 0 ? 'in' : 'out';
        $movement_quantity = abs($quantity_diff);
        $unit_price = isset($input['price']) ? floatval($input['price']) : floatval($current_data['price']);
        $total_value = $movement_quantity * $unit_price;
        
        $insert_movement = $conn->prepare("
            INSERT INTO sales_movements (product_id, store_id, movement_type, quantity, reference_type, unit_price, total_value, performed_by, notes, timestamp) 
            VALUES (?, ?, ?, ?, 'adjustment', ?, ?, 1, 'Inventory adjustment via update', NOW())
        ");
        $insert_movement->bind_param("isiidi", $product_id, $current_data['store_id'], $movement_type, $movement_quantity, $unit_price, $total_value);
        $insert_movement->execute();
    }
    
    // Create audit log entry
    $old_values = json_encode($current_data);
    $new_values = json_encode($input);
    $insert_audit = $conn->prepare("
        INSERT INTO audit_log (table_name, record_id, action, old_values, new_values, changed_by, ip_address, timestamp) 
        VALUES ('products', ?, 'UPDATE', ?, ?, 1, ?, NOW())
    ");
    $ip_address = $_SERVER['REMOTE_ADDR'] ?? 'unknown';
    $insert_audit->bind_param("isss", $product_id, $old_values, $new_values, $ip_address);
    $insert_audit->execute();
    
    // Commit transaction
    $conn->commit();
    
    // Fetch updated product data
    $fetch_updated = $conn->prepare("
        SELECT 
            p.id as product_id,
            p.product_name,
            p.sku,
            p.category,
            p.price as sales_price,
            p.quantity as stock_quantity,
            p.image_url,
            p.created_at,
            p.store_id,
            ps.unit_cost as purchase_price,
            ia.min_stock_level,
            ia.max_stock_level,
            ia.reorder_quantity
        FROM products p
        LEFT JOIN product_suppliers ps ON p.id = ps.product_id AND ps.is_primary = 1
        LEFT JOIN inventory_alerts ia ON p.id = ia.product_id
        WHERE p.id = ?
    ");
    $fetch_updated->bind_param("i", $product_id);
    $fetch_updated->execute();
    $updated_data = $fetch_updated->get_result()->fetch_assoc();
    
    // Determine stock status
    $stock_quantity = intval($updated_data['stock_quantity']);
    $min_stock = intval($updated_data['min_stock_level'] ?? 10);
    $max_stock = intval($updated_data['max_stock_level'] ?? 1000);
    
    $stock_status = 'normal';
    if ($stock_quantity <= 0) {
        $stock_status = 'out_of_stock';
    } elseif ($stock_quantity <= $min_stock) {
        $stock_status = 'low_stock';
    } elseif ($stock_quantity >= $max_stock) {
        $stock_status = 'overstock';
    }
    
    $updated_data['stock_status'] = $stock_status;
    
    // Check if low stock notification should be created
    if ($stock_status === 'low_stock' || $stock_status === 'out_of_stock') {
        // Check if notification already exists for this product
        $check_notification = $conn->prepare("
            SELECT id FROM notifications 
            WHERE store_id = ? AND type = 'lowstock' 
            AND message LIKE ? AND status = 'read'
        ");
        $notification_message = "%{$updated_data['product_name']}%";
        $check_notification->bind_param("is", $updated_data['store_id'], $notification_message);
        $check_notification->execute();
        
        if ($check_notification->get_result()->num_rows == 0) {
            // Create low stock notification
            $notification_title = $stock_status === 'out_of_stock' ? 'Out of Stock Alert' : 'Low Stock Alert';
            $notification_msg = $stock_status === 'out_of_stock' 
                ? "Product '{$updated_data['product_name']}' is out of stock"
                : "Product '{$updated_data['product_name']}' is running low (Current: {$stock_quantity}, Min: {$min_stock})";
            
            $insert_notification = $conn->prepare("
                INSERT INTO notifications (store_id, title, message, type, status, timestamp) 
                VALUES (?, ?, ?, 'lowstock', 'read', NOW())
            ");
            $insert_notification->bind_param("iss", $updated_data['store_id'], $notification_title, $notification_msg);
            $insert_notification->execute();
        }
    }
    
    // Calculate profit margin if both prices are available
    if ($updated_data['purchase_price'] && $updated_data['sales_price']) {
        $profit_margin = (($updated_data['sales_price'] - $updated_data['purchase_price']) / $updated_data['sales_price']) * 100;
        $updated_data['profit_margin'] = round($profit_margin, 2);
    } else {
        $updated_data['profit_margin'] = null;
    }
    
    // Format response
    $response = [
        'success' => true,
        'message' => 'Product updated successfully',
        'data' => $updated_data,
        'changes_made' => !empty($update_fields) || !empty($alert_updates) || $quantity_changed,
        'stock_movement_logged' => $quantity_changed
    ];
    
    echo json_encode($response);
    
} catch (Exception $e) {
    // Rollback transaction on error
    $conn->rollback();
    
    // Log error
    error_log("Product update error: " . $e->getMessage());
    
    echo json_encode([
        'success' => false,
        'error' => $e->getMessage()
    ]);
    
} finally {
    // Restore autocommit
    $conn->autocommit(TRUE);
    $conn->close();
}
?>