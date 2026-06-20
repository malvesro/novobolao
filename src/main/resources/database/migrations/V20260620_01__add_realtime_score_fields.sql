-- Migration V20260620_01: Add real-time score fields to JOG_JOGO
-- Execute este script no banco de produção (Aiven) para habilitar a integração.

-- 1. Adicionar colunas
ALTER TABLE JOG_JOGO ADD COLUMN IF NOT EXISTS JOG_EXTERNAL_ID VARCHAR(64);
ALTER TABLE JOG_JOGO ADD COLUMN IF NOT EXISTS JOG_LAST_CHECKED DATETIME;
ALTER TABLE JOG_JOGO ADD COLUMN IF NOT EXISTS JOG_SOURCE_UPDATED DATETIME;

-- 2. Adicionar índice para performance de busca por ID externo
CREATE INDEX IF NOT EXISTS idx_jog_external_id ON JOG_JOGO(JOG_EXTERNAL_ID);

-- 3. (Opcional) Placeholder para mapeamento de IDs da WC 2026
-- Você deverá atualizar os IDs externos conforme a tabela oficial da football-data.org
-- UPDATE JOG_JOGO SET JOG_EXTERNAL_ID = 'api_id_here' WHERE JOG_ID = xxx;
