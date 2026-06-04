---
title: Novobolao da Copa
emoji: ⚽
colorFrom: green
colorTo: yellow
sdk: docker
pinned: false
---

# Sistema Bolão (Java EE legado)

Este documento é uma análise técnica do projeto **Sistema Bolão**, com foco em arquitetura, tecnologias, funcionalidades e recomendações de evolução. O objetivo é tornar o entendimento rápido e permitir modernização **com o mínimo de alterações** no comportamento atual.

Consulte as **[Diretrizes de Segurança](.ia/diretrizes/seguranca.md)** para detalhes sobre as proteções implementadas.

## 1. Visão Geral

Aplicação web clássica (Java EE) para gerenciamento de um bolão de futebol, com cadastro de participantes, palpites, classificação, gráficos de desempenho e chat. O projeto é empacotado em **WAR** e deployado em **Tomcat**.

Principais sinais de legado:

- Stack baseada em **Spring + Hibernate 3 + WebWork/XWork + Acegi Security + DWR + Cewolf**.
- **Ant** apenas para empacotar o WAR (sem compilação).
- **Configurações e credenciais no código**.
- Tecnologias alinhadas à era 2006 (Copa 2006 aparece em mensagens).

## 2. Passo a Passo da Análise

1. **Entradas de build e deploy**: `build.xml` e `build.properties` indicam empacotamento do WAR e path para Tomcat 5.5.
2. **Web.xml**: filtros, servlets e listeners mostraram uso de WebWork, DWR, Acegi e OpenSessionInView.
3. **Spring contexts**: `applicationContext-*.xml` revelaram datasources, Hibernate, segurança, serviços e scheduler.
4. **Camada de ações**: classes em `src/com/opendev/bolao/action` indicam endpoints e funcionalidades.
5. **Serviços e DAOs**: lógica principal em `service/impl` e persistência em `dao/hibernate`.
6. **Domínio**: entidades em `model` e mapeamentos `*.hbm.xml` definem o banco.
7. **UI**: páginas JSP em `webapp/` e JS legado (Prototype/Scriptaculous).
8. **Integrações**: e-mail via JavaMail e agendamentos via Quartz.

## 3. Funcionalidades Identificadas

- **Cadastro e autenticação** de participantes.
- **Envio de palpites** por jogo (com bloqueio por horário).
- **Classificação geral** e estatísticas de desempenho.
- **Administração**: cadastro de jogos, atualização de resultados, gerenciamento de participantes e papéis.
- **Chat** em tempo real baseado em DWR.
- **Notificações por e-mail** (cadastro e alertas de jogos próximos).
- **Gráficos** de desempenho e líderes (Cewolf + JFreeChart).
- **Filtros de jogos** por fase, data, grupo, equipe.

## 4. Arquitetura Atual (Visão Lógica)

Camadas principais:

1. **Web (JSP + WebWork/DWR)**
2. **Actions (Controllers)**: `ParticipanteAction`, `AdminAction`
3. **Services**: regras de negócio, transações Spring
4. **DAOs**: persistência Hibernate
5. **Banco MySQL**

Fluxo típico:

JSP → WebWork Action → Service (transação) → DAO (Hibernate) → Banco

## 5. Estrutura de Pastas

- `src/` código Java e configurações Spring/XWork.
- `webapp/` páginas JSP, JS, CSS e libs (WAR root).
- `webapp/WEB-INF/lib/` jars locais (sem gerenciador de dependências).
- `webapp/WEB-INF/classes/` recursos duplicados (provável resultado de build antigo).
- `bolao_datamodel.xml` modelo do banco (MySQL).

## 6. Tecnologias e Bibliotecas

Pelos jars em `webapp/WEB-INF/lib/` e configurações:

- **Spring Framework (antigo)**
- **Hibernate 3** (mapeamentos `*.hbm.xml`)
- **WebWork/XWork** (base do Struts 2)
- **Acegi Security** (predecessor do Spring Security)
- **DWR** (AJAX remoting)
- **Quartz Scheduler**
- **Ehcache**
- **JFreeChart + Cewolf** (gráficos)
- **Commons DBCP, Commons Lang, Commons Logging**
- **JavaMail**
- **Prototype/Scriptaculous** (front-end)
- **MySQL JDBC Driver** (`com.mysql.jdbc.Driver`)

## 7. Configurações Principais

- Banco de dados: `src/applicationContext-resources.xml`
- Hibernate: `src/applicationContext-hibernate.xml`
- Segurança (Acegi): `src/applicationContext-security.xml`
- Scheduler: `src/applicationContext-scheduler.xml`
- WebWork/XWork: `src/xwork.xml`
- DWR: `webapp/WEB-INF/dwr.xml`
- E-mail: `src/com/opendev/bolao/email/email.properties`

