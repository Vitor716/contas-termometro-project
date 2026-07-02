DROP INDEX IF EXISTS uq_lancamento_recorrencia_mes;

CREATE UNIQUE INDEX uq_lancamento_recorrencia_mes
    ON lancamentos (recorrencia_id, mes_referencia)
    WHERE recorrencia_id IS NOT NULL
      AND status = 'ATIVO'
      AND recorrencia_excecao = 0;
