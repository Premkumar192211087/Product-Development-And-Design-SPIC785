<?php
// Database connection
require 'db.php';

// Start session to retrieve store_id dynamically (if you're using session to store it)
session_start();

// Assuming store_id is stored in session after login
$store_id = $_SESSION['store_id']; // Use session variable if you're storing the store ID in session

if (!$store_id) {
    // Handle error if store_id is not available
    die("Store ID is missing. Please log in.");
}

// Function to insert notification
function insertNotification($conn, $store_id, $title, $message, $type) {
    $stmt = $conn->prepare("INSERT INTO notifications (store_id, title, message, type, status) VALUES (?, ?, ?, ?, 'unread')");
    $stmt->bind_param("isss", $store_id, $title, $message, $type);
    $stmt->execute();
}

// 1. Restock Suggestions Based on Sold Products
$restock_sql = $conn->prepare("SELECT product_name, SUM(quantity) AS sold_quantity FROM reports WHERE store_id = ? AND type = 'sold' GROUP BY product_name");
$restock_sql->bind_param("i", $store_id);
$restock_sql->execute();
$restock_result = $restock_sql->get_result();

while ($row = $restock_result->fetch_assoc()) {
    $product_name = $row['product_name'];
    $sold_quantity = $row['sold_quantity'];
    
    // Templates for Restock Suggestions Based on Sold Quantity
    $templates = [
    "Our inventory tracking system has detected that $product_name has sold $sold_quantity units over the recent period. Based on historical sales data and current inventory levels, we recommend initiating a restock process to maintain optimal inventory levels and avoid potential stockouts.",
    
    "$product_name is showing strong customer demand with $sold_quantity units sold in the recent sales period. To ensure continuous availability and capitalize on this product's popularity, we suggest placing a reorder with your supplier. Consider adjusting order quantities based on this increased demand pattern.",
    
    "$product_name has been one of your fastest-selling items recently with $sold_quantity units sold! To maintain customer satisfaction and prevent lost sales opportunities, our system recommends restocking this item as a priority. Consider reviewing your reorder point for this high-demand product.",
    
    "Inventory alert: We've noticed significant movement for $product_name with $sold_quantity units sold in the recent period. Based on your current stock levels and this sales velocity, we recommend initiating the restocking process now to ensure uninterrupted availability. Consider adjusting your par levels for this popular item.",
    
    "High demand alert for $product_name! With $sold_quantity units sold, this product is performing exceptionally well. To maximize revenue potential and maintain customer satisfaction, we strongly recommend prioritizing this item for immediate reordering. Consider increasing your standard order quantity to accommodate the growing demand."
];
    
    $title = "Restock Suggestion";
    $message = $templates[array_rand($templates)];
    insertNotification($conn, $store_id, $title, $message, "restock");
}

// 2. Expiry Alert Notifications
$expiry_sql = $conn->prepare("SELECT product_name, exp_date FROM batch_details WHERE store_id = ? AND exp_date <= CURDATE() + INTERVAL 7 DAY");
$expiry_sql->bind_param("i", $store_id);
$expiry_sql->execute();
$expiry_result = $expiry_sql->get_result();

while ($row = $expiry_result->fetch_assoc()) {
    $product_name = $row['product_name'];
    $exp_date = $row['exp_date'];
    
    // Templates for Expiry Alert
    $expiry_templates = [
    "Important inventory management notice: $product_name in your inventory is approaching its expiration date of $exp_date. To maintain product quality standards and prevent potential losses, we recommend conducting a thorough inventory check and implementing appropriate promotional strategies or removal procedures for these items before they expire.",
    
    "Expiration date alert: $product_name will reach its expiration date on $exp_date. To ensure compliance with safety regulations and maintain your store's quality standards, please schedule an inventory review to identify these items and determine appropriate action. Consider special promotions, discounts, or proper disposal procedures.",
    
    "Urgent expiry notification: $product_name is set to expire on $exp_date, which is within your critical timeline threshold. To minimize financial loss and maintain inventory integrity, immediate attention is required. Please verify all affected units and implement your standard protocol for managing soon-to-expire inventory.",
    
    "Quality control alert: Our system has identified that $product_name will expire on $exp_date. To uphold your store's commitment to providing only fresh and quality products to customers, please arrange for a comprehensive inventory check. Consider creating promotional bundles or special offers to move these products before expiration.",
    
    "Inventory management action required: $product_name has been flagged in our system as approaching its expiration date of $exp_date. To optimize inventory turnover and minimize waste, we recommend immediately reviewing stock levels and placement of this product. Consider repositioning, discounting, or preparing for appropriate disposal depending on your store policies."
];
    
    $title = "Expiry Alert";
    $message = $expiry_templates[array_rand($expiry_templates)];
    insertNotification($conn, $store_id, $title, $message, "expiry");
}

