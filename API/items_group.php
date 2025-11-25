<?php
header('Content-Type: application/json');
$conn = new mysqli("localhost", "root", "", "inventory_management");

if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Database connection failed"]));
}

$data = json_decode(file_get_contents("php://input"), true);
$action = isset($data['action']) ? $data['action'] : '';
$store_id = isset($data['store_id']) ? intval($data['store_id']) : null;

if (!$store_id) {
    echo json_encode(["status" => "error", "message" => "Missing store_id"]);
    exit;
}

switch ($action) {
    case 'get':
        getCategories($conn, $store_id);
        break;

    case 'add':
        addCategory($conn, $data, $store_id);
        break;

    case 'update':
        updateCategory($conn, $data, $store_id);
        break;

    case 'delete':
        deleteCategory($conn, $data, $store_id);
        break;

    default:
        echo json_encode(["status" => "error", "message" => "Invalid or missing action"]);
        break;
}

$conn->close();


// ------------------ FUNCTIONS -------------------

function getCategories($conn, $store_id) {
    $sql = "SELECT category_id, category_name FROM categories WHERE store_id = $store_id ORDER BY category_id DESC";
    $result = $conn->query($sql);
    $categories = [];

    while ($row = $result->fetch_assoc()) {
        $categories[] = $row;
    }

    echo json_encode(["status" => "success", "data" => $categories]);
}

function addCategory($conn, $data, $store_id) {
    if (!isset($data['category_name'])) {
        echo json_encode(["status" => "error", "message" => "Missing category_name"]);
        return;
    }

    $name = $conn->real_escape_string($data['category_name']);
    $sql = "INSERT INTO categories (category_name, store_id) VALUES ('$name', $store_id)";
    
    if ($conn->query($sql) === TRUE) {
        echo json_encode(["status" => "success", "message" => "Category added"]);
    } else {
        echo json_encode(["status" => "error", "message" => $conn->error]);
    }
}

function updateCategory($conn, $data, $store_id) {
    if (!isset($data['category_id']) || !isset($data['category_name'])) {
        echo json_encode(["status" => "error", "message" => "Missing category_id or category_name"]);
        return;
    }

    $id = intval($data['category_id']);
    $name = $conn->real_escape_string($data['category_name']);

    $sql = "UPDATE categories SET category_name = '$name' WHERE category_id = $id AND store_id = $store_id";
    
    if ($conn->query($sql) === TRUE) {
        echo json_encode(["status" => "success", "message" => "Category updated"]);
    } else {
        echo json_encode(["status" => "error", "message" => $conn->error]);
    }
}

function deleteCategory($conn, $data, $store_id) {
    if (!isset($data['category_id'])) {
        echo json_encode(["status" => "error", "message" => "Missing category_id"]);
        return;
    }

    $id = intval($data['category_id']);
    $sql = "DELETE FROM categories WHERE category_id = $id AND store_id = $store_id";

    if ($conn->query($sql) === TRUE) {
        echo json_encode(["status" => "success", "message" => "Category deleted"]);
    } else {
        echo json_encode(["status" => "error", "message" => $conn->error]);
    }
}
?>
