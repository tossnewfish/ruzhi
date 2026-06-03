CREATE TABLE IF NOT EXISTS demo_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_demo_order_order_no UNIQUE (order_no),
    INDEX idx_demo_order_status_created_at (status, created_at),
    INDEX idx_demo_order_product_name (product_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
