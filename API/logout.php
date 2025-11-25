<?php
session_start();
session_destroy(); // Destroy session
echo json_encode(["message" => "Logout successful"]);
?>