## 8. Modelo de Dados (Entidades Principais)

Entidades mapeadas em `src/com/opendev/bolao/model/*.hbm.xml`:

- `Participante` → `PAR_PARTICIPANTE`
- `Privilegio` → `PRI_PRIVILEGIO`
- `Jogo` → `JOG_JOGO`
- `Equipe` → `EQP_EQUIPE`
- `Palpite` → `PAL_PALPITE` (chave composta: participante + jogo)
- `BolaoIndividual` / `PalpiteBolaoIndividual`
- `Noticia`

Observações:

- `Palpite` usa chave composta, o que dificulta migração para JPA sem ajustes.
- `Participante` possui cache local de pontuação (classe `Cache`).

## 9. Pontos de Atenção (Riscos e Dívidas Técnicas)

### **AVISO: Riscos Críticos de Segurança Identificados**

A análise inicial revelou vulnerabilidades de **alta criticidade** que necessitam de atenção **imediata**:
1.  **Credenciais de Banco de Dados Expostas:** Usuário e senha do banco de dados estão hardcoded em `src/applicationContext-resources.xml`.
2.  **Hashing de Senha Inseguro:** O sistema utiliza **SHA-1 sem salt** (`SegurancaUtils.java`), um algoritmo obsoleto e inseguro para armazenamento de senhas.
3.  **Falta de HTTPS:** A ausência de HTTPS no servidor de aplicação expõe todo o tráfego, incluindo credenciais de login, à interceptação.
4.  **Bibliotecas de Segurança Obsoletas:** O uso do **Acegi Security** (versão 1.0.0) é um risco grave, pois o framework é descontinuado e possui vulnerabilidades conhecidas.
5.  **DWR com Modo Debug Ativo:** A configuração do DWR com `debug=true` em produção pode expor informações sensíveis sobre a aplicação e seus serviços.
---

1. **Credenciais em código**: usuário e senha de DB hardcoded em `src/applicationContext-resources.xml`.
2. **Senha fraca**: hashing com **SHA-1 sem salt** em `src/com/opendev/bolao/util/SegurancaUtils.java`.
3. **Stack desatualizada**: Acegi, WebWork, Hibernate 3, DWR, JS legado.
4. **Build frágil**: Ant só empacota, não compila. Classes podem estar fora de sync.
5. **Chat em memória**: não persistente e sem suporte a cluster.
6. **Método `Jogo.jaOcorreu()` sempre retorna `true`**, afetando regra de pontuação.
7. **Configs duplicadas**: arquivos em `src/` e `webapp/WEB-INF/classes/`.
8. **Logs e tratamento de erro**: `printStackTrace` em produção.

## 10. Recomendações Imediatas (Baixo Impacto)

1. Externalizar configurações sensíveis (DB, SMTP) usando arquivos externos ou variáveis de ambiente.
2. Ajustar hashing de senha para **BCrypt** mantendo compatibilidade com legado.
3. Revisar `Jogo.jaOcorreu()` para não retornar `true` sempre.
4. Padronizar build e remover duplicidade entre `src/` e `webapp/WEB-INF/classes/`.
5. Adicionar logging estruturado em pontos críticos (cadastro, update de palpite, email).

## 11. Migração para Tecnologias Modernas com Mínimas Alterações

Estratégia: **evolução incremental**, preservando JSP, URL paths e fluxo atual.

### Passo 1 – Modernizar Build (sem mudar runtime)

- Migrar para **Maven/Gradle** com empacotamento WAR.
- Manter `webapp/` como root.
- Importar jars de `WEB-INF/lib` como dependências.

Resultado: build reproduzível, sem reescrever código.

### Passo 2 – Atualizar Runtime

- Subir para **Java 17 ou 21**.
- Migrar de **Tomcat 5.5** para **Tomcat 11.0.x** .

Resultado: segurança e compatibilidade modernas sem refatoração pesada.

## 11. Modernização Concluída (Stack 2026)

O projeto passou por uma migração profunda para garantir segurança e longevidade:

- **Build Moderno**: Migrado para **Maven 3.9** com pipeline de CI/CD para Hugging Face.
- **Runtime Atualizado**: Executando em **Java 17/21** com **Tomcat 10.1+** (Jakarta EE 10).
- **Segurança**: Acegi substituído por **Spring Security 6.2**. Hashing migrado para **BCrypt**.
- **Web Framework**: WebWork substituído por **Struts 7.1** (totalmente Jakarta-compatible).
- **Persistência**: Hibernate 3 migrado para **Hibernate 6.4** com suporte a JPA 3.1.
- **Frontend**: Introdução de **HTMX** para interações dinâmicas e **Vite** para assets modernos.

