<?php
// Function to send FCM notification
function sendFcmNotification($conn, $store_id, $title, $message, $data = []) {
    // Get FCM tokens for this store
    $stmt = $conn->prepare("SELECT fcm_token FROM user_devices
                           WHERE store_id = ? AND is_active = 1
                           GROUP BY fcm_token");
    $stmt->bind_param("i", $store_id);
    $stmt->execute();
    $result = $stmt->get_result();

    $tokens = [];
    while ($row = $result->fetch_assoc()) {
        $tokens[] = $row['fcm_token'];
    }

    if (empty($tokens)) {
        return false;
    }

    // Get your FCM server key from Firebase Console
    // Go to Project Settings > Cloud Messaging > Server key
    $fcm_server_key = "YOUR_FCM_SERVER_KEY"; // Replace with your actual FCM server key

    // Prepare FCM message
    $fcm_data = [
        'registration_ids' => $tokens,
        'notification' => [
            'title' => $title,
            'body' => $message,
            'sound' => 'default',
            'click_action' => 'FLUTTER_NOTIFICATION_CLICK'
        ],
        'data' => $data
    ];

    // Send FCM message
    $headers = [
        'Authorization: key=' . $fcm_server_key,
        'Content-Type: application/json'
    ];

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, 'https://fcm.googleapis.com/fcm/send');
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fcm_data));

    $result = curl_exec($ch);

    if ($result === false) {
        error_log('FCM Send Error: ' . curl_error($ch));
    }

    curl_close($ch);

    return $result;
}
?>