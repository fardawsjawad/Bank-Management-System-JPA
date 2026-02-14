CREATE DATABASE IF NOT EXISTS bank_management_system;
USE bank_management_system;

-- =========================
-- USERS
-- =========================
CREATE TABLE Users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) UNIQUE,
    password VARCHAR(200) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    email_address VARCHAR(100) UNIQUE NOT NULL,
    nationality VARCHAR(100) NOT NULL,
    passport_number VARCHAR(50) UNIQUE NOT NULL,
    phone_number VARCHAR(15) UNIQUE NOT NULL
);

-- =========================
-- ROLES
-- =========================
CREATE TABLE Roles (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    user_role ENUM('ADMIN', 'USER') NOT NULL,

    CONSTRAINT fk_user_role
        FOREIGN KEY (user_id)
        REFERENCES Users(user_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- =========================
-- ACCOUNT NUMBER SEQUENCE
-- =========================
CREATE TABLE AccountNumberSequence (
    next_val INT PRIMARY KEY AUTO_INCREMENT
);

-- =========================
-- ACCOUNTS
-- =========================
CREATE TABLE Accounts (
    account_id INT PRIMARY KEY AUTO_INCREMENT,
    account_number VARCHAR(100) UNIQUE,
    account_owner_id INT NOT NULL,
    account_type ENUM('SAVINGS', 'CURRENT') NOT NULL,
    account_balance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    bic_swift_code VARCHAR(20) NOT NULL,
    overdraft_limit DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    opening_date DATE NOT NULL,
    closing_date DATE,
    status ENUM('ACTIVE', 'FROZEN', 'SUSPENDED', 'DORMANT', 'CLOSED', 'DELETED')
        NOT NULL DEFAULT 'ACTIVE',
    status_reason VARCHAR(2000),

    CONSTRAINT fk_account_owner
        FOREIGN KEY (account_owner_id)
        REFERENCES Users(user_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- =========================
-- TRANSACTIONS
-- =========================
CREATE TABLE Transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    from_account_id INT,
    to_account_id INT,
    transaction_type ENUM('DEPOSIT', 'WITHDRAW', 'TRANSFER') NOT NULL,
    amount DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    available_balance_after DECIMAL(12,2) NOT NULL,
    transaction_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    transaction_status ENUM('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REVERSED') NOT NULL,

    CONSTRAINT fk_from_account
        FOREIGN KEY (from_account_id)
        REFERENCES Accounts(account_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_to_account
        FOREIGN KEY (to_account_id)
        REFERENCES Accounts(account_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- =========================
-- EMPLOYMENT PROFILE
-- =========================
CREATE TABLE EmploymentProfile (
    employment_profile_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    occupation VARCHAR(255) NOT NULL,
    annual_income DECIMAL(12,2) NOT NULL CHECK (annual_income >= 0),
    source_of_funds ENUM('SALARY', 'BUSINESS', 'INVESTMENT', 'OTHER') NOT NULL,
    account_purpose ENUM('PERSONAL', 'BUSINESS', 'SAVINGS', 'OTHER') NOT NULL,

    CONSTRAINT fk_user_employment
        FOREIGN KEY (user_id)
        REFERENCES Users(user_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    UNIQUE (user_id)
);

-- =========================
-- USER ADDRESS
-- =========================
CREATE TABLE UserAddress (
    address_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    address_type ENUM('RESIDENTIAL', 'MAILING', 'OFFICE', 'PERMANENT', 'TEMPORARY') NOT NULL,
    address_line_1 VARCHAR(255) NOT NULL,
    address_line_2 VARCHAR(255),
    locality VARCHAR(255),
    city VARCHAR(255) NOT NULL,
    district VARCHAR(255),
    state_or_province VARCHAR(255) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(50) NOT NULL,

    CONSTRAINT fk_user_address
        FOREIGN KEY (user_id)
        REFERENCES Users(user_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE INDEX idx_user_address_user_id ON UserAddress(user_id);

-- =========================
-- TRIGGERS
-- =========================
DELIMITER $$

CREATE TRIGGER trg_generate_username
BEFORE INSERT ON Users
FOR EACH ROW
BEGIN
    IF NEW.username IS NULL OR NEW.username = '' THEN
        SET NEW.username = CONCAT(
            'USER',
            LPAD(
                (SELECT AUTO_INCREMENT
                 FROM information_schema.TABLES
                 WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'Users'),
                6,
                '0'
            )
        );
    END IF;
END$$

CREATE TRIGGER before_insert_accounts
BEFORE INSERT ON Accounts
FOR EACH ROW
BEGIN
    INSERT INTO AccountNumberSequence VALUES (NULL);
    SET NEW.account_number =
        CONCAT('ACC', LPAD(LAST_INSERT_ID(), 6, '0'));
END$$

DELIMITER ;
