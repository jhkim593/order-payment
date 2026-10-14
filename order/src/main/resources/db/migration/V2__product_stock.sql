DROP TABLE credit_product;

ALTER TABLE product DROP COLUMN product_type;
ALTER TABLE product ADD COLUMN stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0);
ALTER TABLE product ALTER COLUMN stock DROP DEFAULT;

-- Test data
INSERT INTO product (name, description, price, stock, created_at, updated_at)
VALUES ('키보드', '기계식 키보드', 10000, 1000, NOW(), NOW()),
       ('마우스', '무선 마우스', 5000, 1000, NOW(), NOW()),
       ('모니터', '27인치 모니터', 30000, 1000, NOW(), NOW());