# Sessao: Ajuste UX para empates amplos (Home vs Classificação Geral)

**Data:** 2026-06-11
**Autor:** Codex (GPT-5)
**Skills Utilizadas:** ui-ux-pro-max v1.0.0, architecture-guardian v1.0.0

## 1. Prompt Principal (Input)

> "No início do bolão, nos primeiros jogos, é normal haver empate de várias pessoas na liderança e outras posições (...) Melhor otimizar o espaço em tela ? Só mostrar a quantidade de pessoas empatadas ? O detalhe ficaria apenas na tela de classificação geral ? Como UX, o que ficaria melhor ? Ajuste nas subtarefas."

## 2. Resposta da IA (Output Resumido)

- Decisão UX consolidada no plano:
  - **Home** deve permanecer compacta e orientada a resumo (Top 3 + contexto agregado de empate).
  - **Classificação Geral** deve concentrar o detalhamento de todos os empatados e posições.
- Atualizações no `passo-a-passo.md`:
  - diretriz explícita para empates amplos na Tarefa 45;
  - novas subtarefas 45.3.6 (otimização de espaço na home) e 45.3.7 (CTA para classificação completa);
  - nova subtarefa 46.1.1 para estratégia de detalhamento de empatados na Classificação Geral.

## 3. Validacao (Build/Teste)

- Comando: não aplicável (ajuste de backlog/documentação).
- Resultado: não aplicável.
- Observacoes: sem alteração funcional nesta sessão.

## 4. Analise Humana (Veredito)

- [ ] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** decisão reduz sobrecarga visual da home e evita conflito de propósito com a tela de classificação completa.
