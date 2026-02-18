# Plano de Correção do Docker Setup

**Data:** 2026-02-17  
**Responsável:** Kiro (Arquiteto de Software Sênior)  
**Objetivo:** Corrigir Dockerfile e docker-compose.yml para permitir execução completa da aplicação

## Problemas Identificados

### 1. ❌ Estrutura do Banco de Dados Não é Criada
- Não há script SQL de inicialização
- Hibernate pode criar tabelas via `hbm2ddl.auto`, mas não é recomendado para produção
- Dados iniciais (equipes, jogos, usuário admin) não são inseridos

### 2. ❌ Credenciais do Banco Inconsistentes
- **docker-compose.yml** define: `user_bolao` / `pass_bolao`
- **applicationContext-resources.xml** espera variáveis de ambiente, mas tem defaults diferentes
- Falta de consistência pode causar erro de conexão

### 3. ❌ Falta de Health Check
- Aplicação pode tentar conectar ao banco antes dele estar pronto
- Sem `depends_on` com `condition: service_healthy`

### 4. ❌ Falta de Script de Inicialização do Banco
- Nenhum arquivo `.sql` para criar schema
- Nenhum dado inicial (seed data)

### 5. ❌ Configuração do Hibernate Não Verificada
- Precisa verificar se `hbm2ddl.auto` está configurado
- Pode estar como `validate` (não cria tabelas) ou `none`

## Solução Proposta

### Abordagem 1: Usar Hibernate para Criar Tabelas (Desenvolvimento)
**Prós:**
- Rápido para desenvolvimento
- Não precisa criar SQL manualmente
- Hibernate gera schema a partir dos mapeamentos `.hbm.xml`

**Contras:**
- Não recomendado para produção
- Não insere dados iniciais
- Pode gerar schema subótimo

### Abordagem 2: Criar Script SQL Completo (Recomendado)
**Prós:**
- Controle total sobre o schema
- Pode incluir dados iniciais (seed data)
- Recomendado para produção
- Versionável e auditável

**Contras:**
- Mais trabalhoso inicialmente
- Precisa manter sincronizado com entidades

**DECISÃO: Usar Abordagem 2 (Script SQL)**

## Plano de Ação

### Passo 1: Analisar Mapeamentos Hibernate
- [ ] Ler todos os arquivos `.hbm.xml`
- [ ] Identificar tabelas, colunas, chaves, relacionamentos
- [ ] Documentar estrutura completa

### Passo 2: Gerar Script SQL de Criação
- [ ] Criar `docker/mysql/init/01-schema.sql`
- [ ] Definir todas as tabelas baseado nos `.hbm.xml`
- [ ] Adicionar índices e constraints
- [ ] Adicionar comentários explicativos

### Passo 3: Criar Script de Dados Iniciais
- [ ] Criar `docker/mysql/init/02-seed-data.sql`
- [ ] Inserir usuário admin padrão
- [ ] Inserir dados de exemplo (opcional)

### Passo 4: Atualizar docker-compose.yml
- [ ] Adicionar volume para scripts de inicialização
- [ ] Adicionar health check no serviço `db`
- [ ] Configurar `depends_on` com condição de saúde
- [ ] Padronizar variáveis de ambiente

### Passo 5: Atualizar Dockerfile
- [ ] Adicionar wait-for-it.sh ou similar (opcional)
- [ ] Garantir que aplicação aguarda banco estar pronto

### Passo 6: Criar Documentação
- [ ] Atualizar README.md com instruções de execução
- [ ] Documentar variáveis de ambiente
- [ ] Adicionar troubleshooting

### Passo 7: Testar
- [ ] Executar `docker-compose up --build`
- [ ] Verificar criação do banco
- [ ] Verificar conexão da aplicação
- [ ] Testar login e funcionalidades básicas

## Estrutura de Arquivos Proposta

```
projeto/
├── docker/
│   └── mysql/
│       └── init/
│           ├── 01-schema.sql
│           └── 02-seed-data.sql
├── docker-compose.yml (atualizado)
├── Dockerfile (atualizado)
└── README.md (atualizado)
```

## Cronograma

| Passo | Estimativa | Prioridade |
|-------|------------|------------|
| 1. Analisar Mapeamentos | 30 min | Alta |
| 2. Gerar Script SQL | 1-2 horas | Alta |
| 3. Dados Iniciais | 30 min | Média |
| 4. Atualizar docker-compose | 15 min | Alta |
| 5. Atualizar Dockerfile | 15 min | Média |
| 6. Documentação | 30 min | Média |
| 7. Testes | 30 min | Alta |
| **TOTAL** | **3-4 horas** | - |

## Riscos

| Risco | Probabilidade | Impacto | Mitigação |
|-------|---------------|---------|-----------|
| Schema SQL incorreto | Média | Alto | Validar com Hibernate Validator |
| Dados iniciais inválidos | Baixa | Médio | Testar inserção manualmente |
| Timeout de inicialização | Média | Médio | Adicionar health check e wait |
| Incompatibilidade MySQL 8 | Baixa | Alto | Testar com versão específica |

## Critérios de Sucesso

✅ Banco de dados criado automaticamente  
✅ Tabelas criadas com schema correto  
✅ Dados iniciais inseridos  
✅ Aplicação conecta ao banco sem erros  
✅ Login funciona com usuário padrão  
✅ Documentação atualizada  
✅ `docker-compose up` funciona sem intervenção manual  

---

**Próximo Passo:** Executar Passo 1 (Analisar Mapeamentos Hibernate)
