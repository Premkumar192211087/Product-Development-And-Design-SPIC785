<?php
header("Content-Type: application/json");
require 'db.php'; // Database connection

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    // Check required fields
    if (!isset($_POST["product_name"], $_POST["quantity"], $_POST["price"], $_POST["type"], $_POST["status"])) {
        echo json_encode(["success" => false, "message" => "Missing required fields"]);
        exit();
    }

    // Sanitize inputs
    $product_name = trim($_POST["product_name"]);
    $quantity = intval($_POST["quantity"]);
    $price = floatval($_POST["price"]);
    $type = trim($_POST["type"]);
    $status = trim($_POST["status"]);

    // Validate ENUM values
    $valid_types = ['ordered', 'sold', 'product issues'];
    $valid_status = ['pending', 'completed'];

    if (!in_array($type, $valid_types)) {
        echo json_encode(["success" => false, "message" => "Invalid type"]);
        exit();
    }
    if (!in_array($status, $valid_status)) {
        echo json_encode(["success" => false, "message" => "Invalid status"]);
        exit();
    }

    // Prepare SQL Query
    $sql = "INSERT INTO reports (product_name, quantity, price, type, status) VALUES (?, ?, ?, ?, ?)";
    $stmt = $conn->prepare($sql);
    
    if (!$stmt) {
        echo json_encode(["success" => false, "message" => "SQL error: " . $conn->error]);
        exit();
    }

    // Bind Parameters (Fixed: Correct Number of Parameters)
    $stmt->bind_param("sidss", $product_name, $quantity, $price, $type, $status);

    // Execute Query
    if ($stmt->execute()) {
        echo json_encode(["success" => true, "message" => "Report added successfully"]);
    } else {
        echo json_encode(["success" => false, "message" => "Failed to add report"]);
    }

    $stmt->close();
} else {
    echo json_encode(["success" => false, "message" => "Invalid request method"]);
}

$conn->close();
?>
