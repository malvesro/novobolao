# Plano Estratégico: UX + Performance das Telas de Palpites e Resultados

**Data:** 16/06/2026
**Autor:** Codex (Arquiteto de Software Sênior - Time Mercúrio)
**Domínio de negócio:** Bolão da Copa 2026 (registro de palpites de participantes + atualização administrativa de resultados)
**Tipo de sistema:** Monólito Java (Struts 7 + Spring 6 + JSP + HTMX + Vite)
**Público-alvo:**
- Participante final (fluxo de palpites com baixa fricção e alta confiança)
- Operador/Admin (fluxo de atualização de resultados com agilidade e baixa taxa de erro)

## 1) Contexto e objetivo

Evoluir a experiência das telas de palpites (`/seguro/palpites.action`) e resultados administrativos (`/admin/jogos.action`) para aumentar:

1. **Confiabilidade percebida:** usuário saber com clareza que seu dado foi salvo.
2. **Prevenção de perda de dados:** reduzir abandono com alterações não persistidas.
3. **Velocidade operacional:** menos passos por jogo e menos retrabalho no admin.
4. **Legibilidade e acessibilidade:** feedback compreensível por teclado/leitor de tela.
5. **Performance em HF Spaces + Aiven:** menos round-trip desnecessário e menos renderização custosa.

## 2) Diagnóstico atual (arquitetura + UX)

### 2.1 Fluxo de palpites (participante)

Pontos positivos já existentes:
- Há autosave com debounce no `jogos.js` e trigger HTMX de atualização da barra de progresso.
- Existe feedback de sucesso/erro no fragmento (`palpite-cell-response.jspf`) com `aria-live`.
- Há bloqueio por janela de tempo e regra de autorização consolidada no backend.

Lacunas observadas:
- Feedback ainda é **local à célula** e pode passar despercebido em listas longas (falta confirmação global discreta).
- Não há **proteção explícita ao sair da página** quando há alterações pendentes (dirty state real por linha).
- Existe dívida de consistência entre fragmentos legados e fluxo direct-inline (risco de regressão de UX).
- Falta padronização de microcopy e estados de loading/sucesso/erro em todos os caminhos (manual e autosave).

### 2.2 Fluxo de resultados (admin)

Pontos positivos já existentes:
- Edição inline com HTMX por linha (`admin-match-row.jsp`) para data/hora/local/equipes/placar.
- Re-render da linha após atualização com contrato estável no backend (`AdminAction`).

Lacunas observadas:
- Não há confirmação visual clara por linha com timestamp de "salvo" para o operador.
- Em caso de erro, feedback pode ficar implícito (status HTTP) e pouco orientado para recuperação.
- Falta estado agregado da sessão de edição (ex.: quantos jogos alterados com sucesso/falha).

### 2.3 Performance e robustez

Riscos percebidos:
- Muitas requisições em sequência quando há edição rápida de vários jogos/palpites.
- Ausência de estratégia de deduplicação por payload idêntico em alguns cenários de autosave.
- Dependência de vários swaps em lista extensa pode gerar custo visual (reflow/repaint) em dispositivos mais modestos.

## 3) Princípios de solução (Yukai, Meikai, Tsukai)

- **Yukai (agradável):** feedback visual elegante, discreto e consistente (sem poluição de tela).
- **Meikai (intuitivo):** estados de gravação autoexplicativos ("Editando", "Salvando", "Salvo às HH:mm", "Erro - tentar novamente").
- **Tsukai (emocionante ao operar):** sensação de fluidez e domínio, com interação rápida e previsível.

## 4) Proposta estratégica de evolução (priorizada)

## P0 (alto impacto imediato)

1. **Feedback visual unificado de gravação (participante + admin)**
- Introduzir confirmação por célula/linha com timestamp e estado semântico.
- Acrescentar uma confirmação global não intrusiva (toast de sessão) para lotes de alterações.
- Garantir `aria-live="polite"` e texto equivalente (não depender de cor/ícone).

2. **Guard de saída com alterações não salvas (dirty-state real)**
- Rastrear campos editados e pendências de request por jogo.
- Exibir confirmação ao sair/recarregar quando existir alteração pendente.
- Remover alerta quando houver sucesso de persistência.

3. **Estados transacionais claros e resilientes**
- Padronizar estados: `idle`, `dirty`, `saving`, `saved`, `error`, `locked`.
- Em erro, oferecer ação explícita de recuperação (repetir envio da linha/célula).

4. **Telemetria funcional mínima para operações críticas**
- Log estruturado por tentativa/sucesso/erro de palpite e resultado (sem dados sensíveis).
- Correlation id simples por request HTMX para troubleshooting.

## P1 (clareza operacional e produtividade)

5. **Barra de progresso de conclusão por contexto**
- Participante: progresso por recorte ativo e progresso global (quando aplicável).
- Admin: indicador de pendência operacional (jogos sem placar no recorte atual).

6. **Modo revisão rápida (admin)**
- Navegação por teclado entre campos de placar (setas/Enter) e feedback persistente de linha salva.
- Redução de cliques na operação de fechamento de rodada.

7. **Persistência de estado da tela**
- Manter filtros/expansões/contexto após troca de data, reload parcial ou retorno de erro.

## P2 (otimização de experiência avançada)

8. **Batch save opcional para admin (com feature flag)**
- Acumular múltiplas edições e persistir em lote quando apropriado.
- Fallback automático para modo linha-a-linha em falha.

9. **Métricas UX/Performance orientadas a decisão**
- Tempo médio entre edição e confirmação de salvamento.
- Taxa de erro de persistência por sessão.
- Percentual de abandono com alterações pendentes.

## 5) Critérios de aceite recomendados

1. Participante identifica claramente que o palpite foi salvo em até 1 segundo após resposta do backend.
2. Navegação para fora da tela com alterações pendentes exibe confirmação em 100% dos cenários cobertos.
3. Admin recebe confirmação visual por linha salva sem perder contexto da tabela.
4. Erros de gravação sempre apresentam mensagem acionável (com opção de retry).
5. Sem regressão de segurança (CSRF/CSP), sem scripts inline novos e sem quebra do contrato Action -> Service -> DAO.

## 6) Riscos e mitigação

- **Risco:** alerta de saída excessivo (fadiga).
  - **Mitigação:** só ativar quando `dirty=true` e sem request em sucesso finalizado.
- **Risco:** aumento de complexidade no `jogos.js`.
  - **Mitigação:** modularizar por responsabilidade (`dirty-state`, `feedback`, `admin-results`) e cobrir com testes.
- **Risco:** duplicidade de fluxo entre fragmentos antigos e novos.
  - **Mitigação:** mapear e descontinuar gradualmente fragmentos legados com checklist de remoção.

## 7) Validação técnica prevista

- Build frontend: `npm run build`
- Regressão backend: `mvn -Dfrontend.skip=true test`
- Smoke funcional:
  - Participante: salvar/editar palpite múltiplas vezes + sair com edição pendente.
  - Admin: atualizar resultados em sequência + validar feedback por linha.

## 8) Artefatos de rastreabilidade

- Tarefa nova no `passo-a-passo.md` (proposta: Tarefa 59).
- Log de sessão dedicado em `.ia/logs/` para cada iteração executada.
- Sugestão de ADR (se aprovado na execução): `ADR-20260616-ux-palpites-resultados-confiabilidade.md`.

`Auto-Analise: [Risco: Médio] | [Compatibilidade: OK] | [Veredito: Aprovado]`
