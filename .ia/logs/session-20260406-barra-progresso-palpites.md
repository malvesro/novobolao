# Sessao: Barra de progresso de palpites (HTMX + filtro)

## 1. Prompt Principal (Input)
Ajustar a barra de progresso da tela de palpites para refletir palpites preenchidos / total de jogos exibidos pelo filtro atual e atualizar após cada salvamento HTMX. Registrar atividade no passo-a-passo.

## 2. Resposta da IA (Output Resumido)
- Implementado filtro funcional via JPA e contagem de jogos com base no filtro atual.
- Adicionado cálculo de progresso (preenchidos/total e percentual) na action de palpites.
- Criado fragmento JSPF da barra com suporte a swap OOB e integrado na tela e resposta HTMX.

## 3. Validacao (Build/Teste)
- `npm run build`
- `mvn test`
- `mvn package`
- `docker compose up --build -d`

Observacoes:
- `mvn test` e `mvn package` emitiram warning do `sun.misc.Unsafe` e `Log4j API could not find a logging provider`, sem falha de build.
- `npm audit` reportou 2 vulnerabilidades moderadas (nao corrigidas nesta execucao).

## 4. Analise Humana (Veredito)
Pendente.
