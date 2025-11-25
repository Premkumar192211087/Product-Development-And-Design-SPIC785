<?php
header("Content-Type: application/json");
include_once("db.php");

$action = $_GET['action'] ?? '';

switch ($action) {
    case 'add_payment':
        addPayment($conn);
        break;
    case 'get_payments':
        getPayments($conn);
        break;
    case 'get_payment_details':
        getPaymentDetails($conn);
        break;
    case 'get_payment_methods':
        getPaymentMethods();
        break;
    case 'get_vendors':
        getVendors($conn);
        break;
    case 'get_bills':
        getBills($conn);
        break;
    default:
        echo json_encode(['status' => 'error', 'message' => 'Invalid action']);
}

function addPayment($conn) {
    $data = json_decode(file_get_contents("php://input"), true);

    $stmt = $conn->prepare("INSERT INTO payments (store_id, vendor_id, payment_number, payment_date, payment_method, reference_number, amount, bill_id, notes) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

    $stmt->bind_param("iissssdiss",
        $data['store_id'], $data['vendor_id'], $data['payment_number'], $data['payment_date'],
        $data['payment_method'], $data['reference_number'], $data['amount'], $data['bill_id'], $data['notes']);

    if ($stmt->execute()) {
        echo json_encode(['status' => 'success', 'payment_id' => $stmt->insert_id]);
    } else {
        echo json_encode(['status' => 'error', 'message' => $stmt->error]);
    }

    $stmt->close();
}

function getPayments($conn) {
    $store_id = $_GET['store_id'];
    $sql = "SELECT p.*, v.supplier_name AS vendor_name 
            FROM payments p 
            JOIN suppliers v ON p.vendor_id = v.supplier_id 
            WHERE p.store_id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $store_id);
    $stmt->execute();
    $result = $stmt->get_result();
    $payments = $result->fetch_all(MYSQLI_ASSOC);
    echo json_encode($payments);
}

function getPaymentDetails($conn) {
    $payment_id = $_GET['payment_id'];
    $sql = "SELECT p.*, v.supplier_name AS vendor_name 
            FROM payments p 
            JOIN suppliers v ON p.vendor_id = v.supplier_id 
            WHERE p.payment_id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $payment_id);
    $stmt->execute();
    $result = $stmt->get_result();
    $payment = $result->fetch_assoc();
    echo json_encode($payment);
}

function getPaymentMethods() {
    $methods = ["Cash", "Credit Card", "Bank Transfer", "UPI", "Cheque"];
    echo json_encode($methods);
}

function getVendors($conn) {
    $store_id = $_GET['store_id'];
    $result = $conn->query("SELECT supplier_id, supplier_name FROM suppliers WHERE store_id = $store_id");
    $vendors = $result->fetch_all(MYSQLI_ASSOC);
    echo json_encode($vendors);
}

function getBills($conn) {
    $vendor_id = $_GET['vendor_id'];
    $result = $conn->query("SELECT bill_id, bill_number FROM bills WHERE vendor_id = $vendor_id");
    $bills = $result->fetch_all(MYSQLI_ASSOC);
    echo json_encode($bills);
}
