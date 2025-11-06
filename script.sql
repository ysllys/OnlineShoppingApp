-- -----------------------------------------------------
-- Schema online_shopping_db
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `online_shopping_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `online_shopping_db`;

-- -----------------------------------------------------
-- Table `user` (Parent Table 1)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `first_name` VARCHAR(50) NULL,
    `last_name` VARCHAR(50) NULL,
    `is_admin` BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (`id`)
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `product` (Parent Table 2)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `product` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `description` TEXT NULL,
    `retail_price` DECIMAL(10, 2) NOT NULL,
    `wholesale_price` DECIMAL(10, 2) NOT NULL,
    `quantity` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `order` (Child of user)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `order` (
                                       `id` INT NOT NULL AUTO_INCREMENT,
                                       `user_id` INT NOT NULL,
                                       `order_time` DATETIME NOT NULL,
                                       `status` ENUM('PROCESSING', 'SHIPPED', 'COMPLETED', 'CANCELED') NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `fk_order_user_idx` (`user_id` ASC) VISIBLE,
    CONSTRAINT `fk_order_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `order_item` (Child of order and product)
-- This is a many-to-many relationship table, capturing historical price.
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `order_item` (
                                            `id` INT NOT NULL AUTO_INCREMENT,
                                            `order_id` INT NOT NULL,
                                            `product_id` INT NOT NULL,
                                            `quantity` INT NOT NULL,
    -- Store prices at time of order for accurate historical reporting
                                            `retail_price` DECIMAL(10, 2) NOT NULL,
    `wholesale_price` DECIMAL(10, 2) NOT NULL,

    PRIMARY KEY (`id`),
    UNIQUE INDEX `uq_order_item` (`order_id`, `product_id`) VISIBLE,
    INDEX `fk_order_item_product_idx` (`product_id` ASC) VISIBLE,

    CONSTRAINT `fk_order_item_order`
    FOREIGN KEY (`order_id`)
    REFERENCES `order` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
    CONSTRAINT `fk_order_item_product`
    FOREIGN KEY (`product_id`)
    REFERENCES `product` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
    ) ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `watchlist` (Child of user and product - simple many-to-many)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `watchlist` (
                                           `user_id` INT NOT NULL,
                                           `product_id` INT NOT NULL,

    -- Composite primary key ensures a user can only watch a product once
                                           PRIMARY KEY (`user_id`, `product_id`),
    INDEX `fk_watchlist_product_idx` (`product_id` ASC) VISIBLE,

    CONSTRAINT `fk_watchlist_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE -- If user is deleted, remove their watchlist entries
    ON UPDATE NO ACTION,
    CONSTRAINT `fk_watchlist_product`
    FOREIGN KEY (`product_id`)
    REFERENCES `product` (`id`)
    ON DELETE CASCADE -- If product is deleted, remove it from all watchlists
    ON UPDATE NO ACTION
    ) ENGINE = InnoDB;