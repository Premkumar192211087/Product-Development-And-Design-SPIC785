
<?php
header('Content-Type: application/json');
$conn = new mysqli("localhost", "root", "", "inventory_management");

if ($conn->connect_error) {
    echo json_encode(['success' => false, 'message' => 'Connection failed: ' . $conn->connect_error]);
    exit;
}

// Get data from request
$data = json_decode(file_get_contents("php://input"), true);
$user_id = isset($data['user_id']) ? intval($data['user_id']) : 0;
$store_id = isset($data['store_id']) ? intval($data['store_id']) : 0;
$fcm_token = isset($data['fcm_token']) ? $data['fcm_token'] : '';
$device_type = isset($data['device_type']) ? $data['device_type'] : 'android';

if ($user_id === 0 || $store_id === 0 || empty($fcm_token)) {
    echo json_encode(['success' => false, 'message' => 'Missing required parameters']);
    exit;
}

try {
    // Check if token already exists
    $stmt = $conn->prepare("SELECT id FROM user_devices WHERE user_id = ? AND fcm_token = ?");
    $stmt->bind_param("is", $user_id, $fcm_token);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        // Update existing token
        $row = $result->fetch_assoc();
        $device_id = $row['id'];

        $update = $conn->prepare("UPDATE user_devices SET last_used = NOW(), is_active = 1,
                                 store_id = ? WHERE id = ?");
        $update->bind_param("ii", $store_id, $device_id);
        $update->execute();

        echo json_encode(['success' => true, 'message' => 'Token updated successfully']);
    } else {
        // Insert new token
        $insert = $conn->prepare("INSERT INTO user_devices (user_id, store_id, fcm_token, device_type,
                                 created_at, last_used, is_active)
                                 VALUES (?, ?, ?, ?, NOW(), NOW(), 1)");
        $insert->bind_param("iiss", $user_id, $store_id, $fcm_token, $device_type);
        $insert->execute();

        echo json_encode(['success' => true, 'message' => 'Token registered successfully']);
    }
} catch (Exception $e) {
    echo json_encode(['success' => false, 'message' => 'Error: ' . $e->getMessage()]);
}
?>