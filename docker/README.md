# Docker Setup - Sistema Bolão

## Visão Geral

Este diretório contém a configuração Docker para executar o Sistema Bolão localmente com todos os serviços necessários.

## Estrutura

```
docker/
└── mysql/
    └── init/
        ├── 01-schema.sql      # Schema do banco de dados
        └── 02-seed-data.sql   # Dados iniciais (usuários, equipes, jogos)
```

## Serviços

### 1. MySQL 8.0 (db)
- **Porta:** 3306
- **Banco:** `${MYSQL_DATABASE}` (padrão: `bolao`)
- **Usuário:** `${MYSQL_USER}` (padrão: `user_bolao`)
- **Senha:** definida via `MYSQL_PASSWORD` (obrigatória)
- **Root Password:** definido via `MYSQL_ROOT_PASSWORD` (obrigatório)

### 2. Tomcat 10.1 + Aplicação (app)
- **Porta:** 8080
- **URL:** http://localhost:8080
- **Runtime:** Java 17 + Jakarta EE 10

## Como Executar

### Pré-requisitos

- Docker instalado
- Docker Compose instalado
- Portas 8080 e 3306 disponíveis
- Arquivo `.env` configurado na raiz do projeto (use `docker/.env.example` como base)

> **Dica:** copie o template e ajuste valores fortes antes de subir os serviços:
> ```bash
> cp docker/.env.example .env
> # edite .env para definir senhas seguras
> ```

Exemplo de conteúdo para `.env`:

```
# Credenciais do banco
MYSQL_ROOT_PASSWORD=MinhaSenhaRoot@2026
MYSQL_DATABASE=bolao
MYSQL_USER=user_bolao
MYSQL_PASSWORD=MinhaSenhaApp@2026

# Variáveis consumidas pela aplicação
DB_HOST=db
DB_PASS=${MYSQL_PASSWORD}
```

### Como o `.env` é usado e por que é mais seguro?

- O arquivo `.env` deve ficar **na raiz do projeto** (mesmo nível do `docker-compose.yml`). O Docker Compose carrega automaticamente esse arquivo e substitui as variáveis declaradas no compose (`${VARIAVEL}`).
- O template `docker/.env.example` contém valores fictícios; ao copiá-lo para `.env`, você define senhas reais **fora do versionamento** (o `.env` permanece ignorado pelo Git).
- Dessa forma, as credenciais não aparecem no repositório, mas a aplicação continua funcionando localmente porque o Compose injeta essas variáveis nos containers (`MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD`, `DB_PASS`, etc.).
- Sempre use senhas fortes e, se compartilhar o projeto, distribua o template (`.env.example`) em vez de expor o `.env`.

### Comandos

#### 1. Build e Start (primeira vez ou após mudanças)

```bash
# Windows (WSL2)
wsl bash -c "docker-compose up --build -d"

# Linux/Mac
docker-compose up --build -d
```

#### 2. Verificar Status

```bash
# Ver status dos containers
wsl bash -c "docker-compose ps"

# Ver logs da aplicação
wsl bash -c "docker-compose logs -f app"

# Ver logs do banco
wsl bash -c "docker-compose logs -f db"
```

#### 3. Parar os Serviços

```bash
wsl bash -c "docker-compose down"
```

#### 4. Parar e Remover Volumes (limpar banco)

```bash
wsl bash -c "docker-compose down -v"
```

## Inicialização do Banco de Dados

Os scripts SQL em `docker/mysql/init/` são executados automaticamente na primeira inicialização do container MySQL:

1. **01-schema.sql** - Cria todas as tabelas
2. **02-seed-data.sql** - Insere dados iniciais

### Dados Iniciais

#### Usuários Padrão

| Login | Senha | Papel | Email |
|-------|-------|-------|-------|
| admin | admin123 | ADMIN | admin@bolao.local |
| user | user123 | USER | user@bolao.local |

