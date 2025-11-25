<?php
// get_invoice_details.php - Get single invoice with details
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

$invoice_id = isset($_GET['invoice_id']) ? (int)$_GET['invoice_id'] : null;

if (!$invoice_id) {
    echo json_encode(['error' => 'Invoice ID is required']);
    exit;
}

try {
    // Get invoice details
    $sql = "SELECT 
                i.*,
                c.customer_name,
                c.email as customer_email,
                c.phone as customer_phone,
                c.address as customer_address,
                s.store_name,
                s.store_location
            FROM invoices i
            LEFT JOIN consumers c ON i.customer_id = c.customer_id
            LEFT JOIN stores s ON i.store_id = s.store_id
            WHERE i.invoice_id = ?";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $invoice_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        throw new Exception("Invoice not found");
    }

    $invoice = $result->fetch_assoc();

    // Format the invoice data
    $formatted_invoice = array(
        'invoice_id' => (int)$invoice['invoice_id'],
        'invoice_number' => $invoice['invoice_number'],
        'customer' => array(
            'customer_id' => (int)$invoice['customer_id'],
            'customer_name' => $invoice['customer_name'] ?? 'Walk-in Customer',
            'email' => $invoice['customer_email'],
            'phone' => $invoice['customer_phone'],
            'address' => $invoice['customer_address']
        ),
        'store' => array(
            'store_id' => (int)$invoice['store_id'],
            'store_name' => $invoice['store_name'],
            'store_location' => $invoice['store_location']
        ),
        'issue_date' => $invoice['issue_date'],
        'due_date' => $invoice['due_date'],
        'status' => $invoice['status'],
        'subtotal' => (float)$invoice['subtotal'],
        'tax' => (float)$invoice['tax'],
        'discount' => (float)$invoice['discount'],
        'total' => (float)$invoice['total'],
        'notes' => $invoice['notes'],
        'created_at' => $invoice['created_at'],
        'updated_at' => $invoice['updated_at']
    );

    echo json_encode([
        'success' => true,
        'invoice' => $formatted_invoice
    ]);

} catch (Exception $e) {
    echo json_encode([
        'error' => 'Error fetching invoice details: ' . $e->getMessage()
    ]);
} finally {
    $conn->close();
}
?>