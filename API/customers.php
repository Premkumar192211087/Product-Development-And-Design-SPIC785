<?php
header("Content-Type: application/json");
include 'db.php';

$method = $_SERVER['REQUEST_METHOD'];
$response = [];

switch ($method) {
    case 'GET':
        // === FETCH CUSTOMERS ===
        $store_id = isset($_GET['store_id']) ? intval($_GET['store_id']) : 0;
        $status = $_GET['status'] ?? 'All Statuses';
        $sort = $_GET['sort'] ?? 'Most Recent';
        $search = trim($_GET['search'] ?? '');

        if ($store_id === 0) {
            echo json_encode(['success' => false, 'message' => "Invalid store ID"]);
            exit;
        }

        $query = "SELECT customer_id, customer_name, email, phone, status, loyalty_points, 
                  last_purchase_date, registration_date, address, date_of_birth
                  FROM customers 
                  WHERE store_id = ?";
        $params = [$store_id];
        $param_types = "i";

        if (!empty($search)) {
            $query .= " AND (customer_name LIKE ? OR email LIKE ? OR phone LIKE ?)";
            $search_param = "%$search%";
            $params = array_merge($params, [$search_param, $search_param, $search_param]);
            $param_types .= "sss";
        }

        if ($status === 'Active') {
            $query .= " AND status = 'active'";
        } elseif ($status === 'Inactive') {
            $query .= " AND status = 'inactive'";
        }

        switch ($sort) {
            case 'Name (A-Z)': $query .= " ORDER BY customer_name ASC"; break;
            case 'Name (Z-A)': $query .= " ORDER BY customer_name DESC"; break;
            case 'Oldest First': $query .= " ORDER BY registration_date ASC"; break;
            case 'Most Recent':
            default: $query .= " ORDER BY registration_date DESC"; break;
        }

        $stmt = $conn->prepare($query);
        if (!empty($params)) $stmt->bind_param($param_types, ...$params);
        $stmt->execute();
        $result = $stmt->get_result();

        $customers = [];
        while ($row = $result->fetch_assoc()) {
            $last_display = "Never";
            if ($row['last_purchase_date']) {
                $diff = (new DateTime())->diff(new DateTime($row['last_purchase_date']));
                if ($diff->days == 0) $last_display = "Today";
                elseif ($diff->days == 1) $last_display = "Yesterday";
                elseif ($diff->days < 30) $last_display = $diff->days . " days ago";
                elseif ($diff->days < 365) $last_display = floor($diff->days / 30) . " months ago";
                else $last_display = floor($diff->days / 365) . " years ago";
            }

            $customers[] = [
                'customer_id' => $row['customer_id'],
                'name' => $row['customer_name'],
                'email' => $row['email'],
                'phone' => $row['phone'],
                'status' => ucfirst($row['status']),
                'loyalty_points' => $row['loyalty_points'] ?? 0,
                'last_purchase' => $row['last_purchase_date'],
                'last_purchase_display' => $last_display,
                'registration_date' => $row['registration_date'],
                'address' => $row['address'],
                'date_of_birth' => $row['date_of_birth'],
                'avatar_letter' => strtoupper(substr($row['customer_name'], 0, 1)) ?: 'U'
            ];
        }

        $response['success'] = true;
        $response['customers'] = $customers;
        $response['total_count'] = count($customers);
        break;

    case 'DELETE':
    case 'PUT':
    case 'PATCH': // Added PATCH for status updates
        $input = json_decode(file_get_contents('php://input'), true);
        $customer_id = intval($input['customer_id'] ?? 0);
        $store_id = intval($input['store_id'] ?? 0);
        $user_id = intval($input['user_id'] ?? 1);
        $action_type = $input['action'] ?? 'delete'; // 'delete', 'deactivate', or 'toggle_status'

        if ($customer_id === 0 || $store_id === 0) {
            echo json_encode(['success' => false, 'message' => "Invalid customer or store ID"]);
            exit;
        }

        try {
            $conn->begin_transaction();

            $check = $conn->prepare("SELECT customer_id, customer_name, status FROM customers WHERE customer_id = ? AND store_id = ?");
            $check->bind_param("ii", $customer_id, $store_id);
            $check->execute();
            $result = $check->get_result();

            if ($result->num_rows === 0) throw new Exception("Customer not found or doesn't belong to this store");
            $customer_data = $result->fetch_assoc();
            $customer_name = $customer_data['customer_name'];
            $old_status = $customer_data['status'];

            if ($method === 'PUT' || $method === 'PATCH' || $action_type === 'toggle_status' || $action_type === 'deactivate') {
                // Toggle status or set to inactive
                $new_status = ($action_type === 'toggle_status') ? 
                    ($old_status === 'active' ? 'inactive' : 'active') : 'inactive';
                
                if ($old_status === $new_status) {
                    $status_word = $new_status === 'active' ? 'active' : 'inactive';
                    throw new Exception("Customer is already $status_word");
                }

                $audit = $conn->prepare("INSERT INTO audit_logs (table_name, record_id, action, old_values, new_values, changed_by, store_id, ip_address, user_agent) 
                                         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                $table = 'customers';
                $action = 'UPDATE';
                $old = json_encode(['status' => $old_status]);
                $new = json_encode(['status' => $new_status]);
                $ip = $_SERVER['REMOTE_ADDR'] ?? 'unknown';
                $ua = $_SERVER['HTTP_USER_AGENT'] ?? 'unknown';
                
                // FIXED: Corrected parameter types from "sisssisss" to "sisssiiss"
                $audit->bind_param("sisssiiss", $table, $customer_id, $action, $old, $new, $user_id, $store_id, $ip, $ua);
                $audit->execute();

                $update = $conn->prepare("UPDATE customers SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE customer_id = ? AND store_id = ?");
                $update->bind_param("sii", $new_status, $customer_id, $store_id);
                $update->execute();

                if ($update->affected_rows === 0) throw new Exception("Failed to update customer status");

                $status_word = $new_status === 'active' ? 'activated' : 'deactivated';
                $response = [
                    'success' => true, 
                    'message' => "Customer '$customer_name' $status_word", 
                    'customer_id' => $customer_id,
                    'new_status' => ucfirst($new_status)
                ];
            } else {
                // DELETE operation
                // Check sales, returns, invoices
                $tables = ['sales', 'returns', 'invoices'];
                foreach ($tables as $table) {
                    $check = $conn->prepare("SELECT COUNT(*) as count FROM $table WHERE customer_id = ? AND store_id = ?");
                    $check->bind_param("ii", $customer_id, $store_id);
                    $check->execute();
                    $count = $check->get_result()->fetch_assoc()['count'];
                    if ($count > 0) throw new Exception("Cannot delete customer with $table history");
                }

                $audit = $conn->prepare("INSERT INTO audit_logs (table_name, record_id, action, old_values, changed_by, store_id, ip_address, user_agent) 
                                         VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                $table = 'customers';
                $action = 'DELETE';
                $old = json_encode($customer_data);
                $ip = $_SERVER['REMOTE_ADDR'] ?? 'unknown';
                $ua = $_SERVER['HTTP_USER_AGENT'] ?? 'unknown';
                
                // FIXED: Corrected parameter types from "sissisis" to "sissiiss"
                $audit->bind_param("sissiiss", $table, $customer_id, $action, $old, $user_id, $store_id, $ip, $ua);
                $audit->execute();

                $delete = $conn->prepare("DELETE FROM customers WHERE customer_id = ? AND store_id = ?");
                $delete->bind_param("ii", $customer_id, $store_id);
                $delete->execute();

                if ($delete->affected_rows === 0) throw new Exception("Failed to delete customer");

                $response = ['success' => true, 'message' => "Customer '$customer_name' deleted", 'deleted_customer_id' => $customer_id];
            }

            $conn->commit();
        } catch (Exception $e) {
            $conn->rollback();
            $response = ['success' => false, 'message' => $e->getMessage()];
            error_log("Customer Operation Error: " . $e->getMessage());
        }
        break;

    default:
        http_response_code(405);
        $response = ['success' => false, 'message' => 'Method not allowed'];
        break;
}

echo json_encode($response);
?>