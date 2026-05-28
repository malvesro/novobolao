# Sessão: Integração da Carga de Dados para Nuvem (Hugging Face + Aiven)
Data: 2026-05-27

## Contexto
O sistema foi deployado com sucesso no Hugging Face conectado ao Aiven MySQL, porém o banco de dados estava vazio. A carga de dados da Copa 2026 (equipes e jogos) estava configurada apenas nos scripts de inicialização do Docker local e não era executada automaticamente pela aplicação Java ao conectar em um banco externo.

## Ações Realizadas
1.  **Mapeamento do Problema**: Identificado que a configuração de `jdbc:initialize-database` no `applicationContext-resources.xml` não contemplava o script de dados da Copa 2026 (`03-copa-2026-data.sql`).
2.  **Preparação do Script**: Copiado o arquivo `docker/mysql/init/03-copa-2026-data.sql` para `src/main/resources/database/copa-2026-data.sql` para que seja incluído no empacotamento WAR.
3.  **Configuração do Spring**: 
    -   Atualizado o `applicationContext-resources.xml` para incluir `copa-2026-data.sql` na inicialização automática.
    -   Adicionada a propriedade `enabled="${DB_INITIALIZE:true}"` para permitir desativar a inicialização automática via variável de ambiente após a primeira carga.
    -   Configurado encoding UTF-8 para o script de dados para evitar problemas com acentuação.

## Conclusões Técnicas
A utilização de `<jdbc:initialize-database>` é a forma mais direta de garantir que ambientes de nuvem (PaaS) tenham o banco sincronizado sem a necessidade de acesso manual ao console do banco de dados (Aiven). Ao adicionar o suporte a variáveis de ambiente, garantimos flexibilidade para o usuário ativar/desativar este comportamento conforme necessário.

## Próximos Passos
1.  Realizar o commit das alterações.
2.  Instruir o usuário a fazer o redeploy no Hugging Face.
3.  Recomendar que, após a confirmação da carga, o usuário defina `DB_INITIALIZE=false` nas Settings do Hugging Face para evitar TRUNCATEs acidentais em reinicializações futuras (embora o script atual use ON DUPLICATE KEY para o JOG_JOGO, ele ainda faz TRUNCATE nas tabelas iniciais).

> `Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]`
