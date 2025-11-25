<?php
header("Content-Type: application/json");
require_once "db.php"; // Ensure database connection is included

$response = ["success" => false];

if ($_SERVER["REQUEST_METHOD"] === "POST") {
    // If only product_name is provided, return product price
    if (isset($_POST["product_name"]) && !isset($_POST["store_id"])) {
        $product_name = trim($_POST["product_name"]);
        
        if (!empty($product_name)) {
            $stmt = $conn->prepare("SELECT price FROM products WHERE product_name = ? LIMIT 1");
            $stmt->bind_param("s", $product_name);
            $stmt->execute();
            $result = $stmt->get_result();
            
            if ($row = $result->fetch_assoc()) {
                $response["success"] = true;
                $response["price"] = floatval($row["price"]);
            } else {
                $response["error"] = "Product not found.";
            }

            $stmt->close();
        } else {
            $response["error"] = "Product name is required.";
        }

        echo json_encode($response);
        exit; // Stop further execution
    }

    // Process order submission
    if (isset($_POST["store_id"], $_POST["product_name"], $_POST["quantity"])) {
        $store_id = intval($_POST["store_id"]);
        $product_name = trim($_POST["product_name"]);
        $quantity = intval($_POST["quantity"]);
        $status = "Pending";

        // Validate inputs
        if ($store_id > 0 && !empty($product_name) && $quantity > 0) {
            // Fetch product price from database
            $stmt = $conn->prepare("SELECT price FROM products WHERE product_name = ? LIMIT 1");
            $stmt->bind_param("s", $product_name);
            $stmt->execute();
            $result = $stmt->get_result();
            
            if ($row = $result->fetch_assoc()) {
                $price = floatval($row["price"]); // Get price from database
                $total_price = $price * $quantity; // Calculate total price

                // Insert order into reports table
                $stmt = $conn->prepare("INSERT INTO reports (store_id, product_name, quantity, price, status) VALUES (?, ?, ?, ?, ?)");
                $stmt->bind_param("isids", $store_id, $product_name, $quantity, $total_price, $status);

                if ($stmt->execute()) {
                    $response["success"] = true;
                    $response["message"] = "Order placed successfully.";
                    $response["total_price"] = $total_price;
                } else {
                    $response["error"] = "Failed to insert order.";
                }

                $stmt->close();
            } else {
                $response["error"] = "Product not found.";
            }
        } else {
            $response["error"] = "Invalid input values.";
        }
    } else {
        $response["error"] = "Missing required fields.";
    }
} else {
    $response["error"] = "Invalid request method.";
}

echo json_encode($response);
?>
