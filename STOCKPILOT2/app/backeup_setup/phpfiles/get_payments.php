<?php
// Include database connection
require_once 'db.php';

// Set headers for JSON response
header('Content-Type: application/json');

// Function to get payments
function get_payments($conn, $store_id, $filters = []) {
    // Validate inputs
    $store_id = (int)$store_id;
    
    if ($store_id <= 0) {
        return [
            'success' => false,
            'message' => 'Invalid store ID provided'
        ];
    }
    
    try {
        // Build the query
        $sql = "SELECT bp.*, b.bill_number, s.supplier_name, pm.payment_method_name 
                FROM bill_payments bp 
                JOIN bills b ON bp.bill_id = b.bill_id 
                JOIN suppliers s ON b.supplier_id = s.supplier_id 
                LEFT JOIN payment_methods pm ON bp.payment_method_id = pm.payment_method_id 
                WHERE bp.store_id = ?";
        
        $params = [$store_id];
        $types = "i";
        
        // Add filters if provided
        if (isset($filters['bill_id']) && $filters['bill_id'] > 0) {
            $sql .= " AND bp.bill_id = ?";
            $params[] = (int)$filters['bill_id'];
            $types .= "i";
        }
        
        if (isset($filters['supplier_id']) && $filters['supplier_id'] > 0) {
            $sql .= " AND b.supplier_id = ?";
            $params[] = (int)$filters['supplier_id'];
            $types .= "i";
        }
        
        if (isset($filters['payment_method_id']) && $filters['payment_method_id'] > 0) {
            $sql .= " AND bp.payment_method_id = ?";
            $params[] = (int)$filters['payment_method_id'];
            $types .= "i";
        }
        
        if (isset($filters['date_from']) && !empty($filters['date_from'])) {
            $sql .= " AND bp.payment_date >= ?";
            $params[] = $conn->real_escape_string($filters['date_from']);
            $types .= "s";
        }
        
        if (isset($filters['date_to']) && !empty($filters['date_to'])) {
            $sql .= " AND bp.payment_date <= ?";
            $params[] = $conn->real_escape_string($filters['date_to']);
            $types .= "s";
        }
        
        // Add order by
        $sql .= " ORDER BY bp.payment_date DESC, bp.created_at DESC";
        
        // Prepare and execute the statement
        $stmt = $conn->prepare($sql);
        
        // Bind parameters dynamically
        if (!empty($params)) {
            $bind_params = array($types);
            for ($i = 0; $i < count($params); $i++) {
                $bind_params[] = &$params[$i];
            }
            call_user_func_array(array($stmt, 'bind_param'), $bind_params);
        }
        
        $stmt->execute();
        $result = $stmt->get_result();
        
        $payments = [];
        $total_amount = 0;
        while ($payment = $result->fetch_assoc()) {
            $payments[] = $payment;
            $total_amount += $payment['amount'];
        }
        
        return [
            'success' => true,
            'payments' => $payments,
            'count' => count($payments),
            'total_amount' => $total_amount
        ];
    } catch (Exception $e) {
        return [
            'success' => false,
            'message' => 'Error retrieving payments: ' . $e->getMessage()
        ];
    }
}

// Handle the request
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    // Check if store_id is provided
    if (isset($_GET['store_id'])) {
        $store_id = (int)$_GET['store_id'];
        
        // Get optional filters
        $filters = [];
        
        if (isset($_GET['bill_id'])) {
            $filters['bill_id'] = (int)$_GET['bill_id'];
        }
        
        if (isset($_GET['supplier_id'])) {
            $filters['supplier_id'] = (int)$_GET['supplier_id'];
        }
        
        if (isset($_GET['payment_method_id'])) {
            $filters['payment_method_id'] = (int)$_GET['payment_method_id'];
        }
        
        if (isset($_GET['date_from'])) {
            $filters['date_from'] = $_GET['date_from'];
        }
        
        if (isset($_GET['date_to'])) {
            $filters['date_to'] = $_GET['date_to'];
        }
        
        $response = get_payments($conn, $store_id, $filters);
        
        // Set appropriate HTTP status code
        if (!$response['success']) {
            http_response_code(400);
        }
        
        echo json_encode($response);
    } else {
        // Return error if store_id is not provided
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Missing required parameter: store_id'
        ]);
    }
} else {
    // Method not allowed
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'message' => 'Method not allowed. Use GET request.'
    ]);
}

// Close the database connection
$conn->close();
?>