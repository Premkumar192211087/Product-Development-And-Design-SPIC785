-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Sep 09, 2025 at 06:49 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `inventory_management`
--

-- --------------------------------------------------------

--
-- Table structure for table `audit_log`
--

CREATE TABLE `audit_log` (
  `log_id` int(11) NOT NULL,
  `table_name` varchar(100) NOT NULL,
  `record_id` int(11) NOT NULL,
  `action` enum('INSERT','UPDATE','DELETE') NOT NULL,
  `old_values` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`old_values`)),
  `new_values` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`new_values`)),
  `changed_by` int(11) NOT NULL,
  `store_id` int(11) DEFAULT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `user_agent` text DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `batch_details`
--

CREATE TABLE `batch_details` (
  `id` int(255) NOT NULL,
  `store_id` int(255) NOT NULL,
  `product_id` int(255) NOT NULL,
  `barcode` varchar(255) NOT NULL,
  `product_name` varchar(255) NOT NULL,
  `mfg_date` date NOT NULL,
  `exp_date` date NOT NULL,
  `quantity` int(255) NOT NULL,
  `batch_id` varchar(255) NOT NULL,
  `damaged_quantity` int(11) DEFAULT 0 CHECK (`damaged_quantity` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `batch_details`
--

INSERT INTO `batch_details` (`id`, `store_id`, `product_id`, `barcode`, `product_name`, `mfg_date`, `exp_date`, `quantity`, `batch_id`, `damaged_quantity`) VALUES
(1, 1, 1, '1234567890123', 'iPhone 15 Pro', '2024-01-15', '2026-01-15', 25, 'BATCH001', 0),
(2, 1, 2, '1234567890124', 'Samsung Galaxy S24', '2024-02-01', '2026-02-01', 20, 'BATCH002', 0),
(3, 1, 3, '1234567890125', 'MacBook Air M3', '2024-01-20', '2026-01-20', 15, 'BATCH003', 0),
(4, 1, 4, '1234567890126', 'Dell XPS 13', '2024-02-10', '2026-02-10', 12, 'BATCH004', 0),
(5, 1, 5, '1234567890127', 'Sony WH-1000XM5', '2024-01-25', '2025-01-25', 30, 'BATCH005', 0),
(6, 1, 6, '1234567890128', 'PlayStation 5', '2024-02-05', '2026-02-05', 8, 'BATCH006', 0),
(7, 1, 7, '1234567890129', 'Nintendo Switch OLED', '2024-01-30', '2026-01-30', 18, 'BATCH007', 0),
(8, 1, 8, '1234567890130', 'USB-C Cable 6ft', '2024-02-15', '2027-02-15', 100, 'BATCH008', 0),
(9, 1, 9, '1234567890131', 'iPhone 15 Case', '2024-02-20', '2027-02-20', 50, 'BATCH009', 0),
(10, 1, 10, '1234567890132', 'Wireless Charger Pad', '2024-02-12', '2026-02-12', 35, 'BATCH010', 0);

-- --------------------------------------------------------

--
-- Table structure for table `categories`
--

CREATE TABLE `categories` (
  `category_id` int(11) NOT NULL,
  `store_id` int(11) NOT NULL,
  `category_name` varchar(255) NOT NULL,
  `parent_category_id` int(11) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `categories`
--

INSERT INTO `categories` (`category_id`, `store_id`, `category_name`, `parent_category_id`, `description`, `created_at`, `updated_at`) VALUES
(1, 1, 'Electronics', NULL, 'Main electronics category', '2025-06-09 05:28:27', '2025-06-09 05:28:27'),
(2, 1, 'Mobile Devices', 1, 'Smartphones and tablets', '2025-06-09 05:28:27', '2025-06-09 05:28:27'),
(3, 1, 'Computers', 1, 'Laptops and desktops', '2025-06-09 05:28:27', '2025-06-09 05:28:27'),
(4, 1, 'Audio', 1, 'Headphones and speakers', '2025-06-09 05:28:27', '2025-06-09 05:28:27'),
(5, 1, 'Gaming', 1, 'Gaming consoles and accessories', '2025-06-09 05:28:27', '2025-06-09 05:28:27'),
(6, 1, 'Accessories', NULL, 'Electronic accessories', '2025-06-09 05:28:27', '2025-06-09 05:28:27'),
(7, 1, 'Cables', 6, 'Various types of cables', '2025-06-09 05:28:27', '2025-06-09 05:28:27'),
(8, 1, 'Cases', 6, 'Protective cases', '2025-06-09 05:28:27', '2025-06-09 05:28:27'),
(9, 1, 'Chargers', 6, 'Power adapters and chargers', '2025-06-09 05:28:27', '2025-06-09 05:28:27'),
(10, 1, 'Smart Home', 1, 'IoT and smart home devices', '2025-06-09 05:28:27', '2025-06-09 05:28:27'),
(29, 1, 'Electronics', NULL, 'Electronic items and gadgets', '2025-06-01 14:50:12', '2025-06-01 14:50:12'),
(30, 1, 'Groceries', NULL, 'Food and grocery items', '2025-06-01 14:50:12', '2025-06-01 14:50:12'),
(31, 1, 'Clothing', NULL, 'Apparel and accessories', '2025-06-01 14:50:12', '2025-06-01 14:50:12'),
(32, 1, 'Home & Garden', NULL, 'Home improvement and garden supplies', '2025-06-01 14:50:12', '2025-06-01 14:50:12'),
(33, 1, 'Health & Beauty', NULL, 'Health and beauty products', '2025-06-01 14:50:12', '2025-06-01 14:50:12');

-- --------------------------------------------------------

--
-- Table structure for table `customers`
--

CREATE TABLE `customers` (
  `customer_id` int(11) NOT NULL,
  `store_id` int(11) NOT NULL,
  `customer_name` varchar(255) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` text DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `loyalty_points` int(11) DEFAULT 0,
  `registration_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `last_purchase_date` timestamp NULL DEFAULT NULL,
  `status` enum('active','inactive') DEFAULT 'active',
  `notes` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `customers`
--

INSERT INTO `customers` (`customer_id`, `store_id`, `customer_name`, `email`, `phone`, `address`, `date_of_birth`, `loyalty_points`, `registration_date`, `last_purchase_date`, `status`, `notes`) VALUES
(1, 1, 'Alice Johnson', 'alice.johnson@email.com', '555-2001', '123 Customer Lane, Downtown', '1985-03-15', 150, '2025-06-09 05:30:09', NULL, 'active', NULL),
(2, 1, 'Bob Smith', 'bob.smith@email.com', '555-2002', '456 Buyer Street, Downtown', '1978-07-22', 320, '2025-06-09 05:30:09', NULL, 'active', NULL),
(3, 1, 'Carol Davis', 'carol.davis@email.com', '555-2003', '789 Shopper Ave, Downtown', '1992-11-08', 75, '2025-06-09 05:30:09', NULL, 'active', NULL),
(4, 1, 'David Wilson', 'david.wilson@email.com', '555-2004', '321 Purchase Rd, Downtown', '1987-05-30', 200, '2025-06-09 05:30:09', NULL, 'active', NULL),
(5, 1, 'Eva Martinez', 'eva.martinez@email.com', '555-2005', '654 Client Blvd, Downtown', '1990-09-12', 450, '2025-06-09 05:30:09', NULL, 'active', NULL),
(6, 1, 'Frank Taylor', 'frank.taylor@email.com', '555-2006', '987 Patron Way, Downtown', '1983-12-25', 125, '2025-06-09 05:30:09', NULL, 'active', NULL),
(7, 1, 'Grace Lee', 'grace.lee@email.com', '555-2007', '147 Consumer St, Downtown', '1989-04-18', 300, '2025-06-09 05:30:09', NULL, 'active', NULL),
(8, 1, 'Henry Brown', 'henry.brown@email.com', '555-2008', '258 Buyer Lane, Downtown', '1975-08-03', 180, '2025-06-09 05:30:09', NULL, 'active', NULL),
(9, 1, 'Iris Garcia', 'iris.garcia@email.com', '555-2009', '369 Customer Ave, Downtown', '1994-01-27', 95, '2025-06-09 05:30:09', NULL, 'active', NULL),
(10, 1, 'Jack White', 'jack.white@email.com', '555-2010', '741 Shopper Dr, Downtown', '1986-06-14', 275, '2025-06-09 05:30:09', NULL, 'active', NULL),
(11, 1, 'Prem', 'tprem6565@gmail.com', '8985545407', 'peddajonnavaram-V', '2025-06-10', 0, '2025-06-10 05:29:52', NULL, 'active', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `damages`
--

CREATE TABLE `damages` (
  `id` int(11) NOT NULL,
  `batch_id` varchar(255) NOT NULL,
  `store_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `quantity_damaged` int(11) NOT NULL CHECK (`quantity_damaged` > 0),
  `reason` text DEFAULT NULL,
  `damaged_by` int(11) NOT NULL,
  `scanned_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `inventory_alerts`
--

CREATE TABLE `inventory_alerts` (
  `alert_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `store_id` int(11) NOT NULL,
  `min_stock_level` int(11) NOT NULL DEFAULT 10,
  `max_stock_level` int(11) DEFAULT NULL,
  `reorder_quantity` int(11) NOT NULL DEFAULT 50,
  `alert_enabled` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `inventory_alerts`
--

INSERT INTO `inventory_alerts` (`alert_id`, `product_id`, `store_id`, `min_stock_level`, `max_stock_level`, `reorder_quantity`, `alert_enabled`, `created_at`, `updated_at`) VALUES
(1, 1, 1, 5, 50, 20, 1, '2025-06-09 05:24:53', '2025-06-09 05:24:53'),
(2, 2, 1, 5, 40, 15, 1, '2025-06-09 05:24:53', '2025-06-09 05:24:53'),
(3, 3, 1, 3, 30, 10, 1, '2025-06-09 05:24:53', '2025-06-09 05:24:53'),
(4, 4, 1, 3, 25, 8, 1, '2025-06-09 05:24:53', '2025-06-09 05:24:53'),
(5, 5, 1, 10, 60, 25, 1, '2025-06-09 05:24:53', '2025-06-09 05:24:53'),
(6, 6, 1, 2, 20, 5, 1, '2025-06-09 05:24:53', '2025-06-09 05:24:53'),
(7, 7, 1, 5, 35, 12, 1, '2025-06-09 05:24:53', '2025-06-09 05:24:53'),
(8, 8, 1, 25, 200, 75, 1, '2025-06-09 05:24:53', '2025-06-09 05:24:53'),
(9, 9, 1, 15, 100, 40, 1, '2025-06-09 05:24:53', '2025-06-09 05:24:53'),
(10, 10, 1, 10, 70, 30, 1, '2025-06-09 05:24:53', '2025-06-09 05:24:53');

-- --------------------------------------------------------

--
-- Table structure for table `invoices`
--

CREATE TABLE `invoices` (
  `invoice_id` bigint(20) UNSIGNED NOT NULL,
  `customer_id` int(11) NOT NULL,
  `store_id` int(11) NOT NULL,
  `invoice_number` varchar(50) NOT NULL,
  `issue_date` date NOT NULL,
  `due_date` date DEFAULT NULL,
  `status` varchar(20) DEFAULT 'unpaid',
  `subtotal` decimal(10,2) NOT NULL,
  `tax` decimal(10,2) DEFAULT 0.00,
  `discount` decimal(10,2) DEFAULT 0.00,
  `total` decimal(10,2) NOT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `invoices`
--

INSERT INTO `invoices` (`invoice_id`, `customer_id`, `store_id`, `invoice_number`, `issue_date`, `due_date`, `status`, `subtotal`, `tax`, `discount`, `total`, `notes`, `created_at`, `updated_at`) VALUES
(1, 1, 1, 'INV-2024-001', '2024-02-01', '2024-03-01', 'paid', 999.99, 80.00, 0.00, 1079.99, 'iPhone 15 Pro purchase', '2025-06-09 05:21:32', '2025-06-09 05:21:32'),
(2, 2, 1, 'INV-2024-002', '2024-02-02', '2024-03-02', 'paid', 399.99, 30.40, 20.00, 410.39, 'Sony headphones with discount', '2025-06-09 05:21:32', '2025-06-09 05:21:32'),
(3, 3, 1, 'INV-2024-003', '2024-02-03', '2024-03-03', 'paid', 1299.99, 104.00, 0.00, 1403.99, 'MacBook Air M3', '2025-06-09 05:21:32', '2025-06-09 05:21:32'),
(4, 4, 1, 'INV-2024-004', '2024-02-04', '2024-03-04', 'paid', 59.98, 4.80, 0.00, 64.78, 'Accessories bundle', '2025-06-09 05:21:32', '2025-06-09 05:21:32'),
(5, 5, 1, 'INV-2024-005', '2024-02-05', '2024-03-05', 'paid', 499.99, 38.00, 25.00, 512.99, 'PlayStation 5 with loyalty discount', '2025-06-09 05:21:32', '2025-06-09 05:21:32'),
(6, 6, 1, 'INV-2024-006', '2024-02-06', '2024-03-06', 'paid', 349.99, 28.00, 0.00, 377.99, 'Nintendo Switch OLED', '2025-06-09 05:21:32', '2025-06-09 05:21:32'),
(7, 7, 1, 'INV-2024-007', '2024-02-07', '2024-03-07', 'paid', 1099.99, 84.00, 50.00, 1133.99, 'Dell XPS 13 with promo', '2025-06-09 05:21:32', '2025-06-09 05:21:32'),
(8, 8, 1, 'INV-2024-008', '2024-02-08', '2024-03-08', 'paid', 89.97, 7.20, 0.00, 97.17, 'Multiple accessories', '2025-06-09 05:21:32', '2025-06-09 05:21:32'),
(9, 9, 1, 'INV-2024-009', '2024-02-09', '2024-03-09', 'paid', 899.99, 72.00, 0.00, 971.99, 'Samsung Galaxy S24', '2025-06-09 05:21:32', '2025-06-09 05:21:32'),
(10, 10, 1, 'INV-2024-010', '2024-02-10', '2024-03-10', 'paid', 149.97, 11.20, 10.00, 151.17, 'Charger and case combo', '2025-06-09 05:21:32', '2025-06-09 05:21:32');

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `id` int(255) NOT NULL,
  `store_id` int(255) NOT NULL,
  `title` varchar(255) NOT NULL,
  `message` varchar(255) NOT NULL,
  `type` enum('lowstock','expiry','restock') NOT NULL,
  `status` enum('read','mark as read') NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `user_id` int(11) DEFAULT NULL,
  `batch_id` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`id`, `store_id`, `title`, `message`, `type`, `status`, `timestamp`, `user_id`, `batch_id`) VALUES
(6, 1, 'Low Stock Alert', 'MacBook Air M2 stock is running low (5 units remaining)', 'lowstock', 'read', '2025-06-01 14:54:20', NULL, NULL),
(7, 1, 'Expiry Alert', 'Organic Bananas batch expires in 3 days', 'expiry', 'mark as read', '2025-06-01 14:54:20', NULL, NULL),
(8, 1, 'Restock Required', 'Mens Cotton T-Shirt needs restocking', 'restock', 'read', '2025-06-01 14:54:20', NULL, NULL),
(9, 1, 'New Shipment', 'Electronics shipment PO-2024-001 received', '', 'read', '2025-06-01 14:54:20', NULL, NULL),
(10, 1, 'Low Stock Alert', 'Vitamin C Tablets below minimum stock level', 'lowstock', 'mark as read', '2025-06-01 14:54:20', NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `products`
--

CREATE TABLE `products` (
  `id` int(255) NOT NULL,
  `store_id` int(255) NOT NULL,
  `product_name` varchar(255) NOT NULL,
  `quantity` int(255) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `category` varchar(255) NOT NULL,
  `sku` varchar(255) DEFAULT NULL,
  `image_url` varchar(500) DEFAULT NULL,
  `status` enum('active','inactive') DEFAULT 'active'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `products`
--

INSERT INTO `products` (`id`, `store_id`, `product_name`, `quantity`, `price`, `created_at`, `category`, `sku`, `image_url`, `status`) VALUES
(1, 1, 'iPhone 15 Pro', 9, 999.99, '2025-06-09 05:23:54', 'Mobile Devices', 'IPH15PRO001', 'https://example.com/iphone15pro.jpg', 'active'),
(2, 1, 'Samsung Galaxy S24', 20, 899.99, '2025-06-09 05:23:54', 'Mobile Devices', 'SAM24001', 'https://example.com/galaxys24.jpg', 'active'),
(3, 1, 'MacBook Air M3', 15, 1299.99, '2025-06-09 05:23:54', 'Computers', 'MBA2024001', 'https://example.com/macbookair.jpg', 'active'),
(4, 1, 'Dell XPS 13', 12, 1099.99, '2025-06-09 05:23:54', 'Computers', 'DELL13001', 'https://example.com/dellxps13.jpg', 'active'),
(5, 1, 'Sony WH-1000XM5', 30, 399.99, '2025-06-09 05:23:54', 'Audio', 'SONY1000XM5', 'https://example.com/sonywh1000xm5.jpg', 'active'),
(6, 1, 'PlayStation 5', 8, 499.99, '2025-06-09 05:23:54', 'Gaming', 'PS5CONSOLE', 'https://example.com/ps5.jpg', 'active'),
(7, 1, 'Nintendo Switch OLED', 18, 349.99, '2025-06-09 05:23:54', 'Gaming', 'NSWITCHOLED', 'https://example.com/switcholed.jpg', 'active'),
(8, 1, 'USB-C Cable 6ft', 100, 19.99, '2025-06-09 05:23:54', 'Cables', 'USBCABLE6FT', 'https://example.com/usbccable.jpg', 'active'),
(9, 1, 'iPhone 15 Case', 50, 29.99, '2025-06-09 05:23:54', 'Cases', 'IPH15CASE', 'https://example.com/iphone15case.jpg', 'active'),
(10, 1, 'Wireless Charger Pad', 35, 49.99, '2025-06-09 05:23:54', 'Chargers', 'WRLSCHARGER', 'https://example.com/wirelesscharger.jpg', 'active');

-- --------------------------------------------------------

--
-- Table structure for table `product_suppliers`
--

CREATE TABLE `product_suppliers` (
  `ps_id` int(11) NOT NULL,
  `store_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `supplier_id` int(11) NOT NULL,
  `supplier_sku` varchar(100) DEFAULT NULL,
  `lead_time_days` int(11) DEFAULT 7,
  `min_order_quantity` int(11) DEFAULT 1,
  `unit_cost` decimal(10,2) NOT NULL,
  `is_primary` tinyint(1) DEFAULT 0,
  `status` enum('active','inactive') DEFAULT 'active',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `product_suppliers`
--

INSERT INTO `product_suppliers` (`ps_id`, `store_id`, `product_id`, `supplier_id`, `supplier_sku`, `lead_time_days`, `min_order_quantity`, `unit_cost`, `is_primary`, `status`, `created_at`) VALUES
(1, 1, 1, 1, 'TS-IPH15PRO', 7, 5, 750.00, 1, 'active', '2025-06-09 05:24:13'),
(2, 1, 2, 2, 'EW-SAM24', 10, 3, 650.00, 1, 'active', '2025-06-09 05:24:13'),
(3, 1, 3, 1, 'TS-MBA2024', 14, 2, 950.00, 1, 'active', '2025-06-09 05:24:13'),
(4, 1, 4, 2, 'EW-DELLXPS', 12, 2, 800.00, 1, 'active', '2025-06-09 05:24:13'),
(5, 1, 5, 4, 'AP-SONY1000', 7, 5, 280.00, 1, 'active', '2025-06-09 05:24:13'),
(6, 1, 6, 5, 'GZ-PS5CON', 21, 1, 375.00, 1, 'active', '2025-06-09 05:24:13'),
(7, 1, 7, 5, 'GZ-NSWITCH', 14, 3, 250.00, 1, 'active', '2025-06-09 05:24:13'),
(8, 1, 8, 7, 'CC-USBCABLE', 3, 50, 8.99, 1, 'active', '2025-06-09 05:24:13'),
(9, 1, 9, 9, 'TCP-IPH15CASE', 5, 25, 15.99, 1, 'active', '2025-06-09 05:24:13'),
(10, 1, 10, 8, 'PP-WRLSCHARGE', 7, 10, 32.99, 1, 'active', '2025-06-09 05:24:13');

-- --------------------------------------------------------

--
-- Table structure for table `purchase_orders`
--

CREATE TABLE `purchase_orders` (
  `po_id` int(11) NOT NULL,
  `po_number` varchar(50) NOT NULL,
  `supplier_id` int(11) NOT NULL,
  `store_id` int(11) NOT NULL,
  `order_date` date NOT NULL,
  `expected_delivery_date` date DEFAULT NULL,
  `actual_delivery_date` date DEFAULT NULL,
  `total_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `status` enum('pending','partial','received','cancelled') DEFAULT 'pending',
  `created_by` int(11) NOT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `purchase_orders`
--

INSERT INTO `purchase_orders` (`po_id`, `po_number`, `supplier_id`, `store_id`, `order_date`, `expected_delivery_date`, `actual_delivery_date`, `total_amount`, `status`, `created_by`, `notes`, `created_at`, `updated_at`) VALUES
(1, 'PO-2024-001', 1, 1, '2024-01-15', '2024-01-22', NULL, 15000.00, 'received', 2, 'Monthly iPhone stock replenishment', '2025-06-09 05:31:01', '2025-06-09 05:31:01'),
(2, 'PO-2024-002', 2, 1, '2024-01-20', '2024-01-30', NULL, 12000.00, 'received', 2, 'Samsung and Dell products', '2025-06-09 05:31:01', '2025-06-09 05:31:01'),
(3, 'PO-2024-003', 4, 1, '2024-02-01', '2024-02-08', NULL, 8400.00, 'received', 2, 'Audio equipment restock', '2025-06-09 05:31:01', '2025-06-09 05:31:01'),
(4, 'PO-2024-004', 5, 1, '2024-02-05', '2024-02-26', NULL, 6250.00, 'pending', 2, 'Gaming consoles order', '2025-06-09 05:31:01', '2025-06-09 05:31:01'),
(5, 'PO-2024-005', 7, 1, '2024-02-10', '2024-02-13', NULL, 899.00, 'received', 8, 'Cables bulk order', '2025-06-09 05:31:01', '2025-06-09 05:31:01'),
(6, 'PO-2024-006', 8, 1, '2024-02-15', '2024-02-22', NULL, 1650.00, 'partial', 8, 'Charging accessories', '2025-06-09 05:31:01', '2025-06-09 05:31:01'),
(7, 'PO-2024-007', 9, 1, '2024-02-20', '2024-02-25', NULL, 799.50, 'pending', 8, 'Protective cases', '2025-06-09 05:31:01', '2025-06-09 05:31:01'),
(8, 'PO-2024-008', 1, 1, '2024-03-01', '2024-03-08', NULL, 20000.00, 'pending', 2, 'Large electronics order', '2025-06-09 05:31:01', '2025-06-09 05:31:01'),
(9, 'PO-2024-009', 6, 1, '2024-03-05', '2024-03-12', NULL, 3500.00, 'pending', 2, 'Smart home devices', '2025-06-09 05:31:01', '2025-06-09 05:31:01'),
(10, 'PO-2024-010', 10, 1, '2024-03-10', '2024-03-17', NULL, 5000.00, 'pending', 2, 'Import electronics batch', '2025-06-09 05:31:01', '2025-06-09 05:31:01');

-- --------------------------------------------------------

--
-- Table structure for table `purchase_order_items`
--

CREATE TABLE `purchase_order_items` (
  `poi_id` int(11) NOT NULL,
  `store_id` int(11) NOT NULL,
  `po_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `quantity_ordered` int(11) NOT NULL,
  `quantity_received` int(11) DEFAULT 0,
  `unit_price` decimal(10,2) NOT NULL,
  `total_price` decimal(12,2) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `purchase_order_items`
--

INSERT INTO `purchase_order_items` (`poi_id`, `store_id`, `po_id`, `product_id`, `quantity_ordered`, `quantity_received`, `unit_price`, `total_price`, `created_at`) VALUES
(1, 1, 1, 1, 20, 20, 750.00, 15000.00, '2025-06-09 05:31:18'),
(2, 1, 2, 2, 15, 15, 650.00, 9750.00, '2025-06-09 05:31:18'),
(3, 1, 2, 4, 3, 3, 800.00, 2400.00, '2025-06-09 05:31:18'),
(4, 1, 3, 5, 30, 30, 280.00, 8400.00, '2025-06-09 05:31:18'),
(5, 1, 4, 6, 10, 0, 375.00, 3750.00, '2025-06-09 05:31:18'),
(6, 1, 4, 7, 10, 0, 250.00, 2500.00, '2025-06-09 05:31:18'),
(7, 1, 5, 8, 100, 100, 8.99, 899.00, '2025-06-09 05:31:18'),
(8, 1, 6, 10, 50, 25, 32.99, 1649.50, '2025-06-09 05:31:18'),
(9, 1, 7, 9, 50, 0, 15.99, 799.50, '2025-06-09 05:31:18'),
(10, 1, 8, 3, 20, 0, 950.00, 19000.00, '2025-06-09 05:31:18');

-- --------------------------------------------------------

--
-- Table structure for table `reports`
--

CREATE TABLE `reports` (
  `id` int(255) NOT NULL,
  `product_name` varchar(255) NOT NULL,
  `type` enum('invoiced','ordered','damaged','expired','delivered','shipped','packed') NOT NULL,
  `status` enum('pending','completed') NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `quantity` int(255) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `store_id` int(255) NOT NULL,
  `batch_id` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reports`
--

INSERT INTO `reports` (`id`, `product_name`, `type`, `status`, `timestamp`, `quantity`, `price`, `store_id`, `batch_id`) VALUES
(1, 'iPhone 14 Pro', 'invoiced', 'completed', '2025-06-01 14:54:20', 1, 999.99, 1, NULL),
(2, 'MacBook Air M2', 'invoiced', 'completed', '2025-06-01 14:54:20', 1, 1199.99, 1, NULL),
(3, 'Organic Bananas', 'delivered', 'completed', '2025-06-01 14:54:20', 100, 2.99, 1, NULL),
(4, 'Whole Milk 1 Gallon', 'delivered', 'completed', '2025-06-01 14:54:20', 50, 3.49, 1, NULL),
(5, 'Mens Cotton T-Shirt', 'ordered', 'pending', '2025-06-01 14:54:20', 100, 19.99, 1, NULL),
(6, 'LED Desk Lamp', 'ordered', 'pending', '2025-06-01 14:54:20', 25, 34.99, 1, NULL),
(7, 'Samsung Galaxy S23', 'shipped', 'completed', '2025-06-01 14:54:20', 1, 849.99, 1, NULL),
(8, 'Bluetooth Headphones', 'packed', 'completed', '2025-06-01 14:54:20', 1, 79.99, 1, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `returns`
--

CREATE TABLE `returns` (
  `return_id` int(11) NOT NULL,
  `return_number` varchar(50) NOT NULL,
  `sale_id` int(11) NOT NULL,
  `customer_id` int(11) DEFAULT NULL,
  `store_id` int(11) NOT NULL,
  `return_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `total_refund_amount` decimal(12,2) NOT NULL,
  `return_reason` text NOT NULL,
  `processed_by` int(11) NOT NULL,
  `status` enum('pending','processed','cancelled') DEFAULT 'pending',
  `notes` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `returns`
--

INSERT INTO `returns` (`return_id`, `return_number`, `sale_id`, `customer_id`, `store_id`, `return_date`, `total_refund_amount`, `return_reason`, `processed_by`, `status`, `notes`) VALUES
(1, 'RET-2024-001', 2, 2, 1, '2025-06-09 05:32:00', 410.39, 'Defective product - headphones not working properly', 2, 'processed', 'Full refund issued'),
(2, 'RET-2024-002', 4, 4, 1, '2025-06-09 05:32:00', 29.99, 'Customer changed mind about phone case', 3, 'processed', 'Partial return - case only'),
(3, 'RET-2024-003', 8, 8, 1, '2025-06-09 05:32:00', 19.99, 'Wrong cable type ordered', 4, 'processed', 'Exchange for correct cable'),
(4, 'RET-2024-004', 10, 10, 1, '2025-06-09 05:32:00', 49.99, 'Wireless charger compatibility issue', 2, 'pending', 'Under review'),
(5, 'RET-2024-005', 6, 6, 1, '2025-06-09 05:32:00', 377.99, 'Console arrived damaged', 2, 'processed', 'Full refund for damaged Switch'),
(6, 'RET-2024-006', 1, 1, 1, '2025-06-09 05:32:00', 0.00, 'Screen protector request', 3, 'cancelled', 'Customer kept original purchase'),
(7, 'RET-2024-007', 9, 9, 1, '2025-06-09 05:32:00', 0.00, 'Inquiry about warranty', 4, 'cancelled', 'No return needed - warranty info provided'),
(8, 'RET-2024-008', 5, 5, 1, '2025-06-09 05:32:00', 0.00, 'Controller compatibility question', 2, 'cancelled', 'Compatible - no return');

-- --------------------------------------------------------

--
-- Table structure for table `return_items`
--

CREATE TABLE `return_items` (
  `return_item_id` int(11) NOT NULL,
  `store_id` int(11) NOT NULL,
  `return_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `quantity_returned` int(11) NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `refund_amount` decimal(10,2) NOT NULL,
  `condition_status` enum('good','damaged','expired') DEFAULT 'good'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `return_items`
--

INSERT INTO `return_items` (`return_item_id`, `store_id`, `return_id`, `product_id`, `quantity_returned`, `unit_price`, `refund_amount`, `condition_status`) VALUES
(1, 1, 1, 1, 2, 500.00, 1000.00, 'damaged'),
(2, 1, 2, 2, 1, 1500.00, 1500.00, 'good'),
(3, 1, 3, 2, 5, 200.00, 1000.00, 'expired'),
(4, 1, 4, 3, 3, 300.00, 900.00, 'good'),
(5, 1, 5, 4, 1, 500.00, 500.00, 'good');

-- --------------------------------------------------------

--
-- Table structure for table `sales`
--

CREATE TABLE `sales` (
  `sale_id` int(11) NOT NULL,
  `invoice_number` varchar(50) NOT NULL,
  `customer_id` int(11) DEFAULT NULL,
  `store_id` int(11) NOT NULL,
  `sale_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `total_amount` decimal(12,2) NOT NULL,
  `discount_amount` decimal(10,2) DEFAULT 0.00,
  `tax_amount` decimal(10,2) DEFAULT 0.00,
  `final_amount` decimal(12,2) NOT NULL,
  `payment_method` enum('cash','credit_card','debit_card','upi','net_banking','cheque') NOT NULL,
  `payment_status` enum('paid','pending','partial') DEFAULT 'paid',
  `served_by` int(11) NOT NULL,
  `notes` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `sales`
--

INSERT INTO `sales` (`sale_id`, `invoice_number`, `customer_id`, `store_id`, `sale_date`, `total_amount`, `discount_amount`, `tax_amount`, `final_amount`, `payment_method`, `payment_status`, `served_by`, `notes`) VALUES
(1, 'INV-2024-001', 1, 1, '2025-06-09 05:30:28', 999.99, 0.00, 80.00, 1079.99, 'credit_card', 'paid', 3, 'Customer bought iPhone 15 Pro'),
(2, 'INV-2024-002', 2, 1, '2025-06-09 05:30:28', 399.99, 20.00, 30.40, 410.39, 'cash', 'paid', 4, 'Sony headphones with discount'),
(3, 'INV-2024-003', 3, 1, '2025-06-09 05:30:28', 1299.99, 0.00, 104.00, 1403.99, 'credit_card', 'paid', 3, 'MacBook Air purchase'),
(4, 'INV-2024-004', 4, 1, '2025-06-09 05:30:28', 59.98, 0.00, 4.80, 64.78, 'upi', 'paid', 4, 'Accessories bundle'),
(5, 'INV-2024-005', 5, 1, '2025-06-09 05:30:28', 499.99, 25.00, 38.00, 512.99, 'debit_card', 'paid', 3, 'PlayStation 5 with loyalty discount'),
(6, 'INV-2024-006', 6, 1, '2025-06-09 05:30:28', 349.99, 0.00, 28.00, 377.99, 'cash', 'paid', 4, 'Nintendo Switch OLED'),
(7, 'INV-2024-007', 7, 1, '2025-06-09 05:30:28', 1099.99, 50.00, 84.00, 1133.99, 'credit_card', 'paid', 3, 'Dell laptop with promotional discount'),
(8, 'INV-2024-008', 8, 1, '2025-06-09 05:30:28', 89.97, 0.00, 7.20, 97.17, 'upi', 'paid', 4, 'Multiple accessories'),
(9, 'INV-2024-009', 9, 1, '2025-06-09 05:30:28', 899.99, 0.00, 72.00, 971.99, 'credit_card', 'paid', 3, 'Samsung Galaxy S24'),
(10, 'INV-2024-010', 10, 1, '2025-06-09 05:30:28', 149.97, 10.00, 11.20, 151.17, 'cash', 'paid', 4, 'Wireless charger and case combo');

-- --------------------------------------------------------

--
-- Table structure for table `sale_items`
--

CREATE TABLE `sale_items` (
  `sale_item_id` int(11) NOT NULL,
  `store_id` int(11) NOT NULL,
  `sale_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `discount_percent` decimal(5,2) DEFAULT 0.00,
  `discount_amount` decimal(10,2) DEFAULT 0.00,
  `total_price` decimal(12,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `sale_items`
--

INSERT INTO `sale_items` (`sale_item_id`, `store_id`, `sale_id`, `product_id`, `quantity`, `unit_price`, `discount_percent`, `discount_amount`, `total_price`) VALUES
(1, 1, 1, 1, 1, 999.99, 0.00, 0.00, 999.99),
(2, 1, 2, 5, 1, 399.99, 5.00, 20.00, 379.99),
(3, 1, 3, 3, 1, 1299.99, 0.00, 0.00, 1299.99),
(4, 1, 4, 8, 2, 19.99, 0.00, 0.00, 39.98),
(5, 1, 4, 9, 1, 29.99, 0.00, 0.00, 29.99),
(6, 1, 5, 6, 1, 499.99, 5.00, 25.00, 474.99),
(7, 1, 6, 7, 1, 349.99, 0.00, 0.00, 349.99),
(8, 1, 7, 4, 1, 1099.99, 4.55, 50.00, 1049.99),
(9, 1, 8, 8, 3, 19.99, 0.00, 0.00, 59.97),
(10, 1, 8, 10, 1, 49.99, 0.00, 0.00, 49.99),
(11, 1, 9, 2, 1, 899.99, 0.00, 0.00, 899.99),
(12, 1, 10, 10, 2, 49.99, 0.00, 0.00, 99.98),
(13, 1, 10, 9, 1, 29.99, 0.00, 0.00, 29.99);

-- --------------------------------------------------------

--
-- Table structure for table `shipments`
--

CREATE TABLE `shipments` (
  `shipment_id` int(11) NOT NULL,
  `shipment_number` varchar(50) NOT NULL,
  `order_type` enum('purchase_order','sales_order') NOT NULL,
  `order_id` int(11) NOT NULL,
  `carrier_name` varchar(100) DEFAULT NULL,
  `tracking_number` varchar(100) DEFAULT NULL,
  `shipping_method` varchar(50) DEFAULT 'standard',
  `shipping_cost` decimal(10,2) DEFAULT 0.00,
  `ship_date` date DEFAULT NULL,
  `estimated_delivery_date` date DEFAULT NULL,
  `actual_delivery_date` date DEFAULT NULL,
  `status` enum('pending','shipped','in_transit','delivered','returned','cancelled') NOT NULL DEFAULT 'pending',
  `recipient_name` varchar(255) DEFAULT NULL,
  `recipient_address` text DEFAULT NULL,
  `recipient_phone` varchar(20) DEFAULT NULL,
  `store_id` int(11) NOT NULL,
  `notes` text DEFAULT NULL,
  `created_by` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `shipments`
--

INSERT INTO `shipments` (`shipment_id`, `shipment_number`, `order_type`, `order_id`, `carrier_name`, `tracking_number`, `shipping_method`, `shipping_cost`, `ship_date`, `estimated_delivery_date`, `actual_delivery_date`, `status`, `recipient_name`, `recipient_address`, `recipient_phone`, `store_id`, `notes`, `created_by`, `created_at`, `updated_at`) VALUES
(1, 'SHIP2025001', 'purchase_order', 301, 'FedEx', 'TRACK123456789', 'express', 25.00, '2025-06-01', '2025-06-05', '2025-06-04', 'delivered', 'John Doe', '123 Elm Street, Cityville', '555-1234', 1, 'Handle with care', 101, '2025-06-01 02:30:00', '2025-06-04 09:30:00'),
(2, 'SHIP2025002', 'sales_order', 401, 'UPS', 'TRACK987654321', 'standard', 15.50, '2025-06-02', '2025-06-07', NULL, 'in_transit', 'Jane Smith', '456 Oak Avenue, Townsville', '555-5678', 2, NULL, 102, '2025-06-02 04:00:00', '2025-06-08 04:30:00'),
(3, 'SHIP2025003', 'purchase_order', 302, 'DHL', NULL, 'standard', 18.00, '2025-06-03', '2025-06-08', NULL, 'shipped', 'Acme Corp', '789 Pine Road, Villagetown', '555-8765', 1, 'Urgent shipment', 103, '2025-06-03 06:15:00', '2025-06-05 07:50:00'),
(4, 'SHIP2025004', 'sales_order', 402, NULL, NULL, 'standard', 0.00, NULL, NULL, NULL, 'pending', 'Bob Johnson', '321 Maple Blvd, Hamlet', '555-4321', 3, NULL, 104, '2025-06-04 01:45:00', '2025-06-04 01:45:00');

-- --------------------------------------------------------

--
-- Table structure for table `shipment_items`
--

CREATE TABLE `shipment_items` (
  `shipment_item_id` int(11) NOT NULL,
  `store_id` int(11) NOT NULL,
  `shipment_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `quantity_shipped` int(11) NOT NULL,
  `unit_price` decimal(10,2) DEFAULT NULL,
  `total_value` decimal(12,2) DEFAULT NULL,
  `batch_id` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `shipment_items`
--

INSERT INTO `shipment_items` (`shipment_item_id`, `store_id`, `shipment_id`, `product_id`, `quantity_shipped`, `unit_price`, `total_value`, `batch_id`, `created_at`) VALUES
(1, 1, 1, 1, 10, 15.50, 155.00, 'BATCH-001-A', '2025-06-01 02:35:00'),
(2, 1, 1, 2, 5, 25.00, 125.00, 'BATCH-001-B', '2025-06-01 02:35:00'),
(3, 1, 2, 201, 8, 12.00, 96.00, 'BATCH-002-A', '2025-06-02 04:15:00'),
(4, 1, 3, 2, 15, 8.75, 131.25, 'BATCH-003-C', '2025-06-03 06:20:00'),
(5, 1, 4, 301, 20, 5.00, 100.00, NULL, '2025-06-04 02:00:00');

-- --------------------------------------------------------

--
-- Table structure for table `staff_details`
--

CREATE TABLE `staff_details` (
  `staff_id` int(11) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `user_id` int(11) NOT NULL,
  `email` varchar(255) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `role` varchar(255) NOT NULL,
  `store_id` int(11) NOT NULL,
  `address` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `staff_details`
--

INSERT INTO `staff_details` (`staff_id`, `full_name`, `user_id`, `email`, `phone`, `role`, `store_id`, `address`) VALUES
(1, 'tharun', 11, 'thaarun@gmail.com', '8985545407', 'admin', 5, '123 Main Street,City'),
(2, 'T. Prem Kumar Reddy', 12, 'tprem6565@gmail.com', '8985545407', 'Admin', 6, 'peddajonnavaram-V'),
(3, 'Mike Davis', 1, 'mike.davis@electronics.com', '6300724080', 'cashier', 1, '300 Cashier Road, Downtown'),
(4, 'Lisa Wilson', 4, 'lisa.wilson@electronics.com', '555-0104', 'cashier', 1, '400 Cashier Lane, Downtown'),
(8, 'Emma Taylor', 8, 'emma.taylor@electronics.com', '555-0108', 'inventory_manager', 1, '800 Inventory Way, Downtown');

-- --------------------------------------------------------

--
-- Table structure for table `stock_movements`
--

CREATE TABLE `stock_movements` (
  `movement_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `store_id` int(11) NOT NULL,
  `movement_type` enum('in','out','transfer','adjustment','return') NOT NULL,
  `quantity` int(11) NOT NULL,
  `reference_type` enum('sale','purchase','return','damaged','expired','transfer','adjustment') NOT NULL,
  `reference_id` int(11) DEFAULT NULL,
  `unit_price` decimal(10,2) DEFAULT NULL,
  `total_value` decimal(12,2) DEFAULT NULL,
  `performed_by` int(11) NOT NULL,
  `notes` text DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `stock_movements`
--

INSERT INTO `stock_movements` (`movement_id`, `product_id`, `store_id`, `movement_type`, `quantity`, `reference_type`, `reference_id`, `unit_price`, `total_value`, `performed_by`, `notes`, `timestamp`) VALUES
(1, 1, 1, 'in', 20, 'purchase', 1, 750.00, 15000.00, 8, 'Initial stock from PO-2024-001', '2025-06-09 05:31:34'),
(2, 2, 1, 'in', 15, 'purchase', 2, 650.00, 9750.00, 8, 'Samsung phones received', '2025-06-09 05:31:34'),
(3, 1, 1, 'out', 1, 'sale', 1, 999.99, 999.99, 3, 'Sale to customer Alice Johnson', '2025-06-09 05:31:34'),
(4, 5, 1, 'in', 30, 'purchase', 3, 280.00, 8400.00, 8, 'Sony headphones stock', '2025-06-09 05:31:34'),
(5, 5, 1, 'out', 1, 'sale', 2, 399.99, 399.99, 4, 'Sale to customer Bob Smith', '2025-06-09 05:31:34'),
(6, 3, 1, 'in', 15, 'purchase', 2, 950.00, 14250.00, 8, 'MacBook Air inventory', '2025-06-09 05:31:34'),
(7, 3, 1, 'out', 1, 'sale', 3, 1299.99, 1299.99, 3, 'MacBook sale to Carol Davis', '2025-06-09 05:31:34'),
(8, 8, 1, 'in', 100, 'purchase', 5, 8.99, 899.00, 8, 'USB cables bulk order', '2025-06-09 05:31:34'),
(9, 8, 1, 'out', 5, 'sale', 4, 19.99, 99.95, 4, 'Multiple cable sales', '2025-06-09 05:31:34'),
(10, 6, 1, 'in', 8, 'adjustment', NULL, 375.00, 3000.00, 8, 'PS5 inventory adjustment', '2025-06-09 05:31:34');

-- --------------------------------------------------------

--
-- Table structure for table `stores`
--

CREATE TABLE `stores` (
  `store_id` int(11) NOT NULL,
  `store_name` text NOT NULL,
  `store_location` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `stores`
--

INSERT INTO `stores` (`store_id`, `store_name`, `store_location`) VALUES
(1, 'Downtown Main Store', '123 Main Street, Downtown, City'),
(5, 'tech Store', 'downtown Hall'),
(6, 'T. Prem Kumar Reddy', 'chennai');

-- --------------------------------------------------------

--
-- Table structure for table `suppliers`
--

CREATE TABLE `suppliers` (
  `supplier_id` int(11) NOT NULL,
  `store_id` int(11) DEFAULT NULL,
  `supplier_name` varchar(255) NOT NULL,
  `contact_person` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` text DEFAULT NULL,
  `payment_terms` varchar(100) DEFAULT 'Net 30',
  `status` enum('active','inactive') DEFAULT 'active',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `suppliers`
--

INSERT INTO `suppliers` (`supplier_id`, `store_id`, `supplier_name`, `contact_person`, `email`, `phone`, `address`, `payment_terms`, `status`, `created_at`, `updated_at`) VALUES
(1, 1, 'TechWorld Distributors', 'John Smith', 'john@techworld.com', '555-0101', '456 Tech Avenue, Tech City', 'Net 30', 'active', '2025-06-01 14:50:39', '2025-06-01 14:50:39'),
(2, 1, 'Fresh Foods Co.', 'Mary Johnson', 'mary@freshfoods.com', '555-0102', '789 Fresh Street, Farm Town', 'Net 15', 'active', '2025-06-01 14:50:39', '2025-06-01 14:50:39'),
(3, 1, 'Fashion Forward Inc.', 'David Brown', 'david@fashionforward.com', '555-0103', '321 Style Boulevard, Fashion District', 'Net 30', 'active', '2025-06-01 14:50:39', '2025-06-01 14:50:39'),
(4, 1, 'Global Electronics', 'Sarah Wilson', 'sarah@globalelec.com', '555-0104', '654 Circuit Road, Electronics Hub', 'Net 45', 'active', '2025-06-01 14:50:39', '2025-06-01 14:50:39'),
(5, 1, 'Home Essentials Ltd.', 'Mike Davis', 'mike@homeessentials.com', '555-0105', '987 Home Lane, Suburban Area', 'Net 30', 'active', '2025-06-01 14:50:39', '2025-06-01 14:50:39'),
(6, 1, 'TechSupply Co.', 'Robert Johnson', 'robert@techsupply.com', '555-1001', '1000 Tech Industrial Park', 'Net 30', 'active', '2025-06-09 05:17:33', '2025-06-09 05:17:33'),
(7, 1, 'ElectroWholesale', 'Maria Garcia', 'maria@electrowholesale.com', '555-1002', '2000 Electronics District', 'Net 45', 'active', '2025-06-09 05:17:33', '2025-06-09 05:17:33'),
(8, 1, 'MobileGear Inc.', 'Kevin Wang', 'kevin@mobilegear.com', '555-1003', '3000 Mobile Avenue', 'Net 15', 'active', '2025-06-09 05:17:33', '2025-06-09 05:17:33'),
(9, 1, 'AudioPro Distributors', 'Jennifer Davis', 'jennifer@audiopro.com', '555-1004', '4000 Sound Boulevard', 'Net 30', 'active', '2025-06-09 05:17:33', '2025-06-09 05:17:33'),
(10, 1, 'GameZone Supply', 'Michael Chen', 'michael@gamezonesupply.com', '555-1005', '5000 Gaming Street', 'Net 60', 'active', '2025-06-09 05:17:33', '2025-06-09 05:17:33'),
(11, 1, 'SmartHome Solutions', 'Ashley Taylor', 'ashley@smarthomesol.com', '555-1006', '6000 IoT Plaza', 'Net 30', 'active', '2025-06-09 05:17:33', '2025-06-09 05:17:33'),
(12, 1, 'CableConnect Ltd.', 'James Wilson', 'james@cableconnect.com', '555-1007', '7000 Cable Way', 'Net 15', 'active', '2025-06-09 05:17:33', '2025-06-09 05:17:33'),
(13, 1, 'PowerPlus Accessories', 'Linda Brown', 'linda@powerplusacc.com', '555-1008', '8000 Power Drive', 'Net 30', 'active', '2025-06-09 05:17:33', '2025-06-09 05:17:33'),
(14, 1, 'TechCases Pro', 'Daniel Lee', 'daniel@techcasespro.com', '555-1009', '9000 Protection Lane', 'Net 45', 'active', '2025-06-09 05:17:33', '2025-06-09 05:17:33'),
(15, 1, 'GlobalTech Imports', 'Susan Martinez', 'susan@globaltechimports.com', '555-1010', '10000 Import Street', 'Net 30', 'active', '2025-06-09 05:17:33', '2025-06-09 05:17:33');

-- --------------------------------------------------------

--
-- Table structure for table `user_login`
--

CREATE TABLE `user_login` (
  `id` int(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `Password` varchar(255) NOT NULL,
  `store_id` int(255) NOT NULL,
  `role` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user_login`
--

INSERT INTO `user_login` (`id`, `username`, `Password`, `store_id`, `role`) VALUES
(1, 'manager1', 'manager12', 1, 'admin'),
(2, 'manager12', '123456789', 1, 'manager'),
(4, 'cashier2', 'hashed_password_321', 1, 'cashier'),
(8, 'inventory1', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 1, 'Inventory Manager'),
(11, 'testuser123', 'password123', 5, 'admin'),
(12, 'sid', 'siddu@123', 6, 'Admin');

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_current_stock`
-- (See below for the actual view)
--
CREATE TABLE `v_current_stock` (
`product_id` int(255)
,`product_name` varchar(255)
,`category` varchar(255)
,`sku` varchar(255)
,`store_name` text
,`current_stock` decimal(65,0)
,`min_stock_level` int(11)
,`reorder_quantity` int(11)
,`stock_status` varchar(9)
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_profit_loss`
-- (See below for the actual view)
--
CREATE TABLE `v_profit_loss` (
`store_id` int(11)
,`store_name` text
,`total_revenue` decimal(34,2)
,`total_cost` decimal(34,2)
,`total_losses` decimal(32,2)
,`net_profit` decimal(36,2)
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_sales_summary`
-- (See below for the actual view)
--
CREATE TABLE `v_sales_summary` (
`sale_id` int(11)
,`invoice_number` varchar(50)
,`sale_date` timestamp
,`final_amount` decimal(12,2)
,`customer_name` varchar(255)
,`store_name` text
,`served_by_name` varchar(255)
,`total_items` bigint(21)
);

-- --------------------------------------------------------

--
-- Structure for view `v_current_stock`
--
DROP TABLE IF EXISTS `v_current_stock`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_current_stock`  AS SELECT `p`.`id` AS `product_id`, `p`.`product_name` AS `product_name`, `p`.`category` AS `category`, `p`.`sku` AS `sku`, `s`.`store_name` AS `store_name`, coalesce(sum(case when `sm`.`movement_type` in ('in','return') then `sm`.`quantity` else -`sm`.`quantity` end),`p`.`quantity`) AS `current_stock`, `ia`.`min_stock_level` AS `min_stock_level`, `ia`.`reorder_quantity` AS `reorder_quantity`, CASE WHEN coalesce(sum(case when `sm`.`movement_type` in ('in','return') then `sm`.`quantity` else -`sm`.`quantity` end),`p`.`quantity`) <= `ia`.`min_stock_level` THEN 'Low Stock' ELSE 'Normal' END AS `stock_status` FROM (((`products` `p` left join `stores` `s` on(`p`.`store_id` = `s`.`store_id`)) left join `stock_movements` `sm` on(`p`.`id` = `sm`.`product_id`)) left join `inventory_alerts` `ia` on(`p`.`id` = `ia`.`product_id` and `p`.`store_id` = `ia`.`store_id`)) GROUP BY `p`.`id`, `p`.`product_name`, `p`.`category`, `p`.`sku`, `s`.`store_name`, `ia`.`min_stock_level`, `ia`.`reorder_quantity` ;

-- --------------------------------------------------------

--
-- Structure for view `v_profit_loss`
--
DROP TABLE IF EXISTS `v_profit_loss`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_profit_loss`  AS SELECT `s`.`store_id` AS `store_id`, `s`.`store_name` AS `store_name`, coalesce(sum(`si`.`total_price`),0) AS `total_revenue`, coalesce(sum(`poi`.`total_price`),0) AS `total_cost`, coalesce(sum(`ri`.`refund_amount`),0) AS `total_losses`, coalesce(sum(`si`.`total_price`),0) - coalesce(sum(`poi`.`total_price`),0) - coalesce(sum(`ri`.`refund_amount`),0) AS `net_profit` FROM (((`stores` `s` left join `sale_items` `si` on(`s`.`store_id` = `si`.`store_id`)) left join `purchase_order_items` `poi` on(`s`.`store_id` = `poi`.`store_id`)) left join `return_items` `ri` on(`s`.`store_id` = `ri`.`store_id`)) GROUP BY `s`.`store_id`, `s`.`store_name` ;

-- --------------------------------------------------------

--
-- Structure for view `v_sales_summary`
--
DROP TABLE IF EXISTS `v_sales_summary`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_sales_summary`  AS SELECT `s`.`sale_id` AS `sale_id`, `s`.`invoice_number` AS `invoice_number`, `s`.`sale_date` AS `sale_date`, `s`.`final_amount` AS `final_amount`, `c`.`customer_name` AS `customer_name`, `st`.`store_name` AS `store_name`, `staff`.`full_name` AS `served_by_name`, count(`si`.`sale_item_id`) AS `total_items` FROM ((((`sales` `s` left join `customers` `c` on(`s`.`customer_id` = `c`.`customer_id`)) left join `stores` `st` on(`s`.`store_id` = `st`.`store_id`)) left join `staff_details` `staff` on(`s`.`served_by` = `staff`.`staff_id`)) left join `sale_items` `si` on(`s`.`sale_id` = `si`.`sale_id`)) GROUP BY `s`.`sale_id`, `s`.`invoice_number`, `s`.`sale_date`, `s`.`final_amount`, `c`.`customer_name`, `st`.`store_name`, `staff`.`full_name` ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `audit_log`
--
ALTER TABLE `audit_log`
  ADD PRIMARY KEY (`log_id`),
  ADD KEY `idx_table_record` (`table_name`,`record_id`),
  ADD KEY `idx_changed_by` (`changed_by`),
  ADD KEY `idx_timestamp` (`timestamp`),
  ADD KEY `idx_action` (`action`),
  ADD KEY `idx_audit_log_store_id` (`store_id`);

--
-- Indexes for table `batch_details`
--
ALTER TABLE `batch_details`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_store_id` (`store_id`),
  ADD KEY `idx_product_id` (`product_id`),
  ADD KEY `idx_batch_id` (`batch_id`),
  ADD KEY `idx_batch_exp_date` (`exp_date`);

--
-- Indexes for table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`category_id`),
  ADD KEY `idx_parent_category` (`parent_category_id`),
  ADD KEY `idx_categories_store_id` (`store_id`),
  ADD KEY `idx_categories_store_parent` (`store_id`,`parent_category_id`);

--
-- Indexes for table `customers`
--
ALTER TABLE `customers`
  ADD PRIMARY KEY (`customer_id`),
  ADD UNIQUE KEY `idx_email_unique` (`email`),
  ADD KEY `idx_customer_name` (`customer_name`),
  ADD KEY `idx_email` (`email`),
  ADD KEY `idx_phone` (`phone`),
  ADD KEY `idx_consumers_store_id` (`store_id`),
  ADD KEY `idx_customers_store_status` (`store_id`,`status`);

--
-- Indexes for table `damages`
--
ALTER TABLE `damages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `batch_id` (`batch_id`),
  ADD KEY `product_id` (`product_id`),
  ADD KEY `damaged_by` (`damaged_by`),
  ADD KEY `idx_damage_store` (`store_id`);

--
-- Indexes for table `inventory_alerts`
--
ALTER TABLE `inventory_alerts`
  ADD PRIMARY KEY (`alert_id`),
  ADD UNIQUE KEY `idx_product_store` (`product_id`,`store_id`),
  ADD KEY `idx_store_id` (`store_id`);

--
-- Indexes for table `invoices`
--
ALTER TABLE `invoices`
  ADD PRIMARY KEY (`invoice_id`),
  ADD UNIQUE KEY `invoice_number` (`invoice_number`),
  ADD KEY `fk_store_id` (`store_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_store_id` (`store_id`),
  ADD KEY `idx_type` (`type`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `batch_id` (`batch_id`);

--
-- Indexes for table `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_category` (`category`),
  ADD KEY `idx_sku` (`sku`),
  ADD KEY `idx_product_category` (`category`),
  ADD KEY `idx_products_store_category` (`store_id`,`category`);

--
-- Indexes for table `product_suppliers`
--
ALTER TABLE `product_suppliers`
  ADD PRIMARY KEY (`ps_id`),
  ADD UNIQUE KEY `idx_product_supplier` (`product_id`,`supplier_id`),
  ADD KEY `idx_supplier_id` (`supplier_id`),
  ADD KEY `idx_product_suppliers_store_id` (`store_id`);

--
-- Indexes for table `purchase_orders`
--
ALTER TABLE `purchase_orders`
  ADD PRIMARY KEY (`po_id`),
  ADD UNIQUE KEY `po_number` (`po_number`),
  ADD KEY `idx_po_number` (`po_number`),
  ADD KEY `idx_supplier_id` (`supplier_id`),
  ADD KEY `idx_store_id` (`store_id`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_order_date` (`order_date`),
  ADD KEY `created_by` (`created_by`),
  ADD KEY `idx_purchase_orders_store_date` (`store_id`,`order_date`);

--
-- Indexes for table `purchase_order_items`
--
ALTER TABLE `purchase_order_items`
  ADD PRIMARY KEY (`poi_id`),
  ADD KEY `idx_po_id` (`po_id`),
  ADD KEY `idx_product_id` (`product_id`),
  ADD KEY `idx_purchase_order_items_store_id` (`store_id`);

--
-- Indexes for table `reports`
--
ALTER TABLE `reports`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_report_store` (`store_id`),
  ADD KEY `batch_id` (`batch_id`);

--
-- Indexes for table `returns`
--
ALTER TABLE `returns`
  ADD PRIMARY KEY (`return_id`),
  ADD UNIQUE KEY `return_number` (`return_number`),
  ADD KEY `idx_return_number` (`return_number`),
  ADD KEY `idx_sale_id` (`sale_id`),
  ADD KEY `idx_store_id` (`store_id`),
  ADD KEY `idx_return_date` (`return_date`),
  ADD KEY `customer_id` (`customer_id`),
  ADD KEY `processed_by` (`processed_by`);

--
-- Indexes for table `return_items`
--
ALTER TABLE `return_items`
  ADD PRIMARY KEY (`return_item_id`),
  ADD KEY `idx_return_id` (`return_id`),
  ADD KEY `idx_product_id` (`product_id`),
  ADD KEY `idx_return_items_store_id` (`store_id`);

--
-- Indexes for table `sales`
--
ALTER TABLE `sales`
  ADD PRIMARY KEY (`sale_id`),
  ADD UNIQUE KEY `invoice_number` (`invoice_number`),
  ADD KEY `idx_invoice_number` (`invoice_number`),
  ADD KEY `idx_customer_id` (`customer_id`),
  ADD KEY `idx_store_id` (`store_id`),
  ADD KEY `idx_sale_date` (`sale_date`),
  ADD KEY `idx_served_by` (`served_by`),
  ADD KEY `idx_sale_date_store` (`sale_date`,`store_id`),
  ADD KEY `idx_sales_store_date` (`store_id`,`sale_date`);

--
-- Indexes for table `sale_items`
--
ALTER TABLE `sale_items`
  ADD PRIMARY KEY (`sale_item_id`),
  ADD KEY `idx_sale_id` (`sale_id`),
  ADD KEY `idx_product_id` (`product_id`),
  ADD KEY `idx_sales_items_store_id` (`store_id`);

--
-- Indexes for table `shipments`
--
ALTER TABLE `shipments`
  ADD PRIMARY KEY (`shipment_id`),
  ADD UNIQUE KEY `unique_shipment_number` (`shipment_number`),
  ADD KEY `idx_order_type_id` (`order_type`,`order_id`),
  ADD KEY `idx_store_id` (`store_id`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_ship_date` (`ship_date`),
  ADD KEY `idx_tracking_number` (`tracking_number`);

--
-- Indexes for table `shipment_items`
--
ALTER TABLE `shipment_items`
  ADD PRIMARY KEY (`shipment_item_id`),
  ADD KEY `idx_shipment_id` (`shipment_id`),
  ADD KEY `idx_product_id` (`product_id`),
  ADD KEY `idx_shipments_items_store_id` (`store_id`);

--
-- Indexes for table `staff_details`
--
ALTER TABLE `staff_details`
  ADD PRIMARY KEY (`staff_id`),
  ADD KEY `fk_staff_user` (`user_id`),
  ADD KEY `fk_sales_details_store` (`store_id`);

--
-- Indexes for table `stock_movements`
--
ALTER TABLE `stock_movements`
  ADD PRIMARY KEY (`movement_id`),
  ADD KEY `idx_product_id` (`product_id`),
  ADD KEY `idx_store_id` (`store_id`),
  ADD KEY `idx_movement_type` (`movement_type`),
  ADD KEY `idx_timestamp` (`timestamp`),
  ADD KEY `idx_reference` (`reference_type`,`reference_id`),
  ADD KEY `performed_by` (`performed_by`),
  ADD KEY `idx_stock_movement_date` (`timestamp`);

--
-- Indexes for table `stores`
--
ALTER TABLE `stores`
  ADD PRIMARY KEY (`store_id`),
  ADD UNIQUE KEY `idx_store_name_location` (`store_name`,`store_location`) USING HASH;

--
-- Indexes for table `suppliers`
--
ALTER TABLE `suppliers`
  ADD PRIMARY KEY (`supplier_id`),
  ADD KEY `idx_supplier_name` (`supplier_name`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_suppliers_store_id` (`store_id`);

--
-- Indexes for table `user_login`
--
ALTER TABLE `user_login`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_user_store` (`store_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `audit_log`
--
ALTER TABLE `audit_log`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `batch_details`
--
ALTER TABLE `batch_details`
  MODIFY `id` int(255) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
  MODIFY `category_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=38;

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `customer_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `damages`
--
ALTER TABLE `damages`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `inventory_alerts`
--
ALTER TABLE `inventory_alerts`
  MODIFY `alert_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `invoices`
--
ALTER TABLE `invoices`
  MODIFY `invoice_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` int(255) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `products`
--
ALTER TABLE `products`
  MODIFY `id` int(255) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `product_suppliers`
--
ALTER TABLE `product_suppliers`
  MODIFY `ps_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `purchase_orders`
--
ALTER TABLE `purchase_orders`
  MODIFY `po_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `purchase_order_items`
--
ALTER TABLE `purchase_order_items`
  MODIFY `poi_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `reports`
--
ALTER TABLE `reports`
  MODIFY `id` int(255) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `returns`
--
ALTER TABLE `returns`
  MODIFY `return_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `return_items`
--
ALTER TABLE `return_items`
  MODIFY `return_item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `sales`
--
ALTER TABLE `sales`
  MODIFY `sale_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `sale_items`
--
ALTER TABLE `sale_items`
  MODIFY `sale_item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `shipments`
--
ALTER TABLE `shipments`
  MODIFY `shipment_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `shipment_items`
--
ALTER TABLE `shipment_items`
  MODIFY `shipment_item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `staff_details`
--
ALTER TABLE `staff_details`
  MODIFY `staff_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `stock_movements`
--
ALTER TABLE `stock_movements`
  MODIFY `movement_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `stores`
--
ALTER TABLE `stores`
  MODIFY `store_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `suppliers`
--
ALTER TABLE `suppliers`
  MODIFY `supplier_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `user_login`
--
ALTER TABLE `user_login`
  MODIFY `id` int(255) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `audit_log`
--
ALTER TABLE `audit_log`
  ADD CONSTRAINT `audit_log_ibfk_1` FOREIGN KEY (`changed_by`) REFERENCES `staff_details` (`staff_id`),
  ADD CONSTRAINT `fk_audit_log_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `batch_details`
--
ALTER TABLE `batch_details`
  ADD CONSTRAINT `batch_details_ibfk_1` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `batch_details_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_batch_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  ADD CONSTRAINT `fk_batch_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`);

--
-- Constraints for table `categories`
--
ALTER TABLE `categories`
  ADD CONSTRAINT `categories_ibfk_1` FOREIGN KEY (`parent_category_id`) REFERENCES `categories` (`category_id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_categories_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON UPDATE CASCADE;

--
-- Constraints for table `customers`
--
ALTER TABLE `customers`
  ADD CONSTRAINT `fk_consumers_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON UPDATE CASCADE;

--
-- Constraints for table `damages`
--
ALTER TABLE `damages`
  ADD CONSTRAINT `damages_ibfk_1` FOREIGN KEY (`batch_id`) REFERENCES `batch_details` (`batch_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `damages_ibfk_2` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `damages_ibfk_3` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `damages_ibfk_4` FOREIGN KEY (`damaged_by`) REFERENCES `staff_details` (`staff_id`) ON DELETE CASCADE;

--
-- Constraints for table `inventory_alerts`
--
ALTER TABLE `inventory_alerts`
  ADD CONSTRAINT `inventory_alerts_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `inventory_alerts_ibfk_2` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON DELETE CASCADE;

--
-- Constraints for table `invoices`
--
ALTER TABLE `invoices`
  ADD CONSTRAINT `fk_store_id` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON DELETE CASCADE;

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `fk_notification_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `notifications_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user_login` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `notifications_ibfk_3` FOREIGN KEY (`batch_id`) REFERENCES `batch_details` (`batch_id`) ON DELETE SET NULL;

--
-- Constraints for table `products`
--
ALTER TABLE `products`
  ADD CONSTRAINT `fk_product_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`),
  ADD CONSTRAINT `products_ibfk_1` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `product_suppliers`
--
ALTER TABLE `product_suppliers`
  ADD CONSTRAINT `fk_product_suppliers_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `product_suppliers_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `product_suppliers_ibfk_2` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`supplier_id`) ON DELETE CASCADE;

--
-- Constraints for table `purchase_orders`
--
ALTER TABLE `purchase_orders`
  ADD CONSTRAINT `purchase_orders_ibfk_1` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`supplier_id`),
  ADD CONSTRAINT `purchase_orders_ibfk_2` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`),
  ADD CONSTRAINT `purchase_orders_ibfk_3` FOREIGN KEY (`created_by`) REFERENCES `staff_details` (`staff_id`);

--
-- Constraints for table `purchase_order_items`
--
ALTER TABLE `purchase_order_items`
  ADD CONSTRAINT `fk_purchase_order_items_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `purchase_order_items_ibfk_1` FOREIGN KEY (`po_id`) REFERENCES `purchase_orders` (`po_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `purchase_order_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);

--
-- Constraints for table `reports`
--
ALTER TABLE `reports`
  ADD CONSTRAINT `fk_report_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`),
  ADD CONSTRAINT `reports_ibfk_1` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `reports_ibfk_2` FOREIGN KEY (`batch_id`) REFERENCES `batch_details` (`batch_id`) ON DELETE SET NULL;

--
-- Constraints for table `returns`
--
ALTER TABLE `returns`
  ADD CONSTRAINT `returns_ibfk_1` FOREIGN KEY (`sale_id`) REFERENCES `sales` (`sale_id`),
  ADD CONSTRAINT `returns_ibfk_2` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`) ON DELETE SET NULL,
  ADD CONSTRAINT `returns_ibfk_3` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`),
  ADD CONSTRAINT `returns_ibfk_4` FOREIGN KEY (`processed_by`) REFERENCES `staff_details` (`staff_id`);

--
-- Constraints for table `return_items`
--
ALTER TABLE `return_items`
  ADD CONSTRAINT `fk_return_items_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `return_items_ibfk_1` FOREIGN KEY (`return_id`) REFERENCES `returns` (`return_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `return_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);

--
-- Constraints for table `sales`
--
ALTER TABLE `sales`
  ADD CONSTRAINT `sales_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`) ON DELETE SET NULL,
  ADD CONSTRAINT `sales_ibfk_2` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`),
  ADD CONSTRAINT `sales_ibfk_3` FOREIGN KEY (`served_by`) REFERENCES `staff_details` (`staff_id`);

--
-- Constraints for table `sale_items`
--
ALTER TABLE `sale_items`
  ADD CONSTRAINT `fk_sales_items_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `sale_items_ibfk_1` FOREIGN KEY (`sale_id`) REFERENCES `sales` (`sale_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `sale_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);

--
-- Constraints for table `shipment_items`
--
ALTER TABLE `shipment_items`
  ADD CONSTRAINT `fk_shipments_items_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `shipment_items_ibfk_1` FOREIGN KEY (`shipment_id`) REFERENCES `shipments` (`shipment_id`) ON DELETE CASCADE;

--
-- Constraints for table `staff_details`
--
ALTER TABLE `staff_details`
  ADD CONSTRAINT `fk_sales_details_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_staff_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`),
  ADD CONSTRAINT `fk_staff_user` FOREIGN KEY (`user_id`) REFERENCES `user_login` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `staff_details_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_login` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `staff_details_ibfk_2` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `stock_movements`
--
ALTER TABLE `stock_movements`
  ADD CONSTRAINT `stock_movements_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  ADD CONSTRAINT `stock_movements_ibfk_2` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`),
  ADD CONSTRAINT `stock_movements_ibfk_3` FOREIGN KEY (`performed_by`) REFERENCES `staff_details` (`staff_id`);

--
-- Constraints for table `suppliers`
--
ALTER TABLE `suppliers`
  ADD CONSTRAINT `fk_suppliers_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `user_login`
--
ALTER TABLE `user_login`
  ADD CONSTRAINT `fk_user_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`),
  ADD CONSTRAINT `user_login_ibfk_1` FOREIGN KEY (`store_id`) REFERENCES `stores` (`store_id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
