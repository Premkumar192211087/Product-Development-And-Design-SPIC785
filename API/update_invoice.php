<?php
// update_invoice_status.php - Update invoice status API
// DB connection
$host = "localhost";
$user = "root";
$pass = "";
$db = "inventory_management";
$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    die(json_encode(['error' => 'Database connection failed']));
}

// Set content type to JSON
header('Content-Type: application/json');

// Get parameters
$invoice_id = isset($_POST['invoice_id']) ? (int)$_POST['invoice_id'] : null;
$status = isset($_POST['status']) ? $_POST['status'] : null;

if (!$invoice_id || !$status) {
    echo json_encode(['error' => 'Invoice ID and status are required']);
    exit;
}

// Validate status
$valid_statuses = ['paid', 'unpaid', 'cancelled', 'draft'];
if (!in_array($status, $valid_statuses)) {
    echo json_encode(['error' => 'Invalid status value']);
    exit;
}

try {
    // Update invoice status
    $sql = "UPDATE invoices SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE invoice_id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("si", $status, $invoice_id);

    if (!$stmt->execute()) {
        throw new Exception("Failed to update invoice status: " . $stmt->error);
    }

    if ($stmt->affected_rows === 0) {
        throw new Exception("Invoice not found");
    }

    echo json_encode([
        'success' => true,
        'message' => 'Invoice status updated successfully'
    ]);

} catch (Exception $e) {
    echo json_encode([
        'error' => 'Error updating invoice status: ' . $e->getMessage()
    ]);
} finally {
    $conn->close();
}
?>