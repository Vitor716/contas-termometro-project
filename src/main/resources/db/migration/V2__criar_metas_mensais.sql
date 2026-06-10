CREATE TABLE metas_mensais (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    mes_referencia TEXT NOT NULL,
    percentual_meta_investimento_bps INTEGER NOT NULL,
    orcamento_diario_minimo_centavos INTEGER NOT NULL DEFAULT 0,
    motivo TEXT,
    vigente_desde TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    vigente_ate TEXT,
    criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (mes_referencia GLOB '[0-9][0-9][0-9][0-9]-[0-9][0-9]'),
    CHECK (percentual_meta_investimento_bps BETWEEN 0 AND 10000),
    CHECK (orcamento_diario_minimo_centavos >= 0),
    CHECK (vigente_ate IS NULL OR vigente_ate > vigente_desde)
);

CREATE UNIQUE INDEX uq_metas_mensais_mes_vigente
    ON metas_mensais (mes_referencia)
    WHERE vigente_ate IS NULL;

CREATE INDEX idx_metas_mensais_mes_referencia
    ON metas_mensais (mes_referencia);
