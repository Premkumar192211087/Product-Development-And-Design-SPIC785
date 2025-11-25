<?php
// create_invoice.php - Create new invoice API
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

// Get JSON input
$json = file_get_contents('php://input');
$data = json_decode($json, true);

if (!$data) {
    echo json_encode(['error' => 'Invalid JSON data']);
    exit;
}

try {
    // Start transaction
    $conn->begin_transaction();

    // Validate required fields
    $required_fields = ['customer_id', 'store_id', 'issue_date', 'subtotal', 'total'];
    foreach ($required_fields as $field) {
        if (!isset($data[$field])) {
            throw new Exception("Missing required field: $field");
        }
    }

    // Generate invoice number
    $invoice_number = generateInvoiceNumber($conn, $data['store_id']);

    // Insert invoice
    $sql = "INSERT INTO invoices (
                customer_id, store_id, invoice_number, issue_date, due_date, 
                status, subtotal, tax, discount, total, notes
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    $status = $data['status'] ?? 'unpaid';
    $tax = $data['tax'] ?? 0.00;
    $discount = $data['discount'] ?? 0.00;
    $due_date = $data['due_date'] ?? null;
    $notes = $data['notes'] ?? null;

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("iissssdddds", 
        $data['customer_id'],
        $data['store_id'],
        $invoice_number,
        $data['issue_date'],
        $due_date,
        $status,
        $data['subtotal'],
        $tax,
        $discount,
        $data['total'],
        $notes
    );

    if (!$stmt->execute()) {
        throw new Exception("Failed to create invoice: " . $stmt->error);
    }

    $invoice_id = $conn->insert_id;

    // Insert invoice items if provided
    if (isset($data['items']) && is_array($data['items'])) {
        foreach ($data['items'] as $item) {
            $item_sql = "INSERT INTO invoice_items (
                            invoice_id, product_id, quantity, unit_price, 
                            discount_percent, discount_amount, total_price
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            $item_stmt = $conn->prepare($item_sql);
            $item_stmt->bind_param("iiidddd",
                $invoice_id,
                $item['product_id'],
                $item['quantity'],
                $item['unit_price'],
                $item['discount_percent'] ?? 0.00,
                $item['discount_amount'] ?? 0.00,
                $item['total_price']
            );

            if (!$item_stmt->execute()) {
                throw new Exception("Failed to add invoice item: " . $item_stmt->error);
            }
        }
    }

    // Commit transaction
    $conn->commit();

    echo json_encode([
        'success' => true,
        'message' => 'Invoice created successfully',
        'invoice_id' => $invoice_id,
        'invoice_number' => $invoice_number
    ]);

} catch (Exception $e) {
    $conn->rollback();
    echo json_encode([
        'error' => 'Error creating invoice: ' . $e->getMessage()
    ]);
} finally {
    $conn->close();
}

// Function to generate invoice number
function generateInvoiceNumber($conn, $store_id) {
    $prefix = "INV-" . str_pad($store_id, 2, '0', STR_PAD_LEFT) . "-";
    $year = date('Y');
    
    // Get the last invoice number for this store and year
    $sql = "SELECT invoice_number FROM invoices 
            WHERE store_id = ? AND invoice_number LIKE ? 
            ORDER BY invoice_id DESC LIMIT 1";
    
    $like_pattern = $prefix . $year . "%";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("is", $store_id, $like_pattern);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($row = $result->fetch_assoc()) {
        // Extract the sequence number and increment
        $last_number = $row['invoice_number'];
        $sequence = (int)substr($last_number, -4) + 1;
    } else {
        $sequence = 1;
    }
    
    return $prefix . $year . "-" . str_pad($sequence, 4, '0', STR_PAD_LEFT);
}
?>