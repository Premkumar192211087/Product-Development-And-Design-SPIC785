<?php
header("Content-Type: application/json");
require_once "db.php"; // Include database connection

$response = [];

if (!isset($_GET['store_id']) || !isset($_GET['staff_id'])) {
    echo json_encode(["error" => "Missing store_id or staff_id"]);
    exit;
}

$store_id = intval($_GET['store_id']);
$staff_id = intval($_GET['staff_id']);

// Fetch store name
$store_query = "SELECT store_name FROM stores WHERE store_id = ?";
$stmt = $conn->prepare($store_query);
$stmt->bind_param("i", $store_id);
$stmt->execute();
$store_result = $stmt->get_result();
$store_data = $store_result->fetch_assoc();
$response["store_name"] = $store_data['store_name'] ?? "N/A"; // Store name

// Fetch staff details (including address)
$staff_query = "SELECT full_name, email, phone, Role, address FROM staff_details WHERE staff_id = ? AND store_id = ?";
$stmt = $conn->prepare($staff_query);
$stmt->bind_param("ii", $staff_id, $store_id);
$stmt->execute();
$staff_result = $stmt->get_result();

if ($staff_result->num_rows > 0) {
    $staff = $staff_result->fetch_assoc();
    $response["owner_name"] = $staff['full_name'];
    $response["email"] = $staff['email'];
    $response["phone"] = $staff['phone'];
    $response["role"] = $staff['Role']; 
    $response["staff_address"] = $staff['address'] ?? "N/A"; // Fetch staff address
} else {
    echo json_encode(["error" => "Staff not found in this store"]);
    exit;
}

// Fetch total stock quantity
$product_query = "SELECT SUM(quantity) as total_stock FROM products WHERE store_id = ?";
$stmt = $conn->prepare($product_query);
$stmt->bind_param("i", $store_id);
$stmt->execute();
$product_result = $stmt->get_result();
$product_data = $product_result->fetch_assoc();
$response["total_products"] = $product_data['total_stock'] ?? 0;

// Fetch total sales amount
$sales_query = "SELECT SUM(price) as total_sales FROM reports WHERE store_id = ? AND type = 'sold'";
$stmt = $conn->prepare($sales_query);
$stmt->bind_param("i", $store_id);
$stmt->execute();
$sales_result = $stmt->get_result();
$sales_data = $sales_result->fetch_assoc();
$response["total_sales"] = $sales_data['total_sales'] ?? 0;

// Return JSON response
echo json_encode($response);
?>
