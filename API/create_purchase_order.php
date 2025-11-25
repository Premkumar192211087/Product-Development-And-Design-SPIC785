<?php
include 'db.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents("php://input"), true);

if (!$data || !isset($data['store_id']) || !isset($data['supplier_id']) || !isset($data['po_number']) || !isset($data['order_date']) || !isset($data['items'])) {
    echo json_encode(["success" => false, "message" => "Missing required fields."]);
    exit;
}

$store_id = $data['store_id'];
$supplier_id = $data['supplier_id'];
$po_number = $data['po_number'];
$order_date = $data['order_date'];
$expected_date = $data['expected_delivery_date'] ?? null;
$notes = $data['notes'] ?? '';
$total = 0;
$created_by = $data['created_by'] ?? 1;
$items = $data['items']; // array of items: product_id, quantity, unit_price

// Calculate total
foreach ($items as $item) {
    $total += $item['quantity'] * $item['unit_price'];
}

$conn->begin_transaction();

try {
    $stmt = $conn->prepare("INSERT INTO purchase_orders (po_number, supplier_id, store_id, order_date, expected_delivery_date, total_amount, status, created_by, notes) VALUES (?, ?, ?, ?, ?, ?, 'pending', ?, ?)");
    $stmt->bind_param("siissdis", $po_number, $supplier_id, $store_id, $order_date, $expected_date, $total, $created_by, $notes);
    $stmt->execute();

    $po_id = $stmt->insert_id;

    $itemStmt = $conn->prepare("INSERT INTO purchase_order_items (store_id, po_id, product_id, quantity_ordered, unit_price, total_price) VALUES (?, ?, ?, ?, ?, ?)");

    foreach ($items as $item) {
        $product_id = $item['product_id'];
        $quantity = $item['quantity'];
        $unit_price = $item['unit_price'];
        $total_price = $quantity * $unit_price;
        $itemStmt->bind_param("iiiidd", $store_id, $po_id, $product_id, $quantity, $unit_price, $total_price);
        $itemStmt->execute();
    }

    $conn->commit();
    echo json_encode(["success" => true, "message" => "Purchase order added successfully."]);

} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(["success" => false, "message" => "Error adding purchase order.", "error" => $e->getMessage()]);
}
?>
