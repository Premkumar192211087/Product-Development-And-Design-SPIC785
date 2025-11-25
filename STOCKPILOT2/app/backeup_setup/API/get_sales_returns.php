<?php
// Required headers
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

// Include database connection
include_once '../database/db_connect.php';

// Check if store_id is provided
if (!isset($_POST['store_id']) || empty($_POST['store_id'])) {
    echo json_encode(array("error" => true, "message" => "Store ID is required"));
    exit();
}

$store_id = $_POST['store_id'];

try {
    // Create database connection
    $database = new Database();
    $db = $database->getConnection();

    // Query to get all sales returns for the store with customer information
    $query = "SELECT sr.id, sr.return_number, sr.customer_id, c.name as customer_name, 
              sr.date, sr.total_amount, sr.reason, sr.status 
              FROM sales_returns sr 
              LEFT JOIN customers c ON sr.customer_id = c.id 
              WHERE sr.store_id = :store_id 
              ORDER BY sr.date DESC";

    $stmt = $db->prepare($query);
    $stmt->bindParam(':store_id', $store_id);
    $stmt->execute();

    if ($stmt->rowCount() > 0) {
        $returns_arr = array();
        $returns_arr["error"] = false;
        $returns_arr["sales_returns"] = array();

        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            $return_item = array(
                "id" => $row['id'],
                "return_number" => $row['return_number'],
                "customer_id" => $row['customer_id'],
                "customer_name" => $row['customer_name'],
                "date" => $row['date'],
                "total_amount" => $row['total_amount'],
                "reason" => $row['reason'],
                "status" => $row['status']
            );

            array_push($returns_arr["sales_returns"], $return_item);
        }

        echo json_encode($returns_arr);
    } else {
        echo json_encode(array("error" => false, "message" => "No sales returns found", "sales_returns" => array()));
    }
} catch (Exception $e) {
    echo json_encode(array("error" => true, "message" => "Database error: " . $e->getMessage()));
}
?>