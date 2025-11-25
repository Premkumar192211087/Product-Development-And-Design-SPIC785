<?php
// DB connection
$host = "localhost";
$user = "root";
$pass = "";
$db = "inventory_management"; // replace with your actual DB name

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    die(json_encode(['error' => 'Database connection failed']));
}

// Get store_id from POST request
if (!isset($_POST['store_id'])) {
    echo json_encode(['error' => 'Missing store_id']);
    exit;
}

$store_id = (int)$_POST['store_id']; // safely cast to int

// 1. Quantity to be packed (reports with type = 'ordered')
$q1 = $conn->query("SELECT SUM(quantity) AS qty_to_be_packed FROM reports WHERE type='ordered' AND store_id=$store_id");
$packed = $q1->fetch_assoc()['qty_to_be_packed'] ?? 0;

// 2. Packages to be shipped (invoiced but pending)
$q2 = $conn->query("SELECT COUNT(*) AS packages_to_ship FROM reports WHERE type='invoiced' AND status='pending' AND store_id=$store_id");
$ship = $q2->fetch_assoc()['packages_to_ship'] ?? 0;

// 3. Packages to be delivered (invoiced and completed)
$q3 = $conn->query("SELECT COUNT(*) AS packages_to_deliver FROM reports WHERE type='invoiced' AND status='completed' AND store_id=$store_id");
$deliver = $q3->fetch_assoc()['packages_to_deliver'] ?? 0;

// 4. Quantity to be invoiced (same as ordered quantity)
$q4 = $conn->query("SELECT SUM(quantity) AS qty_to_invoice FROM reports WHERE type='ordered' AND store_id=$store_id");
$invoice = $q4->fetch_assoc()['qty_to_invoice'] ?? 0;

// 5. Inventory summary
$q5 = $conn->query("SELECT SUM(quantity) AS in_hand FROM reports WHERE type='invoiced' AND status='completed' AND store_id=$store_id");
$in_hand = $q5->fetch_assoc()['in_hand'] ?? 0;

$q6 = $conn->query("SELECT SUM(quantity) AS to_receive FROM reports WHERE type='ordered' AND status='pending' AND store_id=$store_id");
$to_receive = $q6->fetch_assoc()['to_receive'] ?? 0;

// Send JSON response
$response = [
    'quantity_to_be_packed' => (int)$packed,
    'packages_to_be_shipped' => (int)$ship,
    'packages_to_be_delivered' => (int)$deliver,
    'quantity_to_be_invoiced' => (int)$invoice,
    'inventory_summary' => [
        'in_hand' => (int)$in_hand,
        'to_be_received' => (int)$to_receive
    ]
];

header('Content-Type: application/json');
echo json_encode($response);
?>
