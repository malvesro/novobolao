# README de Migração – Sistema Bolão

Documento executivo e técnico que consolida o estado da modernização do Sistema Bolão em **23/02/2026**. Voltado a stakeholders de negócio (organização do bolão Copa 2026), liderança técnica e Time Mercúrio (desenvolvimento, QA e operações).

---

## 1. Resumo Executivo
- **Objetivo do produto:** plataforma corporativa de bolão para a Copa do Mundo FIFA 2026, preservando fluxos clássicos (cadastro, palpites, ranking, gráficos) enquanto moderniza a stack Java 2006 para tecnologias suportadas em 2026.
- **Arquitetura atual:** monólito Java 17 empacotado em WAR, executando em Tomcat 10.1+/11 com Spring 6.1.14, Struts 7.1.1, Hibernate 6.4.4.Final e frontend JSP + HTMX empacotado via Vite.
- **Estado da modernização:** fases de segurança/back-end concluídas; frontend legado higienizado; pendências críticas concentram-se na remediação de CVEs (Angus Mail 2.0.3 e Quartz 2.3.2) e na finalização do dataset oficial da Copa 2026.
- **Situação operacional:** build Maven com testes JUnit 5 estável; pipeline Node/Vite funcional; container Docker (Tomcat + MySQL 8) validado com TLS self-signed; tarefas de documentação e segurança mantêm rastreabilidade no `passo-a-passo.md`.

---

## 2. Domínio e Jornadas de Usuário

### 2.1 Personas e Papéis
- **Participante (ROLE_USER):** se cadastra, registra palpites, acompanha ranking e gráficos pessoais.
- **Administrador (ROLE_ADMIN):** prepara tabela de jogos, atualiza resultados, gerencia papéis dos participantes, libera fases e monitora estatísticas.
- **Operações/Infra:** garante disponibilidade do ambiente Tomcat, execução dos agendamentos Quartz e entrega de notificações por e-mail (Angus Mail).
- **Analista de Produto:** acompanha métricas de engajamento e status da migração para planejar releases conforme calendário FIFA.

