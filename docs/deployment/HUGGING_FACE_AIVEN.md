# Implantação em Nuvem (Custo Zero - Hugging Face & Aiven)

Este guia descreve como implantar o Sistema Bolão em um ambiente de nuvem totalmente gratuito, utilizando **Hugging Face Spaces** para a aplicação e **Aiven** para o banco de dados MySQL.

## Serviços Utilizados

1.  **Aplicação (Hugging Face Spaces):**
    -   **Tier:** Free (CPU Basic)
    -   **Recursos:** 16 GB RAM, 2 vCPUs (recursos generosos compartilhados).
    -   **Porta Obrigatória:** **7860** (o balanceador de carga do HF redireciona para esta porta).
    -   **Motivação:** Plataforma estável, com excelente suporte a Docker e recursos de memória superiores ao Koyeb.
2.  **Banco de Dados (Aiven):**
    -   **Tipo:** MySQL Free Tier.
    -   **Recursos:** 1 vCPU, 1 GB RAM, 5 GB Storage.
    -   **Motivação:** Serviço gerenciado de alta confiabilidade.

## Ajustes Arquiteturais e Justificativas

Para garantir a compatibilidade com o Hugging Face Spaces, foram realizados os seguintes ajustes:

### 1. Alteração da Porta Nativa (7860)
Como o projeto utiliza **Tomcat 10 (WAR)** e não Spring Boot executável, a alteração da porta é feita diretamente no `server.xml` do Tomcat via Dockerfile:
-   `RUN sed -i 's/port="8080"/port="7860"/g' /usr/local/tomcat/conf/server.xml`
-   Isso garante que o servidor escute na porta exigida pelo redirecionamento rígido do Hugging Face.

### 2. Otimização da JVM
Embora o Hugging Face ofereça 16GB de RAM, mantemos as otimizações para garantir rapidez e baixo custo computacional:
-   **-Xmx256m / -Xms256m:** Heap controlado.
-   **-XX:MaxMetaspaceSize=160m:** Metaspace adequado para a stack Spring/Struts/Hibernate.
-   **-XX:+UseSerialGC:** Mantido para eficiência.

## Configuração de Variáveis e Segredos

No Hugging Face Spaces, você deve configurar as credenciais na aba **Settings > Variables and Secrets**. 

### 1. Banco de Dados (Aiven MySQL)
Estas variáveis permitem que a aplicação conecte ao banco externo e realize a inicialização automática.

| Variável | Tipo | Exemplo / Valor |
| :--- | :--- | :--- |
| `DB_HOST` | Variable | `mysql-xxxx-novobolaocopa.f.aivencloud.com` |
| `DB_PORT` | Variable | `10865` *(porta não-padrão do Aiven — obrigatório!)* |
| `DB_NAME` | Variable | `defaultdb` |
| `DB_USER` | Variable | `avnadmin` |
| `DB_PASSWORD`| **Secret** | (Sua senha do Aiven) |
| `DB_INITIALIZE`| Variable | `true` (mude para `false` após a primeira carga) |

> ⚠️ **SSL Obrigatório:** O Aiven exige SSL. A aplicação já está configurada com `useSSL=true&requireSSL=true`. O hostname e a porta exatos estão disponíveis no console da Aiven em **Services > Overview > Connection information**.

### 2. Configuração de E-mail (Gmail)
Para o Gmail, utilize uma **Senha de Aplicativo** (16 dígitos) gerada na sua conta Google.

