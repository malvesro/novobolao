# Plano de Estabilização e Melhoria: Edição Administrativa de Jogos (Fase 8.2)

## 1. Contexto e Diagnóstico
A funcionalidade de edição estrutural de jogos (times, local, data, hora) via HTMX apresenta falhas de acionamento ("nada acontece") e instabilidade visual. O objetivo é restaurar a operabilidade seguindo os princípios **Meikai** (Intuitivo) e **Yukai** (Agradável).

## 2. Estratégia de Implementação: Edição Inline Robusta
Manteremos a abordagem de substituição de linha (`outerHTML` de `<tr>`), mas corrigindo a integridade técnica:
- **Alvo Preciso:** Uso de IDs únicos (`id="jogo-row-${id}"`) para evitar ambiguidades no DOM.
- **Remoção de Ruído:** Eliminação de scripts de debug (`alert`) e estilos inline que conflitam com o comportamento do HTMX.
- **Tratamento de Erros:** Respostas HTTP semânticas (400/500) e feedback visual de erro.
- **Feedback de Estado:** Uso de `hx-indicator` para mostrar progresso e classes CSS para destacar a linha ativa.

## 3. Detalhamento das Iterações (Subtarefas)

### Iteração 1: Preparação e Auditoria de Backend
- Revisar `AdminAction.java` e `JogoService` para garantir que exceções (ex: `NoSuchElementException`) não quebrem a resposta HTMX sem contexto.
- Validar o fluxo de interceptores para garantir que o `skipTemplate` está sendo aplicado consistentemente.

### Iteração 2: Refatoração da Visualização (admin-match-row.jsp)
- Limpar o markup do botão de edição (remover `onclick` e `z-index`).
- Estabilizar o ID da `<tr>` e garantir que o `hx-target` aponte para o seletor correto.

### Iteração 3: Refatoração do Formulário de Edição (admin-match-edit-row.jsp)
- Ajustar o formulário para submissão via `hx-post`.
- Garantir que o botão "Cancelar" dispare um `hx-get` que restaura a linha original com precisão.
- Incluir o token CSRF de forma explícita se necessário.

### Iteração 4: Estilo e Feedback Visual
- Adicionar no `estilo.css` classes para `match-row--editing` (destaque visual).
- Implementar um indicador de carregamento (spinner) dentro da própria linha.

### Iteração 5: Validação e Documentação
- Testar o ciclo completo de edição e erro.
- Atualizar logs de sessão e versão do sistema.

## 4. Auto-Análise de Risco
- **Risco:** Baixo. As alterações são focadas em markup e lógica de Action administrativa.
- **Impacto:** Alto para a produtividade do administrador do sistema.

---
*Assinado: Arquiteto de Software Sênior (Time Mercúrio)*
