-- 1. Criação da tabela de snapshots
CREATE TABLE snapshots_termometro (
    mes_referencia VARCHAR(7) PRIMARY KEY, -- Formato: YYYY-MM
    status_atual VARCHAR(20) NOT NULL,
    gasto_diario_restante_centavos BIGINT NOT NULL,
    total_investido_centavos BIGINT NOT NULL,
    performance_contra_meta_bps INTEGER NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Garantir que a função de atualização do 'updated_at' exista
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

-- 3. Criação da trigger apontando para a função acima
CREATE TRIGGER update_snapshots_termometro_modtime
    BEFORE UPDATE ON snapshots_termometro
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();