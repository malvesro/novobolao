# Plano: Correção login 500 após sincronização CSRF

**Data:** 2026-02-25  
**Responsável:** Assistente Técnico Líder/IA  
**Contexto:** Após o rebuild de 25/02/2026, o login (`/j_security_check`) passou a responder com HTTP 500. Os POSTs HTMX também devolvem 403. O log do Tomcat (localhost.2026-02-25.log) registra `java.lang.ArrayIndexOutOfBoundsException` na classe `org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler` ao manipular o token.

## Evidências coletadas
- Stack trace completo no arquivo `/usr/local/tomcat/logs/localhost.2026-02-25.log` às 15:26:29 UTC, logo após o deploy.
- Os headers enviados carregam o cookie `XSRF-TOKEN` (35 bytes) e meta `_csrf` (valor sobrescrito pelo script).  
- A exceção ocorre antes de qualquer autenticação, abortando o fluxo e mantendo o usuário não logado.

## Objetivo
Restaurar o fluxo de login e as requisições HTMX evitando a exceção no handler `XorCsrfTokenRequestAttributeHandler`, mantendo a sincronização de tokens sem violar as expectativas de comprimento do Spring Security.

## Análise inicial
1. O handler XOR espera que o token não tenha sido alterado manualmente; ao reescrever o cookie/meta com valores de comprimento diferente, o `arraycopy` rompe (destino 36, origem 35).  
2. A sincronização automática implantada no `cabecalho.jspf` pode estar sobreescrevendo o token com string concatenada incorreta ou com caracteres extras (ex.: `%` não decodificado).  
3. O envio de `_csrf` via `hx-include` precisa ser mantido, mas sem manipular diretamente o conteúdo protegido pelo handler do Spring.

## Etapas propostas
1. **Diagnóstico detalhado do token**
   - Capturar via browser (DevTools) o valor exato do cookie `XSRF-TOKEN` antes e depois do script de sincronização.
   - Confirmar se o meta `_csrf` é idêntico ao cookie (mesmo comprimento, sem caracteres adicionais).
   - Inspecionar `cabecalho.jspf` para garantir que não estamos codificando duas vezes ou forçando `SameSite`/`secure` de maneira incompatível.
2. **Revisar estratégia de propagação**
   - Avaliar uso do `XorCsrfTokenRequestAttributeHandler` nativo (sem reatribuição manual) mantendo apenas `hx-include="#csrfTokenField"` com `_csrf` injetado pelo próprio Spring.
   - Se necessário, substituir a sincronização por chamada a `htmx:configRequest` lendo diretamente `event.detail.parameters` sem reescrever meta/cookie.
3. **Ajustar e validar**
   - Implementar correção nos fragmentos JSP/JS, garantindo que o cookie/meta permaneçam no formato original.
   - Reexecutar `mvn -q -Dfrontend.skip=true test`.
   - Executar `mvn clean package -Dfrontend.skip=false`, `docker compose build app`, `docker compose up -d app`.
4. **Smoke tests**
   - Login com `admin/admin123`.
   - Alteração HTMX no combo “Autorizado”.
   - Registrar evidências (prints + captura network) e atualizar log de sessão.

## Riscos
- Persistir a exceção e impedir qualquer autenticação.
- Quebra de compatibilidade com futuras atualizações do Spring Security (handler XOR ativado por padrão).
- Impacto em outras telas que dependem do token `_csrf` (palpites, formulários públicos).

## Critérios de aceite
- Login (`/j_security_check`) retorna 302 para `/seguro/principal.action` sem erro.
- Requisições HTMX administrativas retornam 200 (ou 204) e atualizam a tabela.
- Nenhuma exceção relacionada a CSRF aparece nos logs após o smoke.

## Execução 25/02/2026 13:45 BRT – Correção aplicada
- Substituída a lógica de sincronização no `webapp/WEB-INF/content/template/cabecalho.jspf`, evitando reescrita do cookie `XSRF-TOKEN` e passando a atualizar meta tags, campo hidden e formulários a partir dos cabeçalhos `X-CSRF-*`. Eventos `fetch`/HTMX agora renovam o token sem tocar no cookie gerenciado pelo `CookieCsrfTokenRepository`.
- Campo global `#csrfTokenField` criado somente após `DOMContentLoaded`, impedindo exceções por `document.body` nulo e garantindo compatibilidade com `hx-include`.
- Testes executados: `mvn -q -Dfrontend.skip=true test`, `mvn clean package -Dfrontend.skip=false`; deploy Docker com `docker compose build app` e `docker compose up -d app`.
- Smoke técnico: autenticação validada com `curl` (`admin/admin123`) via `docker compose exec app`, confirmando resposta 302 para `/seguro/principal.action` e ausência de `ArrayIndexOutOfBoundsException` no log `localhost.2026-02-25.log`.
- Skill aplicada: `modernization-java-migration v1.0.0`.
