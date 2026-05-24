-- ============================================================================
-- Sistema Bolão - Patch de schema
-- Data: 2026-04-09
-- Objetivo: adicionar PAR_DH_ULTIMA_TROCA_SENHA sem recriar tabela
-- ============================================================================

SET NAMES utf8mb4;

SET @col_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'PAR_PARTICIPANTE'
    AND COLUMN_NAME = 'PAR_DH_ULTIMA_TROCA_SENHA'
);

SET @ddl := IF(
  @col_exists = 0,
  'ALTER TABLE `PAR_PARTICIPANTE` ADD COLUMN `PAR_DH_ULTIMA_TROCA_SENHA` TIMESTAMP NULL COMMENT \'Data/hora da última troca de senha\'',
  'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
