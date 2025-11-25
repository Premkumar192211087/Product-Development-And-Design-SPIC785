<?php
require 'db.php'; // Ensure database connection

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $store_id = $_POST['store_id'];
    $role = $_POST['role'];
    $username = $_POST['username'];
    $password = $_POST['password']; // No hashing as per your request

    // Check if user exists with the given store and role
    $query = "SELECT * FROM users WHERE store_id = ? AND role = ? AND username = ? AND password = ?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("ssss", $store_id, $role, $username, $password);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $user = $result->fetch_assoc();
        
        echo json_encode([
            "status" => "success",
            "message" => "Login successful",
            "user" => [
                "id" => $user['id'],
                "username" => $user['username'],
                "role" => $user['role'],
                "store_id" => $user['store_id']
            ]
        ]);
    } else {
        echo json_encode(["status" => "error", "message" => "Invalid credentials or access denied"]);
    }
    $stmt->close();
    $conn->close();
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request"]);
}
?>
