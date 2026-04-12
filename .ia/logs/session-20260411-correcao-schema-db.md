# Sessão: Correção de Erro no Container (Schema Mismatch) - 11/04/2026

## 1. Identificação do Problema
O sistema está fora do ar após o rebuild e publicação devido ao erro `unhealthy` no container `bolao-app`.

### Analise Técnica:
- **Erro:** `org.hibernate.tool.schema.spi.SchemaManagementException: Schema-validation: missing column [PAR_DH_ULTIMA_TROCA_SENHA] in table [PAR_PARTICIPANTE]`.
- **Causa Raiz:** A coluna `PAR_DH_ULTIMA_TROCA_SENHA` foi adicionada recentemente ao script de inicialização `docker/mysql/init/01-schema.sql`, mas como o volume de dados do Docker (`db_data`) já existia, o script não foi reexecutado na base de dados já persistida.
- **Consequência:** O Hibernate, configurado com `hibernate.hbm2ddl.auto=validate`, bloqueia a inicialização do Spring Context ao detectar a discrepância, fazendo com que o Tomcat responda com HTTP 404 para o Healthcheck.

## 2. Verificação do docker-compose.yml
O arquivo `docker-compose.yml` foi revisado e **não apresenta erros de sintaxe ou lógica**. As configurações de rede, dependências e variáveis de ambiente estão corretas. O comportamento de "não atualizar o banco" é intrínseco ao funcionamento do Docker com scripts de `init` e volumes persistentes.

## 3. Plano de Correção
Dada a natureza do erro, a solução é aplicar a alteração de schema manualmente no banco de dados em execução.

### Atividades:
1. Executar `ALTER TABLE` para adicionar a coluna faltante.
2. Reiniciar o container `bolao-app`.
3. Validar o status de saúde.

## 4. Execução das Correções

### Passo 1: Adicionar coluna ao banco
```bash
docker exec -it bolao-db mysql -u root -proot bolao -e "ALTER TABLE PAR_PARTICIPANTE ADD COLUMN PAR_DH_ULTIMA_TROCA_SENHA TIMESTAMP NULL COMMENT 'Data/hora da última troca de senha';"
```

### Passo 2: Reiniciar aplicação
```bash
docker restart bolao-app
```

## 5. Próximos Passos
- Monitorar os logs de inicialização.
- Validar se o sistema volta a responder HTTP 200 em `/index.action`.

> `Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]`
