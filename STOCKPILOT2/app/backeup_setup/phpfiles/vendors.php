<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Content-Type');

// DB connection
$host = "localhost";
$db = "inventory_management";
$user = "root";
$pass = "";

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}

// Parameters
$store_id = isset($_REQUEST['store_id']) ? intval($_REQUEST['store_id']) : 0;
$action = isset($_GET['action']) ? $_GET['action'] : 'suppliers';
$status_filter = isset($_GET['status']) ? strtolower(trim($_GET['status'])) : 'all';
$sort_option = isset($_GET['sort']) ? $_GET['sort'] : 'name_asc';
$search_query = isset($_GET['search']) ? trim($_GET['search']) : '';

if ($store_id <= 0) {
    echo json_encode(["status" => "error", "message" => "Invalid or missing store_id"]);
    exit;
}

// -------- SUPPLIER LIST --------
if ($action === 'suppliers') {
    $query = "SELECT supplier_id, supplier_name, contact_person, email, phone, address, status, created_at 
              FROM suppliers 
              WHERE store_id = ?";
    
    $params = [$store_id];
    $types = "i";

    // Filter
    if ($status_filter === 'active' || $status_filter === 'inactive') {
        $query .= " AND LOWER(status) = ?";
        $params[] = $status_filter;
        $types .= "s";
    }

    // Search
    if (!empty($search_query)) {
        $query .= " AND (
            supplier_name LIKE ? OR
            contact_person LIKE ? OR
            email LIKE ? OR
            phone LIKE ?
        )";
        $like = "%" . $search_query . "%";
        $params = array_merge($params, [$like, $like, $like, $like]);
        $types .= "ssss";
    }

    // Sort
    switch ($sort_option) {
        case 'name_desc':
            $query .= " ORDER BY supplier_name DESC";
            break;
        case 'recent':
            $query .= " ORDER BY created_at DESC";
            break;
        default:
            $query .= " ORDER BY supplier_name ASC";
    }

    $stmt = $conn->prepare($query);
    if (!$stmt) {
        echo json_encode(["status" => "error", "message" => "Prepare failed: " . $conn->error]);
        exit;
    }

    $stmt->bind_param($types, ...$params);
    $stmt->execute();
    $result = $stmt->get_result();

    $suppliers = [];
    while ($row = $result->fetch_assoc()) {
        $suppliers[] = [
            "vendor_id" => $row['supplier_id'],
            "vendor_name" => $row['supplier_name'],
            "contact_person" => $row['contact_person'],
            "email" => $row['email'],
            "phone" => $row['phone'],
            "address" => $row['address'],
            "status" => $row['status'],
            "created_at" => $row['created_at'],
            "outstanding_balance" => 0 // dummy if not tracked
        ];
    }

    echo json_encode(["status" => "success", "vendors" => $suppliers]);

// -------- DISTRIBUTION (Dummy Example) --------
} elseif ($action === 'distribution') {
    $query = "SELECT supplier_name AS vendor_name, COUNT(*) * 100 AS total_amount, 100 AS payment_percentage 
              FROM suppliers 
              WHERE store_id = ?
              GROUP BY supplier_id";

    $stmt = $conn->prepare($query);
    $stmt->bind_param("i", $store_id);
    $stmt->execute();
    $result = $stmt->get_result();

    $distribution = [];
    while ($row = $result->fetch_assoc()) {
        $distribution[] = $row;
    }

    echo json_encode(["status" => "success", "distributions" => $distribution]);

} else {
    echo json_encode(["status" => "error", "message" => "Invalid action"]);
}

$conn->close();
?>
