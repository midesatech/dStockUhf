-- Índices recomendados para dashboard (MariaDB/MySQL)
CREATE INDEX IF NOT EXISTS idx_detecciones_tags_epc_created_at ON detecciones_tags (epc, created_at);
CREATE INDEX IF NOT EXISTS idx_detecciones_tags_ubicacion_created ON detecciones_tags (ubicacion_id, created_at);
