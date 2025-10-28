ALTER TABLE ubicaciones
  ADD COLUMN parent_id BIGINT NULL,
  ADD CONSTRAINT fk_ubicaciones_parent
    FOREIGN KEY (parent_id) REFERENCES ubicaciones(id)
    ON DELETE SET NULL;

CREATE UNIQUE INDEX uq_ubicaciones_parent_nombre
  ON ubicaciones (COALESCE(parent_id, 0), nombre);

-- Acelera agregaciones por ubicación
CREATE INDEX idx_detecciones_ubicacion_created
  ON detecciones_tags (ubicacion_id, created_at);