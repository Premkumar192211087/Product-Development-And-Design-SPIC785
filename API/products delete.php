<?php
header("Content-Type: application/json");
require 'db.php'; // Include database connection

// Check if request method is POST
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    // Check if 'id' is provided
    if (!isset($_POST["id"])) {
        echo json_encode(["success" => false, "message" => "Product ID is required"]);
        exit();
    }

    $id = intval($_POST["id"]); // Sanitize input

    // Prepare delete statement
    $sql = "DELETE FROM products WHERE id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $id);
    $stmt->execute();

    // Check if deletion was successful
    if ($stmt->affected_rows > 0) {
        echo json_encode(["success" => true, "message" => "Product deleted successfully"]);
    } else {
        echo json_encode(["success" => false, "message" => "Product not found or already deleted"]);
    }

    $stmt->close();
} else {
    echo json_encode(["success" => false, "message" => "Invalid request method"]);
}

$conn->close();
?>
