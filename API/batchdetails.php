<?php
header("Content-Type: application/json");
include 'db.php'; // contains $conn

$response = array();

// Collect POST data
$barcode_raw = $_POST['barcode'] ?? null; // PRD123|2024-12-01|4567
$store_id = $_POST['store_id'] ?? null;
$exp_date = $_POST['exp_date'] ?? null;
$quantity = $_POST['quantity'] ?? null;

if ($barcode_raw && $store_id && $exp_date && $quantity) {
    // Split barcode: PRD123|2024-12-01|4567
    $barcode_parts = explode("|", $barcode_raw);
    $product_code = $barcode_parts[0] ?? null;
    $mfg_date = $barcode_parts[1] ?? null;
    $batch_id = isset($barcode_parts[2]) ? intval($barcode_parts[2]) : null;

    if (!$product_code || !$mfg_date || !$batch_id) {
        $response['status'] = "error";
        $response['message'] = "Invalid barcode format. Expecting format: CODE|MFG_DATE|BATCH_ID";
        echo json_encode($response);
        exit;
    }

    // Match product using barcode and store_id
    $stmt = $conn->prepare("SELECT id AS product_id, product_name FROM products WHERE barcode = ? AND store_id = ?");
    $stmt->bind_param("si", $product_code, $store_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result && $result->num_rows > 0) {
        $product = $result->fetch_assoc();
        $product_id = $product['product_id'];
        $product_name = $product['product_name'];

        // Insert batch details (now including mfg_date and batch_id from barcode)
        $insertStmt = $conn->prepare("INSERT INTO batch_details (store_id, product_id, barcode, product_name, mfg_date, exp_date, quantity, batch_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        $insertStmt->bind_param("iissssii", $store_id, $product_id, $product_code, $product_name, $mfg_date, $exp_date, $quantity, $batch_id);

        if ($insertStmt->execute()) {
            $response['status'] = "success";
            $response['message'] = "Batch inserted successfully.";
            $response['product_name'] = $product_name;
            $response['mfg_date'] = $mfg_date;
            $response['batch_id'] = $batch_id;
        } else {
            $response['status'] = "error";
            $response['message'] = "Failed to insert batch details.";
        }

        $insertStmt->close();
    } else {
        $response['status'] = "error";
        $response['message'] = "Product not found for given barcode and store.";
    }

    $stmt->close();
} else {
    $response['status'] = "error";
    $response['message'] = "Required fields missing (barcode, store_id, exp_date, quantity).";
}

$conn->close();
echo json_encode($response);
?>
