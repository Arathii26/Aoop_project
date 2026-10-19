CREATE DATABASE IF NOT EXISTS lost_found_db;
USE lost_found_db;

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    role ENUM('STUDENT', 'ADMIN') NOT NULL,
    reg_no VARCHAR(50) NULL,
    staff_id VARCHAR(50) NULL,
    INDEX idx_user_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS items (
    id INT PRIMARY KEY,
    type ENUM('LOST', 'FOUND') NOT NULL,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    category ENUM('ID_CARD', 'CALCULATOR', 'UMBRELLA', 'CHARGER', 'LAB_EQUIPMENT', 'OTHER') NOT NULL,
    location VARCHAR(150) NOT NULL,
    date_time DATETIME NOT NULL,
    stored_at DATETIME NOT NULL,
    reporter_id INT NOT NULL,
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_item_reporter FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_items_type (type),
    INDEX idx_items_category (category),
    INDEX idx_items_location (location)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS claims (
    id INT PRIMARY KEY,
    lost_id INT NOT NULL,
    found_id INT NOT NULL,
    claimant_id INT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    remarks VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_claim_lost FOREIGN KEY (lost_id) REFERENCES items(id) ON DELETE RESTRICT,
    CONSTRAINT fk_claim_found FOREIGN KEY (found_id) REFERENCES items(id) ON DELETE RESTRICT,
    CONSTRAINT fk_claim_claimant FOREIGN KEY (claimant_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_claim_status (status)
) ENGINE=InnoDB;
