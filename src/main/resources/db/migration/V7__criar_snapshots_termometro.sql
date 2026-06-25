CREATE TABLE snapshots_termometro (
    mes_referencia VARCHAR(7) PRIMARY KEY, -- Formato: YYYY-MM
    status_atual VARCHAR(20) NOT NULL,
    gasto_diario_restante_centavos BIGINT NOT NULL,
    total_investido_centavos BIGINT NOT NULL,
    performance_contra_meta_bps INTEGER NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
);