| Variável | Tipo | Valor Recomendado |
| :--- | :--- | :--- |
| `SMTP_HOST` | Variable | `smtp.gmail.com` |
| `SMTP_PORT` | Variable | `465` (Recomendado para HF) ou `587` |
| `SMTP_USERNAME` | Variable | `novobolaocopa@gmail.com` |
| `SMTP_PASSWORD` | **Secret** | (Sua Senha de Aplicativo) |
| `SMTP_FROM_ADDRESS`| Variable | `novobolaocopa@gmail.com` |
| `SMTP_FROM_NAME` | Variable | `Sistema Bolão 2026` |
| `SMTP_TLS` | Variable | `false` (se usar 465) ou `true` (se usar 587) |
| `SMTP_SSL` | Variable | `true` (se usar 465) ou `false` (se usar 587) |
| `SMTP_AUTH` | Variable | `true` |
| `SMTP_STARTTLS_REQUIRED`| Variable | `false` (se usar 465) ou `true` (se usar 587) |
| `SMTP_SYSTEM_URL` | Variable | `https://novobolaodacopa-bolaocopa.hf.space/` |

> **Nota sobre Conectividade:** O Hugging Face Spaces pode bloquear a porta `587`. Se você encontrar erros de `Connection Timeout`, utilize a porta **465** com `SMTP_SSL=true` e `SMTP_TLS=false`.

> **Nota:** A `SMTP_SYSTEM_URL` é fundamental para que os links enviados por e-mail (como recuperação de senha) apontem para o endereço correto do seu Space.

## Importante: Comportamento do Hugging Face

Diferente de um ambiente local com Docker Compose, o Hugging Face Spaces (SDK Docker) funciona de forma **single-container**:
-   **O arquivo `docker-compose.yml` é ignorado.** Ele serve apenas para o seu desenvolvimento local.
-   **Apenas o `Dockerfile` é executado.** Toda a lógica de build e runtime da aplicação deve estar contida nele.
-   O banco de dados (MySQL) **não deve ser rodado como container no HF**, pois o HF não suporta múltiplos serviços vinculados via compose. Por isso, utilizamos o **Aiven** como banco externo.

## Como Implantar

1.  **Aiven:** Configure o MySQL e anote as credenciais.
2.  **Hugging Face:** 
    -   Crie um novo Space do tipo **Docker**.
    -   Conecte ao seu repositório ou faça o upload dos arquivos.
    -   O Hugging Face detectará o `Dockerfile` e iniciará o build.
    -   Certifique-se de que o health check aponte para a porta 7860.

---

## 🐛 Troubleshooting: Erros Reais e Soluções

Esta seção documenta todos os erros e armadilhas encontrados durante o **deploy real** do Bolão no Hugging Face + Aiven. Use como referência para diagnosticar problemas futuros.

---

### ❌ Erro 1: `UnknownHostException` — DNS não resolve o host do banco

**Sintoma no log:**
```
Caused by: java.net.UnknownHostException: mysql-36ec9956-novobolaocopa.f.aivencloud.com
```

**Causa 1 — Porta hardcoded:** A URL JDBC usava a porta `3306` (padrão MySQL), mas o Aiven usa uma **porta não-padrão (ex: `10865`)**. Isso impede a conexão.

| | Configuração em `applicationContext-resources.xml` |
|---|---|
| ❌ **Errado** | `jdbc:mysql://${DB_HOST:localhost}:3306/${DB_NAME}?useSSL=false` |
| ✅ **Correto** | `jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME}?useSSL=true&requireSSL=true&verifyServerCertificate=false` |

**Causa 2 — Serviço Aiven desligado:** O free tier da Aiven desliga o serviço por inatividade. Quando desligado, **a entrada DNS é removida**, causando `UnknownHostException` mesmo com o host correto.

> ✅ **Solução:** Acesse o console da Aiven e clique em **"Power on"** no serviço. Aguarde ~2 minutos e force um **"Restart Space"** no Hugging Face.

---

### ❌ Erro 2: Variável de ambiente `DB_PORT` ausente

**Sintoma:** Aplicação tenta conectar na porta `3306` mesmo com o Aiven configurado para `10865`.

**Causa:** `DB_PORT` não estava nas variáveis de ambiente do HF Space.

> ✅ **Solução:** Adicionar `DB_PORT = 10865` em **Settings > Variables and Secrets** no painel do HF Space.

