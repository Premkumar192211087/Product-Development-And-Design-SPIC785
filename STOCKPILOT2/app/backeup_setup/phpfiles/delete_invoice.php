<?php
// delete_invoice.php - Delete invoice API
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

$invoice_id = isset($_POST['invoice_id']) ? (int)$_POST['invoice_id'] : null;

if (!$invoice_id) {
    echo json_encode(['error' => 'Invoice ID is required']);
    exit;
}

try {
    // Start transaction
    $conn->begin_transaction();

    // Check if invoice exists and get its status
    $check_sql = "SELECT status FROM invoices WHERE invoice_id = ?";
    $check_stmt = $conn->prepare($check_sql);
    $check_stmt->bind_param("i", $invoice_id);
    $check_stmt->execute();
    $result = $check_stmt->get_result();

    if ($result->num_rows === 0) {
        throw new Exception("Invoice not found");
    }

    $invoice = $result->fetch_assoc();
    
    // Prevent deletion of paid invoices for audit purposes
    if ($invoice['status'] === 'paid') {
        throw new Exception("Cannot delete paid invoices");
    }

    // Delete invoice items first (if you have invoice_items table)
    $delete_items_sql = "DELETE FROM invoice_items WHERE invoice_id = ?";
    $delete_items_stmt = $conn->prepare($delete_items_sql);
    $delete_items_stmt->bind_param("i", $invoice_id);
    $delete_items_stmt->execute();

    // Delete invoice
    $delete_sql = "DELETE FROM invoices WHERE invoice_id = ?";
    $delete_stmt = $conn->prepare($delete_sql);
    $delete_stmt->bind_param("i", $invoice_id);

    if (!$delete_stmt->execute()) {
        throw new Exception("Failed to delete invoice: " . $delete_stmt->error);
    }

    // Commit transaction
    $conn->commit();

    echo json_encode([
        'success' => true,
        'message' => 'Invoice deleted successfully'
    ]);

} catch (Exception $e) {
    $conn->rollback();
    echo json_encode([
        'error' => 'Error deleting invoice: ' . $e->getMessage()
    ]);
} finally {
    $conn->close();
}
?>