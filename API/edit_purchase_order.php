<?php
include 'db.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents("php://input"), true);

if (!$data || !isset($data['po_id']) || !isset($data['store_id']) || !isset($data['supplier_id']) || !isset($data['po_number']) || !isset($data['order_date']) || !isset($data['items'])) {
    echo json_encode(["success" => false, "message" => "Missing required fields."]);
    exit;
}

$po_id = $data['po_id'];
$store_id = $data['store_id'];
$supplier_id = $data['supplier_id'];
$po_number = $data['po_number'];
$order_date = $data['order_date'];
$expected_date = $data['expected_delivery_date'] ?? null;
$notes = $data['notes'] ?? '';
$total = 0;
$items = $data['items']; // array of items

foreach ($items as $item) {
    $total += $item['quantity'] * $item['unit_price'];
}

$conn->begin_transaction();

try {
    $stmt = $conn->prepare("UPDATE purchase_orders SET po_number = ?, supplier_id = ?, order_date = ?, expected_delivery_date = ?, total_amount = ?, notes = ? WHERE po_id = ? AND store_id = ?");
    $stmt->bind_param("sissdsii", $po_number, $supplier_id, $order_date, $expected_date, $total, $notes, $po_id, $store_id);
    $stmt->execute();

    $delStmt = $conn->prepare("DELETE FROM purchase_order_items WHERE po_id = ? AND store_id = ?");
    $delStmt->bind_param("ii", $po_id, $store_id);
    $delStmt->execute();

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
    echo json_encode(["success" => true, "message" => "Purchase order updated successfully."]);

} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(["success" => false, "message" => "Error updating purchase order.", "error" => $e->getMessage()]);
}
?>
