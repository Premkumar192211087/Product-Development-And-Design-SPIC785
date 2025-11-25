<?php
// Required headers for API
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: GET");
header("Access-Control-Max-Age: 3600");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

// Prepare response array
$response = array();

// Include database connection
$database_file = 'db.php';
if (!file_exists($database_file)) {
    $response['success'] = false;
    $response['message'] = 'Database configuration file not found.';
    echo json_encode($response);
    exit();
}
include_once $database_file;

// Check if the Database class exists
if (!class_exists('Database')) {
    $response['success'] = false;
    $response['message'] = 'Database class not found.';
    echo json_encode($response);
    exit();
}

// Check required parameters
if (!isset($_GET['notification_id']) || !isset($_GET['store_id'])) {
    $response['success'] = false;
    $response['message'] = 'Missing required parameters: notification_id and store_id are required.';
    echo json_encode($response);
    exit();
}

// Get parameters
$notification_id = intval($_GET['notification_id']);
$store_id = intval($_GET['store_id']);

// Get database connection
$database = new Database();
$conn = $database->getConnection();

try {
    // Prepare SQL statement
    $query = "UPDATE notifications SET status = 'read' WHERE id = ? AND store_id = ?";
    $stmt = $conn->prepare($query);

    if (!$stmt) {
        throw new Exception("Prepare failed: " . $conn->error);
    }

    // Bind parameters and execute
    $stmt->bind_param("ii", $notification_id, $store_id);
    $stmt->execute();

    // Check affected rows
    if ($stmt->affected_rows > 0) {
        $response['success'] = true;
        $response['message'] = 'Notification marked as read.';
    } else {
        $response['success'] = false;
        $response['message'] = 'No matching notification found or already marked as read.';
    }

    // Close statement and connection
    $stmt->close();
    $conn->close();

} catch (Exception $e) {
    $response['success'] = false;
    $response['message'] = 'Database error: ' . $e->getMessage();
}

// Output response
echo json_encode($response);
?>
