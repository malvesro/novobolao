# Plano: Enriquecimento Arquitetural do README-migracao.md

**Contexto:** consolidar a visão arquitetural do Sistema Bolão no README-migracao.md, facilitando alinhamento com stakeholders técnicos e documentação das decisões mais recentes (HTMX, filtros de segurança, observabilidade).

---

## Objetivo Geral
Atualizar o README-migracao.md com seções arquiteturais detalhadas, incluindo diagramas Mermaid e referências às evidências existentes (logs, planos, passo-a-passo), reforçando rastreabilidade e visão sistêmica.

---

## Etapas Planejadas

1. **Contexto de Componentes Internos**
   - Inventariar camadas principais (Struts Actions, Services, DAOs, Quartz, Security, Frontend HTMX).
   - Adicionar diagrama Mermaid de componentes destacando dependências.
   - **Status:** Concluído em 04/03/2026 (README-migracao.md §4.7, log `.ia/logs/session-20260304-readme-migracao-arquitetura.md`).

2. **Fluxo de Requisição End-to-End**
   - Descrever jornada típica de uma requisição (público e autenticado).
   - Inserir sequence diagram Mermaid evidenciando filtros (Security, RequestContextFilter) e Actions.
   - **Status:** Concluído em 04/03/2026 (README-migracao.md §4.8, log `.ia/logs/session-20260304-readme-migracao-arquitetura.md`).

3. **Tratamento de Segurança Multicamadas**
   - Consolidar medidas (Fetch Metadata, OGNL allowlist, `@StrutsParameter`, CSP/CSRF, sanitização).
   - Incluir tabela/diagrama mostrando em que camada cada defesa atua.
   - **Status:** Concluído em 04/03/2026 (README-migracao.md §4.9, log `.ia/logs/session-20260304-readme-migracao-arquitetura.md`).

4. **Arquitetura HTMX e Renderização Parcial**
   - Documentar o ciclo `skipTemplate`, fragmentos `.jspf`, bundle Vite e logs `[HTMX]`.
  - Adicionar diagrama (state/flow) com transições entre página completa e fragmento.
   - **Status:** Concluído em 04/03/2026 (README-migracao.md §4.10, log `.ia/logs/session-20260304-readme-migracao-arquitetura.md`).

5. **Observabilidade e Logging**
   - Mapear pipeline (SLF4J → Logback) e marcadores de log (`[HTMX][PREP]`, `[SEC][HTMX]`, etc.).
   - Registrar próximos passos para integração com observabilidade corporativa.
   - **Status:** Concluído em 04/03/2026 (README-migracao.md §4.11, roadmap §4.14).

6. **Jobs Quartz e Processos Assíncronos**
   - Catalogar jobs ativos e dependências (Angus Mail, Services).
   - Inserir flowchart Mermaid com trigger → service → efeitos.
   - **Status:** Concluído em 04/03/2026 (README-migracao.md §4.12).

7. **Configuração e Deployment**
   - Explicar gestão de parâmetros (variáveis de ambiente, arquivos externos, Docker).
   - Adicionar tabela com parâmetros críticos (DB, SMTP, CSP) e defaults.
   - **Status:** Concluído em 04/03/2026 (README-migracao.md §4.13).

8. **Roadmap Arquitetural**
   - Registrar próximos upgrades (Quartz 2.5.x, Angus 2.0.4, CSP enforcement, observabilidade).
   - Vincular cada item a tarefas abertas no `passo-a-passo.md`/planos existentes.
   - **Status:** Concluído em 04/03/2026 (README-migracao.md §4.14, passo-a-passo.md item 16).

---

## Referências
- `README-migracao.md`
- `.ia/logs/session-20260303-filtros-principal.md`
- `.ia/planos/plano-correcao-palpites-popup.md`
- `passo-a-passo.md`
- `.ia/diretrizes/*.md`

---

## Observações
- A inclusão deste conteúdo pode requerer nova tarefa no `passo-a-passo.md`. Aguardar aprovação do usuário sobre onde posicioná-la.
- Diagramas Mermaid devem ser validados para renderização correta (uso de delimitadores ```mermaid).
