<?php
include 'db.php';

if (!isset($_GET['po_id']) || !isset($_GET['store_id'])) {
    echo json_encode(["error" => "Missing po_id or store_id"]);
    exit;
}

$po_id = intval($_GET['po_id']);
$store_id = intval($_GET['store_id']);

$sql = "SELECT poi.*, p.product_name
        FROM purchase_order_items poi
        JOIN products p ON poi.product_id = p.id
        WHERE poi.po_id = ? AND poi.store_id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ii", $po_id, $store_id);
$stmt->execute();
$result = $stmt->get_result();

$items = [];
while ($row = $result->fetch_assoc()) {
    $items[] = [
        "product_id" => $row['product_id'],
        "product_name" => $row['product_name'],
        "quantity_ordered" => intval($row['quantity_ordered']),
        "quantity_received" => intval($row['quantity_received']),
        "unit_price" => floatval($row['unit_price']),
        "total_price" => floatval($row['total_price']),
    ];
}

echo json_encode(["items" => $items]);
?>
