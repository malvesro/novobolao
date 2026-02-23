# Sistema Bolão – Guia de Migração

Este documento complementa o `README.md` original com uma visão atualizada da modernização em andamento do Sistema Bolão. Ele destaca o estado **pós-migração** das principais camadas, instruções de build com a stack moderna e detalha as configurações de segurança introduzidas a partir da **Fase 4** do plano.

> Consulte também o arquivo `passo-a-passo.md` para o histórico completo das tarefas executadas e pendentes.

## 1. Visão Geral Atualizada

- **Objetivo do projeto:** manter o fluxo clássico de bolão (cadastro, palpites, ranking, gráficos) enquanto migra gradualmente de um stack Java EE 2006 para tecnologias suportadas em 2026.
- **Empacotamento:** WAR padrão Maven, deployado em contêiner compatível com Jakarta EE 10 (Tomcat 10.1+ / 11).
- **Compatibilidade preservada:** URLs principais (`*.action`), camada JSP e layout herdado continuam válidos para evitar ruptura com o front-end tradicional.

## 2. Stack Modernizada

| Camada                         | Versão/Estado Atual (21/02/2026)                     | Observações-chave                                                                 |
|--------------------------------|------------------------------------------------------|------------------------------------------------------------------------------------|
| Linguagem / Runtime            | Java 17 (mínimo recomendado)                        | Ajustado para compatibilidade com Spring 6 e Struts 7.                              |
| Framework Web                  | Struts 7 (migração concluída)                       | Actions convertem WebWork legado; OGNL endurecido com allowlist.                   |
| Inversão de Controle / Core    | Spring Framework 6.1.x                              | Contextos XML atualizados para namespaces Jakarta.                                 |
| Segurança                      | Spring Security 6.2.x                               | Substitui Acegi; autenticação JDBC com BCrypt.                                     |
| Persistência                   | Hibernate 6.4.x                                     | Mantém HBM XML; API atualizada para `jakarta.persistence`.                         |
| Front-end dinâmico             | JSP + HTMX + JavaScript modular (Vite 5)             | Prototype/DWR removidos; HTMX cobre interações assíncronas.                        |
| Build e Bundler                | Maven 3.9 + Vite (via `frontend-maven-plugin`)       | `npm run build` gera manifest com hash e fallback `app-bundle.js`.                 |

## 3. Fluxo de Build e Testes

1. **Instalar dependências Node (quando necessário):**
   ```bash
   npm install
   ```
2. **Gerar assets modernos:**
   ```bash
   npm run build
   ```
3. **Executar a suíte Java (com skip controlável do front-end):**
   ```bash
   mvn test -Dfrontend.skip=true
   ```
4. **Empacotar o WAR completo (inclui pipeline Vite):**
   ```bash
   mvn package -Dfrontend.skip=false
   ```

> Em ambientes sem acesso à internet, mantenha o fallback `webapp/assets/js/app-bundle.js` atualizado manualmente e use `-Dfrontend.skip=true`.

## 4. Configurações de Segurança (Detalhadas)

Todas as políticas abaixo são definidas em `src/main/resources/applicationContext-security.xml` e já aplicadas no ambiente migrado.

### 4.1 CSRF – CookieCsrfTokenRepository
- **Definição:** `<security:csrf token-repository-ref="csrfTokenRepository" />`
- **Repositório:** `CookieCsrfTokenRepository` com `cookieHttpOnly=false` e `cookiePath=/`.
- **Propagação do token:**
  - O prelude JSP (`webapp/WEB-INF/content/template/cabecalho.jspf`) injeta metatags `_csrf`, `_csrf_header` e `_csrf_parameter`.
  - Script utilitário adiciona o token em:
    - Todos os formulários `POST` (auto-injeção em submissões dinâmicas).
    - Chamadas `fetch`.
    - Requisições HTMX via evento `htmx:configRequest`.
  - Logout HTML foi convertido para formulário `POST`, consumindo o token automaticamente (`webapp/WEB-INF/content/template/menu.jspf`).

### 4.2 HSTS (HTTP Strict Transport Security)
- **Configuração:** `<security:hsts include-subdomains="true" max-age-seconds="31536000" />`
- **Efeito:** força uso de HTTPS por 1 ano para domínio e subdomínios; recomendável habilitar apenas quando TLS estiver ativo em produção.

### 4.3 CSP (Content-Security-Policy)
- **Diretivas ativas:**
  ```
  default-src 'self';
  script-src 'self' 'unsafe-inline';
  style-src 'self' 'unsafe-inline';
  img-src 'self' data:;
  font-src 'self' data:;
  connect-src 'self';
  form-action 'self';
  frame-ancestors 'self';
  base-uri 'self'
  ```
- **Notas:**
  - `'unsafe-inline'` temporariamente permitido para script/style devido ao legado JSP; reduzir gradualmente conforme JS modularizado.
  - `img-src` e `font-src` aceitam `data:` para compatibilidade com assets em base64.

### 4.4 Referrer-Policy
- **Configuração:** `<security:referrer-policy policy="strict-origin-when-cross-origin" />`
- **Propósito:** reduz vazamento de informações sensíveis em requisições cross-origin, mantendo compatibilidade com recursos internos.

### 4.5 X-Frame-Options
- **Configuração:** `<security:frame-options policy="SAMEORIGIN" />`
- **Motivo:** previne clickjacking mantendo possíveis embeds controlados dentro do mesmo domínio.

