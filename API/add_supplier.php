<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');

$host = "localhost";
$db = "inventory_management";
$user = "root";
$pass = "";

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "DB connection failed"]));
}

$data = json_decode(file_get_contents("php://input"), true);

$store_id = $data['store_id'];
$supplier_name = $data['supplier_name'];
$contact_person = $data['contact_person'];
$email = $data['email'];
$phone = $data['phone'];
$address = $data['address'];
$payment_terms = $data['payment_terms'];
$status = $data['status'];

$sql = "INSERT INTO suppliers (store_id, supplier_name, contact_person, email, phone, address, payment_terms, status)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

$stmt = $conn->prepare($sql);
$stmt->bind_param("isssssss", $store_id, $supplier_name, $contact_person, $email, $phone, $address, $payment_terms, $status);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Supplier added successfully"]);
} else {
    echo json_encode(["status" => "error", "message" => $stmt->error]);
}

$conn->close();
?>
