# Implantação em Nuvem (Custo Zero - Koyeb & Aiven)

Este guia descreve como implantar o Sistema Bolão em um ambiente de nuvem totalmente gratuito, utilizando **Koyeb** para a aplicação e **Aiven** para o banco de dados MySQL.

## Serviços Utilizados

1.  **Aplicação (Koyeb):**
    -   **Tier:** Free (Nano)
    -   **Recursos:** 512 MB RAM, 0.1 vCPU.
    -   **Motivação:** Plataforma serverless amigável ao desenvolvedor que suporta deploys Docker nativos.
2.  **Banco de Dados (Aiven):**
    -   **Tipo:** MySQL Free Tier.
    -   **Recursos:** 1 vCPU, 1 GB RAM, 5 GB Storage.
    -   **Motivação:** Serviço gerenciado de alta confiabilidade com plano gratuito generoso.

## Ajustes Arquiteturais e Justificativas

Para garantir a estabilidade em um ambiente tão restrito (especialmente os 512MB de RAM do Koyeb), foram realizados os seguintes ajustes:

### 1. Otimização da JVM
O `Dockerfile` foi configurado com `CATALINA_OPTS` para um controle rigoroso de memória:
-   **-Xmx256m / -Xms256m:** Define o Heap em 256MB. Este valor foi equilibrado para permitir espaço suficiente para o Metaspace sem exceder o limite do Koyeb.
-   **-XX:MaxMetaspaceSize=160m:** Limite de Metaspace aumentado para suportar o carregamento de todas as classes da stack moderna (Spring/Hibernate/Struts).
-   **-XX:+UseSerialGC:** Escolhido em vez do G1GC. O Serial GC é muito mais eficiente em sistemas com apenas 1 thread de CPU (0.1 vCPU), pois consome menos recursos para gerenciar a coleta de lixo.
-   **-Xss256k:** Redução do tamanho da stack de cada thread. Como o sistema não possui recursão profunda, isso economiza RAM preciosa para cada conexão simultânea.

### 2. Imagem Runtime Slim
A imagem base foi alterada para `tomcat:10.1-jdk17-slim`:
-   Reduz o tamanho da imagem final.
-   Elimina componentes desnecessários do sistema operacional que consumiriam RAM e CPU.
-   Remoção manual das aplicações padrão do Tomcat (`manager`, `docs`, `examples`, `ROOT`).

### 3. Padronização de Variáveis de Ambiente
As configurações em `applicationContext-resources.xml` foram atualizadas para suportar nomes padronizados:
-   `DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.
-   Isso facilita a integração com os segredos (Secrets) do Koyeb e as strings de conexão fornecidas pelo Aiven.

## Configuração de Credenciais e Segredos

No Koyeb, as credenciais não devem ser colocadas em arquivos no repositório. Em vez disso, utilize a seção **Environment Variables** nas configurações do serviço.

### 1. Variáveis do Banco de Dados (Aiven)

Configure as seguintes variáveis com os dados fornecidos pelo Aiven:

| Variável | Descrição | Exemplo |
| :--- | :--- | :--- |
| `DB_HOST` | Endpoint do MySQL no Aiven | `mysql-xxxx.aivencloud.com` |
| `DB_NAME` | Nome do banco de dados | `defaultdb` |
| `DB_USER` | Usuário do banco | `avnadmin` |
| `DB_PASSWORD` | Senha do banco (Use o tipo **Secret**) | `********` |

### 2. Configuração de E-mail (Gmail - novobolaocopa@gmail.com)

Para utilizar o Gmail, você **não** deve usar sua senha normal. É obrigatório gerar uma **Senha de Aplicativo**.

**Passo a Passo para o Gmail:**
1.  Acesse a conta `novobolaocopa@gmail.com`.
2.  Ative a **Verificação em duas etapas** nas configurações de segurança do Google.
3.  Procure por **Senhas de Aplicativo**.
4.  Gere uma senha para o app "Sistema Bolão" e anote os 16 dígitos gerados.

**Variáveis no Koyeb:**

| Variável | Valor Recomendado | Observação |
| :--- | :--- | :--- |
| `SMTP_HOST` | `smtp.gmail.com` | Servidor do Google |
| `SMTP_PORT` | `587` | Porta para STARTTLS |
| `SMTP_USERNAME` | `novobolaocopa@gmail.com` | Seu e-mail completo |
| `SMTP_PASSWORD` | `********` | **Senha de Aplicativo** (16 dígitos) |
| `SMTP_FROM_ADDRESS`| `novobolaocopa@gmail.com` | Remetente visível |
| `SMTP_FROM_NAME` | `Sistema Bolão 2026` | Nome que aparece no e-mail |
| `SMTP_TLS` | `true` | Habilita STARTTLS |
| `SMTP_SYSTEM_URL` | `https://sua-app.koyeb.app/` | URL pública da sua app no Koyeb |

## Como Implantar

1.  **Aiven:**
    -   Crie um banco de dados MySQL no Aiven.
    -   Anote as credenciais (Host, Porta, Usuário, Senha).
    -   Execute o script de schema inicial (`docker/mysql/init/01-schema.sql` e subsequentes).

2.  **Koyeb:**
    -   Crie uma nova App no Koyeb.
    -   Conecte ao seu repositório GitHub (na branch `nuvem`).
    -   Configure as seguintes variáveis de ambiente:
        -   `DB_HOST`: Host do Aiven.
        -   `DB_NAME`: Nome do banco (ex: `bolao`).
        -   `DB_USER`: Usuário do banco.
        -   `DB_PASSWORD`: Senha do banco.
    -   **Atenção:** Devido ao baixo CPU (0.1 vCPU), defina o tempo de timeout do Health Check para pelo menos **120 segundos**, pois o Spring/Tomcat levará tempo para inicializar o contexto completo.
