<?php
header("Content-Type: application/json");
require 'db.php'; // Include your database connection file

// Check if the request method is GET
if ($_SERVER["REQUEST_METHOD"] == "GET") {
    // Check if a specific user ID is requested
    if (isset($_GET["id"])) {
        $id = intval($_GET["id"]); // Sanitize input
        
        // Prepare and execute query
        $stmt = $conn->prepare("SELECT id, username, role FROM user_login WHERE id = ?");
        $stmt->bind_param("i", $id);
        $stmt->execute();
        $result = $stmt->get_result();

        // Check if user exists
        if ($result->num_rows > 0) {
            echo json_encode(["success" => true, "user" => $result->fetch_assoc()]);
        } else {
            echo json_encode(["success" => false, "message" => "User not found"]);
        }
        
        $stmt->close();
    } 
    // If no ID is provided, fetch all users
    else {
        $sql = "SELECT id, username, role FROM user_login";
        $result = $conn->query($sql);
        
        if ($result->num_rows > 0) {
            $users = [];
            while ($row = $result->fetch_assoc()) {
                $users[] = $row;
            }
            echo json_encode(["success" => true, "users" => $users]);
        } else {
            echo json_encode(["success" => false, "message" => "No users found"]);
        }
    }
} 
else {
    echo json_encode(["success" => false, "message" => "Invalid request method"]);
}

$conn->close();
?>
