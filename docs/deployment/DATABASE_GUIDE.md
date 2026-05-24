# Guia de Configuração e Segurança do Banco de Dados

Este documento detalha o funcionamento, a segurança e a inicialização automática do banco de dados MySQL para o Sistema Bolão em ambientes de nuvem.

## 1. Segurança e Variáveis de Ambiente

Para manter a segurança e a facilidade de manutenção, o sistema utiliza o padrão **Injeção via Ambiente**. Nenhuma credencial deve ser gravada nos arquivos fonte.

### Configuração no Hugging Face Spaces (Settings > Secrets)
Você deve configurar as seguintes chaves para que a aplicação consiga se conectar ao Aiven:

*   `DB_HOST`: O hostname fornecido pelo Aiven (ex: `mysql-26...aivencloud.com`).
*   `DB_NAME`: O nome do banco de dados (geralmente `defaultdb` no Aiven).
*   `DB_USER`: O usuário administrativo (geralmente `avnadmin`).
*   `DB_PASSWORD`: **(Obrigatório como SECRET)** A senha de acesso.

## 2. Inicialização Automática (Opção B)

O sistema foi preparado para ser auto-suficiente. Ao ser iniciado, ele realiza as seguintes ações:

### A. Criação do Esquema (DDL)
Através do Hibernate (`hibernate.hbm2ddl.auto=update`), o sistema verifica as entidades Java e cria as tabelas caso elas não existam. Isso garante que a estrutura básica esteja lá desde o primeiro segundo.

### B. Carga de Dados (DML Idempotente)
O Spring executa automaticamente dois arquivos localizados em `src/main/resources/database/`:
1.  `schema.sql`: Reforça a criação de tabelas e índices específicos.
2.  `data.sql`: Insere o administrador padrão (`admin/admin123`), o usuário de teste e as notícias iniciais.

#### Por que é seguro rodar em todo boot?
Utilizamos a cláusula **`INSERT IGNORE INTO`**. 
-   **Na primeira vez:** Os dados são inseridos.
-   **Nos boots seguintes:** O banco detecta que as Chaves Primárias já existem e ignora o comando silenciosamente. Isso garante que suas alterações manuais (como trocar a senha do admin) nunca sejam sobrescritas pela carga inicial.

## 3. SSL e Conectividade
A string de conexão foi otimizada para nuvem com os parâmetros:
-   `useSSL=false`: Desabilita a exigência de certificado CA local para simplificar a conexão (o túnel do Aiven é seguro).
-   `allowPublicKeyRetrieval=true`: Necessário para o plugin de autenticação do MySQL 8.4.

## 4. Manutenção e Backup
Como os dados estão no Aiven, você pode utilizar qualquer ferramenta de dump local conectando-se ao endpoint remoto. O Aiven realiza backups automáticos no plano gratuito, garantindo a integridade dos palpites dos participantes.
