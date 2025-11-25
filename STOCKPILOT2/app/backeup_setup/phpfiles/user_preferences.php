<?php
// Database configuration
require_once 'db.php';

// Headers
header('Content-Type: application/json');

// Response array
$response = array();

// Check if required parameters are provided
if (!isset($_POST['store_id']) || empty($_POST['store_id'])) {
    $response['success'] = false;
    $response['message'] = 'Store ID is required';
    echo json_encode($response);
    exit;
}

// Get store_id
$store_id = intval($_POST['store_id']);

// Check connection
if ($conn->connect_error) {
    $response['success'] = false;
    $response['message'] = 'Database connection failed: ' . $conn->connect_error;
    echo json_encode($response);
    exit;
}

// Determine action based on request method
$action = isset($_POST['action']) ? $_POST['action'] : 'get';

switch ($action) {
    case 'set':
        // Set user preference
        if (!isset($_POST['preference_name']) || empty($_POST['preference_name']) || 
            !isset($_POST['preference_value'])) {
            $response['success'] = false;
            $response['message'] = 'Preference name and value are required';
            echo json_encode($response);
            exit;
        }
        
        $preference_name = $_POST['preference_name'];
        $preference_value = $_POST['preference_value'];
        
        // Check if preference already exists
        $check_stmt = $conn->prepare("SELECT id FROM user_preferences WHERE store_id = ? AND preference_name = ?");
        $check_stmt->bind_param("is", $store_id, $preference_name);
        $check_stmt->execute();
        $check_result = $check_stmt->get_result();
        
        if ($check_result->num_rows > 0) {
            // Update existing preference
            $row = $check_result->fetch_assoc();
            $stmt = $conn->prepare("UPDATE user_preferences SET preference_value = ? WHERE id = ?");
            $stmt->bind_param("si", $preference_value, $row['id']);
        } else {
            // Insert new preference
            $stmt = $conn->prepare("INSERT INTO user_preferences (store_id, preference_name, preference_value) VALUES (?, ?, ?)");
            $stmt->bind_param("iss", $store_id, $preference_name, $preference_value);
        }
        
        if ($stmt->execute()) {
            $response['success'] = true;
            $response['message'] = 'Preference saved successfully';
        } else {
            $response['success'] = false;
            $response['message'] = 'Failed to save preference: ' . $stmt->error;
        }
        
        $stmt->close();
        $check_stmt->close();
        break;
        
    case 'get':
    default:
        // Get user preferences
        $preference_name = isset($_POST['preference_name']) ? $_POST['preference_name'] : null;
        
        if ($preference_name) {
            // Get specific preference
            $stmt = $conn->prepare("SELECT preference_name, preference_value FROM user_preferences WHERE store_id = ? AND preference_name = ?");
            $stmt->bind_param("is", $store_id, $preference_name);
        } else {
            // Get all preferences
            $stmt = $conn->prepare("SELECT preference_name, preference_value FROM user_preferences WHERE store_id = ?");
            $stmt->bind_param("i", $store_id);
        }
        
        $stmt->execute();
        $result = $stmt->get_result();
        
        $preferences = array();
        while ($row = $result->fetch_assoc()) {
            $preferences[$row['preference_name']] = $row['preference_value'];
        }
        
        $response['success'] = true;
        $response['preferences'] = $preferences;
        
        $stmt->close();
        break;
}

// Close connection
$conn->close();

// Output JSON
echo json_encode($response);
?>