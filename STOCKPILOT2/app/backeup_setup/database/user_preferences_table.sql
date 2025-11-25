-- Create user_preferences table if it doesn't exist
CREATE TABLE IF NOT EXISTS `user_preferences` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `store_id` int(11) NOT NULL,
  `preference_name` varchar(100) NOT NULL,
  `preference_value` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `store_preference_unique` (`store_id`, `preference_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert default preferences for testing (optional)
-- INSERT INTO `user_preferences` (`store_id`, `preference_name`, `preference_value`) VALUES
-- (1, 'notifications_enabled', 'true'),
-- (1, 'low_stock_notifications_enabled', 'true'),
-- (1, 'expiry_notifications_enabled', 'true'),
-- (1, 'damaged_items_notifications_enabled', 'true'),
-- (1, 'low_stock_threshold', '10'),
-- (1, 'expiry_days_threshold', '7');