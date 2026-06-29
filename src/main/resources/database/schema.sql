-- ============================================================================
-- Sistema Bolão - Database Schema
-- MySQL 8.4+
-- Gerado a partir dos mapeamentos Hibernate (.hbm.xml)
-- Data: 2026-04-11 (atualizado: coluna PAR_DH_ULTIMA_TROCA_SENHA, EQP_PAIS length corrigido)
-- ============================================================================

-- Configurações iniciais
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- Tabela: PAR_PARTICIPANTE (Participantes/Usuários)
-- ============================================================================
CREATE TABLE IF NOT EXISTS `PAR_PARTICIPANTE` (
  `PAR_ID` BIGINT NOT NULL AUTO_INCREMENT,
  `PAR_NOME` VARCHAR(100) NOT NULL COMMENT 'Nome completo do participante',
  `PAR_LOGIN` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Login único para autenticação',
  `PAR_SENHA` VARCHAR(255) NOT NULL COMMENT 'Senha hash (SHA-1 legado ou BCrypt)',
  `PAR_EMAIL` VARCHAR(100) NOT NULL COMMENT 'E-mail do participante',
  `PAR_HABILITADO` CHAR(1) NOT NULL DEFAULT 'F' COMMENT 'T=habilitado, F=desabilitado',
  `PAR_IP` VARCHAR(45) NOT NULL COMMENT 'IP do cadastro (suporta IPv6)',
  `PAR_DH_CADASTRO` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data/hora do cadastro',
  `PAR_DH_ULTIMA_TROCA_SENHA` TIMESTAMP NULL COMMENT 'Data/hora da última troca de senha',
  PRIMARY KEY (`PAR_ID`),
  INDEX `idx_par_login` (`PAR_LOGIN`),
  INDEX `idx_par_email` (`PAR_EMAIL`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Participantes do bolão';

-- ============================================================================
-- Tabela: PRI_PRIVILEGIO (Papéis/Roles dos Participantes)
-- ============================================================================
CREATE TABLE IF NOT EXISTS `PRI_PRIVILEGIO` (
  `PRI_PAR_ID` BIGINT NOT NULL COMMENT 'ID do participante',
  `PRI_PAPEL` VARCHAR(50) NOT NULL COMMENT 'Papel: ROLE_USER, ROLE_ADMIN, etc',
  PRIMARY KEY (`PRI_PAR_ID`, `PRI_PAPEL`),
  CONSTRAINT `fk_pri_participante` FOREIGN KEY (`PRI_PAR_ID`) 
    REFERENCES `PAR_PARTICIPANTE` (`PAR_ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Papéis/privilégios dos participantes';

-- ============================================================================
-- Tabela: EQP_EQUIPE (Equipes/Seleções)
-- ============================================================================
CREATE TABLE IF NOT EXISTS `EQP_EQUIPE` (
  `EQP_ID` BIGINT NOT NULL AUTO_INCREMENT,
  `EQP_PAIS` VARCHAR(100) NOT NULL COMMENT 'Nome do país/seleção',
  `EQP_GRUPO` CHAR(1) NOT NULL COMMENT 'Grupo da Copa (A, B, C, etc)',
  PRIMARY KEY (`EQP_ID`),
  INDEX `idx_eqp_grupo` (`EQP_GRUPO`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Equipes participantes da Copa';

-- ============================================================================
-- Tabela: JOG_JOGO (Jogos/Partidas)
-- ============================================================================
CREATE TABLE IF NOT EXISTS `JOG_JOGO` (
  `JOG_ID` BIGINT NOT NULL AUTO_INCREMENT,
  `JOG_DATA` DATE NOT NULL COMMENT 'Data do jogo',
  `JOG_HORA` TIME NOT NULL COMMENT 'Horário do jogo',
  `JOG_LOCAL` VARCHAR(100) NOT NULL COMMENT 'Local/estádio do jogo',
  `JOG_EQP1_ID` BIGINT NOT NULL COMMENT 'ID da equipe 1',
  `JOG_EQP2_ID` BIGINT NOT NULL COMMENT 'ID da equipe 2',
  `JOG_EQP1_GOLS` INT NULL COMMENT 'Gols da equipe 1 (NULL se jogo não ocorreu)',
  `JOG_EQP2_GOLS` INT NULL COMMENT 'Gols da equipe 2 (NULL se jogo não ocorreu)',
  `JOG_FASE` INT NOT NULL COMMENT 'Fase: 11/12/13=Grupos (rodadas 1-3), 16=32-avos, 8=Oitavas, 4=Quartas, 2=Semifinal, 3=3º lugar, 1=Final',
  PRIMARY KEY (`JOG_ID`),
  INDEX `idx_jog_data` (`JOG_DATA`),
  INDEX `idx_jog_fase` (`JOG_FASE`),
  CONSTRAINT `fk_jog_equipe1` FOREIGN KEY (`JOG_EQP1_ID`) 
    REFERENCES `EQP_EQUIPE` (`EQP_ID`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_jog_equipe2` FOREIGN KEY (`JOG_EQP2_ID`) 
    REFERENCES `EQP_EQUIPE` (`EQP_ID`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Jogos/partidas da Copa';

-- ============================================================================
-- Tabela: PAL_PALPITE (Palpites dos Participantes)
-- ============================================================================
CREATE TABLE IF NOT EXISTS `PAL_PALPITE` (
  `PAL_PAR_ID` BIGINT NOT NULL COMMENT 'ID do participante',
  `PAL_JOG_ID` BIGINT NOT NULL COMMENT 'ID do jogo',
  `PAL_EQP1_GOLS` INT NOT NULL COMMENT 'Palpite de gols da equipe 1',
  `PAL_EQP2_GOLS` INT NOT NULL COMMENT 'Palpite de gols da equipe 2',
  `PAL_IP` VARCHAR(45) NOT NULL COMMENT 'IP da última atualização',
  `PAL_DH_ATUALIZACAO` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Data/hora da última atualização',
  PRIMARY KEY (`PAL_PAR_ID`, `PAL_JOG_ID`),
  INDEX `idx_pal_jogo` (`PAL_JOG_ID`),
  CONSTRAINT `fk_pal_participante` FOREIGN KEY (`PAL_PAR_ID`) 
    REFERENCES `PAR_PARTICIPANTE` (`PAR_ID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_pal_jogo` FOREIGN KEY (`PAL_JOG_ID`) 
    REFERENCES `JOG_JOGO` (`JOG_ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Palpites dos participantes nos jogos';

-- ============================================================================
-- Tabela: BOI_BOLAO_INDIVIDUAL (Bolões Individuais por Jogo)
-- ============================================================================
CREATE TABLE IF NOT EXISTS `BOI_BOLAO_INDIVIDUAL` (
  `BOI_ID` BIGINT NOT NULL AUTO_INCREMENT,
  `BOI_JOG_ID` BIGINT NOT NULL UNIQUE COMMENT 'ID do jogo (one-to-one)',
  `BOI_VALOR_COTA` DECIMAL(10,2) NOT NULL COMMENT 'Valor da cota do bolão',
  `BOI_STATUS` INT NOT NULL COMMENT 'Status: 0=ABERTO, 1=FECHADO, 2=FINALIZADO',
  PRIMARY KEY (`BOI_ID`),
  CONSTRAINT `fk_boi_jogo` FOREIGN KEY (`BOI_JOG_ID`) 
    REFERENCES `JOG_JOGO` (`JOG_ID`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bolões individuais por jogo';

-- ============================================================================
-- Tabela: PAI_PALPITE_INDIVIDUAL (Palpites em Bolões Individuais)
-- ============================================================================
CREATE TABLE IF NOT EXISTS `PAI_PALPITE_INDIVIDUAL` (
  `PAI_PAR_ID` BIGINT NOT NULL COMMENT 'ID do participante',
  `PAI_BOI_ID` BIGINT NOT NULL COMMENT 'ID do bolão individual',
  `PAI_EQP1_GOLS` INT NOT NULL COMMENT 'Palpite de gols da equipe 1',
  `PAI_EQP2_GOLS` INT NOT NULL COMMENT 'Palpite de gols da equipe 2',
  `PAL_IP` VARCHAR(45) NOT NULL COMMENT 'IP do palpite',
  `PAI_PAR_PAGO` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0=não pago, 1=pago',
  `PAI_DH_PAGTO` TIMESTAMP NULL COMMENT 'Data/hora do pagamento',
  PRIMARY KEY (`PAI_PAR_ID`, `PAI_BOI_ID`),
  INDEX `idx_pai_bolao` (`PAI_BOI_ID`),
  CONSTRAINT `fk_pai_participante` FOREIGN KEY (`PAI_PAR_ID`) 
    REFERENCES `PAR_PARTICIPANTE` (`PAR_ID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_pai_bolao` FOREIGN KEY (`PAI_BOI_ID`) 
    REFERENCES `BOI_BOLAO_INDIVIDUAL` (`BOI_ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Palpites em bolões individuais';

-- ============================================================================
-- Tabela: NOT_NOTICIA (Notícias do Sistema)
-- ============================================================================
CREATE TABLE IF NOT EXISTS `NOT_NOTICIA` (
  `NOT_ID` BIGINT NOT NULL AUTO_INCREMENT,
  `NOT_TITULO` VARCHAR(200) NOT NULL COMMENT 'Título da notícia',
  `NOT_CONTEUDO` TEXT NOT NULL COMMENT 'Conteúdo da notícia',
  `NOT_DATA` DATE NOT NULL COMMENT 'Data da notícia',
  `NOT_AUTOR` VARCHAR(100) NOT NULL COMMENT 'Autor da notícia',
  PRIMARY KEY (`NOT_ID`),
  INDEX `idx_not_data` (`NOT_DATA`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Notícias do sistema';

-- ============================================================================
-- Tabela: CHT_CHAT_MENSAGEM (Mensagens do chat)
-- ============================================================================
CREATE TABLE IF NOT EXISTS `CHT_CHAT_MENSAGEM` (
  `CHT_ID` BIGINT NOT NULL AUTO_INCREMENT,
  `CHT_LOGIN_AUTOR` VARCHAR(32) NOT NULL COMMENT 'Login autor da mensagem',
  `CHT_NOME_EXIBICAO` VARCHAR(80) NOT NULL COMMENT 'Nome de exibição do autor',
  `CHT_TEXTO` VARCHAR(300) NOT NULL COMMENT 'Mensagem sanitizada',
  `CHT_DATA_ENVIO` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data/hora de envio',
  `CHT_REPLY_TO_ID` BIGINT NULL COMMENT 'Mensagem pai referenciada como resposta',
  PRIMARY KEY (`CHT_ID`),
  INDEX `IDX_CHT_DATA_ENVIO` (`CHT_DATA_ENVIO`),
  INDEX `IDX_CHT_LOGIN` (`CHT_LOGIN_AUTOR`),
  INDEX `IDX_CHT_REPLY_TO` (`CHT_REPLY_TO_ID`),
  CONSTRAINT `FK_CHT_REPLY_TO` FOREIGN KEY (`CHT_REPLY_TO_ID`)
    REFERENCES `CHT_CHAT_MENSAGEM` (`CHT_ID`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Mensagens persistidas do chat';

SET @cht_reply_col_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'CHT_CHAT_MENSAGEM'
    AND COLUMN_NAME = 'CHT_REPLY_TO_ID'
);
SET @cht_reply_col_sql := IF(
  @cht_reply_col_exists = 0,
  'ALTER TABLE `CHT_CHAT_MENSAGEM` ADD COLUMN `CHT_REPLY_TO_ID` BIGINT NULL COMMENT ''Mensagem pai referenciada como resposta''',
  'SELECT 1'
);
PREPARE stmt_chm_col FROM @cht_reply_col_sql;
EXECUTE stmt_chm_col;
DEALLOCATE PREPARE stmt_chm_col;

SET @cht_reply_idx_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'CHT_CHAT_MENSAGEM'
    AND INDEX_NAME = 'IDX_CHT_REPLY_TO'
);
SET @cht_reply_idx_sql := IF(
  @cht_reply_idx_exists = 0,
  'ALTER TABLE `CHT_CHAT_MENSAGEM` ADD INDEX `IDX_CHT_REPLY_TO` (`CHT_REPLY_TO_ID`)',
  'SELECT 1'
);
PREPARE stmt_chm_idx FROM @cht_reply_idx_sql;
EXECUTE stmt_chm_idx;
DEALLOCATE PREPARE stmt_chm_idx;

SET @cht_reply_fk_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND CONSTRAINT_NAME = 'FK_CHT_REPLY_TO'
    AND TABLE_NAME = 'CHT_CHAT_MENSAGEM'
);
SET @cht_reply_fk_sql := IF(
  @cht_reply_fk_exists = 0,
  'ALTER TABLE `CHT_CHAT_MENSAGEM` ADD CONSTRAINT `FK_CHT_REPLY_TO` FOREIGN KEY (`CHT_REPLY_TO_ID`) REFERENCES `CHT_CHAT_MENSAGEM` (`CHT_ID`) ON DELETE SET NULL ON UPDATE CASCADE',
  'SELECT 1'
);
PREPARE stmt_chm_fk FROM @cht_reply_fk_sql;
EXECUTE stmt_chm_fk;
DEALLOCATE PREPARE stmt_chm_fk;

-- ============================================================================
-- Tabela: CHT_CHAT_MENCAO (Pendências e histórico de menções do chat)
-- ============================================================================
CREATE TABLE IF NOT EXISTS `CHT_CHAT_MENCAO` (
  `CHM_ID` BIGINT NOT NULL AUTO_INCREMENT,
  `CHM_DEST_LOGIN` VARCHAR(32) NOT NULL COMMENT 'Login destinatário da menção',
  `CHM_AUTOR_LOGIN` VARCHAR(32) NOT NULL COMMENT 'Login de quem mencionou',
  `CHM_AUTOR_NOME` VARCHAR(80) NOT NULL COMMENT 'Nome de exibição de quem mencionou',
  `CHM_CHT_ID` BIGINT NOT NULL COMMENT 'ID da mensagem do chat de origem',
  `CHM_PREVIEW` VARCHAR(120) NOT NULL COMMENT 'Prévia sanitizada da mensagem',
  `CHM_DATA_CRIACAO` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data/hora de criação da menção',
  `CHM_DATA_CONFIRMACAO` TIMESTAMP NULL COMMENT 'Data/hora de confirmação da menção (ACK)',
  PRIMARY KEY (`CHM_ID`),
  UNIQUE KEY `UK_CHT_MENCAO_DEST_MSG` (`CHM_DEST_LOGIN`, `CHM_CHT_ID`),
  INDEX `IDX_CHT_MENCAO_DEST_PEND` (`CHM_DEST_LOGIN`, `CHM_DATA_CONFIRMACAO`, `CHM_DATA_CRIACAO`),
  INDEX `IDX_CHT_MENCAO_DEST_DATA` (`CHM_DEST_LOGIN`, `CHM_DATA_CRIACAO`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Menções pendentes e histórico persistidos para entrega cross-screen';

-- ============================================================================
-- Restaurar configurações
-- ============================================================================
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- Fim do Schema
-- ============================================================================
