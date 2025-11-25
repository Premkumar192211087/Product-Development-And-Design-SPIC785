<?php
header("Content-Type: application/json");
require_once "db.php"; // Ensure this connects correctly

// Check database connection
if (!$conn) {
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}

$response = ["status" => "error", "message" => "Unknown error"];

// --- Helper: Get date range ---
function getDateRange($option) {
    $today = new DateTime();
    switch ($option) {
        case "Today":
            $start = $today->format("Y-m-d 00:00:00");
            $end   = $today->format("Y-m-d 23:59:59");
            break;
        case "Yesterday":
            $yesterday = (clone $today)->modify("-1 day");
            $start = $yesterday->format("Y-m-d 00:00:00");
            $end   = $yesterday->format("Y-m-d 23:59:59");
            break;
        case "This Week":
            $monday = (clone $today)->modify("monday this week");
            $start = $monday->format("Y-m-d 00:00:00");
            $end   = $today->format("Y-m-d 23:59:59");
            break;
        case "Last Week":
            $lastMonday = (clone $today)->modify("monday last week");
            $lastSunday = (clone $lastMonday)->modify("sunday this week");
            $start = $lastMonday->format("Y-m-d 00:00:00");
            $end   = $lastSunday->format("Y-m-d 23:59:59");
            break;
        case "This Month":
            $start = $today->format("Y-m-01 00:00:00");
            $end   = $today->format("Y-m-d 23:59:59");
            break;
        case "Last Month":
            $firstDay = (clone $today)->modify("first day of last month");
            $lastDay  = (clone $today)->modify("last day of last month");
            $start = $firstDay->format("Y-m-d 00:00:00");
            $end   = $lastDay->format("Y-m-d 23:59:59");
            break;
        case "Last 3 Months":
            $start = (clone $today)->modify("-3 months")->format("Y-m-d 00:00:00");
            $end   = $today->format("Y-m-d 23:59:59");
            break;
        case "Last 6 Months":
            $start = (clone $today)->modify("-6 months")->format("Y-m-d 00:00:00");
            $end   = $today->format("Y-m-d 23:59:59");
            break;
        case "This Year":
            $start = $today->format("Y-01-01 00:00:00");
            $end   = $today->format("Y-m-d 23:59:59");
            break;
        case "Last Year":
            $lastYear = $today->format("Y") - 1;
            $start = $lastYear . "-01-01 00:00:00";
            $end   = $lastYear . "-12-31 23:59:59";
            break;
        case "All Time":
        default:
            $start = "1970-01-01 00:00:00";
            $end   = $today->format("Y-m-d 23:59:59");
    }
    return [$start, $end];
}

// --- Input params ---
$store_id   = isset($_POST['store_id']) ? intval($_POST['store_id']) : 0;
$date_range = isset($_POST['date_range']) ? $_POST['date_range'] : "All Time";
$start_date = isset($_POST['start_date']) ? $_POST['start_date'] : null;
$end_date   = isset($_POST['end_date']) ? $_POST['end_date'] : null;

if ($store_id <= 0) {
    echo json_encode(["status" => "error", "message" => "Invalid store ID"]);
    exit;
}

if ($date_range === "Custom Range" && $start_date && $end_date) {
    $start = $start_date . " 00:00:00";
    $end   = $end_date . " 23:59:59";
} else {
    list($start, $end) = getDateRange($date_range);
}

