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

    // Query to get all invoices for the store with customer information
    $query = "SELECT i.id, i.invoice_number, i.customer_id, c.name as customer_name, 
              i.date, i.due_date, i.total_amount, i.status 
              FROM invoices i 
              LEFT JOIN customers c ON i.customer_id = c.id 
              WHERE i.store_id = :store_id 
              ORDER BY i.date DESC";

    $stmt = $db->prepare($query);
    $stmt->bindParam(':store_id', $store_id);
    $stmt->execute();

    if ($stmt->rowCount() > 0) {
        $invoices_arr = array();
        $invoices_arr["error"] = false;
        $invoices_arr["invoices"] = array();

        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            $invoice_item = array(
                "id" => $row['id'],
                "invoice_number" => $row['invoice_number'],
                "customer_id" => $row['customer_id'],
                "customer_name" => $row['customer_name'],
                "date" => $row['date'],
                "due_date" => $row['due_date'],
                "total_amount" => $row['total_amount'],
                "status" => $row['status']
            );

            array_push($invoices_arr["invoices"], $invoice_item);
        }

        echo json_encode($invoices_arr);
    } else {
        echo json_encode(array("error" => false, "message" => "No invoices found", "invoices" => array()));
    }
} catch (Exception $e) {
    echo json_encode(array("error" => true, "message" => "Database error: " . $e->getMessage()));
}
?>