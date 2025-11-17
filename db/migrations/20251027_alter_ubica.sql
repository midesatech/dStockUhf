ALTER TABLE location
  ADD COLUMN parent_id BIGINT NULL,
  ADD CONSTRAINT fk_location_parent
    FOREIGN KEY (parent_id) REFERENCES location(id)
    ON DELETE SET NULL;

CREATE UNIQUE INDEX uq_location_parent_nombre
  ON location (COALESCE(parent_id, 0), nombre);

-- Acelera agregaciones por ubicación
CREATE INDEX idx_detections_ubicacion_created
  ON uhf_detections (ubicacion_id, created_at);