## 12. Fluxos e Arquitetura Detalhada

Para detalhes sobre fluxos específicos do sistema, consulte:

- 📖 **[Fluxo de Recuperação de Senha (OTP)](docs/architecture/password-recovery-flow.md)**: Detalhamento técnico do processo de recuperação com diagrama Mermaid.
- 📖 **[Arquitetura Frontend (HTMX)](docs/adr/001-htmx-direct-inline-palpites.md)**: Princípios da modernização da UI.
- 📖 **[Sistema de E-mail](docs/architecture/EMAIL_SYSTEM.md)**: Integração com Brevo via REST API.

## 13. Como Rodar (Moderno - Docker)

**Recomendado para desenvolvimento e testes.**

A aplicação pode ser executada facilmente usando Docker Compose. Consulte a documentação completa em:

📖 **[docker/README.md](docker/README.md)**

### Quick Start

```bash
# Setup de ambiente e build
cp .env.example .env
docker compose up --build -d
```

### 13.1. Publicação em Nuvem (Custo Zero - Hugging Face & Aiven)

Para implantar em um ambiente de produção gratuito:

📖 **[docs/deployment/HUGGING_FACE_AIVEN.md](docs/deployment/HUGGING_FACE_AIVEN.md)**

---

## 14. Próximos Passos (Evolução Contínua)

1. **Refatoração JPA**: Converter mapeamentos residuais `.hbm.xml` para anotações `@Entity`.
2. **Remoção de DWR**: Migrar as últimas funcionalidades de Chat e popups para HTMX.
3. **Auditoria de Acessibilidade**: Implementar melhorias baseadas em relatórios do `axe-core`.
4. **Otimização de Assets**: Expandir o uso do Vite para todos os componentes CSS/JS legados.

---

Este README serve como base para tomada de decisão técnica e execução incremental.

## 15. Manutenção de Dados (Copa 2026)

O sistema utiliza um dataset normalizado para a carga inicial de equipes e jogos. Para atualizar os dados da Copa 2026 (seleções qualificadas, resultados, sedes ou partidas do mata-mata):

1. **Editar Dataset**: Modifique o arquivo `data/copa2026_tabela_brt_normalizado.csv`.
2. **Gerar SQL**: Execute o script helper para regerar o script de carga do banco.

```bash
# Execução via bash (Ubuntu/WSL)
bash scripts/gerar_sql_copa2026.sh
```

O script gerará o arquivo `data/sql/03-copa-2026-data.sql` que é utilizado no processo de deploy/banco.

## 12. Arquitetura Frontend (Modernização 2026)

A partir da **Fase 7**, o projeto adotou uma estratégia de modernização de UI baseada em **HTMX**, substituindo tecnologias legadas (DWR e Prototype).

### Princípios de Design
- **Mapeamento Natural (Split Inputs)**: Inputs de placar posicionados adjacentes ao nome de cada time para intuitividade imediata.
- **Feedback Visual Atômico**: Uso de animações CSS ("Saved Flash") em toda a linha de dados para confirmação de salvamento.
- **Design System**: Tipografia baseada em **Inter** e **Outfit**, com paleta moderna e efeitos de **Glassmorphism**.

### Estratégia Técnica (HTMX)
Para garantir a estabilidade em tabelas complexas com linhas de detalhe expansíveis, utilizamos os seguintes padrões:

1.  **Padrão Multi-Tbody**: Cada partida na listagem de jogos é encapsulada em seu próprio elemento `<tbody>`. Isso permite que o HTMX atualize o container da partida de forma isolada e atômica.
2.  **Fragmentos JSP (`.jspf`)**: A lógica de renderização da linha de jogo está centralizada em `match-row.jspf`. Este fragmento é incluído:
    - Na renderização inicial da página (`jogos.jsp`).
    - Na resposta parcial de ações HTMX (`palpite-cell-response.jsp`).
3.  **Swap Containers**: As atualizações de palpite utilizam `hx-target="closest tbody"` e `hx-swap="innerHTML"`. Isso substitui o conteúdo interno do `<tbody>` do jogo, garantindo integridade do DOM e evitando duplicidade de IDs.
4.  **UX Helper JS**: O arquivo `ux-helper.js` gerencia comportamentos complementares como navegação automática de foco (Auto-Advance) e barra de progresso em tempo real.

Consulte os registros de decisão para detalhes profundos:
- **[ADR 001](docs/adr/001-htmx-direct-inline-palpites.md)**: Introdução do HTMX.
- **[ADR 004](docs/adr/004-split-inputs-row-level-htmx.md)**: Mapeamento Natural e Multi-Tbody.
