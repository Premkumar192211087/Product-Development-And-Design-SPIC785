<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET");

// Database connection
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "inventory_management";

$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(["success" => false, "error" => "Database connection failed"]);
    exit();
}

// Get filters from URL parameters
$store_id = isset($_GET['store_id']) ? intval($_GET['store_id']) : 0;
$type = isset($_GET['type']) ? $_GET['type'] : "All";
$timeFilter = isset($_GET['time']) ? $_GET['time'] : "All";

// Base query and dynamic conditions
$query = "SELECT id, product_name, quantity, price, type, timestamp, status, store_id FROM reports";
$conditions = [];
$params = [];
$types = "";

// Store filter
if ($store_id > 0) {
    $conditions[] = "store_id = ?";
    $params[] = $store_id;
    $types .= "i";
}

// Type filter (multi-select support)
if ($type !== "All" && !empty($type)) {
    $typeArray = explode(",", $type);
    $placeholders = implode(',', array_fill(0, count($typeArray), '?'));
    $conditions[] = "type IN ($placeholders)";
    foreach ($typeArray as $t) {
        $params[] = trim($t);
        $types .= "s";
    }
}

// Time filter (multi-select support)
if ($timeFilter !== "All" && !empty($timeFilter)) {
    $timeArray = explode(",", $timeFilter);
    $timeConditions = [];
    foreach ($timeArray as $tf) {
        switch (trim($tf)) {
            case "Last Week":
                $timeConditions[] = "timestamp >= NOW() - INTERVAL 1 WEEK";
                break;
            case "Last 1 Month":
                $timeConditions[] = "timestamp >= NOW() - INTERVAL 1 MONTH";
                break;
            case "Last 3 Months":
                $timeConditions[] = "timestamp >= NOW() - INTERVAL 3 MONTH";
                break;
            case "Last 6 Months":
                $timeConditions[] = "timestamp >= NOW() - INTERVAL 6 MONTH";
                break;
            default:
                echo json_encode(["success" => false, "error" => "Invalid time filter"]);
                exit();
        }
    }
    if (!empty($timeConditions)) {
        $conditions[] = "(" . implode(" OR ", $timeConditions) . ")";
    }
}

// Combine conditions
if (!empty($conditions)) {
    $query .= " WHERE " . implode(" AND ", $conditions);
}

$query .= " ORDER BY timestamp DESC";

// Prepare and execute
$stmt = $conn->prepare($query);

if (!$stmt) {
    echo json_encode(["success" => false, "error" => "Query preparation failed"]);
    exit();
}

if (!empty($params)) {
    $stmt->bind_param($types, ...$params);
}

$stmt->execute();
$result = $stmt->get_result();

$reports = [];

while ($row = $result->fetch_assoc()) {
    $row['show_status_button'] = ($row['type'] === 'ordered');
    $reports[] = $row;
}

$stmt->close();
$conn->close();

echo json_encode(["success" => true, "reports" => $reports]);
?>
