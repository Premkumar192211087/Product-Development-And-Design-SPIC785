<?php
// Include database connection
require_once 'db.php';

// Set headers for JSON response
header('Content-Type: application/json');

// Function to get all vendors for a specific store
function get_vendors($conn, $store_id) {
    // Validate store_id
    $store_id = (int)$store_id;
    
    if ($store_id <= 0) {
        return [
            'success' => false,
            'message' => 'Invalid store ID provided'
        ];
    }
    
    // Prepare SQL query to get vendors
    $sql = "SELECT * FROM suppliers WHERE store_id = ? ORDER BY supplier_name ASC";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $store_id);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result->num_rows > 0) {
        $vendors = [];
        while ($row = $result->fetch_assoc()) {
            $vendors[] = $row;
        }
        
        return [
            'success' => true,
            'count' => count($vendors),
            'vendors' => $vendors
        ];
    } else {
        return [
            'success' => true,
            'count' => 0,
            'vendors' => [],
            'message' => 'No vendors found for this store'
        ];
    }
}

// Handle the request
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    // Check if store_id is provided
    if (isset($_GET['store_id'])) {
        $store_id = (int)$_GET['store_id'];
        $response = get_vendors($conn, $store_id);
        echo json_encode($response);
    } else {
        // Return error if store_id is not provided
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Store ID is required'
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