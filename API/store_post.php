<?php
$host = "localhost";
$user = "root";
$password = "";
$dbname = "inventory_management";

$conn = new mysqli($host, $user, $password, $dbname);

if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Database connection failed."]));
}

// Get POST data
$store_name = $_POST['store_name'];
$location   = $_POST['location'];
$full_name  = $_POST['full_name'];
$address    = $_POST['address'];
$email      = $_POST['email'];
$phone      = $_POST['phone'];
$password   = $_POST['Password']; // No hashing here
$role       = "admin";

// Start transaction
$conn->begin_transaction();

try {
    // 1. Insert into stores table
    $stmt1 = $conn->prepare("INSERT INTO stores (store_name, location) VALUES (?, ?)");
    $stmt1->bind_param("ss", $store_name, $location);
    $stmt1->execute();
    $store_id = $stmt1->insert_id;
    $stmt1->close();

    // 2. Insert into user_login
    $stmt2 = $conn->prepare("INSERT INTO user_login (username, Password, role, store_id) VALUES (?, ?, ?, ?)");
    $stmt2->bind_param("sssi", $email, $password, $role, $store_id);
    $stmt2->execute();
    $user_id = $stmt2->insert_id;
    $stmt2->close();

    // 3. Insert into staff_details
    $stmt3 = $conn->prepare("INSERT INTO staff_details (user_id, full_name, email, phone, Role, store_id, address) VALUES (?, ?, ?, ?, ?, ?, ?)");
    $stmt3->bind_param("issssis", $user_id, $full_name, $email, $phone, $role, $store_id, $address);
    $stmt3->execute();
    $stmt3->close();

    $conn->commit();
    echo json_encode(["success" => true, "message" => "Store and admin registered successfully."]);

} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(["success" => false, "message" => "Signup failed: " . $e->getMessage()]);
}

$conn->close();
?>
