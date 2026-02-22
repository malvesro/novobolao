# Plano: Ajustes de Locais no `web.xml` e Correção de Layout em `admin/jogos`

**Data:** 2026-02-22  
**Responsável:** Assistente Técnico Líder  
**Contexto:** Foram solicitados dois aprimoramentos: (1) substituir a lista atual de estádios no parâmetro `locais` do `web.xml` pelos nomes das cidades-sede oficiais da Copa do Mundo 2026; (2) corrigir a renderização da tela administrativa de jogos (`/admin/jogos.action`), que apresenta várias linhas vazias entre a tabela de partidas e o rodapé, conforme evidência `telas/Erro-desing-tela.png`.

## Objetivos
1. Garantir que o contexto `locais` reflita os nomes das cidades-sede, preservando a compatibilidade com os fluxos que exibem os dados.
2. Eliminar o espaçamento indevido no layout administrativo, mantendo acessibilidade e responsividade.

## Etapa 1 – Atualização das Cidades em `web.xml`
1. **Inventário de utilização**  
   - Mapear onde o parâmetro `locais` é consumido (Actions, Services, JSPs).  
   - Confirmar dependências e validar se há testes cobrindo a funcionalidade.
2. **Coleta e validação das cidades-sede**  
   - Referenciar o dataset interno (`data/`, `scripts/`) e ADRs/planos da Fase 2.7 para listar as cidades oficiais.  
   - Caso haja divergências, alinhar com o planejamento de dados (`plano-fase-6-copa-2026.md`).
3. **Atualização do `web.xml`**  
   - Substituir a lista atual de estádios pelos nomes das cidades, mantendo ordenação alinhada às datas.  
   - Documentar a alteração via comentário sucinto e garantir formatação consistente.
4. **Validação**  
   - Executar `mvn -q -Dfrontend.skip=true test`.  
   - Realizar smoke no container (`docker compose build app` + `docker compose up -d app`).  
   - Capturar evidências das telas afetadas, registrando-as na pasta `telas/`.
5. **Documentação**  
   - Atualizar `passo-a-passo.md` (Fase 2.7) e registrar log de sessão específico.  
   - Considerar ADR caso a alteração impacte premissas de negócio (nomes exibidos aos usuários).

## Etapa 2 – (migrada para `.ia/planos/plano-layout-admin-jogos.md`)
