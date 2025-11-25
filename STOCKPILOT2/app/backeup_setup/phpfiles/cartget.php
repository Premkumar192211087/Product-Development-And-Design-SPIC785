<?php
// db.php
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "inventory_management";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// get_product_details.php
if (isset($_GET['store_id'])) {
    $store_id = $_GET['store_id'];

    $sql = "SELECT p.product_name, p.price, p.product_code, bd.quantity
            FROM products p
            JOIN batch_details bd ON p.id = bd.product_id
            WHERE  p.store_id = '$store_id' AND p.product_code IS NULL";

    $result = $conn->query($sql);

    if ($result->num_rows > 0) {
        $data = array();
        while ($row = $result->fetch_assoc()) {
            $data[] = $row;
        }
        echo json_encode($data); // Return an array of products
    } else {
        echo json_encode(array("message" => "No products found for this store."));
    }
} else {
    echo json_encode(array("message" => "Invalid request. Store ID is required."));
}
$conn->close();
?>
