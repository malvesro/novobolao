# Plano de Arquitetura e Contratos Tecnicos - Recuperacao de Senha com OTP em Memoria

**Data:** 2026-04-08
**Status:** Em Progresso
**Referencia ADR:** `.ia/historico/ADR-20260408-recuperacao-senha-token-db.md`

## 1. Objetivo
Definir a arquitetura e os contratos tecnicos para o fluxo publico de recuperacao de senha usando **OTP numerico em memoria (volatil)**, com expiracao de 30 minutos, uso unico, limite de tentativas e resposta neutra contra enumeracao de usuarios. A solucao deve registrar **apenas a data da ultima troca de senha** no participante.

## 2. Escopo
- **Inclui:** endpoints Struts publicos, servico de OTP, envio de e-mail, validacao de senha e atualizacao de senha com `PasswordEncoder`.
- **Exclui:** tabela de tokens no banco (substituida por OTP em memoria).

## 3. Fluxo Funcional (alto nivel)
1. Usuario acessa "Esqueci minha senha" na tela de login.
2. Usuario informa e-mail.
3. Sistema gera OTP numerico, armazena em memoria (hash + expiracao + tentativas) e envia e-mail com o codigo.
4. Usuario informa OTP + nova senha.
5. Sistema valida OTP e senha, atualiza senha e registra a data da ultima troca de senha no participante.

## 4. Endpoints/Actions Struts
### 4.1 Publicos (namespace padrão)
- `recuperarSenhaForm` (GET): exibe formulario de solicitacao de OTP.
- `enviarOtpRecuperacao` (POST): valida e-mail, gera OTP e envia e-mail (resposta sempre neutra).
- `validarOtpRecuperacao` (POST): valida OTP e prepara tela para redefinicao de senha.
- `redefinirSenha` (POST): valida OTP + nova senha e aplica troca.

### 4.2 Autenticado (namespace /seguro)
- `trocaSenha` (GET): fluxo existente de troca autenticada (permanece inalterado).

## 5. Servicos e Contratos
### 5.1 Novo servico
`RecuperacaoSenhaService`
- `SolicitacaoOtp solicitarOtp(String email, String ip, String userAgent)`
- `ValidacaoOtp validarOtp(String email, String otp, String ip)`
- `ResultadoTrocaSenha redefinirSenha(String email, String otp, String novaSenha, String ip)`

### 5.2 Contratos
- `SolicitacaoOtp`
  - `boolean enviado`
  - `String mensagemNeutra`
- `ValidacaoOtp`
  - `boolean valido`
  - `String mensagemNeutra`
- `ResultadoTrocaSenha`
  - `boolean sucesso`
  - `String mensagem`

## 6. Armazenamento em Memoria (OTP)
### 6.1 Estrutura sugerida
`OtpStore` (componente simples em memoria, sincronizado):
- Chave: `emailNormalizado` (lowercase + trim)
- Valor: `OtpEntry`
  - `String otpHash`
  - `Instant expiracao`
  - `int tentativas`
  - `int maxTentativas` (ex.: 5)
  - `Instant criadoEm`
  - `String ipSolicitacao`

### 6.2 Regras
- OTP numerico (6 digitos).
- Hash do OTP em memoria (SHA-256 com salt interno).
- Expiracao em 30 minutos.
- Incrementar tentativas a cada falha.
- Invalida apos sucesso ou exceder tentativas.

## 7. Dados e Persistencia
- **Sem nova tabela.**
- **Ajuste autorizado:** adicionar `PAR_DH_ULTIMA_TROCA_SENHA` em `PAR_PARTICIPANTE` no schema e remover `RST_RESET_TOKEN` de `docker/mysql/init/01-schema.sql`.

## 8. Validacoes e Seguranca
- Mensagens neutras para evitar enumeracao de usuarios.
- Rate limit por IP/e-mail (a definir na subtarefa de observabilidade/seguranca).
- Logs de auditoria sem expor OTP em texto puro.
- Sanitizacao de entrada via `SanitizationUtils`.

## 9. Emails
- Template: `recuperacao-senha-otp` (novo template JSP/HTML).
- Conteudo: codigo OTP, prazo de expiracao, recomendacoes de seguranca.
- Envio usando a infraestrutura de [`com.opendev.bolao.email.Email`](src/com/opendev/bolao/email/Email.java:1).

## 10. I18n
- Novas chaves em `messages.properties` (ex.: `recuperacao.titulo`, `recuperacao.otp.enviado`, `recuperacao.otp.invalido`).

## 11. Observacoes
- Requer alinhamento com o ADR e com o passo-a-passo.
- A implementacao deve respeitar as diretrizes de seguranca e arquitetura.

Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
