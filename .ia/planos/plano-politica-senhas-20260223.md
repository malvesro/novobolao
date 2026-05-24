# Plano: Revisão da Política de Senhas (2026-02-23)

**Objetivo:** Permitir caracteres especiais nas senhas, alinhar validações front/back e atualizar comunicação ao usuário.

## Escopo
- Ajustar `ValidacaoUtils` e `ParticipanteAction` para aceitar senhas de 8 a 64 caracteres com qualquer caractere imprimível (exceto whitespace de controle), mantendo bloqueio a HTML.
- Atualizar mensagens/tooltips no front e documentação correspondente.
- Garantir que o fluxo com BCrypt continue funcionando após a mudança.

## Etapas
1. Revisar validações existentes (`ValidacaoUtils.isSenhaValida`, regras em `ParticipanteAction`) e definir regex aceita.
2. Implementar novas regras, unificando faixas de tamanho e remoção de restrições a caracteres especiais.
3. Atualizar texto da interface (tooltip/login, mensagens de erro) e documentação relevante.
4. Testar cadastro/login com diferentes senhas (incluindo caracteres especiais) e registrar log.

## Entregáveis
- Código ajustado para aceitar caracteres especiais em senha.
- Mensagens/documentação atualizadas.
- Log de sessão reportando testes e validações.
