# Plano Fase 6 — Adequação do Sistema Bolão para a Copa do Mundo 2026

## 1. Contexto e Objetivos
- **Domínio:** bolão de futebol voltado à Copa do Mundo FIFA 2026, público-alvo torcedores brasileiros.
- **Cenário 2026:** torneio expandido para 48 seleções, 12 grupos (A–L) e fase eliminatória iniciando nos **32-avos de final**.
- **Objetivo geral:** garantir que o sistema aceite o calendário completo da Copa 2026 (dados, regras de pontuação e experiência do usuário) sem quebrar fluxos existentes.

## 2. Premissas e Restrições
1. Manter compatibilidade com o stack atual (Spring 6 + Struts 7 + Hibernate 6 + JSP/HTMX).
2. Dados oficiais ainda possuem placeholders (playoffs UEFA e intercontinentais). Atualizações devem ser incrementais e versionadas.
3. Build Maven com `frontend.skip=true` permanece padrão enquanto o pipeline Vite não for obrigatório.
4. Evitar regressões na base de usuários legada; migrações de schema precisam de scripts reversíveis.

## 3. Entregáveis Principais
- Dataset oficial em CSV + script SQL (`03-copa-2026-data.sql`) com 48 seleções, 12 grupos e fases eliminatórias.
- Ajustes de domínio (entidades/DAOs/serviços) para suportar:
  - novos grupos (A–L),
  - fase de 32-avos,
  - ampliação das regras de pontuação e filtros.
- Atualizações de UI/UX para cadastro de palpites, classificação e dashboards (incluindo nova identidade visual).
- Documentação e testes cobrindo os novos fluxos.

## 4. Plano de Trabalho (Tarefas e Subtarefas)
Status inicial: `Pendente` para todas as atividades.

### 4.1. Dados e Scripts (F6-T2-Dados)
1. **Normalizar dataset interno** (`data/copa2026_tabela_brt.csv`) com estrutura definitiva e colunas auxiliares (grupo, rodada, `fase_codigo`, `fase_ordem`, slots de placeholders) conforme `.ia/documentacao/fase6-normalizacao-dataset.md`.  
2. **Gerar script SQL** `03-copa-2026-data.sql` com carga completa dos times, jogos, horários e estádios.  
3. **Automatizar atualização pós-playoffs:** criar script/python para substituir placeholders (UEFA/Intercontinental) assim que os classificados forem divulgados.  
4. **Documentar processo** na seção 4.8 do `README-migracao.md`.

### 4.2. Domínio e Regras (F6-T2-Dominio)
1. **Inventariar o modelo atual** (entidades, mapeamentos Hibernate, scripts SQL) para avaliar se suporta 48 seleções e a fase de 32-avos sem alterações estruturais. *(Concluído em 21/02/2026 — ver `.ia/documentacao/fase6-inventario-modelo-dados.md`)*  
2. **Modelar nova fase de 32-avos** na lógica de brackets (services `JogoService`, `PalpiteService`, regras de pontuação).  
3. **Ampliar entidades** (`Jogo`, `Grupo`, `Fase`) para suportar 12 grupos e novo mapeamento de chaves.  
4. **Atualizar validações** de cadastro de palpites (cutoff por horário, limites de fase ampliados).  
5. **Criar testes** unitários/integrados cobrindo os fluxos de cadastro e cálculo de pontos para fases expandidas.

### 4.3. UI/UX e Experience (F6-T2-Frontend)
1. **Reorganizar telas de grupos e classificação** para listar 12 grupos com paginação ou grid responsivo.  
2. **Atualizar filtros e dashboards** da área segura (`seguro/principal.jsp`, `seguro/classificacao.jsp`, `seguro/jogos.jsp`) para incluir fase de 32-avos e etapas subsequentes.  
3. **Projetar nova identidade visual** baseada na inspiração solicitada (nova imagem para `bolao_logo.png` respeitando proporções).  
4. **Validar experiência mobile** com o aumento do volume de dados (scroll, dialog, tooltips).  
5. **Documentar guidelines visuais** em `.ia/diretrizes/frontend.md` (nova seção Copa 2026).

### 4.4. Integração e Observabilidade (F6-T2-Integracao)
1. **Atualizar APIs/exports** (se existentes) para refletir nova estrutura de fases e grupos.  
2. **Monitorar performance** das queries ampliadas (ranking, dashboard) e ajustar índices quando necessário.  
3. **Revisar logs/auditoria** para garantir rastreamento de novos eventos de palpite e jogos.

### 4.5. QA e Implantação (F6-T2-QA)
1. **Plano de testes** cobrindo cenários de cadastro, alteração e fechamento de palpites em todas as fases.  
2. **Testes de regressão automatizados** (JUnit/HTMX E2E onde possível).  
3. **Checklist de implantação** com ordem de execução (migrations, deploy, limpeza de cache, rebuild dos bundles).  
4. **Comunicação ao usuário final** (release notes) destacando as mudanças da Copa 2026.

## 5. Dependências e Sequência Recomendada
1. **Dados** → base para ajustes de domínio.  
2. **Domínio/Regras** → necessário antes de atualizar UI.  
3. **UI/UX** → depende das APIs/serviços prontos.  
4. **Integração/Observabilidade** → roda em paralelo após domínio ajustado.  
5. **QA/Implantação** → etapa final antes do go-live.

## 6. Riscos e Mitigações
- **Mudanças de calendário pela FIFA:** manter automação de atualização e versionamento do dataset.  
- **Aumento de carga no banco:** avaliar índices e caching após incluir Round of 32.  
- **Complexidade de UI:** adotar componentes responsivos e testes manuais antecipados para evitar regressões.  
- **Prazo curto para playoffs (março/2026):** preparar scripts parametrizados para incorporar resultados rapidamente.

## 7. Próximos Passos
1. Aprovar este plano com o time/usuário.  
2. Converter itens das seções 4.1 a 4.5 em tarefas no `passo-a-passo.md` com status e responsáveis.  
3. Iniciar execução pela trilha de dados (F6-T2-Dados) após aprovação.
