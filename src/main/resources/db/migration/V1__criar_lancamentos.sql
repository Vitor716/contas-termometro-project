CREATE TABLE lancamentos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo TEXT NOT NULL,
    descricao TEXT NOT NULL,
    valor_centavos INTEGER NOT NULL,
    data_lancamento TEXT NOT NULL,
    mes_referencia TEXT NOT NULL,
    categoria TEXT,
    observacao TEXT,
    status TEXT NOT NULL DEFAULT 'ATIVO',
    criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (tipo IN ('ENTRADA', 'SAIDA_FIXA', 'GASTO_DIARIO', 'INVESTIMENTO', 'AJUSTE_SALDO')),
    CHECK (
        (tipo = 'AJUSTE_SALDO' AND valor_centavos <> 0)
        OR
        (tipo <> 'AJUSTE_SALDO' AND valor_centavos > 0)
    ),
    CHECK (mes_referencia GLOB '[0-9][0-9][0-9][0-9]-[0-9][0-9]'),
    CHECK (status IN ('ATIVO', 'CANCELADO'))
);

CREATE INDEX idx_lancamentos_mes_referencia
    ON lancamentos (mes_referencia);

CREATE INDEX idx_lancamentos_mes_tipo
    ON lancamentos (mes_referencia, tipo);

CREATE INDEX idx_lancamentos_data_lancamento
    ON lancamentos (data_lancamento);
