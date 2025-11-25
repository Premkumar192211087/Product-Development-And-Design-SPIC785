<?php
// Database configuration
$host = 'localhost';
$db   = 'inventory_management';
$user = 'root';
$pass = '';

// Create MySQLi connection
$conn = new mysqli($host, $user, $pass, $db);

// Check connection
if ($conn->connect_error) {
    error_log("Database connection failed: " . $conn->connect_error);
    die(json_encode(['error' => 'Database connection failed', 'message' => $conn->connect_error]));
}

// Set charset to utf8mb4
$conn->set_charset("utf8mb4");

// Optional: Set timezone
// $conn->query("SET time_zone = '+00:00'");
?>