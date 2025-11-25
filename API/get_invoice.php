<?php
// get_invoices.php - Main invoice listing API
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

// Get parameters from POST request
$store_id = isset($_POST['store_id']) ? (int)$_POST['store_id'] : null;
$search = isset($_POST['search']) ? $_POST['search'] : '';
$status_filter = isset($_POST['status']) ? $_POST['status'] : 'all';

try {
    // Base query to get invoices with customer information
    $sql = "SELECT 
                i.invoice_id,
                i.invoice_number,
                i.customer_id,
                i.store_id,
                i.issue_date,
                i.due_date,
                i.status,
                i.subtotal,
                i.tax,
                i.discount,
                i.total,
                i.notes,
                i.created_at,
                i.updated_at,
                c.customer_name,
                c.email as customer_email,
                c.phone as customer_phone,
                s.store_name
            FROM invoices i
            LEFT JOIN consumers c ON i.customer_id = c.customer_id
            LEFT JOIN stores s ON i.store_id = s.store_id
            WHERE 1=1";

    $params = array();

    // Add store filter if provided
    if ($store_id !== null) {
        $sql .= " AND i.store_id = ?";
        $params[] = $store_id;
    }

    // Add search filter if provided
    if (!empty($search)) {
        $sql .= " AND (
            i.invoice_number LIKE ? OR 
            c.customer_name LIKE ? OR 
            c.email LIKE ? OR
            c.phone LIKE ?
        )";
        $search_param = '%' . $search . '%';
        $params[] = $search_param;
        $params[] = $search_param;
        $params[] = $search_param;
        $params[] = $search_param;
    }

    // Add status filter if not 'all'
    if ($status_filter !== 'all') {
        $sql .= " AND i.status = ?";
        $params[] = $status_filter;
    }

    // Order by issue date (newest first)
    $sql .= " ORDER BY i.issue_date DESC, i.created_at DESC";

    // Prepare and execute query
    $stmt = $conn->prepare($sql);
    if (!empty($params)) {
        $types = str_repeat('s', count($params));
        $stmt->bind_param($types, ...$params);
    }
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result === false) {
        throw new Exception("Query failed: " . $conn->error);
    }

    $invoices = array();

    while ($row = $result->fetch_assoc()) {
        // Format dates for better display
        $issue_date = $row['issue_date'] ? date('d M Y', strtotime($row['issue_date'])) : null;
        $due_date = $row['due_date'] ? date('d M Y', strtotime($row['due_date'])) : null;
        
        // Calculate days overdue if applicable
        $days_overdue = 0;
        $is_overdue = false;
        if ($row['due_date'] && $row['status'] !== 'paid') {
            $today = new DateTime();
            $due_date_obj = new DateTime($row['due_date']);
            if ($today > $due_date_obj) {
                $days_overdue = $today->diff($due_date_obj)->days;
                $is_overdue = true;
            }
        }

        // Determine display status
        $display_status = $row['status'];
        if ($is_overdue && $row['status'] !== 'paid') {
            $display_status = 'overdue';
        }

        $invoice = array(
            'invoice_id' => (int)$row['invoice_id'],
            'invoice_number' => $row['invoice_number'],
            'customer_id' => (int)$row['customer_id'],
            'customer_name' => $row['customer_name'] ?? 'Walk-in Customer',
            'customer_email' => $row['customer_email'],
            'customer_phone' => $row['customer_phone'],
            'store_id' => (int)$row['store_id'],
            'store_name' => $row['store_name'],
            'issue_date' => $issue_date,
            'due_date' => $due_date,
            'status' => $row['status'],
            'display_status' => $display_status,
            'subtotal' => (float)$row['subtotal'],
            'tax' => (float)$row['tax'],
            'discount' => (float)$row['discount'],
            'total' => (float)$row['total'],
            'formatted_total' => '₹' . number_format((float)$row['total'], 2),
            'notes' => $row['notes'],
            'is_overdue' => $is_overdue,
            'days_overdue' => $days_overdue,
            'created_at' => $row['created_at'],
            'updated_at' => $row['updated_at']
        );

        $invoices[] = $invoice;
    }

    // Get statistics
    $stats_sql = "SELECT 
                    COUNT(*) as total_invoices,
                    COUNT(CASE WHEN status = 'paid' THEN 1 END) as paid_invoices,
                    COUNT(CASE WHEN status = 'unpaid' THEN 1 END) as unpaid_invoices,
                    COUNT(CASE WHEN status = 'unpaid' AND due_date < CURDATE() THEN 1 END) as overdue_invoices,
                    SUM(total) as total_amount,
                    SUM(CASE WHEN status = 'paid' THEN total ELSE 0 END) as paid_amount,
                    SUM(CASE WHEN status = 'unpaid' THEN total ELSE 0 END) as outstanding_amount
                  FROM invoices";

    if ($store_id !== null) {
        $stats_sql .= " WHERE store_id = $store_id";
    }

    $stats_result = $conn->query($stats_sql);
    $stats = $stats_result->fetch_assoc();

    // Response
    $response = array(
        'success' => true,
        'invoices' => $invoices,
        'total_count' => count($invoices),
        'statistics' => array(
            'total_invoices' => (int)$stats['total_invoices'],
            'paid_invoices' => (int)$stats['paid_invoices'],
            'unpaid_invoices' => (int)$stats['unpaid_invoices'],
            'overdue_invoices' => (int)$stats['overdue_invoices'],
            'total_amount' => (float)$stats['total_amount'],
            'paid_amount' => (float)$stats['paid_amount'],
            'outstanding_amount' => (float)$stats['outstanding_amount']
        )
    );

    echo json_encode($response);

} catch (Exception $e) {
    echo json_encode([
        'error' => 'Error fetching invoices: ' . $e->getMessage()
    ]);
} finally {
    $conn->close();
}
?>