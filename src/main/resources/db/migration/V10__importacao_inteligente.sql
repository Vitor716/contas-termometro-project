ALTER TABLE lotes_importacao ADD COLUMN status TEXT NOT NULL DEFAULT 'PENDENTE';
ALTER TABLE lotes_importacao ADD COLUMN hash_arquivo TEXT;

CREATE UNIQUE INDEX idx_lotes_importacao_hash_arquivo
    ON lotes_importacao (hash_arquivo)
    WHERE hash_arquivo IS NOT NULL;

CREATE TABLE linhas_importacao_preview (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    lote_id TEXT NOT NULL,
    descricao_original TEXT NOT NULL,
    descricao_limpa TEXT NOT NULL,
    valor_centavos INTEGER NOT NULL,
    data TEXT NOT NULL,
    hash_linha TEXT NOT NULL,
    categoria_sugerida TEXT,
    is_parcelamento INTEGER NOT NULL DEFAULT 0,
    parcela_atual INTEGER,
    parcela_total INTEGER,
    is_duplicidade INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_preview_lote
        FOREIGN KEY (lote_id)
        REFERENCES lotes_importacao (id_lote)
        ON DELETE CASCADE
);

CREATE INDEX idx_linhas_importacao_preview_lote
    ON linhas_importacao_preview (lote_id);

CREATE INDEX idx_linhas_importacao_preview_hash
    ON linhas_importacao_preview (hash_linha);
