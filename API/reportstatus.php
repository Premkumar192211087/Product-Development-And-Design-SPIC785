<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

// Database credentials
$servername = "localhost";
$username   = "root";
$password   = "";
$dbname     = "inventory_management";

// Connect to the database
$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["error" => "Database connection failed"]);
    exit();
}

// Read JSON input
$data = json_decode(file_get_contents("php://input"), true);
$report_id = isset($data['report_id']) ? intval($data['report_id']) : 0;
$store_id  = isset($data['store_id']) ? intval($data['store_id']) : 0;

// Validate input
if ($report_id <= 0 || $store_id <= 0) {
    http_response_code(400);
    echo json_encode(["error" => "Invalid report ID or store ID"]);
    exit();
}

// 1. Fetch report
$stmt = $conn->prepare("SELECT product_name, quantity, type, status FROM reports WHERE id = ? AND store_id = ?");
$stmt->bind_param("ii", $report_id, $store_id);
$stmt->execute();
$result = $stmt->get_result();
$report = $result->fetch_assoc();
$stmt->close();

if (!$report) {
    http_response_code(404);
    echo json_encode(["error" => "Report not found for the store"]);
    exit();
}

if ($report['type'] !== "ordered") {
    http_response_code(400);
    echo json_encode(["error" => "Only 'ordered' type reports can update inventory"]);
    exit();
}

if ($report['status'] === "completed") {
    http_response_code(409);
    echo json_encode(["error" => "Report already completed"]);
    exit();
}

$product_name     = $report['product_name'];
$ordered_quantity = intval($report['quantity']);

// 2. Fetch current inventory quantity
$stmt = $conn->prepare("SELECT quantity FROM products WHERE product_name = ? AND store_id = ?");
$stmt->bind_param("si", $product_name, $store_id);
$stmt->execute();
$result = $stmt->get_result();
$inventory = $result->fetch_assoc();
$stmt->close();

if (!$inventory) {
    http_response_code(404);
    echo json_encode(["error" => "Product not found in inventory for this store"]);
    exit();
}

$current_quantity = intval($inventory['quantity']);
$new_quantity     = $current_quantity + $ordered_quantity;

// 3. Update inventory
$stmt = $conn->prepare("UPDATE products SET quantity = ? WHERE product_name = ? AND store_id = ?");
$stmt->bind_param("isi", $new_quantity, $product_name, $store_id);
if (!$stmt->execute()) {
    http_response_code(500);
    echo json_encode(["error" => "Failed to update inventory"]);
    $stmt->close();
    $conn->close();
    exit();
}
$stmt->close();

// 4. Update report status
$stmt = $conn->prepare("UPDATE reports SET status = 'completed' WHERE id = ? AND store_id = ?");
$stmt->bind_param("ii", $report_id, $store_id);
if (!$stmt->execute()) {
    http_response_code(500);
    echo json_encode(["error" => "Failed to update report status"]);
    $stmt->close();
    $conn->close();
    exit();
}
$stmt->close();

$conn->close();

echo json_encode([
    "success" => true,
    "message" => "Inventory updated and report marked as completed"
]);
?>