try {
    // --- Financial Summary (time-based) ---
    // Revenue
    $sql_revenue = "SELECT SUM(final_amount) as revenue
                    FROM sales
                    WHERE store_id = ? AND sale_date BETWEEN ? AND ?";
    $stmt = $conn->prepare($sql_revenue);
    if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
    $stmt->bind_param("iss", $store_id, $start, $end);
    $stmt->execute();
    $revenue = $stmt->get_result()->fetch_assoc()['revenue'] ?? 0;

    // Cost of goods sold
    $sql_cost = "SELECT SUM(si.quantity * p.cost_price) as cost
                 FROM sale_items si
                 JOIN products p ON si.product_id = p.id
                 JOIN sales s ON si.sale_id = s.sale_id
                 WHERE s.store_id = ? AND s.sale_date BETWEEN ? AND ?";
    $stmt = $conn->prepare($sql_cost);
    if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
    $stmt->bind_param("iss", $store_id, $start, $end);
    $stmt->execute();
    $cost = $stmt->get_result()->fetch_assoc()['cost'] ?? 0;

    // Losses from returns
    $sql_returns = "SELECT SUM(ri.quantity * p.price) as return_losses
                    FROM returns r
                    JOIN return_items ri ON r.return_id = ri.return_id
                    JOIN products p ON ri.product_id = p.id
                    WHERE r.store_id = ? AND r.return_date BETWEEN ? AND ?";
    $stmt = $conn->prepare($sql_returns);
    if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
    $stmt->bind_param("iss", $store_id, $start, $end);
    $stmt->execute();
    $return_losses = $stmt->get_result()->fetch_assoc()['return_losses'] ?? 0;

    // Losses from damages
    $sql_damages = "SELECT SUM(d.quantity_damaged * p.cost_price) as damage_cost
                    FROM damages d
                    JOIN products p ON d.product_id = p.id
                    WHERE d.store_id = ? AND d.scanned_at BETWEEN ? AND ?";
    $stmt = $conn->prepare($sql_damages);
    if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
    $stmt->bind_param("iss", $store_id, $start, $end);
    $stmt->execute();
    $damage_cost = $stmt->get_result()->fetch_assoc()['damage_cost'] ?? 0;

    $losses = $return_losses + $damage_cost;
    $profit = $revenue - $cost - $losses;

    $financial = [
        "revenue" => $revenue,
        "cost" => $cost,
        "losses" => $losses,
        "profit" => $profit
    ];

    // --- Inventory Snapshot (current, ignore date) ---
    $sql_inv = "SELECT COUNT(DISTINCT product_id) as total_skus,
                       SUM(CASE WHEN current_stock < 10 THEN 1 ELSE 0 END) as low_stock
                FROM batch_details
                WHERE store_id = ? AND current_stock >= 0";
    $stmt = $conn->prepare($sql_inv);
    if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
    $stmt->bind_param("i", $store_id);
    $stmt->execute();
    $inventory = $stmt->get_result()->fetch_assoc() ?: ["total_skus" => 0, "low_stock" => 0];

    // Low stock items
    $sql_low = "SELECT p.product_name, bd.current_stock
                FROM batch_details bd
                JOIN products p ON bd.product_id = p.id
                WHERE bd.store_id = ? AND bd.current_stock < 10
                LIMIT 20";
    $stmt = $conn->prepare($sql_low);
    if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
    $stmt->bind_param("i", $store_id);
    $stmt->execute();
    $lowStockList = $stmt->get_result()->fetch_all(MYSQLI_ASSOC);

    // Stock by product for chart
    $sql_stock = "SELECT p.product_name, SUM(bd.current_stock) as current_stock
                  FROM batch_details bd
                  JOIN products p ON bd.product_id = p.id
                  WHERE bd.store_id = ?
                  GROUP BY p.product_name
                  ORDER BY current_stock DESC
                  LIMIT 10";
    $stmt = $conn->prepare($sql_stock);
    if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
    $stmt->bind_param("i", $store_id);
    $stmt->execute();
    $stockByProduct = $stmt->get_result()->fetch_all(MYSQLI_ASSOC);

    // --- Sales Overview ---
    $sql_sales = "SELECT SUM(final_amount) as total_sales,
                         COUNT(DISTINCT sale_id) as total_orders
                  FROM sales
                  WHERE store_id = ? AND sale_date BETWEEN ? AND ?";
    $stmt = $conn->prepare($sql_sales);
    if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
    $stmt->bind_param("iss", $store_id, $start, $end);
    $stmt->execute();
    $sales = $stmt->get_result()->fetch_assoc() ?: ["total_sales" => 0, "total_orders" => 0];

    // Top products
    $sql_top = "SELECT si.product_id, si.product_name, SUM(si.quantity) as qty_sold
                FROM sale_items si
                JOIN sales s ON si.sale_id = s.sale_id
                WHERE s.store_id = ? AND s.sale_date BETWEEN ? AND ?
                GROUP BY si.product_id, si.product_name
                ORDER BY qty_sold DESC
                LIMIT 10";
    $stmt = $conn->prepare($sql_top);
    if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
    $stmt->bind_param("iss", $store_id, $start, $end);
    $stmt->execute();
    $topProducts = $stmt->get_result()->fetch_all(MYSQLI_ASSOC);

    // Recent invoices
    $sql_invoices = "SELECT invoice_id, invoice_number, total_amount, invoice_date
                     FROM invoices
                     WHERE store_id = ? AND invoice_date BETWEEN ? AND ?
                     ORDER BY invoice_date DESC
                     LIMIT 10";
    $stmt = $conn->prepare($sql_invoices);
    if (!$stmt) {
        $invoices = []; // Fallback if invoices table is missing
    } else {
        $stmt->bind_param("iss", $store_id, $start, $end);
        $stmt->execute();
        $invoices = $stmt->get_result()->fetch_all(MYSQLI_ASSOC);
    }

    // Daily revenue trend (for LineChart)
    $sql_trend = "SELECT DATE(sale_date) as day, SUM(final_amount) as revenue
                  FROM sales
                  WHERE store_id = ? AND sale_date BETWEEN ? AND ?
                  GROUP BY DATE(sale_date)
                  ORDER BY day ASC";
    $stmt = $conn->prepare($sql_trend);
    if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
    $stmt->bind_param("iss", $store_id, $start, $end);
    $stmt->execute();
    $trend = $stmt->get_result()->fetch_all(MYSQLI_ASSOC);

    // Revenue by category (for PieChart)
    $sql_cat = "SELECT c.category_name, SUM(si.quantity * si.unit_price) as revenue
                FROM sale_items si
                JOIN products p ON si.product_id = p.id
                JOIN categories c ON p.category_id = c.category_id
                JOIN sales s ON si.sale_id = s.sale_id
                WHERE s.store_id = ? AND s.sale_date BETWEEN ? AND ?
                GROUP BY c.category_name";
    $stmt = $conn->prepare($sql_cat);
    if (!$stmt) throw new Exception("Prepare failed: " . $conn->error);
    $stmt->bind_param("iss", $store_id, $start, $end);
    $stmt->execute();
    $revenueByCategory = $stmt->get_result()->fetch_all(MYSQLI_ASSOC);

    // --- Response ---
    $response = [
        "status" => "success",
        "store_id" => $store_id,
        "date_range" => ["label" => $date_range, "start" => $start, "end" => $end],
        "financial" => $financial,
        "inventory" => [
            "summary" => $inventory,
            "low_stock_items" => $lowStockList,
            "stock_by_product" => $stockByProduct
        ],
        "sales" => [
            "summary" => $sales,
            "top_products" => $topProducts,
            "recent_invoices" => $invoices,
            "revenue_trend" => $trend,
            "revenue_by_category" => $revenueByCategory
        ]
    ];
} catch (Exception $e) {
    $response = ["status" => "error", "message" => "Query failed: " . $e->getMessage()];
}

echo json_encode($response);
$conn->close();
?>