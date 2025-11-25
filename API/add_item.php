<?php
header('Content-Type: application/json');

// Database config
$host = "localhost";
$db = "inventory_management";
$user = "root";
$pass = "";

// Connect
$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    echo json_encode(['status' => 'error', 'message' => 'Database connection failed']);
    exit;
}

// Get POST data safely
$store_id     = $_POST['store_id'] ?? null;
$product_name = trim($_POST['product_name'] ?? '');
$sku          = trim($_POST['sku'] ?? '');
$category     = trim($_POST['category'] ?? '');
$price        = trim($_POST['price'] ?? '');
$quantity     = trim($_POST['quantity'] ?? '');
$barcode      = trim($_POST['barcode'] ?? '');
$enable_batch = trim($_POST['enable_batch'] ?? 'no');
$batch_id     = trim($_POST['batch_id'] ?? '');
$mfg_date     = trim($_POST['mfg_date'] ?? '');
$exp_date     = trim($_POST['exp_date'] ?? '');

// Validate required fields
if (!$store_id || !$product_name || !$sku || !$price || !$quantity) {
    echo json_encode(['status' => 'error', 'message' => 'Missing required fields']);
    exit;
}

// Check for duplicates (same SKU within the same store)
$checkSql = "SELECT id FROM products WHERE store_id = ? AND sku = ?";
$stmtCheck = $conn->prepare($checkSql);
$stmtCheck->bind_param("is", $store_id, $sku);
$stmtCheck->execute();
$stmtCheck->store_result();

if ($stmtCheck->num_rows > 0) {
    echo json_encode(['status' => 'error', 'message' => 'Product with same SKU already exists in this store']);
    $stmtCheck->close();
    $conn->close();
    exit;
}
$stmtCheck->close();

// Insert query
$sql = "INSERT INTO products 
    (store_id, product_name, sku, category, price, quantity, barcode, enable_batch, batch_id, mfg_date, exp_date, status) 
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'active')";

$stmt = $conn->prepare($sql);
$stmt->bind_param("isssdisssss", 
    $store_id, 
    $product_name, 
    $sku, 
    $category, 
    $price, 
    $quantity, 
    $barcode, 
    $enable_batch, 
    $batch_id, 
    $mfg_date, 
    $exp_date
);

if ($stmt->execute()) {
    echo json_encode(['status' => 'success', 'message' => 'Product added successfully']);
} else {
    echo json_encode(['status' => 'error', 'message' => 'Failed to add product']);
}

$stmt->close();
$conn->close();
