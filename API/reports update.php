<?php
include 'db.php'; // Ensure you have a proper database connection

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $id = $_POST['id'];  // Report ID to be updated
    $store_id = $_POST['store_id'];    // Store ID from the user
    $status = $_POST['status'];        // Status: "pending" or "received"

    // Validate inputs
    if (empty($id) || empty($store_id) || empty($status)) {
        echo json_encode(["status" => "error", "message" => "Missing parameters"]);
        exit;
    }

    // Ensure the report belongs to the store before updating
    $query = "UPDATE reports SET status = ? WHERE id = ? AND store_id = ?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("sii", $status, $id, $store_id);

    if ($stmt->execute()) {
        echo json_encode(["status" => "success", "message" => "Report updated successfully"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Failed to update report"]);
    }

    $stmt->close();
    $conn->close();
}
?>
