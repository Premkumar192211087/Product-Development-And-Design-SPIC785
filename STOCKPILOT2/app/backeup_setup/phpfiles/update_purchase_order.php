<?php
// Include database connection
require_once 'db.php';

// Set headers for JSON response
header('Content-Type: application/json');

// Function to update a purchase order
function update_purchase_order($conn, $data) {
    // Validate required fields
    if (!isset($data['po_id']) || !isset($data['store_id']) || !isset($data['updated_by'])) {
        return [
            'success' => false,
            'message' => 'Missing required fields: po_id, store_id, updated_by'
        ];
    }
    
    // Sanitize inputs
    $po_id = (int)$data['po_id'];
    $store_id = (int)$data['store_id'];
    $updated_by = (int)$data['updated_by'];
    
    // Check if purchase order exists
    $check_sql = "SELECT * FROM purchase_orders WHERE po_id = ? AND store_id = ?";
    $check_stmt = $conn->prepare($check_sql);
    $check_stmt->bind_param("ii", $po_id, $store_id);
    $check_stmt->execute();
    $result = $check_stmt->get_result();
    
    if ($result->num_rows === 0) {
        return [
            'success' => false,
            'message' => 'Purchase order not found or you do not have permission to update it'
        ];
    }
    
    $current_po = $result->fetch_assoc();
    
    // Check if purchase order can be updated (only pending or draft orders can be updated)
    if (!in_array($current_po['status'], ['pending', 'draft'])) {
        return [
            'success' => false,
            'message' => 'Cannot update purchase order with status: ' . $current_po['status'] . '. Only pending or draft orders can be updated.'
        ];
    }
    
    // Begin transaction
    $conn->begin_transaction();
    
    try {
        // Update purchase order fields
        $update_fields = [];
        $update_params = [];
        $update_types = "";
        
        // Optional fields that can be updated
        $optional_fields = [
            'supplier_id' => 'i',
            'order_date' => 's',
            'expected_delivery_date' => 's',
            'notes' => 's',
            'status' => 's'
        ];
        
        foreach ($optional_fields as $field => $type) {
            if (isset($data[$field])) {
                $value = $data[$field];
                
                // Sanitize string values
                if ($type === 's') {
                    $value = $conn->real_escape_string($value);
                } else if ($type === 'i') {
                    $value = (int)$value;
                }
                
                $update_fields[] = "{$field} = ?";
                $update_params[] = $value;
                $update_types .= $type;
            }
        }
        
        // Add updated_at and updated_by fields
        $update_fields[] = "updated_at = NOW()";
        $update_fields[] = "updated_by = ?";
        $update_params[] = $updated_by;
        $update_types .= "i";
        
        // If there are fields to update
        if (!empty($update_fields)) {
            // Build the SQL query
            $update_sql = "UPDATE purchase_orders SET " . implode(", ", $update_fields) . 
                          " WHERE po_id = ? AND store_id = ?";
            
            // Add po_id and store_id to parameters
            $update_params[] = $po_id;
            $update_params[] = $store_id;
            $update_types .= "ii";
            
            // Prepare and execute the statement
            $update_stmt = $conn->prepare($update_sql);
            
            // Bind parameters dynamically
            $bind_params = array($update_types);
            for ($i = 0; $i < count($update_params); $i++) {
                $bind_params[] = &$update_params[$i];
            }
            
            call_user_func_array(array($update_stmt, 'bind_param'), $bind_params);
            $update_stmt->execute();
        }
        
        // Update items if provided
        if (isset($data['items']) && is_array($data['items'])) {
            // First, get current items
            $current_items_sql = "SELECT * FROM purchase_order_items WHERE po_id = ? AND store_id = ?";
            $current_items_stmt = $conn->prepare($current_items_sql);
            $current_items_stmt->bind_param("ii", $po_id, $store_id);
            $current_items_stmt->execute();
            $current_items_result = $current_items_stmt->get_result();
            
            $current_items = [];
            while ($item = $current_items_result->fetch_assoc()) {
                $current_items[$item['item_id']] = $item;
            }
            
            // Process new and updated items
            $items_to_update = [];
            $items_to_add = [];
            $item_ids_to_keep = [];
            
            foreach ($data['items'] as $item) {
                if (isset($item['item_id']) && $item['item_id'] > 0) {
                    // Existing item to update
                    $items_to_update[] = $item;
                    $item_ids_to_keep[] = $item['item_id'];
                } else {
                    // New item to add
                    $items_to_add[] = $item;
                }
            }
            
            // Delete items not in the updated list
            $items_to_delete = array_diff(array_keys($current_items), $item_ids_to_keep);
            
            if (!empty($items_to_delete)) {
                $delete_items_sql = "DELETE FROM purchase_order_items WHERE item_id IN (" . 
                                    implode(",", $items_to_delete) . ") AND po_id = ? AND store_id = ?";
                $delete_items_stmt = $conn->prepare($delete_items_sql);
                $delete_items_stmt->bind_param("ii", $po_id, $store_id);
                $delete_items_stmt->execute();
            }
            
            // Update existing items
            if (!empty($items_to_update)) {
                $update_item_sql = "UPDATE purchase_order_items SET product_id = ?, quantity_ordered = ?, 
                                    unit_price = ?, total_price = ?, updated_at = NOW() 
                                    WHERE item_id = ? AND po_id = ? AND store_id = ?";
                $update_item_stmt = $conn->prepare($update_item_sql);
                
                foreach ($items_to_update as $item) {
                    $product_id = (int)$item['product_id'];
                    $quantity_ordered = (int)$item['quantity_ordered'];
                    $unit_price = (float)$item['unit_price'];
                    $total_price = $quantity_ordered * $unit_price;
                    $item_id = (int)$item['item_id'];
                    
                    $update_item_stmt->bind_param("iiddiii", $product_id, $quantity_ordered, $unit_price, 
                                                $total_price, $item_id, $po_id, $store_id);
                    $update_item_stmt->execute();
                }
            }
            
            // Add new items
            if (!empty($items_to_add)) {
                $add_item_sql = "INSERT INTO purchase_order_items (store_id, po_id, product_id, quantity_ordered, 
                                                              unit_price, total_price, created_at) 
                                VALUES (?, ?, ?, ?, ?, ?, NOW())";
                $add_item_stmt = $conn->prepare($add_item_sql);
                
                foreach ($items_to_add as $item) {
                    $product_id = (int)$item['product_id'];
                    $quantity_ordered = (int)$item['quantity_ordered'];
                    $unit_price = (float)$item['unit_price'];
                    $total_price = $quantity_ordered * $unit_price;
                    
                    $add_item_stmt->bind_param("iiidd", $store_id, $po_id, $product_id, $quantity_ordered, 
                                              $unit_price, $total_price);
                    $add_item_stmt->execute();
                }
            }
            
            // Recalculate total amount
            $total_sql = "SELECT SUM(total_price) as total FROM purchase_order_items WHERE po_id = ? AND store_id = ?";
            $total_stmt = $conn->prepare($total_sql);
            $total_stmt->bind_param("ii", $po_id, $store_id);
            $total_stmt->execute();
            $total_result = $total_stmt->get_result()->fetch_assoc();
            $total_amount = $total_result['total'] ?? 0;
            
            // Update total amount in purchase order
            $update_total_sql = "UPDATE purchase_orders SET total_amount = ? WHERE po_id = ? AND store_id = ?";
            $update_total_stmt = $conn->prepare($update_total_sql);
            $update_total_stmt->bind_param("dii", $total_amount, $po_id, $store_id);
            $update_total_stmt->execute();
        }
        
        // Log the update in audit log
        $log_sql = "INSERT INTO audit_log (store_id, user_id, action, entity_type, entity_id, details, created_at) 
                    VALUES (?, ?, 'update', 'purchase_order', ?, ?, NOW())";
        
        $details = json_encode([
            'po_id' => $po_id,
            'updated_fields' => isset($update_fields) ? implode(", ", $update_fields) : 'None',
            'items_updated' => isset($items_to_update) ? count($items_to_update) : 0,
            'items_added' => isset($items_to_add) ? count($items_to_add) : 0,
            'items_deleted' => isset($items_to_delete) ? count($items_to_delete) : 0
        ]);
        
        $log_stmt = $conn->prepare($log_sql);
        $log_stmt->bind_param("iiss", $store_id, $updated_by, $po_id, $details);
        $log_stmt->execute();
        
        // Create notification for updated purchase order
        $notification_title = "Purchase Order Updated";
        $notification_message = "Purchase order {$current_po['po_number']} has been updated";
        $notification_type = "purchase_order";
        $notification_status = "unread";
        
        $notification_sql = "INSERT INTO notifications (store_id, title, message, type, status) 
                             VALUES (?, ?, ?, ?, ?)";
        
        $notification_stmt = $conn->prepare($notification_sql);
        $notification_stmt->bind_param("issss", $store_id, $notification_title, $notification_message, 
                                      $notification_type, $notification_status);
        $notification_stmt->execute();
        
        // Commit transaction
        $conn->commit();
        
        return [
            'success' => true,
            'message' => 'Purchase order updated successfully',
            'po_id' => $po_id
        ];
    } catch (Exception $e) {
        // Rollback transaction on error
        $conn->rollback();
        
        return [
            'success' => false,
            'message' => 'Error updating purchase order: ' . $e->getMessage()
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
        
        // Parse items if provided as string
        if (isset($input['items']) && is_string($input['items'])) {
            $input['items'] = json_decode($input['items'], true);
        }
    }
    
    $response = update_purchase_order($conn, $input);
    
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