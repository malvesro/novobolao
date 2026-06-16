# Sessao: Tarefa 70 - Pontuacao em Tempo Real a Partir do Inicio do Jogo

**Data:** 2026-06-16
**Autor:** Codex (GPT-5)
**Skills Utilizadas:** senior-java-dev-legacy v1.0.0, architecture-guardian v1.0.0

## 1. Prompt Principal (Input)

> "ajuste a regra: A partir do horário de início do jogo, qualquer atualização do resultado do jogo (pelo administrador) já permite o cálculo da pontuação para todos e atualização da classificação geral."

## 2. Resposta da IA (Output Resumido)

> Regra temporal de pontuação ajustada para comportamento em tempo real durante a partida.
> `Jogo.jaOcorreu()` deixou de esperar +2h e passa a considerar jogo ocorrido a partir do horário de início.
> Para evitar atraso por cache, `ParticipanteServiceImpl` passou a revalidar automaticamente o ranking quando muda o minuto corrente (timezone canônico), garantindo atualização da classificação sem depender de nova gravação no banco.

**Arquivos alterados:**
- `src/com/opendev/bolao/model/Jogo.java`
- `src/com/opendev/bolao/service/impl/ParticipanteServiceImpl.java`
- `tests/com/opendev/bolao/model/JogoTest.java`
- `tests/com/opendev/bolao/service/impl/ParticipanteServiceTest.java`
- `passo-a-passo.md`

## 3. Validacao (Build/Teste)

- Comando: `mvn -Dfrontend.skip=true -Dtest=JogoTest,ParticipanteServiceTest,PalpiteAuthorizationServiceImplTest test`
- Resultado: **Sucesso** (`20` testes, `0` falhas).
- Observacoes:
  - `JogoTest` atualizado para validar `jaOcorreu()` imediatamente após início.
  - Novo teste em `ParticipanteServiceTest` valida invalidação do cache por avanço temporal (bucket de minuto).

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** regra de pontuação em tempo real entregue sem alterar a regra de bloqueio de palpites (continua até 1h antes do início).
