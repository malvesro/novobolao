# Plano: Sanitização Unificada das Entradas de Cadastro (2026-02-23)

**Objetivo:** Garantir que login, nome e e-mail sejam sanitizados de forma consistente contra HTML/SQL e outros caracteres indesejados.

## Escopo
- Aplicar `SanitizationUtils.cleanText`/`containsHtml` às entradas processadas por `ParticipanteAction`.
- Revisar utilitários de sanitização para suportar limites de tamanho e normalização.
- Validar que os dados persistidos (Hibernate/MySQL) permanecem consistentes e livres de tags.

## Etapas
1. Inventariar pontos de entrada no fluxo de cadastro (setters, `validarCadastroEntradas`, `ParticipanteService`).
2. Implementar sanitização imediata dos campos (login/nome/e-mail) usando os utilitários existentes.
3. Ajustar validações/mensagens de erro conforme necessário.
4. Testar o fluxo completo (cadastro com inputs contendo HTML, SQL-like, whitespace etc.) e registrar log.

## Entregáveis
- Código atualizado com sanitização centralizada.
- Mensagens de validação alinhadas às novas restrições.
- Log de sessão documentando testes de regressão.
