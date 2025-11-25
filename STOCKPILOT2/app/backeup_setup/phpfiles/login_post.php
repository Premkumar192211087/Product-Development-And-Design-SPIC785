<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

// Database configuration
$servername = "localhost";  // Fixed: server name should be localhost
$username = "root";         // Fixed: username should be root
$password = "";             // Your database password
$dbname = "inventory_management";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(array(
        "status" => "error",
        "message" => "Database connection failed"
    ));
    exit;
}

// Check if POST data exists
if (!isset($_POST['username']) || !isset($_POST['password'])) {
    http_response_code(400);
    echo json_encode(array(
        "status" => "error",
        "message" => "Username and password are required"
    ));
    exit;
}

$input_username = $_POST['username'];
$input_password = $_POST['password'];

// Validate input
if (empty($input_username) || empty($input_password)) {
    http_response_code(400);
    echo json_encode(array(
        "status" => "error",
        "message" => "Username and password cannot be empty"
    ));
    exit;
}

// SECURE: Use prepared statements to prevent SQL injection
$sql = "SELECT store_id, role, password FROM user_login WHERE username = ?";
$stmt = $conn->prepare($sql);

if (!$stmt) {
    http_response_code(500);
    echo json_encode(array(
        "status" => "error",
        "message" => "Database query preparation failed"
    ));
    exit;
}

$stmt->bind_param("s", $input_username);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    
    // Plain text password comparison (as requested)
    if ($input_password === $row['password']) {
        $store_id = $row['store_id'];
        $role = $row['role'];
        
        // Get store name using prepared statement
        $store_sql = "SELECT store_name FROM Stores WHERE store_id = ?";
        $store_stmt = $conn->prepare($store_sql);
        $store_stmt->bind_param("i", $store_id);
        $store_stmt->execute();
        $store_result = $store_stmt->get_result();
        
        if ($store_result->num_rows > 0) {
            $store_name = $store_result->fetch_assoc()['store_name'];
        } else {
            $store_name = "Unknown Store";
        }
        
        // Success response
        http_response_code(200);
        echo json_encode(array(
            "status" => "success",
            "store_id" => $store_id,
            "store_name" => $store_name,
            "role" => $role,
            "message" => "Login successful"
        ));
        
        $store_stmt->close();
    } else {
        // Invalid password
        http_response_code(401);
        echo json_encode(array(
            "status" => "failed",
            "message" => "Invalid username or password"
        ));
    }
} else {
    // User not found
    http_response_code(401);
    echo json_encode(array(
        "status" => "failed",
        "message" => "Invalid username or password"
    ));
}

$stmt->close();
$conn->close();
?>