// 3. Low Stock Alerts
$low_stock_sql = $conn->prepare("SELECT product_name, quantity FROM products WHERE store_id = ? AND quantity <= 10");
$low_stock_sql->bind_param("i", $store_id);
$low_stock_sql->execute();
$low_stock_result = $low_stock_sql->get_result();

while ($row = $low_stock_result->fetch_assoc()) {
    $product_name = $row['product_name'];
    $quantity = $row['quantity'];
    
    // Templates for Low Stock Alerts
   $low_stock_templates = [
    "Inventory level warning: $product_name has reached a critical stock threshold with only $quantity units remaining in inventory. To prevent potential stockouts and maintain customer satisfaction, we recommend placing a replenishment order immediately. Consider reviewing your minimum stock level settings for this regularly purchased item.",
    
    "$product_name inventory status: Current stock has dropped to $quantity units, which is below your established reorder point. To ensure continuous product availability and meet ongoing customer demand, please initiate your restocking process with your supplier. Consider evaluating whether seasonal factors are affecting demand for this product.",
    
    "Critical inventory alert: $product_name is showing dangerously low stock levels with only $quantity units available. To prevent potential lost sales and customer disappointment, immediate restocking is strongly recommended. This product has historically been a consistent seller requiring regular inventory maintenance.",
    
    "Stock replenishment notification: Your inventory of $product_name has decreased to $quantity units remaining. Based on historical sales patterns and current demand forecasts, we recommend placing a reorder immediately to maintain optimal service levels and prevent potential revenue loss from out-of-stock situations.",
    
    "Urgent inventory action needed: $product_name has reached critically low levels with only $quantity units left in stock. To prevent stockouts and maintain customer satisfaction, immediate replenishment is required. Consider reviewing your reorder quantities and safety stock levels for this high-turnover item to prevent future inventory shortages."
];
    
    $title = "Low Stock Alert";
    $message = $low_stock_templates[array_rand($low_stock_templates)];
    insertNotification($conn, $store_id, $title, $message, "low_stock");
}

// 4. Damaged Product Notifications
$damaged_sql = $conn->prepare("SELECT product_name, quantity FROM reports WHERE store_id = ? AND type = 'damaged' AND status = 'completed'");
$damaged_sql->bind_param("i", $store_id);
$damaged_sql->execute();
$damaged_result = $damaged_sql->get_result();

while ($row = $damaged_result->fetch_assoc()) {
    $product_name = $row['product_name'];
    $quantity = $row['quantity'];
    
    // Templates for Damaged Product Notifications
    $damaged_templates = [
    "Quality control notification: Our inventory management system has recorded $quantity units of $product_name as damaged or compromised. To maintain accurate inventory records and ensure proper financial accounting, please conduct a thorough assessment of these items, document the extent of damage, and implement appropriate disposal or return procedures according to your store policies.",
    
    "Damaged inventory alert: $quantity units of $product_name have been reported as damaged in your inventory. To maintain store quality standards and accurate stock counts, please verify the condition of these items and update inventory records accordingly. Consider investigating the cause of damage to prevent similar occurrences in the future.",
    
    "Product quality issue detected: $product_name has $quantity units flagged as damaged in your inventory system. To ensure customer satisfaction and maintain inventory accuracy, these items require immediate attention. Please assess the extent of damage, determine if supplier return is possible, and update your inventory records accordingly.",
    
    "Inventory discrepancy notification: $quantity units of $product_name have been marked as damaged in your system. To maintain accurate financial reporting and inventory control, please verify these items, document the damage with photos if required for supplier claims, and process appropriate write-offs or returns based on your standard operating procedures.",
    
    "Damaged product action required: Your inventory records indicate $quantity units of $product_name have been reported as damaged. To ensure proper inventory management and accounting practices, please inspect these items promptly, determine appropriate next steps (disposal, return to vendor, or clearance sale if appropriate), and update your inventory management system accordingly."
];
    
    $title = "Damaged Product Alert";
    $message = $damaged_templates[array_rand($damaged_templates)];
    insertNotification($conn, $store_id, $title, $message, "damaged");
}

echo "Notifications generated successfully.";
?>
