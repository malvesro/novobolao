# ADR 003: Restrição de Perfil Administrador em Funcionalidades de Participação

**Data:** 28 de Março de 2026  
**Status:** Aceito

## Contexto

No sistema do `novobolao`, o perfil de Administrador (`ROLE_ADMIN`) é destinado à gestão técnica de dados (cidades, equipes, resultados oficiais). Atualmente, não há um bloqueio sistêmico que impeça esses usuários de participarem ativamente das apostas (palpites), o que pode distorcer a competitividade do ranking e os dashboards de desempenho coletivos.

## Decisão

Isolar completamente os perfis administrativos das funcionalidades de participação competitiva baseando-se estritamente no papel/nível (`ROLE_ADMIN`) associado ao usuário.

1. **Bloqueio de Palpites**: O serviço de autorização de palpites deve identificar a autoridade `ROLE_ADMIN` e negar a permissão, classificando-a como `ADMIN_RESTRICTED`. 
2. **Exclusão de Rankings**: A lógica de cálculo da classificação (`buscarClassificacao`) passará a filtrar e remover qualquer usuário que possua o privilégio administrativo.
3. **Exclusão de Dashboards**: Como consequência do filtro de classificação, o gráfico de líderes e as comparações de performance individual não exibirão mais administradores.
4. **Resiliência da Ação**: O bloqueio deve ocorrer tanto na camada visual (botões desabilitados/badge informativa) quanto na camada de persistência (re-validação no salvamento).

## Consequências

**Positivas:**
* **Integridade do Ranking**: O ranking passa a refletir apenas participantes reais, eliminando "ruído" de usuários técnicos.
* **Foco Administrativo**: Garante que o usuário admin veja o placar oficial de forma clara, sem ser confundido com campos de palpites individuais.

**Negativas:**
* **Impossibilidade de Teste de Aposta via Admin**: Para validar o fluxo de usuário, o desenvolvedor/admin precisará usar uma conta que possua apenas o papel `ROLE_USER`.
* **Inconsistência de Histórico**: Se um admin já possuía palpites no passado, eles deixarão de ser computados no ranking imediatamente após o deploy desta regra.
