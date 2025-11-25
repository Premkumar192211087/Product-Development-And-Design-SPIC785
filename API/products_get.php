<?php
// Set content type to JSON
header('Content-Type: application/json');

// Enable error reporting for debugging
ini_set('display_errors', 1);
error_reporting(E_ALL);

// Database connection parameters
$host = "localhost";  // Replace with your database host
$user = "root";   // Replace with your database username
$password = "";   // Replace with your database password
$database = "inventory_management";  // Replace with your database name

// Log the request for debugging
$log_file = "api_log.txt";
file_put_contents($log_file, date("Y-m-d H:i:s") . " - Request received: " . $_SERVER['REQUEST_URI'] . "\n", FILE_APPEND);

// Create database connection
$conn = new mysqli($host, $user, $password, $database);

// Check connection
if ($conn->connect_error) {
    file_put_contents($log_file, date("Y-m-d H:i:s") . " - DB Connection failed: " . $conn->connect_error . "\n", FILE_APPEND);
    echo json_encode([
        "status" => "error",
        "message" => "Database connection failed: " . $conn->connect_error
    ]);
    exit();
}

// Check if store_id parameter exists
if (!isset($_GET['store_id'])) {
    file_put_contents($log_file, date("Y-m-d H:i:s") . " - Missing store_id parameter\n", FILE_APPEND);
    echo json_encode([
        "status" => "error",
        "message" => "Missing store_id parameter"
    ]);
    exit();
}

$store_id = intval($_GET['store_id']);

// Validate store_id
if ($store_id <= 0) {
    file_put_contents($log_file, date("Y-m-d H:i:s") . " - Invalid store_id: " . $store_id . "\n", FILE_APPEND);
    echo json_encode([
        "status" => "error",
        "message" => "Invalid store_id"
    ]);
    exit();
}

// Log the store_id
file_put_contents($log_file, date("Y-m-d H:i:s") . " - Processing request for store_id: " . $store_id . "\n", FILE_APPEND);

// Prepare SQL statement to prevent SQL injection
$stmt = $conn->prepare("SELECT id, product_code, product_name, category, price, quantity
                        FROM products
                        WHERE store_id = ?");

if (!$stmt) {
    file_put_contents($log_file, date("Y-m-d H:i:s") . " - Prepare statement failed: " . $conn->error . "\n", FILE_APPEND);
    echo json_encode([
        "status" => "error",
        "message" => "Database query error: " . $conn->error
    ]);
    exit();
}

$stmt->bind_param("i", $store_id);
$stmt->execute();
$result = $stmt->get_result();

// Check if we have results
if ($result->num_rows > 0) {
    $products = [];
    file_put_contents($log_file, date("Y-m-d H:i:s") . " - Found " . $result->num_rows . " products\n", FILE_APPEND);

    while ($row = $result->fetch_assoc()) {
        // Add product to the products array
        $products[] = [
            "id" => $row['id'],
            "product_code" => $row['product_code'],
            "product_name" => $row['product_name'],
            "category" => $row['category'],
            "price" => (float)$row['price'],  // Convert to float
            "quantity" => (int)$row['quantity']   // Convert to int
        ];
    }

    // Return success response with products
    $response = [
        "status" => "success",
        "products" => $products
    ];

    file_put_contents($log_file, date("Y-m-d H:i:s") . " - Successfully processed request for store_id: " . $store_id . "\n", FILE_APPEND);
    echo json_encode($response);
} else {
    // No products found
    file_put_contents($log_file, date("Y-m-d H:i:s") . " - No products found for store_id: " . $store_id . "\n", FILE_APPEND);
    echo json_encode([
        "status" => "success",
        "message" => "No products found for this store",
        "products" => []
    ]);
}

// Close statement and connection
$stmt->close();
$conn->close();
?>