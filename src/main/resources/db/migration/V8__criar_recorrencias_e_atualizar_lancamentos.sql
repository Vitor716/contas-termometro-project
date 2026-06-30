CREATE TABLE recorrencias_lancamento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo TEXT NOT NULL,
    descricao TEXT NOT NULL,
    valor_centavos INTEGER NOT NULL,
    categoria TEXT,
    observacao TEXT,
    mes_inicio TEXT NOT NULL,
    mes_fim TEXT,
    dia_preferencial INTEGER NOT NULL,
    frequencia TEXT NOT NULL DEFAULT 'MENSAL',
    status TEXT NOT NULL DEFAULT 'ATIVA',
    criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CHECK (tipo IN ('ENTRADA', 'SAIDA_FIXA')),
    CHECK (mes_inicio GLOB '[0-9][0-9][0-9][0-9]-[0-9][0-9]'),
    CHECK (mes_fim IS NULL OR mes_fim GLOB '[0-9][0-9][0-9][0-9]-[0-9][0-9]'),
    CHECK (status IN ('ATIVA', 'ENCERRADA', 'CANCELADA'))
);

ALTER TABLE lancamentos ADD COLUMN recorrencia_id INTEGER REFERENCES recorrencias_lancamento(id);
ALTER TABLE lancamentos ADD COLUMN recorrencia_excecao BOOLEAN NOT NULL DEFAULT 0;

CREATE UNIQUE INDEX uq_lancamento_recorrencia_mes
    ON lancamentos (recorrencia_id, mes_referencia)
    WHERE recorrencia_id IS NOT NULL;