⚠️ **IMPORTANTE:** Troque as senhas padrão após o primeiro acesso!

#### Equipes

16 equipes distribuídas em 4 grupos (A, B, C, D)

#### Jogos

12 jogos de exemplo na fase de grupos

## Health Checks

### Banco de Dados
- **Intervalo:** 10s
- **Timeout:** 5s
- **Retries:** 5
- **Start Period:** 30s

### Aplicação
- **Intervalo:** 30s
- **Timeout:** 10s
- **Retries:** 3
- **Start Period:** 60s

## Troubleshooting

### Problema: Aplicação não conecta ao banco

**Solução:**
```bash
# Verificar se o banco está saudável
wsl bash -c "docker-compose ps"

# Ver logs do banco
wsl bash -c "docker-compose logs db"

# Reiniciar serviços
wsl bash -c "docker-compose restart"
```

### Problema: Porta 8080 já em uso

**Solução:**
Editar `docker-compose.yml` e mudar a porta:
```yaml
ports:
  - "8081:8080"  # Usar porta 8081 no host
```

### Problema: Banco não inicializa

**Solução:**
```bash
# Remover volumes e recriar
wsl bash -c "docker-compose down -v"
wsl bash -c "docker-compose up --build -d"
```

### Problema: Erro de permissão nos scripts SQL

**Solução:**
```bash
# Verificar permissões (Linux/Mac)
chmod 644 docker/mysql/init/*.sql
```

## Variáveis de Ambiente

As seguintes variáveis podem ser customizadas no `docker-compose.yml`:

### Banco de Dados
- `MYSQL_ROOT_PASSWORD` (obrigatório) – Senha do usuário root no MySQL.
- `MYSQL_DATABASE` (opcional, padrão `bolao`) – Nome do banco a ser criado.
- `MYSQL_USER` (opcional, padrão `user_bolao`) – Usuário de aplicação.
- `MYSQL_PASSWORD` (obrigatório) – Senha do usuário de aplicação.

### Aplicação
- `DB_HOST` (opcional, padrão `db`) – Host do banco.
- `DB_NAME` (opcional, padrão `bolao`) – Nome do banco utilizado pela aplicação.
- `DB_USER` (opcional, padrão `user_bolao`) – Usuário do banco.
- `DB_PASS` (obrigatório) – Senha do banco utilizada pela aplicação (deve espelhar `MYSQL_PASSWORD`).

## Desenvolvimento

### Rebuild Apenas da Aplicação

```bash
wsl bash -c "docker-compose up --build app"
```

### Acessar Container

```bash
# Acessar container da aplicação
wsl bash -c "docker exec -it bolao-app sh"

# Acessar container do banco
wsl bash -c "docker exec -it bolao-db bash"
```

### Conectar ao MySQL Diretamente

```bash
wsl bash -c 'docker exec -it bolao-db mysql -u "${MYSQL_USER:-user_bolao}" -p"${MYSQL_PASSWORD}" "${MYSQL_DATABASE:-bolao}"'
```

## Logs

### Ver Logs em Tempo Real

```bash
# Todos os serviços
wsl bash -c "docker-compose logs -f"

# Apenas aplicação
wsl bash -c "docker-compose logs -f app"

# Apenas banco
wsl bash -c "docker-compose logs -f db"
```

### Ver Últimas 100 Linhas

```bash
wsl bash -c "docker-compose logs --tail=100 app"
```

## Limpeza

### Remover Containers e Volumes

```bash
wsl bash -c "docker-compose down -v"
```

### Remover Imagens

```bash
wsl bash -c "docker-compose down --rmi all -v"
```

## Próximos Passos

Após subir a aplicação:

1. Acesse http://localhost:8080
2. Faça login com `admin` / `admin123`
3. Troque a senha padrão
4. Explore as funcionalidades

---

**Documentação Atualizada:** 2026-02-17