### 4.6 Sanitização e Validação de Entrada
- **Utilitário central:** `SanitizationUtils` (strings normalizadas com `Normalizer`, remoção de HTML/control characters via regex e limites de tamanho).
- **Ações Struts:** setters anotados com `@StrutsParameter` agora sanitizam entradas críticas (`ParticipanteAction`, `AdminAction`).
- **Fluxo de cadastro:** validações explícitas para login, nome, e-mail e senha; rejeição de HTML/scripts e feedback ao usuário.
- **Campos administrativos:** dados como `local`, `status` e `papel` são higienizados antes de persistidos.

### 4.7 Auditoria de Segredos
- **Datasource:** senhas do banco lidas exclusivamente de variáveis de ambiente (`DB_PASS`) em `applicationContext-resources.xml`.
- **Docker Compose:** exige `MYSQL_ROOT_PASSWORD`/`MYSQL_PASSWORD` (configure via `.env`, ex. `docker/.env.example`); o arquivo `.env` na raiz é carregado automaticamente pelo Compose e permanece fora do versionamento, garantindo que senhas reais não fiquem no repositório.
- **Varredura automatizada:** `scripts/scan-secrets.sh` usa `rg` para detectar possíveis segredos; incluir em pipelines locais/CI.

### 4.8 Base de Jogos (Fase 6)
- **Dataset CSV:** `data/copa2026_tabela_brt.csv` consolida 89 partidas (fase de grupos + 32-avos + final) com horário oficial de Brasília e nomes em português.
- **Placeholders:** seleções oriundas de repescagens permanecem indicadas como `DEN/MKD/CZE/IRL`, etc., até que a FIFA confirme os classificados.
- **Próximos passos:** completar a chave eliminatória (oitavas em diante) quando a FIFA liberar horários/estádios definitivos e atualizar os scripts de carga (`03-copa-2026-data.sql`).
- **Atualização de equipes indefinidas:**  
  1. Edite `data/copa2026_placeholders.json` preenchendo os campos `name` (nome oficial da seleção) e, se necessário, `group` para cada vaga definida após os playoffs.  
  2. Execute o script:  
     ```bash
     python3 scripts/atualizar_copa2026_dataset.py \
       --input data/copa2026_tabela_brt_normalizado.csv \
       --placeholders data/copa2026_placeholders.json \
       --output-sql data/sql/03-copa-2026-data.sql \
       --output-csv data/copa2026_tabela_brt_final.csv
     ```  
  3. Revise o diff do SQL antes de aplicar no banco (via Docker ou ambiente alvo).  
  4. Registre a execução em `.ia/logs/` com os placeholders substituídos e, se necessário, crie ADR documentando a rodada final dos dados.
- **Ajustes através da interface administrativa:**  
  - Usuários com papel `ADMIN` podem incluir/editar partidas em **Admin → Cadastrar jogos** (`admin/inclusaoJogo.jsp`) e atualizar resultados em **Admin → Atualizar resultados**.  
  - Use as novas opções de fase (incluindo 32-avos) e grupos A–L para refletir correções pontuais antes da atualização automatizada.  
  - Recomenda-se sincronizar qualquer alteração manual com o dataset CSV/SQL para evitar divergências entre UI e cargas automatizadas.

## 5. Migração das Views para `WEB-INF`

- Todas as JSPs (públicas e autenticadas) residem agora em `webapp/WEB-INF/content/`.
- Struts 7 responde por rotas `.action`, eliminando acesso direto a JSPs via browser.
- Includes (`jsp:include`) ajustados para caminhos internos (`/WEB-INF/content/...`).

## 6. Principais Marcos Concluídos

| Data (2026) | Marco                                                                                           |
|-------------|--------------------------------------------------------------------------------------------------|
| 17/02       | Atualização do Spring para 6.x e remoção do Acegi; introdução de BCrypt e testes JUnit 5.        |
| 19/02       | Migração completa para Struts 7, substituição de Cewolf, remoção de Prototype/Scriptaculous.     |
| 20/02       | Pipeline HTMX/Vite consolidado com manifest hash e fallback seguro.                              |
| 21/02       | Proteção de JSPs sob `WEB-INF` e reforço da camada Web (HSTS, CSP, CSRF, logout seguro).         |

## 7. Próximos Passos

1. **Fase 4 Tarefa 4:** Revisar validadores Struts 7 e fortalecer sanitização contra XSS.
2. **Fase 4 Tarefa 5:** Auditoria automatizada de segredos em repositório e configurações.
3. **Fase 5:** Planejar reestruturação modular e evolução completa do front-end, caso aprovado.

## 8. Como Rodar Localmente

1. Configure as variáveis sensíveis (datasource, SMTP) via `applicationContext-resources.xml` ou variáveis de ambiente definidas nos contextos Spring.
2. Execute os steps de build conforme seção 3.
3. Suba o WAR em Tomcat 10.1+:
   ```bash
   mvn package
   cp target/sistema-bolao-0.2.0-SNAPSHOT.war $TOMCAT_HOME/webapps/ROOT.war
   ```
4. Acesse a aplicação em `https://localhost:8443/` (TLS fortemente recomendado para aproveitar HSTS).

---

**Contato interno:** Time Mercúrio – Arquiteto de Software Sênior (Assistente Técnico Líder). Qualquer dúvida sobre as decisões de arquitetura ou segurança deve ser registrada em `.ia/logs/` ou em ADRs dentro de `.ia/historico/`.
