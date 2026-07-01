-- V9__atualiza_recorrencias_lancamento.sql
-- Flyway gerencia a transação; PRAGMA foreign_keys vai no application.properties

CREATE TABLE recorrencias_lancamento_new (
                                             id               INTEGER PRIMARY KEY AUTOINCREMENT,
                                             tipo             TEXT    NOT NULL,
                                             descricao        TEXT    NOT NULL,
                                             valor_centavos   INTEGER NOT NULL,
                                             categoria        TEXT,
                                             observacao       TEXT,
                                             mes_inicio       TEXT    NOT NULL,
                                             mes_fim          TEXT,
                                             dia_preferencial INTEGER NOT NULL,
                                             frequencia       TEXT    NOT NULL DEFAULT 'MENSAL',
                                             status           TEXT    NOT NULL DEFAULT 'ATIVO',
                                             criado_em        TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             atualizado_em    TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                             CHECK (tipo IN ('ENTRADA', 'SAIDA_FIXA', 'GASTO_DIARIO')),
                                             CHECK (mes_inicio GLOB '[0-9][0-9][0-9][0-9]-[0-9][0-9]'),
    CHECK (mes_fim IS NULL OR mes_fim GLOB '[0-9][0-9][0-9][0-9]-[0-9][0-9]'),
    CHECK (frequencia IN ('MENSAL', 'BIMESTRAL', 'TRIMESTRAL', 'SEMESTRAL', 'ANUAL')),
    CHECK (status IN ('ATIVO', 'INATIVO', 'PAUSADO'))
);

INSERT INTO recorrencias_lancamento_new
SELECT
    id,
    tipo,
    descricao,
    valor_centavos,
    categoria,
    observacao,
    mes_inicio,
    mes_fim,
    dia_preferencial,
    frequencia,
    CASE status
        WHEN 'ATIVA'     THEN 'ATIVO'
        WHEN 'ENCERRADA' THEN 'INATIVO'
        WHEN 'CANCELADA' THEN 'INATIVO'
        ELSE status
        END,
    criado_em,
    atualizado_em
FROM recorrencias_lancamento;

DROP TABLE recorrencias_lancamento;

ALTER TABLE recorrencias_lancamento_new RENAME TO recorrencias_lancamento;