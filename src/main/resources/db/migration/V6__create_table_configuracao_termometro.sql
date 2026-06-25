CREATE TABLE configuracao_termometro (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    -- Valores monetários diretos (até 9 trilhões, com 2 casas decimais)
    reserva_minima_intocavel NUMERIC(15, 2) NOT NULL,
    orcamento_diario_minimo NUMERIC(15, 2) NOT NULL,

    -- Porcentagens/Taxas (ex: 0.3000 = 30%)
    comprometimento_maximo_renda NUMERIC(5, 4) NOT NULL,
    margem_seguranca NUMERIC(5, 4) NOT NULL,

    -- Estratégia salva como texto (mapeado para o Enum no código)
    estrategia VARCHAR(50) NOT NULL,

    -- Campos de auditoria recomendados
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
);