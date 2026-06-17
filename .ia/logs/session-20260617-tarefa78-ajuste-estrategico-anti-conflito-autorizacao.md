# Sessao: Tarefa 78.7 - Ajuste estrategico anti-conflito de autorizacao (palpites x grupo)

**Data:** 2026-06-17
**Autor:** Codex (GPT-5)
**Skills Utilizadas:** architecture-guardian v1.0.0, senior-java-dev-legacy v1.0.0, htmx (guideline)

## 1. Prompt Principal (Input)

> Voltou o problema de todos os jogos futuros nao permitirem palpite. Verificar o que a correcao de um atrapalha no funcionamento do outro e ajustar estrategicamente.

## 2. Resposta da IA (Output Resumido)

> Analise de conflito: apos centralizacao da autorizacao no backend, a view passou a depender 100% de `autorizacoesPalpitePorJogo`. Em cenarios de descompasso de contexto de seguranca (backend marcando `roleMissing` em request que a view reconhece como `ROLE_USER`), a tela pode degradar para bloqueio global aparente.

Ajuste estrategico aplicado:
- Mantida regra principal canonica do backend (`autorizacoesPalpitePorJogo`).
- Adicionado fallback **defensivo e restritivo** no `jogos-lista-fragmento.jsp` apenas quando:
  - autorizacao estiver ausente **ou** motivo vier como `roleMissing`; e
  - `request.isUserInRole('ROLE_USER')` for verdadeiro; e
  - `request.isUserInRole('ROLE_ADMIN')` for falso.
- No fallback, permissao segue estritamente `jogo.podeDarPalpite` (janela de 1h), mantendo admin bloqueado.

Resultado esperado:
- elimina bloqueio global indevido por divergencia de contexto,
- sem reintroduzir logica antiga de `sec:authorize` como regra primária,
- sem abrir excecao para admin.

## 3. Validacao (Build/Teste)

- Comando: `npm run test:frontend -- tests/frontend/jogos.test.js`
- Resultado: **Sucesso** (`14` testes aprovados)
- Observacoes: teste de contrato atualizado para exigir presenca do fallback por `request.isUserInRole(...)` e ausencia de `hasRole(...)` no fragmento.

- Comando: `mvn -Dfrontend.skip=true -Dtest=ParticipanteActionLoadTest,PalpiteAuthorizationServiceImplTest test`
- Resultado: **Sucesso** (`22` testes aprovados)
- Observacoes: sem regressao das regras de autorizacao temporal/perfil no backend.

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** estrategia adotada privilegia consistencia canônica e adiciona resiliencia controlada na camada de view para evitar falhas sistêmicas de UX em cenarios de contexto parcial.
