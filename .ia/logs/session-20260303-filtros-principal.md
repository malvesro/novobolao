# Sessao: Palpite inline – restauração do Principal HTMX

**Data:** 2026-03-03
**Autor:** Assistente Tecnico Lider/IA
**Skills Utilizadas:** modernization-java-migration v1.0.0

## 1. Prompt Principal (Input)

> Investigar o pipeline de filtros Spring para restaurar o Principal no HttpServletRequest durante chamadas HTMX, aplicar o ajuste definitivo e repetir os testes ROLE_USER/ROLE_ADMIN registrando evidências.

## 2. Resposta da IA (Output Resumido)

> - Adicionei `RequestContextFilter` ao `web.xml` após a cadeia `springSecurityFilterChain` e ajustei `RequestUtils.getRequest()` para priorizar o `HttpServletRequest` fornecido pelo Struts, mantendo fallback via `RequestContextHolder`.  
> - Incluí telemetria `[SEC][HTMX] principal recuperado via HttpServletRequest ...` quando o `getUserPrincipal()` atende HTMX, permitindo rastrear o uso direto do wrapper fornecido por Spring Security.  
> - Recompilei o projeto (`mvn -Dfrontend.skip=true test`, `mvn clean package -Dfrontend.skip=false`) e reconstrui a imagem Docker.  
> - Validei os fluxos HTMX com `curl` dentro do container para `palpiteFormPartial`/`atualizarPalpitePartial` com `palpiteuser` (ROLE_USER) e `admin` (ROLE_ADMIN); os logs agora mostram o principal disponível via `HttpServletRequest` e o update retornando `resultado=success`.  
> - Finalizei derrubando os containers (`docker compose down`) e preservei as evidências no `docker compose logs app`.

## 3. Validacao (Build/Teste)

- Comando: `mvn -Dfrontend.skip=true test`  
  Resultado: Sucesso (28 testes verdes; avisos de APIs deprecatadas já conhecidos)
- Comando: `mvn clean package -Dfrontend.skip=false`  
  Resultado: Sucesso (empacotamento WAR completo)
- Comando: `docker compose build app`  
  Resultado: Imagem `novobolao-app` rebuildada com o novo WAR
- Comando: `docker compose up -d app` → `curl -k … palpiteFormPartial/atualizarPalpitePartial` (ROLE_USER/ROLE_ADMIN)  
  Resultado: HTTP 200 com fragmentos HTML corretos; logs registram `[SEC][HTMX] principal recuperado via HttpServletRequest name=palpiteuser/admin` e `[HTMX][UPDATE] resultado=success palpiteAtualizado=true`
- Comando: `docker compose down`  
  Resultado: Containers e rede removidos sem erros

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** Com o principal propagado diretamente no `HttpServletRequest`, a dependência do fallback no `SecurityContextHolder` fica restrita a cenários não Struts. Próximo passo do plano: prosseguir para os ajustes CSP/UX conforme `plano-correcao-palpites-popup.md` (subtarefas remanescentes).
