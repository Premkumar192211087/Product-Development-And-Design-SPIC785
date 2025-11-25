<?php
header('Content-Type: application/json');

// Simple, defensive implementation. In production, include real db connection.
$store_id = isset($_GET['store_id']) ? $_GET['store_id'] : '';

// TODO: Replace with real DB connection and query
// Example output structure expected by the app
$batches = [
    [
        'product_name' => 'Sample Product A',
        'barcode' => 'ABC123',
        'mfg_date' => '2025-01-01',
        'exp_date' => '2026-01-01',
        'quantity' => 10,
        'store_id' => $store_id
    ],
    [
        'product_name' => 'Sample Product B',
        'barcode' => 'XYZ789',
        'mfg_date' => '2025-02-01',
        'exp_date' => '2026-02-01',
        'quantity' => 5,
        'store_id' => $store_id
    ]
];

echo json_encode($batches);
exit;

