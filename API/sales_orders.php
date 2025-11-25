<?php
require 'db.php';

$store_id       = $_GET['store_id'] ?? null;
$date_range     = $_GET['date_range'] ?? 'All Time';
$payment_status = $_GET['payment_status'] ?? 'All Status';
$payment_method = $_GET['payment_method'] ?? 'All Methods';
$amount_range   = $_GET['amount_range'] ?? 'All Amounts';
$search         = $_GET['search_query'] ?? '';

if (!$store_id) {
    echo json_encode(['error' => 'Missing store_id']);
    exit;
}

$conditions = ["s.store_id = :store_id"];
$params = [':store_id' => $store_id];

// Date Range
switch ($date_range) {
    case 'Today':
        $conditions[] = "DATE(s.sale_date) = CURDATE()";
        break;
    case 'Yesterday':
        $conditions[] = "DATE(s.sale_date) = CURDATE() - INTERVAL 1 DAY";
        break;
    case 'This Week':
        $conditions[] = "YEARWEEK(s.sale_date, 1) = YEARWEEK(CURDATE(), 1)";
        break;
    case 'Last Week':
        $conditions[] = "YEARWEEK(s.sale_date, 1) = YEARWEEK(CURDATE(), 1) - 1";
        break;
    case 'This Month':
        $conditions[] = "MONTH(s.sale_date) = MONTH(CURDATE()) AND YEAR(s.sale_date) = YEAR(CURDATE())";
        break;
    case 'Last Month':
        $conditions[] = "MONTH(s.sale_date) = MONTH(CURDATE() - INTERVAL 1 MONTH) AND YEAR(s.sale_date) = YEAR(CURDATE() - INTERVAL 1 MONTH)";
        break;
    case 'Last 3 Months':
        $conditions[] = "s.sale_date >= CURDATE() - INTERVAL 3 MONTH";
        break;
    case 'Last 6 Months':
        $conditions[] = "s.sale_date >= CURDATE() - INTERVAL 6 MONTH";
        break;
    case 'This Year':
        $conditions[] = "YEAR(s.sale_date) = YEAR(CURDATE())";
        break;
    case 'Last Year':
        $conditions[] = "YEAR(s.sale_date) = YEAR(CURDATE()) - 1";
        break;
}

// Payment Status
if ($payment_status !== 'All Status') {
    $conditions[] = "s.payment_status = :payment_status";
    $params[':payment_status'] = strtolower($payment_status);
}

// Payment Method
if ($payment_method !== 'All Methods') {
    $conditions[] = "s.payment_method = :payment_method";
    $params[':payment_method'] = strtolower(str_replace(' ', '_', $payment_method));
}

// Amount Range
switch ($amount_range) {
    case '₹0 - ₹500':
        $conditions[] = "s.final_amount BETWEEN 0 AND 500";
        break;
    case '₹501 - ₹1,000':
        $conditions[] = "s.final_amount BETWEEN 501 AND 1000";
        break;
    case '₹1,001 - ₹2,500':
        $conditions[] = "s.final_amount BETWEEN 1001 AND 2500";
        break;
    case '₹2,501 - ₹5,000':
        $conditions[] = "s.final_amount BETWEEN 2501 AND 5000";
        break;
    case '₹5,001 - ₹10,000':
        $conditions[] = "s.final_amount BETWEEN 5001 AND 10000";
        break;
    case '₹10,001 - ₹25,000':
        $conditions[] = "s.final_amount BETWEEN 10001 AND 25000";
        break;
    case '₹25,001 - ₹50,000':
        $conditions[] = "s.final_amount BETWEEN 25001 AND 50000";
        break;
    case '₹50,001 - ₹1,00,000':
        $conditions[] = "s.final_amount BETWEEN 50001 AND 100000";
        break;
    case '₹1,00,001 - ₹2,50,000':
        $conditions[] = "s.final_amount BETWEEN 100001 AND 250000";
        break;
    case 'Above ₹2,50,000':
        $conditions[] = "s.final_amount > 250000";
        break;
}

// Search
if (!empty($search)) {
    $conditions[] = "(s.invoice_number LIKE :search OR c.customer_name LIKE :search)";
    $params[':search'] = '%' . $search . '%';
}

$where = implode(" AND ", $conditions);

$sql = "SELECT s.*, c.customer_name, sd.full_name AS served_by
        FROM sales s
        LEFT JOIN customers c ON s.customer_id = c.customer_id
        LEFT JOIN staff_details sd ON s.served_by = sd.staff_id
        WHERE $where
        ORDER BY s.sale_date DESC";

$stmt = $pdo->prepare($sql);
$stmt->execute($params);
$results = $stmt->fetchAll(PDO::FETCH_ASSOC);

header('Content-Type: application/json');
echo json_encode($results);
