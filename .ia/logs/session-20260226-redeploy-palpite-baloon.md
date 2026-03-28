# Sessao: Rebuild e publicação pós-correção palpite

**Data:** 2026-02-26
**Autor:** Assistente Tecnico Lider/IA
**Skills Utilizadas:** modernization-java-migration v1.0.0

## 1. Prompt Principal (Input)

> faça o rebuild e publique a aplicação atualizada para eu testar.

## 2. Resposta da IA (Output Resumido)

> Pipeline completo executado após a correção do atributo `data-palpite-allowed` em `webapp/WEB-INF/content/seguro/jogos.jsp`. O Vite recompilou os assets, o Maven gerou o WAR 0.2.7-SNAPSHOT e o container `novobolao-app` foi reconstruído e reiniciado via Docker Compose. Smoke automático com `curl` confirmou a exibição da nova versão no login e o rótulo “Senha”.

## 3. Validacao (Build/Teste)

- Comando: npm install
  - Resultado: Sucesso
  - Observacoes: 46 pacotes reinstalados; sem alterações em `package-lock.json`.
- Comando: npm run build
  - Resultado: Sucesso
  - Observacoes: Bundle principal `main-CSYQJaVV.js` recompilado (~9.88 kB).
- Comando: mvn clean package -Dfrontend.skip=false
  - Resultado: Sucesso
  - Observacoes: 24 testes executados; aviso já conhecido do Log4j sobre provider ausente.
- Comando: docker compose build app
  - Resultado: Sucesso
  - Observacoes: Imagem `novobolao-app` reconstruída (sha256:3d7acba9…92028).
- Comando: docker compose up -d app
  - Resultado: Sucesso
  - Observacoes: Containers `bolao-db` e `bolao-app` saudáveis; app recriado com WAR publicado.
- Comando: docker compose exec app curl -k -s https://localhost:8443/login.action \| grep -i "Versão"
  - Resultado: `Versão 0.2.7-SNAPSHOT - compilado em 26/02/2026 18:15`
  - Observacoes: Confirmação da versão após rebuild.
- Comando: docker compose exec app curl -k -s https://localhost:8443/login.action \| grep -i "Senha"
  - Resultado: `Senha`
  - Observacoes: Verifica renderização do rótulo de senha pós-deploy.

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** Nenhuma intervenção manual adicional foi necessária durante o pipeline.

---

## 5. Atualização 26/02/2026 18:36 BRT

- **Motivação:** Publicar a liberação de `/assets/**` no Spring Security e garantir que o bundle Vite volte a carregar para usuários autenticados.
- **Pipeline executado:** `npm run build`; `mvn clean package -Dfrontend.skip=false`; `docker compose build app`; `docker compose up -d app`.
- **Smoke pós-deploy:** `curl -k -s -b /tmp/bolao_marcio_cookies.txt https://localhost:8443/assets/.vite/manifest.json` e `/assets/js/app-bundle.js` retornam HTTP 200, confirmando acesso aos assets.
- **Status:** Aplicação 0.2.7-SNAPSHOT ativa com a correção de segurança aplicada; próximos passos focam na validação manual do popup de palpite.
