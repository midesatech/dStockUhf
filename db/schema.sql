-- schema.sql (MySQL/MariaDB)
CREATE TABLE IF NOT EXISTS roles (id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE IF NOT EXISTS permissions (id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(80) NOT NULL UNIQUE);
CREATE TABLE IF NOT EXISTS roles_permissions (role_id BIGINT NOT NULL, permission_id BIGINT NOT NULL, PRIMARY KEY (role_id, permission_id));
CREATE TABLE IF NOT EXISTS users (id BIGINT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(64) NOT NULL UNIQUE, password_hash VARCHAR(100) NOT NULL, system_user BOOLEAN NOT NULL DEFAULT 0);
CREATE TABLE IF NOT EXISTS user_roles (user_id BIGINT NOT NULL, role_id BIGINT NOT NULL, PRIMARY KEY (user_id, role_id));
CREATE TABLE IF NOT EXISTS categorias (id BIGINT PRIMARY KEY AUTO_INCREMENT, nombre VARCHAR(255) NOT NULL);

CREATE TABLE IF NOT EXISTS ubicaciones (id BIGINT PRIMARY KEY AUTO_INCREMENT, nombre VARCHAR(150) NOT NULL UNIQUE);

CREATE TABLE IF NOT EXISTS tags_uhf (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  epc VARCHAR(64) NOT NULL UNIQUE,
  tipo ENUM('EMPLEADO', 'EQUIPMENT') NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS empleados (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  codigo VARCHAR(64) UNIQUE,
  full_name VARCHAR(150) NOT NULL,
  last_name VARCHAR(150) NOT NULL,

  doc_type VARCHAR(5) NOT NULL,
  doc_number VARCHAR(30) NOT NULL UNIQUE,
  birth_date DATE NOT NULL,
  blood_type VARCHAR(6) NOT NULL,

  email VARCHAR(120),
  phone VARCHAR(25)
);
CREATE TABLE IF NOT EXISTS equipment (
id BIGINT PRIMARY KEY AUTO_INCREMENT,
sku VARCHAR(100) UNIQUE,
nombre VARCHAR(200) NOT NULL,
categoria_id BIGINT,
ubicacion_id BIGINT,
FOREIGN KEY (categoria_id) REFERENCES categorias(id),
FOREIGN KEY (ubicacion_id) REFERENCES ubicaciones(id));

ALTER TABLE empleados ADD COLUMN tag_id BIGINT UNIQUE;
ALTER TABLE empleados ADD CONSTRAINT fk_empleado_tag FOREIGN KEY (tag_id) REFERENCES tags_uhf(id);

ALTER TABLE equipment ADD COLUMN tag_id BIGINT UNIQUE;
ALTER TABLE equipment ADD CONSTRAINT fk_producto_tag FOREIGN KEY (tag_id) REFERENCES tags_uhf(id);

CREATE TABLE IF NOT EXISTS lectores_uhf (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  codigo VARCHAR(64) NOT NULL UNIQUE, -- Identificador único (se usará en JSON)
  descripcion VARCHAR(255),
  ubicacion_id BIGINT NOT NULL,
  FOREIGN KEY (ubicacion_id) REFERENCES ubicaciones(id)
);

CREATE TABLE IF NOT EXISTS detecciones_tags (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  lector_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  intensidad INT, -- RSSI opcional
  estado ENUM('OK', 'FUERA_DE_LUGAR') DEFAULT 'OK',
  FOREIGN KEY (lector_id) REFERENCES lectores_uhf(id),
  FOREIGN KEY (tag_id) REFERENCES tags_uhf(id)
);

ALTER TABLE taguhf
  ADD CONSTRAINT uk_taguhf_epc UNIQUE KEY (epc);

ALTER TABLE empleados
  ADD CONSTRAINT fk_empleados_tag
    FOREIGN KEY (tag_id) REFERENCES taguhf(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  ADD CONSTRAINT uk_empleados_tag UNIQUE KEY (tag_id);

ALTER TABLE equipment
  ADD CONSTRAINT fk_equipment_tag
    FOREIGN KEY (tag_id) REFERENCES taguhf(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  ADD CONSTRAINT uk_equipment_tag UNIQUE KEY (tag_id);



CREATE TABLE tag_assignment (
  tag_id       BIGINT NOT NULL,
  empleado_id  BIGINT NULL,
  equipment_id BIGINT NULL,
  CONSTRAINT pk_tag_assignment PRIMARY KEY (tag_id),
  CONSTRAINT fk_ta_tag       FOREIGN KEY (tag_id) REFERENCES taguhf(id) ON DELETE CASCADE,
  CONSTRAINT fk_ta_empleado  FOREIGN KEY (empleado_id)  REFERENCES empleados(id)  ON DELETE SET NULL,
  CONSTRAINT fk_ta_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE SET NULL,
  -- exactly one of empleado_id or equipment_id must be non-null
  CONSTRAINT ck_ta_one_side CHECK (
    (empleado_id IS NOT NULL) <> (equipment_id IS NOT NULL)
  )
);

ALTER TABLE empleados
  DROP INDEX uk_empleados_codigo,        -- only if an anonymous unique exists; skip if already named
  ADD  CONSTRAINT uk_empleados_codigo     UNIQUE KEY (codigo),
  DROP INDEX doc_number,                  -- same note as above; skip if not present
  ADD  CONSTRAINT uk_empleados_doc_number UNIQUE KEY (doc_number);

-- Drop existing FK if present and recreate with ON DELETE SET NULL / ON UPDATE CASCADE
ALTER TABLE empleados
  DROP FOREIGN KEY fk_empleados_tag;

ALTER TABLE empleados
  ADD CONSTRAINT fk_empleados_tag
    FOREIGN KEY (tag_id) REFERENCES taguhf(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- Ensure one-to-one inside empleados
ALTER TABLE empleados
  ADD CONSTRAINT uk_empleados_tag UNIQUE KEY (tag_id);