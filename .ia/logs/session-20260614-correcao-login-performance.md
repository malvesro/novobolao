# Sessão de Correção: Erro de Login após Hardening de Performance
**Data:** 14/06/2026
**Agente:** @arquiteto

## Contexto
Após o commit `8200384` (Hardening de Performance), o login passou a apresentar erro HTTP 404/500 com a mensagem `No result defined for action com.opendev.bolao.action.ParticipanteAction and result error`.

## Análise Técnica
1.  **Causa Raiz do Erro Struts:** A `ParticipanteAction.obterDadosPaginaPrincipal` (alvo do redirecionamento pós-login) não possuía um mapeamento para o resultado `error`. Quando uma exceção ocorria, o `global-exception-mappings` tentava retornar `error`, falhando por falta de definição.
2.  **Causa da Exceção Original:** 
    *   O `ParticipanteServiceImpl.buscarClassificacao()` passou a retornar uma lista imutável (`Collections.unmodifiableList`) para proteger o cache in-memory.
    *   A `ParticipanteAction.obterDadosPaginaPrincipal` ainda continha uma chamada a `Collections.sort(classificacao)`, o que disparava um `UnsupportedOperationException`.
    *   Adicionalmente, realizar ordenação fora da camada de serviço (e fora da transação) corria o risco de `LazyInitializationException` caso o cache estivesse expirado.

## Alterações Realizadas
1.  **`struts.xml`**: Adicionado `<global-results>` com `error` mapeado para `/WEB-INF/content/error.jsp` no pacote pai, garantindo que falhas futuras mostrem a página de erro em vez de um 404 genérico.
2.  **`ParticipanteServiceImpl.java`**: Alterado `buscarClassificacao()` para retornar uma `new ArrayList<>(cacheRanking)`. Isso permite que as Actions manipulem a lista (ex: remover o próprio participante para gráficos ou reordenar por nome) sem quebrar.
3.  **`ParticipanteAction.java`**: Removidas chamadas redundantes a `Collections.sort()` em `obterDadosPaginaPrincipal` e `buscarParticipantes`, já que o serviço já entrega a lista com a ordenação oficial do bolão.

## Validação Sugerida
1.  Reiniciar os containers Docker: `docker-compose down && docker-compose up -d`.
2.  Tentar o login novamente.
3.  Verificar se a página principal (dashboard) carrega corretamente com o ranking.

Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
