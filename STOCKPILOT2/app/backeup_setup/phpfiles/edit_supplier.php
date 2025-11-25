<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: PUT');

$host = "localhost";
$db = "inventory_management";
$user = "root";
$pass = "";

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "DB connection failed"]));
}

$data = json_decode(file_get_contents("php://input"), true);

$supplier_id = $data['supplier_id'];
$supplier_name = $data['supplier_name'];
$contact_person = $data['contact_person'];
$email = $data['email'];
$phone = $data['phone'];
$address = $data['address'];
$payment_terms = $data['payment_terms'];
$status = $data['status'];

$sql = "UPDATE suppliers 
        SET supplier_name = ?, contact_person = ?, email = ?, phone = ?, address = ?, payment_terms = ?, status = ?
        WHERE supplier_id = ?";

$stmt = $conn->prepare($sql);
$stmt->bind_param("sssssssi", $supplier_name, $contact_person, $email, $phone, $address, $payment_terms, $status, $supplier_id);

if ($stmt->execute()) {
    echo json_encode(["status" => "success", "message" => "Supplier updated"]);
} else {
    echo json_encode(["status" => "error", "message" => $stmt->error]);
}

$conn->close();
?>
