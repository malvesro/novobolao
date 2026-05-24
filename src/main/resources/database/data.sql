-- ============================================================================
-- Sistema Bolão - Dados Iniciais (Seed Data)
-- MySQL 8.0+
-- Data: 2026-02-17
-- ============================================================================


-- ============================================================================
-- Usuário Administrador Padrão
-- ============================================================================
-- Login: admin
-- Senha: admin123 (BCrypt)
-- IMPORTANTE: Trocar a senha após primeiro login!
-- ============================================================================

INSERT IGNORE INTO `PAR_PARTICIPANTE` (
  `PAR_ID`, `PAR_NOME`, `PAR_LOGIN`, `PAR_SENHA`, `PAR_EMAIL`, 
  `PAR_HABILITADO`, `PAR_IP`, `PAR_DH_CADASTRO`
) VALUES (
  1, 
  'Administrador do Sistema', 
  'admin',
  '$2a$12$Z9GIzlFcAhxkOjbcMdogteO0sfyibnSHRGxoL9.fG.Z27hPTJIX3G', 
  'admin@bolao.local', 
  'T', 
  '127.0.0.1', 
  CURRENT_TIMESTAMP
);

-- Privilégios do administrador
INSERT IGNORE INTO `PRI_PRIVILEGIO` (`PRI_PAR_ID`, `PRI_PAPEL`) VALUES
(1, 'ROLE_ADMIN'),
(1, 'ROLE_USER');

-- ============================================================================
-- Usuário de Teste
-- ============================================================================
-- Login: user
-- Senha: user123 (BCrypt)
-- ============================================================================

INSERT IGNORE INTO `PAR_PARTICIPANTE` (
  `PAR_ID`, `PAR_NOME`, `PAR_LOGIN`, `PAR_SENHA`, `PAR_EMAIL`, 
  `PAR_HABILITADO`, `PAR_IP`, `PAR_DH_CADASTRO`
) VALUES (
  2, 
  'Usuário Teste', 
  'user',
  '$2b$12$JZ1I7s1fL7bypQYT.K46Heh2bc854U2SKrSNGfzILkf6rR2T3kMNa', -- BCrypt de 'user123'
  'user@bolao.local', 
  'T', 
  '127.0.0.1', 
  CURRENT_TIMESTAMP
);

-- ============================================================================
-- Notícia de Boas-vindas
-- ============================================================================
INSERT IGNORE INTO `NOT_NOTICIA` (
  `NOT_ID`, `NOT_TITULO`, `NOT_CONTEUDO`, `NOT_DATA`, `NOT_AUTOR`
) VALUES (
  1,
  'Bem-vindo ao Sistema Bolão!',
  'O sistema foi inicializado com sucesso. Use as credenciais padrão para fazer login:\n\nAdmin: admin / admin123\nUsuário: user / user123\n\nIMPORTANTE: Troque as senhas padrão após o primeiro acesso!',
  CURDATE(),
  'Sistema'
);

-- ============================================================================
-- Fim dos Dados Iniciais
-- ============================================================================
