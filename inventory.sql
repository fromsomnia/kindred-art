DROP TABLE IF EXISTS inventory;
-- remove table if it already exists, and start from scratch

CREATE TABLE history (
	inv_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	price Float(2) NOT NULL
);