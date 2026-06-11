# Sessao: Validação de consistência de classificação (Home vs Classificação Geral)

**Data:** 2026-06-11
**Autor:** Codex (GPT-5)
**Skills Utilizadas:** ui-ux-pro-max v1.0.0, senior-java-dev-legacy v1.0.0

## 1. Prompt Principal (Input)

> "Adicione também a verificação se a ordem de classificação da tela principal segue a mesma ordem da tela de classificação geral, as duas não podem estar diferentes e gerar confusão. Valide as duas conforme as regras."

## 2. Resposta da IA (Output Resumido)

- Validação de consistência entre telas:
  - `principal.jsp` usa `/seguro/graficoLiderancaImagem.action`;
  - `ParticipanteAction.gerarGraficoLiderancaImagem()` chama `ParticipanteService.construirGraficoDeBarrasDosLideres()`;
  - `construirGraficoDeBarrasDosLideres()` usa `buscarClassificacao()` + `Collections.sort(participantes)`;
  - `classificacao.jsp` recebe `participantes` de `ParticipanteAction.buscarParticipantes()`, que também usa `buscarClassificacao()` + `Collections.sort(...)`.
  - Conclusão: as duas telas compartilham a mesma base de ordenação (não há bifurcação de algoritmo hoje).

- Validação contra regras oficiais (`regras.jsp` / `messages.properties`):
  - Regras descritas: pontuação total, depois acertos totais (6 pts), depois acertos parciais com bônus (3 pts), depois ordem alfabética.
  - Implementação atual em `Participante.compareTo(...)`: pontuação total e, em empate, ordem alfabética.
  - Conclusão: existe gap de aderência entre regra publicada e implementação efetiva de desempate.

- Rastreabilidade:
  - `passo-a-passo.md` atualizado na Tarefa 45 com:
    - 45.1.1 (consistência entre telas) concluída;
    - 45.1.2 (validação contra regras) concluída com identificação de gap;
    - 45.1.3 criada para correção funcional e teste automatizado;
    - 45.1.4 criada para ajuste textual das regras de ordenação/desempate após a correção funcional.

## 3. Validacao (Build/Teste)

- Comando: não aplicável nesta sessão (análise estática + atualização de backlog).
- Resultado: não aplicável.
- Observacoes: próxima iteração deve incluir ajuste do comparador e cobertura de testes para cenários de empate.

## 4. Analise Humana (Veredito)

- [ ] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** decisão crítica para UX e confiança: manter uma única regra de classificação entre Home, Classificação Geral e Regras públicas.