### 2.2 Fluxos Essenciais
1. **Onboarding:** cadastro público (`cadastro.jsp`) e ativação automática (com RBAC e sanitização Spring Security 6).  
2. **Registro de palpites:** participantes informam placares por jogo; bloqueio ocorre quando `Jogo.jaOcorreu()` retorna verdadeiro (respeitando horário BRT).  
3. **Ranking e dashboards:** serviços `ParticipanteService` e `RankingService` consolidam pontuação, alimentando gráficos (JFreeChart) e listas Struts.  
4. **Administração:** telas `/admin/*.action` permitem criar/julgar partidas, ajustar participantes e reexecutar cálculos; interações assíncronas usam HTMX + endpoints Struts; uploads utilizam `commons-fileupload2`.  
5. **Notificações e jobs:** Quartz agenda processos (ex.: lembretes de jogos, consolidação diária) e Angus Mail envia e-mails SMTP.  
   - **Configuração SMTP moderna:** via `EmailConfiguration` é possível sobrepor `mail.smtp.*` com arquivo externo (`bolao.email.config` ou variável `BOLAO_EMAIL_CONFIG`) e/ou variáveis de ambiente (`SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `SMTP_TLS`, `SMTP_SSL`, `SMTP_SSL_TRUST`).  
   - **Docker Compose:** o serviço `app` aceita as variáveis acima; ajustar `SMTP_AUTH=true` para habilitar autenticação e utilizar TLS (`SMTP_TLS=true`) ou SMTPS (`SMTP_SSL=true`) conforme o servidor corporativo.  
   - **Timeouts padrão:** `mail.smtp.connectiontimeout`, `mail.smtp.timeout` e `mail.smtp.writetimeout` ficam em 10s e podem ser sobrepostos sem rebuild.  
6. **Chat legacy:** recurso histórico foi desativado durante a migração; existe ADR para futura reimplementação com tecnologias modernas.

### 2.3 Regras de Pontuação
Implementadas em `Palpite#getPontuacao()`:
- **6 pontos** — palpite acerta placar exato (gols das duas equipes).  
- **3 pontos** — acerta resultado (vitória/empate) + número de gols de uma equipe (bônus).  
- **2 pontos** — acerta apenas o resultado (sem bônus).  
- **1 ponto** — acerta somente os gols de uma das equipes.  
- **0 pontos** — demais cenários.  
Os contadores (`DadosClassificacao`) alimentam rankings, gráficos temporais e estatísticas por categoria de acerto.

### 2.4 Dependências Externas de Negócio
- **Calendário FIFA 2026:** datas (11/06–19/07), sedes e formato ampliado (48 seleções, fases 32-avos → final).  
- **Repositório corporativo Maven (nx-mvn.tse.jus.br):** controla disponibilidade de versões seguras (Struts, Spring, Angus, Quartz).  
- **Infraestrutura de e-mail corporativa:** exige atualização de Angus Mail para 2.0.4 assim que liberada.

---

## 3. Contexto Copa do Mundo FIFA 2026 e Gestão de Dados

### 3.1 Formato do Torneio
- **Fase de grupos:** 12 grupos (A–L) com 4 seleções cada.  
- **Eliminatórias:** 32-avos, 16-avos, oitavas, quartas, semifinal, disputa de 3º lugar e final (total 104 partidas).  
- **Locais oficiais:** 16 cidades entre Canadá, México e EUA (configuradas em `web.xml`).  
- **Fuso horário de referência:** horário oficial de Brasília (BRT), com normalização automática no dataset.

### 3.2 Pipeline de Dados
1. **Fonte primária:** planilhas CSV normalizadas (`data/copa2026_tabela_brt_normalizado.csv`).  
2. **Transformação:** script `scripts/atualizar_copa2026_dataset.py` gera SQL (`data/sql/03-copa-2026-data.sql`) e CSV final, aplicando placeholders e ids sequenciais.  
3. **Carga:** executada via MySQL (`mysql --default-character-set=utf8mb4`) ou scripts de inicialização Docker (`docker/mysql/init`).  
4. **Placeholders:** `data/copa2026_placeholders.json` registra seleções pendentes (repescagens). O script substitui nome, grupo e slot assim que confirmados.  
5. **Assets de bandeiras:** `scripts/download_flags.py` e `download_missing_flags.py` garantem PNGs atualizados em `webapp/img/bandeiras/` (UTF-8).  
6. **Auditoria de dados:** logs dedicados (`.ia/logs/session-20260222-*.md`) descrevem cargas, validações SQL e smoke tests.

### 3.3 Pendências de Dados
- Atualização final do SQL após definição oficial dos playoffs.  
- Revisão das descrições das fases e horários definitivos fornecidos pela FIFA.  
- Inclusão de métricas adicionais (ocupação de estádios, público) caso aprovadas para fases futuras.

---

## 4. Arquitetura e Stack

### 4.1 Visão de Camadas
```
JSP/HTMX (UI) → Struts Actions → Serviços Spring → DAOs Hibernate → MySQL
                                  ↓
                             Quartz Jobs
                                  ↓
                              Angus Mail
```
- **UI:** JSP modularizados em `webapp/WEB-INF/content/` com fragments compartilhados (`cabecalho.jspf`, `rodape.jspf`).  
- **Actions Struts:** controlam roteamento `.action`, validam entrada (`@StrutsParameter`) e orquestram serviços.  
- **Serviços Spring:** implementados em `src/com/opendev/bolao/service/impl`, encapsulam regras de negócio e transações.  
- **Persistência:** Hibernate 6 usa mapeamentos XML (HBM) migrados para Jakarta (`jakarta.persistence`).  
- **Infra auxiliar:** Quartz agenda rotinas; Angus Mail envia notificações; Logback padroniza logs.

### 4.2 Módulos e Responsabilidades
| Módulo / Pacote | Responsabilidade | Notas |
|-----------------|------------------|-------|
| `com.opendev.bolao.action` | Actions Struts (participante, admin, relatórios) | Usa `@StrutsParameter` e interceptores HTMX |
| `com.opendev.bolao.service` | Lógica de negócio, cálculos de ranking, notificações | Métodos com segurança declarativa (Spring Security) |
| `com.opendev.bolao.dao` | Acesso a dados via Hibernate 6 | SessionFactory atualizada (`getCurrentSession()`) |
| `com.opendev.bolao.model` | Entidades de domínio (`Participante`, `Jogo`, `Palpite`) | Mantém HBM por compatibilidade |
| `src/frontend/` | Módulos JS (tooltips, telas HTMX) | Empacotamento Vite com manifest/fallback |
| `scripts/` | Automação de dados, auditoria (axe), download de assets | Rodar com Python 3.10+ |

### 4.3 Stack Tecnológica (Versões Chave)
| Camada | Versão | Observações |
|--------|--------|-------------|
| Java / Compiler | 17 | `maven.compiler.source/target` |
| Spring Framework | 6.1.14 | BOM importado no `pom.xml` |
| Spring Security | 6.2.2 | CSRF + HSTS/HPP |
| Struts | 7.1.1 | `commons-fileupload2` 2.0.0-M4 |
| Hibernate ORM | 6.4.4.Final | Compatível Jakarta EE 10 |
| Database | MySQL 8.0/8.3 driver | HikariCP 5.1.0 |
| Frontend | JSP + HTMX 1.9+, Vite 5 | Bundles com hash e fallback |
| Logging | SLF4J 2.0.12 + Logback 1.5.0 | Config custom em `logback.xml` |
| Testes | JUnit 5.10, Mockito 5.10, AssertJ 3.25 | Suites unitárias e mocks |

### 4.4 Integrações e Jobs
- **Quartz (`org.quartz-scheduler:quartz` 2.3.2):** roda notificações e tarefas de housekeeping; pendente upgrade para 2.5.2.  
- **Angus Mail 2.0.3:** envio SMTP (cadastro, lembretes); aguardando 2.0.4 no repositório interno.  
- **Vite/Node:** `frontend-maven-plugin` instala Node 20.11.1 em builds Maven completos.  
- **Scripts Python:** dependem de `pandas`/`python-dateutil` conforme requirements privados (ver comentários em scripts).  
- **Dependency-Check:** plugin OWASP 12.1.0 configurado com `failBuildOnCVSS=7` e dataDir local.

### 4.5 Topologia de Deploy
- **Runtime padrão:** Tomcat 10.1 (Docker distroless custom) com TLS (keystore auto-gerado).  
- **Portas:** 8080 (HTTP) e 8443 (HTTPS).  
- **Secrets:** injetados via variáveis (`DB_HOST`, `DB_USER`, `DB_PASS`) ou `applicationContext-resources.xml` parametrizado.  
- **Docker Compose:** orquestra `app` (Tomcat) e `db` (MySQL 8) com health checks e volume persistente (`db_data`).  
- **CI/Build offline:** `frontend.skip=true` permite builds sem Node; fallback `webapp/assets/js/app-bundle.js` mantido atualizado.

---

## 5. Experiência de Desenvolvimento

### 5.1 Pré-requisitos
- JDK 17+, Maven 3.9+, Node.js 20.11.1 (ou usar plugin Maven), npm 10+.  
- Python 3.10+ (scripts de dados).  
- Docker/Docker Compose opcionais para testes integrados.  
- Acesso ao repositório Maven corporativo (`nx-mvn.tse.jus.br`) configurado em `~/.m2/settings.xml`.

### 5.2 Setup Rápido
```bash
npm install                    # instala dependências front
npm run build                  # gera manifest Vite
mvn -Dfrontend.skip=true test  # build rápido com testes Java
mvn package -Dfrontend.skip=false  # build completo (Node + tests)
```

### 5.3 Frontend e Bundler
- Código-fonte em `src/frontend/` (ES Modules).  
- Vite emite bundles com hash + fallback (`webapp/assets/js/app-bundle.js`).  
- `webapp/template/cabecalho.jspf` consulta `manifest.json`; fallback acionado se o manifest não existir.  
- Interações assíncronas usam HTMX (`hx-get`, `hx-post`) com headers CSRF centralizados em script utilitário.  
- Diretrizes de CSS/HTML em `.ia/diretrizes/frontend.md`; evitar inline script/style (preparação para CSP rígida).

### 5.4 Testes e Qualidade
- **JUnit/Mockito:** `mvn -q -Dfrontend.skip=true test`.  
- **Dependency-Check:** `mvn -Dfrontend.skip=true org.owasp:dependency-check-maven:check`.  
- **A11y (pendente de ambiente):** `scripts/run-axe-audit.sh` requer Chrome headless externo.  
- **Secret scan:** `scripts/scan-secrets.sh` (usa `rg`).  
- **Smoke manual:** `docker compose up -d app db` + validações via navegador ou `curl -k https://localhost:8443/login.jsp`.

### 5.5 Boas Práticas
- Consultar `.ia/diretrizes/` antes de propor alterações.  
- Criar ADR em `.ia/historico/` para decisões arquiteturais não triviais; promover para `docs/adr/` após aprovação.  
- Vincular commits às tarefas do `passo-a-passo.md` e atualizar logs em `.ia/logs/`.  
- Manter dataset FIFA sincronizado e documentar cada carga (logs específicos).  
- Respeitar instruções de segurança (sem reintroduzir DWR, Prototype, script inline).

---

## 6. Operação e Observabilidade

### 6.1 Configuração de Ambiente
- Variáveis obrigatórias: `MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD`, `DB_HOST`, `DB_USER`, `DB_PASS`.  
- `applicationContext-resources.xml` suporta leitura via variáveis (sem credenciais hardcoded).  
- `logback.xml` define appenders rotativos (arquivo e console); ajustar níveis conforme ambiente.  
- Para produção, substituir o keystore auto-gerado por certificado oficial e configurar HSTS em modo pleno.

### 6.2 Deploy via Docker
```bash
docker compose build app
docker compose up -d app db
docker compose logs -f app   # acompanhar startup
```
- Health check do app usa `curl http://localhost:8080/`; DB usa `mysqladmin ping`.  
- Volumes (`db_data`) preservam estado do MySQL; para reset completo usar `docker compose down -v`.  
- Configuração HTTPS reside em `/usr/local/tomcat/conf/server.xml` (ver Dockerfile).

### 6.3 Monitoramento e Logs
- Logs de aplicação: `target/logs/` no build local ou `docker compose logs app`.  
- Quartz registra execuções em `logs/bolao-scheduler.log` (configurable).  
- Recomenda-se integrar com observabilidade corporativa (Stackdriver/ELK) ao mover para produção.

### 6.4 Troubleshooting Comum
- **Falha em Dependency-Check:** confirmar que mirror corporativo disponibiliza versões seguras (Angus 2.0.4, Quartz 2.5.2).  
- **Erro 403 após login:** verificar interceptor Spring Security para `/favicon.ico` (ajustado em 22/02).  
- **Manifest ausente:** rodar `npm run build` ou garantir fallback `app-bundle.js`.  
- **Datasets inconsistentes:** reexecutar script Python com `--placeholders` atualizados e aplicar SQL.

---

## 7. Postura de Segurança
- **Transporte:** HTTPS habilitado com HSTS (`max-age=31536000`), `X-Frame-Options SAMEORIGIN`, `Referrer-Policy strict-origin-when-cross-origin`.  
- **Proteção de sessão:** Spring Security 6 com `migrateSession`, logout POST, `CookieCsrfTokenRepository`.  
- **CSP:** `default-src 'self'; script/style 'self' 'unsafe-inline'; img/font data:` — plano para remover `'unsafe-inline'` após migração total para bundles.  
- **Struts Hardening:** `@StrutsParameter`, allowlist OGNL, limites de expressão configurados, JSPs dentro de `WEB-INF/`.  
- **Sanitização:** `SanitizationUtils` aplicado a entrada crítica; validações por campo nas Actions e camada de serviço.  
- **Segredos:** lidos de variáveis de ambiente ou arquivos externos; `scan-secrets.sh` auxilia auditorias.  
- **Dependências:** monitoramento contínuo pelo OWASP Dependency-Check; CVEs abertos em Angus/Quartz registrados em `.ia/historico/ADR-20260223-aguardar-angus-quartz.md`.  
- **Próximos passos de segurança:** endurecer CSP, concluir upgrades Angus/Quartz, retomar auditoria Axe e testes cross-browser.

---

## 8. Riscos e Pendências (23/02/2026)
| Prioridade | Item | Impacto | Mitigação / Responsável |
|------------|------|---------|-------------------------|
| Alta | CVEs Angus Mail 2.0.3 / Activation 2.0.2 | Bloqueia aprovação do Dependency-Check; risco em SMTP | Aguardar 2.0.4 no mirror; Time Mercúrio monitorando |
| Alta | CVEs Quartz 2.3.2 | Jobs críticos com exposição a vulnerabilidades elevadas | Monitorar 2.5.2 no mirror; repetir upgrade assim que liberado |
| Média | Auditoria Axe + cross-browser | Conformidade WCAG e navegadores alvo pendente | Provisionar ambiente com Chrome headless; reabrir tarefas 7/8 fase 2.5 |
| Média | Dataset final playoffs | Atualização de dados oficiais pode impactar relatórios | Sincronizar com calendário FIFA; registrar cargas e validar smoke |
| Baixa | CSP rígida | Segurança adicional contra XSS depende de eliminar inline | Continuar migração de scripts e atualizar cabeçalhos gradualmente |

---

## 9. Roadmap e Próximas Ações
1. **Tarefa 15 – Remediação Dependency-Check:** concluir upgrades Angus/Quartz e repetir varredura OWASP.  
2. **Tarefa 16 – Evolução do README:** complementar seções (negócio, arquitetura, operação) e manter documento vivo conforme migrações subsequentes.  
3. **Dados Copa 2026:** atualizar SQL final quando playoffs forem publicados e registrar evidências.  
4. **Auditoria de segurança e acessibilidade:** executar `run-axe-audit.sh` e testes cross-browser assim que infraestrutura permitir.  
5. **Endurecimento CSP e observabilidade:** revisar `logback.xml`, integrar com monitoramento corporativo e remover `'unsafe-inline'`.

---

## 10. Referências e Rastreabilidade
- **Planos:** `passo-a-passo.md` (Tarefas 14–16), `.ia/planos/plano-readme-migracao.md`, `.ia/planos/plano-evolucao-readme-migracao-2026.md`.  
- **Diretrizes:** `.ia/diretrizes/arquitetura.md`, `.ia/diretrizes/frontend.md`, `.ia/diretrizes/seguranca.md`.  
- **ADRs relevantes:** `ADR-20260217-upgrade-spring-framework.md`, `ADR-20260219-upgrade-struts-7.md`, `ADR-20260220-arquitetura-monolito-manter.md`, `ADR-20260219-jquery-remocao-gradual.md`, `ADR-20260223-aguardar-angus-quartz.md`.  
- **Logs chave (fev/2026):** ver diretório `.ia/logs/` para upgrades de dependências, dataset Copa 2026, bundler Vite e validações de segurança.  
- **Documentos auxiliares:** `README.md` (análise legado), `analise-inicial.md`, `.ia/documentacao/README-migracao-2026-v1.md` (versão anterior focada em segurança).

---

## 11. Histórico do Documento
| Data | Responsável | Alteração |
|------|-------------|-----------|
| 23/02/2026 | Assistente Técnico Líder (IA) | Criação do README consolidado conforme Tarefa 14 (versão base). |
| 23/02/2026 | Assistente Técnico Líder (IA) | Reestruturação completa alinhada à Tarefa 16, incluindo seção de jornada de negócio, arquitetura detalhada, operação, segurança e guia expandido para desenvolvedores. |
