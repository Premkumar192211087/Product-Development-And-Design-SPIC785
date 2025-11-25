<?php
include 'db_connection.php';

$product_code = $_POST['product_code'];
$manufacturing_date = $_POST['manufacturing_date'];
$expiry_date = $_POST['expiry_date'];
$quantity = $_POST['quantity'];
$store_id = $_POST['store_id'];

// Generate a unique batch ID
$batch_id = uniqid('batch_');

// Get product details from products table
$sql = "SELECT id, product_name FROM products WHERE sku = ? AND store_id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("si", $product_code, $store_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    $product_id = $row['id'];
    $product_name = $row['product_name'];

    // Insert into batch_details table
    $sql = "INSERT INTO batch_details (store_id, product_id, barcode, product_name, mfg_date, exp_date, quantity, batch_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("iisssssi", $store_id, $product_id, $product_code, $product_name, $manufacturing_date, $expiry_date, $quantity, $batch_id);

    if ($stmt->execute()) {
        // Update product quantity
        $sql = "UPDATE products SET quantity = quantity + ? WHERE id = ? AND store_id = ?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("iii", $quantity, $product_id, $store_id);
        $stmt->execute();

        echo json_encode(array("status" => "success", "message" => "Batch added successfully"));
    } else {
        echo json_encode(array("status" => "error", "message" => "Failed to add batch"));
    }
} else {
    echo json_encode(array("status" => "error", "message" => "Product not found"));
}

$conn->close();
?>