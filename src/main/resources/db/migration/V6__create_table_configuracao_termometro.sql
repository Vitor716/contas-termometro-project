CREATE TABLE configuracao_termometro (
    id LONG PRIMARY KEY,

    -- Valores monetários diretos (até 9 trilhões, com 2 casas decimais)
    reserva_minima_intocavel NUMERIC(15, 2) NOT NULL,
    orcamento_diario_minimo NUMERIC(15, 2) NOT NULL,

    -- Porcentagens/Taxas (ex: 0.3000 = 30%)
    comprometimento_maximo_renda NUMERIC(5, 4) NOT NULL,
    margem_seguranca NUMERIC(5, 4) NOT NULL,

    -- Estratégia salva como texto (mapeado para o Enum no código)
    estrategia VARCHAR(50) NOT NULL,

    -- Campos de auditoria recomendados
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- (Opcional) Trigger para atualizar o 'updated_at' automaticamente no PostgreSQL
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_config_termometro_modtime
    BEFORE UPDATE ON configuracao_termometro
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();