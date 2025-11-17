CREATE TABLE people_type (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  nombre VARCHAR(60) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE people
  ADD COLUMN people_id BIGINT NULL,
  ADD CONSTRAINT fk_people_type
    FOREIGN KEY (people_id) REFERENCES people_type(id)
    ON DELETE SET NULL;


INSERT INTO people_type (nombre) VALUES ('Employee'), ('Tourist'), ('Guest');
