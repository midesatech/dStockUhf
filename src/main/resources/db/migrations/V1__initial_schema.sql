-- Clean CREATE schema (MySQL 8+)
DROP TRIGGER IF EXISTS biu_people_tag_guard;
DROP TRIGGER IF EXISTS bu_people_tag_guard;
DROP TRIGGER IF EXISTS biu_product_tag_guard;
DROP TRIGGER IF EXISTS bu_product_tag_guard;

DROP TABLE IF EXISTS people;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS uhf_tag;
DROP TABLE IF EXISTS location;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles_permissions;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS uhf_detection;
DROP TABLE IF EXISTS uhf_reader;

CREATE TABLE roles (id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE permissions (id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(80) NOT NULL UNIQUE);
CREATE TABLE roles_permissions (role_id BIGINT NOT NULL, permission_id BIGINT NOT NULL, PRIMARY KEY (role_id, permission_id));
CREATE TABLE users (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 username VARCHAR(60) NOT NULL UNIQUE,
 password VARCHAR(120) NOT NULL,
 system_user BOOLEAN NOT NULL DEFAULT 0);

CREATE TABLE user_roles (user_id BIGINT NOT NULL, role_id BIGINT NOT NULL, PRIMARY KEY (user_id, role_id));

CREATE TABLE category (id BIGINT PRIMARY KEY AUTO_INCREMENT, nombre VARCHAR(255) NOT NULL);

CREATE TABLE location (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 nombre VARCHAR(150) NOT NULL UNIQUE,
 parent_id BIGINT NULL,
 CONSTRAINT fk_location_parent
     FOREIGN KEY (parent_id) REFERENCES location(id)
     ON DELETE SET NULL
);

CREATE UNIQUE INDEX uq_location_parent_nombre
  ON location (parent_id, nombre);

CREATE TABLE people_type (id bigint(20) NOT NULL AUTO_INCREMENT, nombre varchar(60) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_nombre_pt UNIQUE KEY (nombre)
);

CREATE TABLE uhf_reader (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  codigo VARCHAR(64) NOT NULL UNIQUE,
  descripcion VARCHAR(255),
  ubicacion_id BIGINT NOT NULL,
  CONSTRAINT fk_readers_location
    FOREIGN KEY (ubicacion_id) REFERENCES location(id) ON DELETE RESTRICT
);

CREATE TABLE uhf_tag (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  epc VARCHAR(64) NOT NULL,
  tipo VARCHAR(32) NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT 1,
  CONSTRAINT uk_taguhf_epc UNIQUE KEY (epc)
);

CREATE TABLE people (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name  VARCHAR(150) NOT NULL,
  last_name  VARCHAR(150) NOT NULL,
  doc_type   VARCHAR(20)  NOT NULL,
  doc_number VARCHAR(30)  NOT NULL,
  birth_date DATE         NOT NULL,
  blood_type VARCHAR(6)   NOT NULL,
  email      VARCHAR(120),
  phone      VARCHAR(25),
  tag_id     BIGINT NULL,
  people_type_id  bigint(20) NOT NULL,
  CONSTRAINT uk_people_doc UNIQUE KEY (doc_type, doc_number),
  CONSTRAINT uk_people_tag UNIQUE KEY (tag_id),
  CONSTRAINT fk_people_type_id FOREIGN KEY (people_type_id) REFERENCES people_type (id),
  CONSTRAINT fk_people_tag FOREIGN KEY (tag_id) REFERENCES uhf_tag(id) ON DELETE SET NULL ON UPDATE CASCADE
);

-- Índices para búsquedas frecuentes
CREATE INDEX idx_people_last_name ON people (last_name);
CREATE INDEX idx_people_doc_type  ON people (doc_type);


CREATE TABLE product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  nombre  VARCHAR(150),
  sku     VARCHAR(64),
  categoria_id bigint(20) DEFAULT NULL,
  tag_id  BIGINT NULL,
  ubicacion_id bigint(20) DEFAULT NULL,
  CONSTRAINT uk_product_tag UNIQUE KEY (tag_id),
  CONSTRAINT fk_categoria_id FOREIGN KEY (categoria_id) REFERENCES category (id),
  CONSTRAINT fk_ubicacion_id FOREIGN KEY (ubicacion_id) REFERENCES location (id),
  CONSTRAINT fk_product_tag FOREIGN KEY (tag_id) REFERENCES uhf_tag(id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE uhf_detection (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  lector_id     BIGINT NOT NULL,
  ubicacion_id  BIGINT NULL,
  epc           VARCHAR(64) NOT NULL,
  rssi          INT NULL,
  machine       VARCHAR(100) NULL,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_detection_reader
    FOREIGN KEY (lector_id) REFERENCES uhf_reader(id) ON DELETE RESTRICT,
  CONSTRAINT fk_detection_location
    FOREIGN KEY (ubicacion_id) REFERENCES location(id) ON DELETE SET NULL
);

CREATE INDEX idx_detection_epc              ON uhf_detection (epc);
CREATE INDEX idx_detection_created          ON uhf_detection (created_at);
CREATE INDEX idx_detection_reader_created   ON uhf_detection (lector_id, created_at);
CREATE INDEX idx_detection_epc_created      ON uhf_detection (epc, created_at);
CREATE INDEX idx_detection_location_created ON uhf_detection (ubicacion_id, created_at);

DELIMITER $$

CREATE TRIGGER biu_people_tag_guard
BEFORE INSERT ON people
FOR EACH ROW
BEGIN
  IF NEW.tag_id IS NOT NULL AND EXISTS (SELECT 1 FROM product WHERE tag_id = NEW.tag_id) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tag already assigned to an product';
  END IF;
END$$

CREATE TRIGGER bu_people_tag_guard
BEFORE UPDATE ON people
FOR EACH ROW
BEGIN
  IF NEW.tag_id IS NOT NULL AND NEW.tag_id <> OLD.tag_id
     AND EXISTS (SELECT 1 FROM product WHERE tag_id = NEW.tag_id) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tag already assigned to an product';
  END IF;
END$$

CREATE TRIGGER biu_product_tag_guard
BEFORE INSERT ON product
FOR EACH ROW
BEGIN
  IF NEW.tag_id IS NOT NULL AND EXISTS (SELECT 1 FROM people WHERE tag_id = NEW.tag_id) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tag already assigned to an empleado';
  END IF;
END$$

CREATE TRIGGER bu_product_tag_guard
BEFORE UPDATE ON product
FOR EACH ROW
BEGIN
  IF NEW.tag_id IS NOT NULL AND NEW.tag_id <> OLD.tag_id
     AND EXISTS (SELECT 1 FROM people WHERE tag_id = NEW.tag_id) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tag already assigned to an empleado';
  END IF;
END$$

DELIMITER ;