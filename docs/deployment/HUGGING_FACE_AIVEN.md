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

> ⚠️ **SSL Obrigatório:** O Aiven exige SSL. A aplicação já está configurada com `useSSL=true&requireSSL=true`. O hostname e a porta exatos estão disponíveis no console da Aiven em **Services > Overview > Connection information**.

### 2. Configuração de E-mail (Gmail)
Para o Gmail, utilize uma **Senha de Aplicativo** (16 dígitos) gerada na sua conta Google.

| Variável | Tipo | Valor Recomendado |
| :--- | :--- | :--- |
| `SMTP_HOST` | Variable | `smtp.gmail.com` |
| `SMTP_PORT` | Variable | `587` |
| `SMTP_USERNAME` | Variable | `novobolaocopa@gmail.com` |
| `SMTP_PASSWORD` | **Secret** | (Sua Senha de Aplicativo) |
| `SMTP_FROM_ADDRESS`| Variable | `novobolaocopa@gmail.com` |
| `SMTP_FROM_NAME` | Variable | `Sistema Bolão 2026` |
| `SMTP_TLS` | Variable | `true` |
| `SMTP_AUTH` | Variable | `true` |
| `SMTP_STARTTLS_REQUIRED`| Variable | `true` |
| `SMTP_SYSTEM_URL` | Variable | `https://novobolaodacopa-bolaocopa.hf.space/` |

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
