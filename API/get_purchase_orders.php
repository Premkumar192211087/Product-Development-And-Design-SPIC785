<?php
include 'db.php';

header('Content-Type: application/json');

$store_id = isset($_GET['store_id']) ? intval($_GET['store_id']) : 0;
$filter = isset($_GET['status']) ? $_GET['status'] : 'All';
$search = isset($_GET['search']) ? trim($_GET['search']) : '';
$sort = isset($_GET['sort']) ? $_GET['sort'] : 'Newest First';

if ($store_id === 0) {
    echo json_encode(["error" => "Missing or invalid store_id"]);
    exit;
}

// Base query
$query = "SELECT po.po_id, po.po_number, po.store_id, po.order_date, po.expected_delivery_date,
                 po.status, po.total_amount, po.notes, po.created_at, po.updated_at,
                 s.supplier_name
          FROM purchase_orders po
          JOIN suppliers s ON po.supplier_id = s.supplier_id
          WHERE po.store_id = ?";

// Apply status filter
if ($filter !== 'All') {
    $query .= " AND po.status = ?";
}

// Apply search
if (!empty($search)) {
    $query .= " AND (po.po_number LIKE ? OR s.supplier_name LIKE ?)";
}

// Apply sort
switch ($sort) {
    case 'Oldest First':
        $query .= " ORDER BY po.created_at ASC";
        break;
    case 'PO Number (A-Z)':
        $query .= " ORDER BY po.po_number ASC";
        break;
    case 'PO Number (Z-A)':
        $query .= " ORDER BY po.po_number DESC";
        break;
    case 'Vendor Name (A-Z)':
        $query .= " ORDER BY s.supplier_name ASC";
        break;
    case 'Vendor Name (Z-A)':
        $query .= " ORDER BY s.supplier_name DESC";
        break;
    case 'Amount (High to Low)':
        $query .= " ORDER BY po.total_amount DESC";
        break;
    case 'Amount (Low to High)':
        $query .= " ORDER BY po.total_amount ASC";
        break;
    default: // Newest First
        $query .= " ORDER BY po.created_at DESC";
        break;
}

// Prepare statement
$stmt = $conn->prepare($query);

if ($filter !== 'All' && !empty($search)) {
    $likeSearch = "%" . $search . "%";
    $stmt->bind_param("isss", $store_id, $filter, $likeSearch, $likeSearch);
} elseif ($filter !== 'All') {
    $stmt->bind_param("is", $store_id, $filter);
} elseif (!empty($search)) {
    $likeSearch = "%" . $search . "%";
    $stmt->bind_param("iss", $store_id, $likeSearch, $likeSearch);
} else {
    $stmt->bind_param("i", $store_id);
}

$stmt->execute();
$result = $stmt->get_result();

$orders = [];
while ($row = $result->fetch_assoc()) {
    $orders[] = [
        "id" => $row['po_id'],
        "po_number" => $row['po_number'],
        "vendor_id" => "", // Optional
        "vendor_name" => $row['supplier_name'],
        "store_id" => $row['store_id'],
        "po_date" => $row['order_date'],
        "expected_date" => $row['expected_delivery_date'],
        "status" => $row['status'],
        "subtotal" => floatval($row['total_amount']),
        "tax" => 0,
        "discount" => 0,
        "total" => floatval($row['total_amount']),
        "notes" => $row['notes'],
        "created_at" => $row['created_at'],
        "updated_at" => $row['updated_at']
    ];
}

echo json_encode(["purchase_orders" => $orders]);
?>
