# Sessao: Correcao de 404 no fluxo de login (`j_security_check`)

**Data:** 2026-06-11
**Autor:** Assistente Tecnico Lider/IA
**Skills Utilizadas:** architecture-guardian v1.0.0, senior-java-dev-legacy v1.0.0

## 1. Prompt Principal (Input)

> Muitas vezes ao tentar fazer o login, ocorre erro de segurança em `/j_security_check` com HTTP 404. Averigue causas e melhor ajuste (justificado).

## 2. Resposta da IA (Output Resumido)

> - Causa reproduzida: `GET /j_security_check` retornava 404 no Tomcat em cenários de reuso de URL.
> - Durante o ajuste, foi identificado um problema adicional de inicialização: `struts.xml` com ordem de elementos incompatível com o DTD (`default-action-ref` posicionado após `global-exception-mappings`), derrubando o filtro Struts em runtime.
> - Ajuste final adotado:
>   1) `login-processing-url` alterado para `/login.action` (Spring Security processa o POST antes do Struts);
>   2) `login.jsp` com action context-aware absoluta e CSRF explícito no formulário;
>   3) correção da ordem no `struts.xml` para conformidade com DTD e bootstrap estável;
>   4) compatibilidade legada validada sem action extra: `GET /j_security_check` passou a responder `302` para `/login.action`.

## 3. Arquivos alterados

- `src/main/resources/applicationContext-security.xml`
- `webapp/WEB-INF/content/login.jsp`
- `src/main/resources/struts.xml`
- `passo-a-passo.md`

## 4. Validacao (Build/Teste)

- Reproducao inicial:
  - `GET /j_security_check` -> HTTP 404 (confirmado)
- Diagnostico adicional (runtime):
  - erro de parsing Struts em startup: `The content of element type "package" must match ...` (linha 112 do `struts.xml`), bloqueando o filtro Struts.
- Pos-ajuste (validado em runtime com rebuild via Docker):
  - `GET /login.action` -> HTTP 200;
  - `POST /login.action` com CSRF + credenciais válidas (`admin/admin123`) -> HTTP 302 para `/seguro/principal.action`;
  - `GET /seguro/palpites.action` com sessão autenticada -> HTTP 200;
  - `GET /j_security_check` -> HTTP 302 para `/login.action` (sem 404).
- Testes automatizados:
  - `mvn -Dfrontend.skip=true test` -> 52 testes, 0 falhas, 0 erros.

## 5. Justificativa tecnica da solucao

1. **Confiabilidade do endpoint:** usar `/login.action` como processing URL elimina dependência de endpoint legado sem representação GET.
2. **Compatibilidade de UX:** usuario permanece no mesmo endpoint visual de login (GET) e o POST é tratado no mesmo path de forma transparente.
3. **Retrocompatibilidade:** mantendo a segurança na borda, `GET /j_security_check` é redirecionado para o login sem expor endpoint órfão no Struts.
4. **Seguranca mantida:** CSRF continua obrigatório e agora também fica explícito no próprio formulário.

## 6. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** Correcao prioriza estabilidade operacional sem relaxar controles de seguranca e elimina uma fragilidade estrutural de inicializacao do Struts.
