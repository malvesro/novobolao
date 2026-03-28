# Sessao: Adequação CSP popup palpites

**Data:** 2026-02-27
**Autor:** Assistente Tecnico Lider/IA
**Skills Utilizadas:** modernization-java-migration v1.0.0

## 1. Prompt Principal (Input)

> O problema permanece, ao clicar na linha do jogo, nem a linha é selecionada, nem o popup de palpite é mostrado. Melhor prossegir para a próxima tarefa de adequação completa da CSP na subtarefa 3c para ver se resolve ?

## 2. Resposta da IA (Output Resumido)

> Identifiquei que o CSP report-only vigente bloqueava scripts inline/HTMX por ausência de nonce, resultando no import do bundle como `https://localhost:8443/`. Criei o filtro `com.opendev.bolao.security.CspNonceFilter` que gera nonce por requisição, remove o header estático em `applicationContext-security.xml` e aplica `Content-Security-Policy` com `'strict-dynamic'` + `nonce`. Atualizei todos os `<script>` relevantes (`cabecalho.jspf`, `menu.jspf`, `cadastro.jsp`, `admin/inclusaoJogo.jsp`, `login.jsp`) para usar `nonce="${cspNonce}"`. Rebuild executado com `npm run build` e suíte `mvn -Dfrontend.skip=true test` (24 testes verdes).

## 3. Validacao (Build/Teste)

- Comando: npm run build
- Resultado: Sucesso
- Observacoes: Manifest mantém `js/main-DNF89Gpv.js`.

- Comando: mvn -Dfrontend.skip=true test
- Resultado: Sucesso
- Observacoes: 24 testes executados com sucesso; aviso conhecido do Log4j persiste.

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** N/A

## 5. Atualização 27/02/2026 21:52 BRT

- Sintoma persiste no ambiente Docker: hover destaca a linha, mas o clique não abre o popup; console indica CSP em modo report-only registrando violações para `/js/htmx.min.js`, bloco inline principal e `/assets/js/main-DNF89Gpv.js`. Apesar da violação reportada, o manifest é resolvido com status 200 e o bundle é importado (telemetria `[bolao:bundle-loader]` e `[bolao:jogos]` mostram `initJogosPage` concluído e eventos associados).
- Conclusão parcial: ainda falta propagar nonce em todas as respostas HTMX/partials (possivelmente loaders HTMX em fragmentos ou scripts retornados) ou remover o modo report-only. Próxima etapa: revisar fragments HTMX (`seguro/jogos.jsp`, includes via `skipTemplate`) para garantir que nenhum `<script>` inline é reemitido sem nonce e planejar substituição dos scripts inline restantes (menu, fallback) por módulos externos.
- Ponto de parada: investigar subtarefa 3b (remover uso de `$j`/scripts inline no menu) e mapear scripts inline restantes em fragmentos HTMX antes de promover CSP para modo enforcement.
