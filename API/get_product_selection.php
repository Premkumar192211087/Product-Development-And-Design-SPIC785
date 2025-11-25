<?php
header('Content-Type: application/json');
// Connect to DB
include('db.php'); // your DB connection setup file here

try {
    // Optional: Get store_id parameter from request if needed
     $store_id = isset($_GET['store_id']) ? intval($_GET['store_id']) : 0;

    $sql = "SELECT id AS product_id, product_name, sku, category, quantity AS stock_quantity, price AS selling_price
            FROM products
            WHERE status = 'active' "; // Add store filter if needed, e.g. AND store_id = ?

    $result = $conn->query($sql);
    if (!$result) {
        throw new Exception("DB Query failed: " . $conn->error);
    }

    $products = [];
    while ($row = $result->fetch_assoc()) {
        $products[] = $row;
    }
    echo json_encode($products);
} catch (Exception $e) {
    echo json_encode(["error" => true, "message" => $e->getMessage()]);
}
$conn->close();
?>
