<?php
header("Content-Type: application/json");
require 'db.php'; // Database connection

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    if (!isset($_POST["id"])) {
        echo json_encode(["success" => false, "message" => "Product ID is required"]);
        exit();
    }

    $id = intval($_POST["id"]);
    $sql = "DELETE FROM reports WHERE id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $id);
    
    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            echo json_encode(["success" => true, "message" => "Product deleted successfully"]);
        } else {
            echo json_encode(["success" => false, "message" => "Product not found"]);
        }
    } else {
        echo json_encode(["success" => false, "message" => "Failed to delete product"]);
    }

    $stmt->close();
} else {
    echo json_encode(["success" => false, "message" => "Invalid request method"]);
}

$conn->close();
?>
