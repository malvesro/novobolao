-- ============================================================================
-- Sistema Bolão - Dados Iniciais (Seed Data)
-- MySQL 8.0+
-- Data: 2026-02-17
-- ============================================================================

USE `bolao`;

-- ============================================================================
-- Usuário Administrador Padrão
-- ============================================================================
-- Login: admin
-- Senha: admin123 (BCrypt)
-- IMPORTANTE: Trocar a senha após primeiro login!
-- ============================================================================

INSERT INTO `PAR_PARTICIPANTE` (
  `PAR_ID`, `PAR_NOME`, `PAR_LOGIN`, `PAR_SENHA`, `PAR_EMAIL`, 
  `PAR_HABILITADO`, `PAR_IP`, `PAR_DH_CADASTRO`
) VALUES (
  1, 
  'Administrador do Sistema', 
  'admin',
  '$2b$12$QGastsU88DIlT8u5nVXfbuIlEbeXifCooeLiPcYz2MeZBdgXrr9/u', -- BCrypt de 'admin123'
  'admin@bolao.local', 
  'T', 
  '127.0.0.1', 
  CURRENT_TIMESTAMP
);

-- Privilégios do administrador
INSERT INTO `PRI_PRIVILEGIO` (`PRI_PAR_ID`, `PRI_PAPEL`) VALUES
(1, 'ROLE_USER'),
(1, 'ROLE_ADMIN');

-- ============================================================================
-- Usuário de Teste
-- ============================================================================
-- Login: user
-- Senha: user123 (BCrypt)
-- ============================================================================

INSERT INTO `PAR_PARTICIPANTE` (
  `PAR_ID`, `PAR_NOME`, `PAR_LOGIN`, `PAR_SENHA`, `PAR_EMAIL`, 
  `PAR_HABILITADO`, `PAR_IP`, `PAR_DH_CADASTRO`
) VALUES (
  2, 
  'Usuário de Teste', 
  'user',
  '$2b$12$JZ1I7s1fL7bypQYT.K46Heh2bc854U2SKrSNGfzILkf6rR2T3kMNa', -- BCrypt de 'user123'
  'user@bolao.local', 
  'T', 
  '127.0.0.1', 
  CURRENT_TIMESTAMP
);

-- Privilégios do usuário de teste
INSERT INTO `PRI_PRIVILEGIO` (`PRI_PAR_ID`, `PRI_PAPEL`) VALUES
(2, 'ROLE_USER');

-- ============================================================================
-- Equipes de Exemplo (Copa do Mundo 2026 - Grupos Fictícios)
-- ============================================================================

INSERT INTO `EQP_EQUIPE` (`EQP_ID`, `EQP_PAIS`, `EQP_GRUPO`) VALUES
-- Grupo A
(1, 'Brasil', 'A'),
(2, 'Argentina', 'A'),
(3, 'Uruguai', 'A'),
(4, 'Chile', 'A'),

-- Grupo B
(5, 'Alemanha', 'B'),
(6, 'França', 'B'),
(7, 'Inglaterra', 'B'),
(8, 'Espanha', 'B'),

-- Grupo C
(9, 'Portugal', 'C'),
(10, 'Itália', 'C'),
(11, 'Holanda', 'C'),
(12, 'Bélgica', 'C'),

-- Grupo D
(13, 'México', 'D'),
(14, 'Estados Unidos', 'D'),
(15, 'Canadá', 'D'),
(16, 'Costa Rica', 'D');

-- ============================================================================
-- Jogos de Exemplo (Fase de Grupos)
-- ============================================================================

INSERT INTO `JOG_JOGO` (
  `JOG_ID`, `JOG_DATA`, `JOG_HORA`, `JOG_LOCAL`, 
  `JOG_EQP1_ID`, `JOG_EQP2_ID`, `JOG_FASE`,
  `JOG_EQP1_GOLS`, `JOG_EQP2_GOLS`
) VALUES
-- Grupo A
(1, '2026-06-11', '16:00:00', 'Estádio Azteca', 1, 3, 0, NULL, NULL),
(2, '2026-06-11', '19:00:00', 'Estádio Azteca', 2, 4, 0, NULL, NULL),
(3, '2026-06-16', '16:00:00', 'Estádio Azteca', 1, 4, 0, NULL, NULL),
(4, '2026-06-16', '19:00:00', 'Estádio Azteca', 2, 3, 0, NULL, NULL),
(5, '2026-06-21', '16:00:00', 'Estádio Azteca', 1, 2, 0, NULL, NULL),
(6, '2026-06-21', '16:00:00', 'Estádio Azteca', 3, 4, 0, NULL, NULL),

-- Grupo B
(7, '2026-06-12', '13:00:00', 'MetLife Stadium', 5, 7, 0, NULL, NULL),
(8, '2026-06-12', '16:00:00', 'MetLife Stadium', 6, 8, 0, NULL, NULL),
(9, '2026-06-17', '13:00:00', 'MetLife Stadium', 5, 8, 0, NULL, NULL),
(10, '2026-06-17', '16:00:00', 'MetLife Stadium', 6, 7, 0, NULL, NULL),
(11, '2026-06-22', '16:00:00', 'MetLife Stadium', 5, 6, 0, NULL, NULL),
(12, '2026-06-22', '16:00:00', 'MetLife Stadium', 7, 8, 0, NULL, NULL);

-- ============================================================================
-- Notícia de Boas-Vindas
-- ============================================================================

INSERT INTO `NOT_NOTICIA` (
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
