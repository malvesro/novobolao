# Plano de Migração DWR → HTMX/REST

**Data:** 2026-02-19  
**Responsável:** Assistente Técnico Líder (Time Mercúrio)  
**Relacionamento:** Fase 2.5 – Auditoria e Ajuste do Frontend (Tarefa 2)  
**Referência:** `.ia/logs/session-20260219-inventario-scripts-fase-2-5.md`

---

## 1. Contexto e Objetivos

- **Situação atual:** A aplicação consome serviços remotos via DWR 2.0 (arquivos `engine.js`, `util.js`) com chamadas RPC dinamicamente expostas (`AdminAction`, `ParticipanteAction`). O frontend também depende de Prototype/Scriptaculous.
- **Problemas identificados:**
  - Superfície de ataque elevada (RPC direto com pouca validação, histórico de CVEs).
  - Incompatibilidade com Content-Security-Policy rígida (uso extenso de scripts inline e `eval`).
  - Custo de manutenção alto e falta de compatibilidade com navegadores modernos.
- **Objetivo:** Reescrever gradualmente os fluxos que utilizam DWR para endpoints REST/JSON servidos pelo Struts 6 (ou controllers Spring) e consumidos via HTMX/fetch, eliminando dependências de Prototype e DWR.

---

## 2. Inventário de Fluxos DWR Prioritários

| Fluxo | JSP/Arquivo | Serviço DWR | Descrição resumida |
|-------|-------------|-------------|--------------------|
| Palpites de Jogos | `webapp/seguro/jogos.jsp` | `ParticipanteAction` (métodos `buscarPalpite`, `salvarPalpite`, `listarPalpites`) | CRUD de palpites em tempo real, atualização de tabelas e mensagens. |
| Administração de Participantes | `webapp/admin/participantes.jsp` | `AdminAction` (`alterarStatus`, `excluirParticipante`, `atualizarPapel`) | Ações administrativas (aprovação, exclusão, alteração de papéis). |
| Inclusão de Jogos | `webapp/admin/inclusaoJogo.jsp` | `AdminAction` (`buscarEquipes`, `criarJogo`) | Auto-complete e submissão dynamic da tela de jogos (uso menor). |
| Cadastro Público | `webapp/cadastro.jsp` | `DWRUtil` para feedback instantâneo (ex.: mensagens de ajuda) | Baixa criticidade; pode ser revista para HTML5/HTMX. |

---

## 3. Estratégia de Migração

1. **Camada de Serviço/Controller**
   - Mapear métodos existentes nos services Spring (`ParticipanteService`, `JogoService`) garantindo que regras de negócio permanecem centralizadas.
   - Criar Actions Struts específicas ou endpoints REST (JSON) para cada operação hoje exposta via DWR.
   - Normalizar respostas em JSON (payload consistente: `status`, `data`, `mensagem`).
   - Aplicar segurança Spring Security (roles) e validações adicionais antes de expor endpoints.

2. **Camada Frontend**
   - Substituir chamadas `DWRUtil` por requisições HTMX (`hx-post`, `hx-get`) ou `fetch` (casos onde atualização parcial complexa).
   - Reescrever templates das tabelas e feedbacks em fragmentos JSP/HTMX (ex.: `<div hx-target="...">`).
   - Migrar manipulação de DOM atualmente feita com Prototype para Vanilla JS ou utilitários HTMX (ex.: `hx-trigger`, `hx-swap`).

3. **Remoção Gradual das Dependências**
   - Após cada fluxo migrado, remover referências específicas a DWR no JSP correspondente.
   - Ao finalizar todos os fluxos, excluir `dwr.xml`, `web.xml` mapping, bibliotecas `engine.js`/`util.js` e a dependência DWR do `pom.xml`.
   - Revisar `cabecalho.jspf` para retirar Prototype/Scriptaculous e reorganizar carregamento de scripts.

4. **Segurança e Observabilidade**
   - Adicionar logging estruturado das chamadas REST (SLF4J) e validação dos parâmetros de entrada.
   - Implementar proteção CSRF (Spring Security) quando formulários passarem de DWR para HTMX.
   - Garantir retorno apropriado de códigos HTTP (200/400/403) para integração com testes automatizados.

5. **Testes**
   - Cobertura unitária e de integração para Actions REST (JUnit + MockMvc/Struts TestCase).
   - Testes web (Selenium/Playwright) para validar o comportamento dinâmico após troca para HTMX.
   - Atualizar suíte de testes existente (`ParticipanteActionTest`, `GraficosJFreeChartTest`) para considerar novas rotas.

---

## 4. Cronograma Sugerido (Sprint-level)

| Sprint | Entregas principais |
|--------|---------------------|
| **S1** | Criar endpoints REST para `ParticipanteAction` (listar/buscar/salvar palpite), ajustar JSP `jogos.jsp` para HTMX. |
| **S2** | Migrar ações administrativas (`participantes.jsp`) para HTMX/fetch + REST, remover dependências Prototype nessas telas. |
| **S3** | Ajustar inclusão de jogos e cadastro, consolidar remoção de DWR (limpar `web.xml`, `pom.xml`). Introduzir bundler (Vite/ESBuild). |
| **S4** | Revisar CSP, ativar `strict-dynamic`, finalizar migração de tooltips e aplicar testes cross-browser. |

---

## 5. Riscos e Mitigações

- **Impacto em funcionalidades críticas (palpites/admin):** Mitigar com testes regressivos e ambientes homologação.
- **Tempo de migração:** Priorizar fluxos de maior uso; manter fallback DWR temporariamente com feature flags (apenas se necessário).
- **Compatibilidade de sessão/autenticação:** Garantir que endpoints REST reutilizem filtros Spring Security e mantenham `JSESSIONID`.
- **Debate de performance:** Monitorar novos endpoints via logs e, se necessário, aplicar caching nos serviços.

---

## 6. Próximos Passos

1. Planejar endpoints REST detalhados (assinaturas, payload) para `ParticipanteAction`.
2. Definir padrão de respostas JSON e mensagens de erro globais.
3. Preparar POC HTMX em `jogos.jsp` substituindo `DWRUtil.addRows` por fragmentos HTML.
4. Atualizar `passo-a-passo.md` com subtarefas específicas e criar logs por etapa executada.
5. Após cada migração, revisar dependências no `pom.xml` e remover bibliotecas legadas.

---

**Auto-Analise:** [Risco: Médio] | [Compatibilidade: Atenção] | [Veredito: Revisar]  
**Log relacionado:** `.ia/logs/session-20260219-plano-migracao-dwr-htmx.md`
