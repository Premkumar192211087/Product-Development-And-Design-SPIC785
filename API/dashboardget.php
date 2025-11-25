<?php
// Database connection settings
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "inventory_management"; // Database name

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die(json_encode(["error" => "Database connection failed: " . $conn->connect_error]));
}

// Retrieve store_id from GET or POST request
$store_id = $_GET['store_id'] ?? $_POST['store_id'] ?? null;

// Check if store_id is provided
if ($store_id === null) {
    echo json_encode(["error" => "store_id is required"]);
    exit();
}

// Prepare and execute query for total stock
$total_stock_query = "SELECT SUM(quantity) AS total_stock FROM products WHERE store_id = ?";
$stmt = $conn->prepare($total_stock_query);
if (!$stmt) {
    die(json_encode(["error" => "SQL preparation failed: " . $conn->error]));
}
$stmt->bind_param("i", $store_id);
$stmt->execute();
$stmt->bind_result($total_stock);
$stmt->fetch();
$total_stock = $total_stock ?? 0;
$stmt->close();

// Fetch most purchased products
$most_purchased_query = "
    SELECT product_name, SUM(quantity) AS total_quantity 
    FROM reports 
    WHERE type = 'sold' AND store_id = ? 
    GROUP BY product_name 
    ORDER BY total_quantity DESC 
    LIMIT 5
";
$stmt = $conn->prepare($most_purchased_query);
if (!$stmt) {
    die(json_encode(["error" => "SQL preparation failed: " . $conn->error]));
}
$stmt->bind_param("i", $store_id);
$stmt->execute();
$most_purchased_products = [];
$stmt->bind_result($product_name, $quantity);
while ($stmt->fetch()) {
    $most_purchased_products[] = ['name' => $product_name, 'quantity' => (int)$quantity];
}
$stmt->close();

// Fetch most damaged products
$most_damaged_query = "
    SELECT product_name, SUM(quantity) AS total_quantity 
    FROM reports 
    WHERE type = 'damaged' AND store_id = ? 
    GROUP BY product_name 
    ORDER BY total_quantity DESC 
    LIMIT 5
";
$stmt = $conn->prepare($most_damaged_query);
if (!$stmt) {
    die(json_encode(["error" => "SQL preparation failed: " . $conn->error]));
}
$stmt->bind_param("i", $store_id);
$stmt->execute();
$most_damaged_products = [];
$stmt->bind_result($damaged_product_name, $damaged_quantity);
while ($stmt->fetch()) {
    $most_damaged_products[] = ['name' => $damaged_product_name, 'quantity' => (int)$damaged_quantity];
}
$stmt->close();

// Fetch weekly sales revenue for graph
$revenue_query = "
    SELECT WEEK(timestamp) AS week, SUM(price * quantity) AS weekly_revenue 
    FROM reports 
    WHERE type = 'sold' AND store_id = ? 
    GROUP BY WEEK(timestamp) 
    ORDER BY WEEK(timestamp)
";
$stmt = $conn->prepare($revenue_query);
if (!$stmt) {
    die(json_encode(["error" => "SQL preparation failed: " . $conn->error]));
}
$stmt->bind_param("i", $store_id);
$stmt->execute();
$revenue_data = [];
$stmt->bind_result($week, $weekly_revenue);
while ($stmt->fetch()) {
    $revenue_data[] = ['week' => (int)$week, 'revenue' => (float)$weekly_revenue];
}
$stmt->close();

// Construct response
$response = [
    'total_stock' => $total_stock,
    'most_purchased_products' => $most_purchased_products,
    'most_damaged_products' => $most_damaged_products,
    'revenue_data' => $revenue_data
];

// Optional: Log the response for debugging purposes (for development only)
file_put_contents("dashboard_debug_log.txt", print_r($response, true));

// Close database connection
$conn->close();

// Set the content type and return the response as JSON
header('Content-Type: application/json');
echo json_encode($response);
?>
