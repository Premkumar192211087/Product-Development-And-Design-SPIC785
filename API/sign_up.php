<?php
require 'db.php'; // your DB connection file

// Set content type to JSON
header('Content-Type: application/json');

// Normalize keys to lowercase
$_POST = array_change_key_case($_POST, CASE_LOWER);

$response = ["status" => "error", "message" => "Something went wrong"];

// Debug logs
error_log("REQUEST_METHOD: " . $_SERVER['REQUEST_METHOD']);
error_log("POST data: " . print_r($_POST, true));
error_log("Raw input: " . file_get_contents('php://input'));

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    $response['message'] = 'Only POST requests are allowed.';
    echo json_encode($response);
    exit;
}

$response['debug_received_fields'] = array_keys($_POST);

// Updated required fields - removed 'role' since it's now hardcoded
$requiredFields = ['store_name', 'store_location', 'username', 'password', 'full_name', 'email', 'phone', 'address'];
$missingFields = [];
$receivedFields = [];

foreach ($requiredFields as $field) {
    if (isset($_POST[$field])) {
        $receivedFields[$field] = $_POST[$field];
        if (empty(trim($_POST[$field]))) {
            $missingFields[] = $field . " (empty)";
        }
    } else {
        $missingFields[] = $field . " (not set)";
    }
}

if (!empty($missingFields)) {
    $response['message'] = 'Missing or empty fields: ' . implode(', ', $missingFields);
    $response['debug_all_post_keys'] = array_keys($_POST);
    $response['debug_post_values'] = $_POST;
    echo json_encode($response);
    exit;
}

// Sanitize inputs
$storeName = trim($_POST['store_name']);
$storeLocation = trim($_POST['store_location']);
$username = trim($_POST['username']);
$password = $_POST['password']; // plain text password
$role = 'owner'; // Directly set role as owner
$fullName = trim($_POST['full_name']);
$email = trim($_POST['email']);
$phone = trim($_POST['phone']);
$address = trim($_POST['address']);

// Email validation
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    $response['message'] = 'Invalid email format.';
    echo json_encode($response);
    exit;
}

try {
    $conn->autocommit(FALSE);

    // Check if store already exists
    $stmt = $conn->prepare("SELECT store_id FROM stores WHERE store_name = ? AND store_location = ?");
    $stmt->bind_param("ss", $storeName, $storeLocation);
    $stmt->execute();
    $stmt->store_result();
    if ($stmt->num_rows > 0) {
        $response['message'] = "This store already exists at that location.";
        echo json_encode($response);
        $stmt->close();
        exit;
    }
    $stmt->close();

    // Check if username exists
    $stmt = $conn->prepare("SELECT id FROM user_login WHERE username = ?");
    $stmt->bind_param("s", $username);
    $stmt->execute();
    $stmt->store_result();
    if ($stmt->num_rows > 0) {
        $response['message'] = "Username already exists.";
        echo json_encode($response);
        $stmt->close();
        exit;
    }
    $stmt->close();

    // Check if email exists
    $stmt = $conn->prepare("SELECT staff_id FROM staff_details WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $stmt->store_result();
    if ($stmt->num_rows > 0) {
        $response['message'] = "Email is already registered.";
        echo json_encode($response);
        $stmt->close();
        exit;
    }
    $stmt->close();

    // Insert into stores
    $stmt = $conn->prepare("INSERT INTO stores (store_name, store_location) VALUES (?, ?)");
    $stmt->bind_param("ss", $storeName, $storeLocation);
    $stmt->execute();
    $storeId = $stmt->insert_id;
    $stmt->close();

    // Insert into user_login WITHOUT password hashing - role is now hardcoded as 'owner'
    $stmt = $conn->prepare("INSERT INTO user_login (username, password, store_id, role) VALUES (?, ?, ?, ?)");
    $stmt->bind_param("ssis", $username, $password, $storeId, $role);
    $stmt->execute();
    $userId = $stmt->insert_id;
    $stmt->close();

    // Insert into staff_details - role is hardcoded as 'owner'
    $stmt = $conn->prepare("INSERT INTO staff_details (full_name, user_id, email, phone, role, store_id, address) VALUES (?, ?, ?, ?, ?, ?, ?)");
    $stmt->bind_param("sisssss", $fullName, $userId, $email, $phone, $role, $storeId, $address);
    $stmt->execute();
    $stmt->close();

    $conn->commit();

    $response['status'] = "success";
    $response['message'] = "Account created successfully.";

} catch (Exception $e) {
    $conn->rollback();
    $response['message'] = "Error: " . $e->getMessage();
} finally {
    $conn->autocommit(TRUE);
}

echo json_encode($response);
?>