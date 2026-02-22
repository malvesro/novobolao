# Plano: Correção Final de Bandeiras e Atualização dos Dados da Copa 2026

**Data:** 2026-02-22  
**Responsável:** Assistente Técnico Líder  
**Contexto:** Bandeiras de Chile e França ainda apresentam inconsistências (asset incorreto/obsoleto). Também foi reportada a grafia exibida como “FranÃ§a”, indicando problema de codificação. Além disso, a planilha `data/Copa_do_Mundo_2026_Fase_de_Grupos_Completa_Brasilia.xlsx` traz jogos oficiais corrigidos, exigindo atualização integral do dataset (grupos, datas, horas, cidades e confrontos) usado pelo sistema.

## Objetivos
1. Garantir que todos os assets de bandeiras estejam corretos (especialmente Chile e França) e que a renderização preserve acentos.
2. Reprocessar os jogos da Copa 2026 a partir da nova planilha, ajustando nomes de cidades, grupos, equipes, datas e horários.
3. Validar a aplicação end-to-end após as correções.

## Etapas Planejadas

### 1. Diagnóstico e Correção dos Assets de Bandeiras
- Verificar arquivos atuais (`webapp/img/bandeiras/cl.png`, `fr.png`) e validar dimensões/qualidade.
- Regenerar/baixar novamente usando fontes oficiais (FlagCDN), garantindo sobrescrita.
- Atualizar scripts (`scripts/download_flags.py` / `download_missing_flags.py`) se necessário para permitir “force download”.
- Regenerar `flags.properties` caso ocorram alterações.

### 2. Ajustes de Codificação/Renderização
- Revisar configuração de encoding (JSPs, `web.xml`, filtros de encoding).
- Garantir que respostas sejam servidas em UTF-8 e que dados oriundos do banco não sofram dupla conversão.
- Validar páginas (`login`, `seguro/principal.jsp`, `seguro/jogos.jsp`) para confirmar grafia correta (ex.: “França”).

### 3. Atualização dos Dados dos Jogos
- Inspecionar `data/Copa_do_Mundo_2026_Fase_de_Grupos_Completa_Brasilia.xlsx` e documentar formato/abas.
- Adaptar `scripts/atualizar_copa2026_dataset.py` (ou criar novo pipeline) para consumir a planilha e gerar:
  - CSV/JSON intermediários atualizados.
  - Script SQL (`data/sql/03-copa-2026-data.sql`) com grupos, equipes, jogos (datas, horários, cidades).
- Executar o script no banco local (Docker) para refletir os novos dados.
- Atualizar testes/dataset auxiliar (`copa2026_tabela_brt_final.csv`) se necessário.

### 4. Validação Integrada
- Rodar `mvn -q -Dfrontend.skip=true test`.
- Recriar imagem Docker e executar smoke manual:
  - Autenticar como `admin`.
  - Verificar telas: atualização de resultados, listagens de jogos, fases.
  - Conferir bandeiras de Chile e França especificamente.
- Atualizar evidências (`telas/`) com novos screenshots.

### 5. Documentação e Follow-up
- Registrar logs de sessão para cada etapa relevante.
- Atualizar `passo-a-passo.md` com progresso/novas subtarefas.
- Preparar orientações finais (ex.: reconstrução de dados em outros ambientes).

## Riscos e Mitigações
- **Diferenças de timezone/formato na planilha:** validar timezone (Brasília) antes de carregar.
- **Inconsistência no banco após sobrescrita:** executar backup/take snapshot antes do update.
- **Impacto em funcionalidades dependentes** (relatórios, dashboards): rodar smoke completo e ajustar testes conforme necessário.

## Critérios de Conclusão
- Bandeiras de todas as seleções em especial Chile e França exibidas corretamente nas telas autenticadas.
- Nomes com acentuação normalizados (sem “FranÃ§a”).
- Tabelas/agenda de jogos alinhadas com a planilha oficial (grupos, cidades, horários).
- Testes automatizados verdes e evidências documentadas.
