CREATE TABLE lotes_importacao (
    -- O id_lote será a string híbrida (ex: lote_20260618_121530_123e4567)
    id_lote TEXT PRIMARY KEY,

    -- Qual a origem dos dados (ex: 'NUBANK_CSV', 'ITAU_OFX', 'MANUAL')
    origem TEXT NOT NULL,

    -- Metadados numéricos para montar a tela de "Histórico de Importações"
    qtd_sucessos INTEGER NOT NULL DEFAULT 0,
    qtd_falhas INTEGER NOT NULL DEFAULT 0,
    total_processado INTEGER NOT NULL DEFAULT 0,

    -- Opcional: Salvar os erros brutos num JSON text para debugar depois
    log_falhas_json TEXT,

    criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);