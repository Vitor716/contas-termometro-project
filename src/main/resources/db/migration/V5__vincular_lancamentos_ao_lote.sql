CREATE TABLE lancamentos_novo (
  id INTEGER PRIMARY KEY AUTOINCREMENT,

  id_lote TEXT,

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

-- Constraints originais mantidas
  CHECK (tipo IN ('ENTRADA', 'SAIDA_FIXA', 'GASTO_DIARIO', 'INVESTIMENTO', 'AJUSTE_SALDO')),
  CHECK (
      (tipo = 'AJUSTE_SALDO' AND valor_centavos <> 0)
          OR
      (tipo <> 'AJUSTE_SALDO' AND valor_centavos > 0)
      ),
  CHECK (data_lancamento GLOB '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]'),
    CHECK (mes_referencia GLOB '[0-9][0-9][0-9][0-9]-[0-9][0-9]'),
    CHECK (status IN ('ATIVO', 'CANCELADO')),

    -- >>> A LIGAÇÃO (FOREIGN KEY) AQUI <<<
    -- Se o lote for apagado da tabela lotes_importacao, o SQLite
    -- vai APAGAR EM CASCATA todos os lançamentos que pertenciam a ele!
    CONSTRAINT fk_lancamento_lote
        FOREIGN KEY (id_lote)
        REFERENCES lotes_importacao (id_lote)
        ON DELETE CASCADE
);

-- Copia os dados da tabela antiga para a nova (o id_lote ficará NULL para os dados antigos)
INSERT INTO lancamentos_novo (
    id, tipo, descricao, valor_centavos, data_lancamento, mes_referencia,
    categoria, observacao, status, criado_em, atualizado_em
)
SELECT
    id, tipo, descricao, valor_centavos, data_lancamento, mes_referencia,
    categoria, observacao, status, criado_em, atualizado_em
FROM lancamentos;

-- Exclui a tabela velha
DROP TABLE lancamentos;

-- Renomeia a nova para assumir o lugar oficial
ALTER TABLE lancamentos_novo RENAME TO lancamentos;

-- Recria os índices para performance
CREATE INDEX idx_lancamentos_mes_referencia ON lancamentos (mes_referencia);
CREATE INDEX idx_lancamentos_mes_tipo ON lancamentos (mes_referencia, tipo);
CREATE INDEX idx_lancamentos_data_lancamento ON lancamentos (data_lancamento);
-- Novo índice: Muito importante para buscar os lançamentos de um lote rapidamente
CREATE INDEX idx_lancamentos_lote ON lancamentos (id_lote);