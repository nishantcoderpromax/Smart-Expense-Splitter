ALTER TABLE expenses ADD COLUMN category_id BIGINT REFERENCES categories(id);
