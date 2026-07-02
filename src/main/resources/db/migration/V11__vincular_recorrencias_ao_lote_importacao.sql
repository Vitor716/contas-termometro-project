ALTER TABLE recorrencias_lancamento ADD COLUMN id_lote TEXT;

CREATE INDEX idx_recorrencias_lote
    ON recorrencias_lancamento (id_lote);
