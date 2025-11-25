<?php
include 'db.php';

parse_str(file_get_contents("php://input"), $_DELETE);

if (!isset($_GET['id']) || !isset($_DELETE['store_id'])) {
    echo json_encode(["success" => false, "message" => "Missing ID or store_id"]);
    exit;
}

$id = intval($_GET['id']);
$store_id = intval($_DELETE['store_id']);

$sql = "DELETE FROM purchase_orders WHERE po_id = ? AND store_id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ii", $id, $store_id);
if ($stmt->execute()) {
    echo json_encode(["success" => true]);
} else {
    echo json_encode(["success" => false, "message" => "Delete failed"]);
}
?>
