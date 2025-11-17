-- Índices recomendados para dashboard (MariaDB/MySQL)
CREATE INDEX IF NOT EXISTS idx_uhf_detections_epc_created_at ON uhf_detections (epc, created_at);
CREATE INDEX IF NOT EXISTS idx_uhf_detections_location_created ON uhf_detections (ubicacion_id, created_at);
