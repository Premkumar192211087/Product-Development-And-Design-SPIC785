<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: DELETE');

$host = "localhost";
$db = "inventory_management";
$user = "root";
$pass = "";

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "DB connection failed"]));
}

$supplier_id = isset($_GET['supplier_id']) ? intval($_GET['supplier_id']) : 0;

if ($supplier_id <= 0) {
    echo json_encode(["status" => "error", "message" => "Invalid supplier_id"]);
    exit;
}

$sql = "DELETE FROM suppliers WHERE supplier_id = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $supplier_id);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Supplier deleted"]);
} else {
    echo json_encode(["status" => "error", "message" => $stmt->error]);
}

$conn->close();
?>
