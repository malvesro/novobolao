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
- **Banco:** bolao
- **Usuário:** user_bolao
- **Senha:** pass_bolao
- **Root Password:** root

### 2. Tomcat 10.1 + Aplicação (app)
- **Porta:** 8080
- **URL:** http://localhost:8080
- **Runtime:** Java 17 + Jakarta EE 10

## Como Executar

### Pré-requisitos

- Docker instalado
- Docker Compose instalado
- Portas 8080 e 3306 disponíveis

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
- `MYSQL_ROOT_PASSWORD` - Senha do root
- `MYSQL_DATABASE` - Nome do banco
- `MYSQL_USER` - Usuário da aplicação
- `MYSQL_PASSWORD` - Senha do usuário

### Aplicação
- `DB_HOST` - Host do banco (padrão: db)
- `DB_NAME` - Nome do banco (padrão: bolao)
- `DB_USER` - Usuário do banco (padrão: user_bolao)
- `DB_PASS` - Senha do banco (padrão: pass_bolao)

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
wsl bash -c "docker exec -it bolao-db mysql -u user_bolao -ppass_bolao bolao"
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
