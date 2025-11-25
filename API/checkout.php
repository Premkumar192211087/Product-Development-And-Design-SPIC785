<?php
require 'db.php'; // Include database connection

header('Content-Type: application/json');

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $data = json_decode(file_get_contents("php://input"), true); // Decode JSON data

    if (!isset($data['products']) || !is_array($data['products'])) {
        echo json_encode(["success" => false, "message" => "Invalid data format."]);
        exit();
    }

    $conn->begin_transaction(); // Start transaction

    try {
        foreach ($data['products'] as $product) {
            $product_name = isset($product['product_name']) ? trim($product['product_name']) : '';
            $price = isset($product['price']) ? (float)$product['price'] : 0;
            $quantity = isset($product['quantity']) ? (int)$product['quantity'] : 0;
            $store_id = isset($product['store_id']) ? (int)$product['store_id'] : 0;

            if (empty($product_name) || $price <= 0 || $quantity <= 0 || $store_id <= 0) {
                throw new Exception("Invalid product details provided.");
            }

            // Insert into reports table
            $sql = "INSERT INTO reports (product_name, price, quantity, type, status, store_id) 
                    VALUES (?, ?, ?, 'sold', 'completed', ?)";

            $stmt = $conn->prepare($sql);
            $stmt->bind_param("sdii", $product_name, $price, $quantity, $store_id);

            if (!$stmt->execute()) {
                throw new Exception("Error inserting report: " . $stmt->error);
            }
            $stmt->close();

            // Reduce quantity from products table
            $updateSql = "UPDATE products SET quantity = quantity - ? 
                          WHERE product_name = ? AND store_id = ? AND quantity >= ?";
            $updateStmt = $conn->prepare($updateSql);
            $updateStmt->bind_param("isii", $quantity, $product_name, $store_id, $quantity);

            if (!$updateStmt->execute() || $updateStmt->affected_rows == 0) {
                throw new Exception("Stock update failed: Not enough stock or product not found.");
            }
            $updateStmt->close();
        }

        $conn->commit(); // Commit transaction
        echo json_encode(["success" => true, "message" => "Checkout successful, stock updated."]);
    } catch (Exception $e) {
        $conn->rollback(); // Rollback on error
        echo json_encode(["success" => false, "message" => $e->getMessage()]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Invalid request method."]);
}

$conn->close();
?>
