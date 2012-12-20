DROP TABLE IF EXISTS inventory;
-- remove table if it already exists, and start from scratch

CREATE TABLE inventory (
	inv_id INT NOT NULL PRIMARY KEY,
	price Float(2) NOT NULL
);