---

### ❌ Erro 3: Tela em branco no iframe do Hugging Face (Space "Running" mas sem conteúdo)

**Sintoma:** O badge do Space mostra "Running" (verde), mas a aba "App" exibe uma página completamente em branco.

**Causa:** O Spring Security enviava o header `X-Frame-Options: SAMEORIGIN` por padrão. Como o HF embute a aplicação em um `<iframe>` servido de uma origem diferente (`huggingface.co` vs `hf.space`), o browser bloqueava silenciosamente o iframe.

| | Configuração em `applicationContext-security.xml` |
|---|---|
| ❌ **Errado (SAMEORIGIN)** | `<security:frame-options policy="SAMEORIGIN" />` |
| ❌ **Inválido no schema** | `<security:frame-options policy="DISABLE" />` ← o schema XML do Spring Security **não aceita** este valor |
| ✅ **Correto** | Usar `defaults-disabled="true"` e **omitir** o elemento `frame-options` inteiramente |

**Configuração correta:**
```xml
<!-- ✅ CORRETO: defaults-disabled omite o X-Frame-Options -->
<security:headers defaults-disabled="true">
    <security:cache-control />
    <security:content-type-options />
    <security:xss-protection />
    <security:hsts disabled="true" />
    <security:referrer-policy policy="strict-origin-when-cross-origin" />
    <!-- frame-options omitido: HF Spaces embute a app via iframe cross-origin -->
</security:headers>
```

> ⚠️ **Atenção:** O schema XML do Spring Security (`spring-security.xsd`) só aceita os valores `DENY`, `SAMEORIGIN` e `ALLOW-FROM` para o atributo `policy`. Tentar usar `DISABLE` causa falha crítica no startup (`XmlBeanDefinitionStoreException` na linha do arquivo XML).

---

### ❌ Erro 4: SSL desabilitado (`useSSL=false`) com Aiven

**Causa:** A configuração padrão usava `useSSL=false`, mas o Aiven exige SSL (`ssl-mode=REQUIRED`).

| | Parâmetro JDBC |
|---|---|
| ❌ **Errado** | `useSSL=false` |
| ✅ **Correto** | `useSSL=true&requireSSL=true&verifyServerCertificate=false` |

> `verifyServerCertificate=false` dispensa a instalação do CA certificate da Aiven no container, simplificando o deploy sem comprometer a criptografia do tráfego.

---

### 🛢️ Tuning do Banco de Dados e Pool (HikariCP)

Para garantir estabilidade em conexões de longa distância (Hugging Face -> Aiven), as seguintes configurações foram aplicadas:

| Parâmetro | Valor | Motivação |
| :--- | :--- | :--- |
| `initializationFailTimeout` | `0` | Impede que a app falhe se o banco demorar a responder no boot. |
| `leakDetectionThreshold` | `60000` | Reporta no log se houver vazamento de conexão (thread presa > 1 min). |
| `cachePrepStmts` | `true` | Habilita o cache de statements no lado do driver MySQL. |
| `prepStmtCacheSize` | `250` | Define a quantidade de statements cacheados. |

> 💡 **Dica de Debug:** Se você vir a mensagem `Apparent connection leak detected` nos logs do Hugging Face, isso indica um ponto no código que abriu uma conexão mas não a fechou.

---

### 🔗 URLs de Teste

Após o deploy, valide a aplicação pelos links diretos (sem iframe):

| Propósito | URL |
|---|---|
| App (direto) | `https://novobolaodacopa-bolaocopa.hf.space/` |
| Health check | `https://novobolaodacopa-bolaocopa.hf.space/health.txt` |
| Tela de Login | `https://novobolaodacopa-bolaocopa.hf.space/login.action` |

> 💡 A URL `*.hf.space` **não usa iframe** — sempre funciona independente do `X-Frame-Options`. Use-a para confirmar que a aplicação está respondendo antes de investigar o iframe.
