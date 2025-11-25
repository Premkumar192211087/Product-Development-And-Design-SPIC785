<?php
// Include database connection
require_once 'db.php';

// Set headers for JSON response
header('Content-Type: application/json');

// Function to update a bill
function update_bill($conn, $data) {
    // Validate required fields
    if (!isset($data['bill_id']) || !isset($data['store_id']) || !isset($data['updated_by'])) {
        return [
            'success' => false,
            'message' => 'Missing required fields: bill_id, store_id, updated_by'
        ];
    }
    
    // Sanitize inputs
    $bill_id = (int)$data['bill_id'];
    $store_id = (int)$data['store_id'];
    $updated_by = (int)$data['updated_by'];
    
    // Check if bill exists
    $check_sql = "SELECT * FROM bills WHERE bill_id = ? AND store_id = ?";
    $check_stmt = $conn->prepare($check_sql);
    $check_stmt->bind_param("ii", $bill_id, $store_id);
    $check_stmt->execute();
    $result = $check_stmt->get_result();
    
    if ($result->num_rows === 0) {
        return [
            'success' => false,
            'message' => 'Bill not found or you do not have permission to update it'
        ];
    }
    
    $current_bill = $result->fetch_assoc();
    
    // Check if bill can be updated (only pending or draft bills can be updated)
    if (!in_array($current_bill['status'], ['pending', 'draft'])) {
        return [
            'success' => false,
            'message' => 'Cannot update bill with status: ' . $current_bill['status'] . '. Only pending or draft bills can be updated.'
        ];
    }
    
    // Begin transaction
    $conn->begin_transaction();
    
    try {
        // Update bill fields
        $update_fields = [];
        $update_params = [];
        $update_types = "";
        
        // Optional fields that can be updated
        $optional_fields = [
            'supplier_id' => 'i',
            'po_id' => 'i',
            'bill_date' => 's',
            'due_date' => 's',
            'bill_number' => 's',
            'reference' => 's',
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
            $update_sql = "UPDATE bills SET " . implode(", ", $update_fields) . 
                          " WHERE bill_id = ? AND store_id = ?";
            
            // Add bill_id and store_id to parameters
            $update_params[] = $bill_id;
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
            $current_items_sql = "SELECT * FROM bill_items WHERE bill_id = ? AND store_id = ?";
            $current_items_stmt = $conn->prepare($current_items_sql);
            $current_items_stmt->bind_param("ii", $bill_id, $store_id);
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
                $delete_items_sql = "DELETE FROM bill_items WHERE item_id IN (" . 
                                    implode(",", $items_to_delete) . ") AND bill_id = ? AND store_id = ?";
                $delete_items_stmt = $conn->prepare($delete_items_sql);
                $delete_items_stmt->bind_param("ii", $bill_id, $store_id);
                $delete_items_stmt->execute();
            }
            
            // Update existing items
            if (!empty($items_to_update)) {
                $update_item_sql = "UPDATE bill_items SET product_id = ?, quantity = ?, 
                                    unit_price = ?, total_price = ?, updated_at = NOW() 
                                    WHERE item_id = ? AND bill_id = ? AND store_id = ?";
                $update_item_stmt = $conn->prepare($update_item_sql);
                
                foreach ($items_to_update as $item) {
                    $product_id = (int)$item['product_id'];
                    $quantity = (int)$item['quantity'];
                    $unit_price = (float)$item['unit_price'];
                    $total_price = $quantity * $unit_price;
                    $item_id = (int)$item['item_id'];
                    
                    $update_item_stmt->bind_param("iiddiii", $product_id, $quantity, $unit_price, 
                                                $total_price, $item_id, $bill_id, $store_id);
                    $update_item_stmt->execute();
                }
            }
            
            // Add new items
            if (!empty($items_to_add)) {
                $add_item_sql = "INSERT INTO bill_items (store_id, bill_id, product_id, quantity, 
                                                     unit_price, total_price, created_at) 
                                VALUES (?, ?, ?, ?, ?, ?, NOW())";
                $add_item_stmt = $conn->prepare($add_item_sql);
                
                foreach ($items_to_add as $item) {
                    $product_id = (int)$item['product_id'];
                    $quantity = (int)$item['quantity'];
                    $unit_price = (float)$item['unit_price'];
                    $total_price = $quantity * $unit_price;
                    
                    $add_item_stmt->bind_param("iiidd", $store_id, $bill_id, $product_id, $quantity, 
                                              $unit_price, $total_price);
                    $add_item_stmt->execute();
                }
            }
            
            // Recalculate total amount
            $total_sql = "SELECT SUM(total_price) as total FROM bill_items WHERE bill_id = ? AND store_id = ?";
            $total_stmt = $conn->prepare($total_sql);
            $total_stmt->bind_param("ii", $bill_id, $store_id);
            $total_stmt->execute();
            $total_result = $total_stmt->get_result()->fetch_assoc();
            $total_amount = $total_result['total'] ?? 0;
            
            // Update total amount in bill
            $update_total_sql = "UPDATE bills SET total_amount = ? WHERE bill_id = ? AND store_id = ?";
            $update_total_stmt = $conn->prepare($update_total_sql);
            $update_total_stmt->bind_param("dii", $total_amount, $bill_id, $store_id);
            $update_total_stmt->execute();
        }
        
        // Log the update in audit log
        $log_sql = "INSERT INTO audit_log (store_id, user_id, action, entity_type, entity_id, details, created_at) 
                    VALUES (?, ?, 'update', 'bill', ?, ?, NOW())";
        
        $details = json_encode([
            'bill_id' => $bill_id,
            'updated_fields' => isset($update_fields) ? implode(", ", $update_fields) : 'None',
            'items_updated' => isset($items_to_update) ? count($items_to_update) : 0,
            'items_added' => isset($items_to_add) ? count($items_to_add) : 0,
            'items_deleted' => isset($items_to_delete) ? count($items_to_delete) : 0
        ]);
        
        $log_stmt = $conn->prepare($log_sql);
        $log_stmt->bind_param("iiss", $store_id, $updated_by, $bill_id, $details);
        $log_stmt->execute();
        
        // Create notification for updated bill
        $notification_title = "Bill Updated";
        $notification_message = "Bill {$current_bill['bill_number']} has been updated";
        $notification_type = "bill";
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
            'message' => 'Bill updated successfully',
            'bill_id' => $bill_id
        ];
    } catch (Exception $e) {
        // Rollback transaction on error
        $conn->rollback();
        
        return [
            'success' => false,
            'message' => 'Error updating bill: ' . $e->getMessage()
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
    
    $response = update_bill($conn, $input);
    
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