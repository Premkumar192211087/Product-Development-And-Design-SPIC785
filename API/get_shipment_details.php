<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

require 'db.php'; // your DB connection

if (!isset($_GET['shipment_id'])) {
    echo json_encode(['status' => 'error', 'message' => 'Missing shipment_id']);
    exit;
}

$shipment_id = intval($_GET['shipment_id']);

$sql = "SELECT * FROM shipments WHERE shipment_id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $shipment_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $shipment = $result->fetch_assoc();
    echo json_encode(['status' => 'success', 'shipment' => $shipment]);
} else {
    echo json_encode(['status' => 'error', 'message' => 'Shipment not found']);
}
