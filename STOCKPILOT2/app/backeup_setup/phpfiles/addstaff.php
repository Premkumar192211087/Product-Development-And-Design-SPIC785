<?php
include 'db.php';

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    // Required fields check (removed 'username', use email instead)
    $required = ['password', 'full_name', 'email', 'phone', 'address', 'store_id'];
    foreach ($required as $field) {
        if (empty($_POST[$field])) {
            die(json_encode(["success" => false, "message" => "Missing field: $field"]));
        }
    }

    // Get sanitized input
    $full_name = mysqli_real_escape_string($conn, $_POST['full_name']);
    $email = mysqli_real_escape_string($conn, $_POST['email']);
    $phone = mysqli_real_escape_string($conn, $_POST['phone']);
    $address = mysqli_real_escape_string($conn, $_POST['address']);
    $password = mysqli_real_escape_string($conn, $_POST['password']);
    $store_id = (int) $_POST['store_id']; // Cast to int for safety
    $role = 'staff';
    $username = $email; // Use email as username

    // Check if email or phone already exists
    $stmt1 = $conn->prepare("SELECT staff_id FROM staff_details WHERE email = ? OR phone = ?");
    $stmt1->bind_param("ss", $email, $phone);
    $stmt1->execute();
    $stmt1->store_result();
    if ($stmt1->num_rows > 0) {
        die(json_encode(["success" => false, "message" => "Staff with this email or phone already exists."]));
    }

    // Insert into user_login (using email as username)
    $stmt2 = $conn->prepare("INSERT INTO user_login (username, password, role, store_id) VALUES (?, ?, ?, ?)");
    $stmt2->bind_param("sssi", $username, $password, $role, $store_id);
    if ($stmt2->execute()) {
        $user_id = $stmt2->insert_id;

        // Insert into staff_details
        $stmt3 = $conn->prepare("INSERT INTO staff_details (user_id, full_name, email, phone, role, store_id, address) VALUES (?, ?, ?, ?, ?, ?, ?)");
        $stmt3->bind_param("issssis", $user_id, $full_name, $email, $phone, $role, $store_id, $address);
        if ($stmt3->execute()) {
            echo json_encode(["success" => true, "message" => "Staff added successfully"]);
        } else {
            echo json_encode(["success" => false, "message" => "Error inserting staff details."]);
        }
        $stmt3->close();
    } else {
        echo json_encode(["success" => false, "message" => "Error inserting user login."]);
    }

    $stmt1->close();
    $stmt2->close();
    $conn->close();
}
?>
