<?php
// Database connection
require_once 'db_connect.php';

// Set headers for JSON response
header('Content-Type: application/json');

// Check if store_id is provided
if (!isset($_POST['store_id']) || empty($_POST['store_id'])) {
    echo json_encode(['success' => false, 'message' => 'Store ID is required']);
    exit;
}

$store_id = $_POST['store_id'];
$from_date = isset($_POST['from_date']) ? $_POST['from_date'] : null;
$to_date = isset($_POST['to_date']) ? $_POST['to_date'] : null;
$period = isset($_POST['period']) ? $_POST['period'] : 'monthly'; // daily, weekly, monthly

// Initialize response array
$response = [
    'success' => true,
    'summary' => [
        'total_sales' => 0,
        'total_revenue' => 0,
        'total_returns' => 0,
        'net_profit' => 0
    ],
    'sales_trend' => [],
    'payment_methods' => [],
    'invoice_status' => [
        'paid' => 0,
        'unpaid' => 0,
        'partial' => 0
    ]
];

// Build date filter condition
$date_condition = '';
if ($from_date && $to_date) {
    $date_condition = " AND DATE(sale_date) BETWEEN '$from_date' AND '$to_date'";
    $invoice_date_condition = " AND DATE(issue_date) BETWEEN '$from_date' AND '$to_date'";
} elseif ($from_date) {
    $date_condition = " AND DATE(sale_date) >= '$from_date'";
    $invoice_date_condition = " AND DATE(issue_date) >= '$from_date'";
} elseif ($to_date) {
    $date_condition = " AND DATE(sale_date) <= '$to_date'";
    $invoice_date_condition = " AND DATE(issue_date) <= '$to_date'";
} else {
    // Default to last 30 days if no dates provided
    $default_from = date('Y-m-d', strtotime('-30 days'));
    $default_to = date('Y-m-d');
    $date_condition = " AND DATE(sale_date) BETWEEN '$default_from' AND '$default_to'";
    $invoice_date_condition = " AND DATE(issue_date) BETWEEN '$default_from' AND '$default_to'";
}

// Get total sales and revenue
$query = "SELECT 
            SUM(final_amount) as total_sales,
            SUM(final_amount - tax_amount) as total_revenue
          FROM sales 
          WHERE store_id = ? $date_condition";

$stmt = $conn->prepare($query);
$stmt->bind_param('i', $store_id);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    $response['summary']['total_sales'] = floatval($row['total_sales']) ?: 0;
    $response['summary']['total_revenue'] = floatval($row['total_revenue']) ?: 0;
}

// Get total returns
$query = "SELECT SUM(total_refund_amount) as total_returns
          FROM returns 
          WHERE store_id = ? AND status = 'processed' $date_condition";

$stmt = $conn->prepare($query);
$stmt->bind_param('i', $store_id);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    $response['summary']['total_returns'] = floatval($row['total_returns']) ?: 0;
}

// Calculate net profit (simplified - in a real system, would include COGS, expenses, etc.)
// For this example, we'll use a simple calculation: revenue - returns
$response['summary']['net_profit'] = $response['summary']['total_revenue'] - $response['summary']['total_returns'];

// Get sales trend based on period
$group_by = '';
$date_format = '';

switch ($period) {
    case 'daily':
        $group_by = "DATE(sale_date)";
        $date_format = "%Y-%m-%d";
        break;
    case 'weekly':
        $group_by = "YEARWEEK(sale_date, 1)";
        $date_format = "%x-W%v"; // ISO week format
        break;
    case 'monthly':
    default:
        $group_by = "YEAR(sale_date), MONTH(sale_date)";
        $date_format = "%Y-%m";
        break;
}

$query = "SELECT 
            DATE_FORMAT(sale_date, '$date_format') as period,
            SUM(final_amount) as total,
            COUNT(*) as count
          FROM sales 
          WHERE store_id = ? $date_condition
          GROUP BY $group_by
          ORDER BY MIN(sale_date)";

$stmt = $conn->prepare($query);
$stmt->bind_param('i', $store_id);
$stmt->execute();
$result = $stmt->get_result();

while ($row = $result->fetch_assoc()) {
    $response['sales_trend'][] = [
        'period' => $row['period'],
        'total' => floatval($row['total']),
        'count' => intval($row['count'])
    ];
}

// Get payment methods breakdown
$query = "SELECT 
            payment_method, 
            COUNT(*) as count, 
            SUM(final_amount) as total 
          FROM sales 
          WHERE store_id = ? $date_condition 
          GROUP BY payment_method";

$stmt = $conn->prepare($query);
$stmt->bind_param('i', $store_id);
$stmt->execute();
$result = $stmt->get_result();

while ($row = $result->fetch_assoc()) {
    $response['payment_methods'][] = [
        'method' => $row['payment_method'],
        'count' => intval($row['count']),
        'total' => floatval($row['total'])
    ];
}

// Get invoice status counts
$query = "SELECT 
            status, 
            COUNT(*) as count 
          FROM invoices 
          WHERE store_id = ? $invoice_date_condition 
          GROUP BY status";

$stmt = $conn->prepare($query);
$stmt->bind_param('i', $store_id);
$stmt->execute();
$result = $stmt->get_result();

while ($row = $result->fetch_assoc()) {
    $status = strtolower($row['status']);
    if (isset($response['invoice_status'][$status])) {
        $response['invoice_status'][$status] = intval($row['count']);
    }
}

// Return the response
echo json_encode($response);
$conn->close();