<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET');

$host = "localhost";
$db   = "inventory_management";
$user = "root";
$pass = "";

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    echo json_encode(["status" => "error", "message" => "Connection failed: " . $conn->connect_error]);
    exit;
}

// Handle GET to fetch data
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $store_id = isset($_GET['store_id']) ? intval($_GET['store_id']) : 0;
    $action = isset($_GET['action']) ? $_GET['action'] : '';

    if ($action === 'fetch_data') {
        // Fetch customers
        $customers = [];
        $result = $conn->query("SELECT customer_id, customer_name FROM customers WHERE store_id = $store_id AND status = 'active'");
        while ($row = $result->fetch_assoc()) {
            $customers[] = $row;
        }

        // Fetch products
        $products = [];
        $result = $conn->query("SELECT id as product_id, product_name, quantity, price FROM products WHERE store_id = $store_id AND status = 'active'");
        while ($row = $result->fetch_assoc()) {
            $products[] = $row;
        }

        // Static Payment Types & Status
        $payment_methods = ['cash', 'credit_card', 'debit_card', 'upi', 'net_banking', 'cheque'];
        $payment_statuses = ['paid', 'pending', 'partial'];

        echo json_encode([
            "status" => "success",
            "customers" => $customers,
            "products" => $products,
            "payment_methods" => $payment_methods,
            "payment_statuses" => $payment_statuses
        ]);
        exit;
    }
}

// Handle POST to insert sale
$data = json_decode(file_get_contents('php://input'), true);
if (!$data) {
    echo json_encode(["status" => "error", "message" => "Invalid JSON"]);
    exit;
}

$store_id = intval($data['store_id']);
$customer_id = intval($data['customer_id']);
$invoice_number = $conn->real_escape_string($data['invoice_number']);
$payment_method = $conn->real_escape_string($data['payment_method']);
$payment_status = $conn->real_escape_string($data['payment_status']);
$served_by = intval($data['served_by']);
$discount = floatval($data['discount_amount']);
$tax = floatval($data['tax_amount']);
$notes = $conn->real_escape_string($data['notes']);
$items = $data['items'];

if (!$store_id || !$customer_id || !$invoice_number || !$served_by || empty($items)) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

// Begin transaction
$conn->begin_transaction();

try {
    // Check stock for each item
    foreach ($items as $item) {
        $product_id = intval($item['product_id']);
        $quantity = intval($item['quantity']);

        $stock = $conn->query("SELECT quantity FROM products WHERE id = $product_id AND store_id = $store_id FOR UPDATE");
        if ($stock->num_rows === 0) throw new Exception("Product $product_id not found");

        $stock_qty = $stock->fetch_assoc()['quantity'];
        if ($stock_qty < $quantity) throw new Exception("Insufficient stock for product ID $product_id (available: $stock_qty)");
    }

    // Calculate totals
    $subtotal = 0;
    foreach ($items as $item) {
        $subtotal += floatval($item['unit_price']) * intval($item['quantity']);
    }
    $total = $subtotal - $discount + $tax;

    // Insert into sales
    $stmt = $conn->prepare("INSERT INTO sales (invoice_number, customer_id, store_id, total_amount, discount_amount, tax_amount, final_amount, payment_method, payment_status, served_by, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
    $stmt->bind_param("siiddddssiss", $invoice_number, $customer_id, $store_id, $subtotal, $discount, $tax, $total, $payment_method, $payment_status, $served_by, $notes);
    $stmt->execute();
    $sale_id = $stmt->insert_id;
    $stmt->close();

    // Insert sale items and update product quantity
    foreach ($items as $item) {
        $product_id = intval($item['product_id']);
        $quantity = intval($item['quantity']);
        $unit_price = floatval($item['unit_price']);
        $discount_percent = floatval($item['discount_percent']);
        $discount_amount = floatval($item['discount_amount']);
        $total_price = floatval($item['total_price']);

        $stmt_item = $conn->prepare("INSERT INTO sale_items (store_id, sale_id, product_id, quantity, unit_price, discount_percent, discount_amount, total_price) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        $stmt_item->bind_param("iiiiiddd", $store_id, $sale_id, $product_id, $quantity, $unit_price, $discount_percent, $discount_amount, $total_price);
        $stmt_item->execute();
        $stmt_item->close();

        $conn->query("UPDATE products SET quantity = quantity - $quantity WHERE id = $product_id AND store_id = $store_id");
    }

    $conn->commit();
    echo json_encode(["status" => "success", "message" => "Sale recorded", "sale_id" => $sale_id]);
} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(["status" => "error", "message" => $e->getMessage()]);
}
?>
