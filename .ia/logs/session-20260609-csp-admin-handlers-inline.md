# Sessao: Correção de handlers inline CSP em telas administrativas

**Data:** 2026-06-09
**Autor:** Arquiteto de Software Sênior (Time Mercúrio)
**Skills Utilizadas:** security-audit v1.0.0, modernization-java-migration v1.0.0

## 1. Prompt Principal (Input)

> Antes, verifique se o mesmo problema ocorre em outras telas.
>
> Crie a tarefa e subtarefas específicas para essas correções e ajustes no passo-a-passo.md, aplicando a mesma estratégia. Depois comece a execução passo a passo em pequenas iterações, uma subtarefa por vez.

## 2. Resposta da IA (Output Resumido)

> Foi executada uma varredura nas telas administrativas e confirmados handlers inline remanescentes bloqueáveis por CSP:
> - `webapp/WEB-INF/content/admin/inclusaoJogo.jsp`
> - `webapp/WEB-INF/content/admin/partials/admin-match-edit-panel.jsp`
>
> Correções aplicadas em pequenas iterações:
> 1. Criação da tarefa dedicada no `passo-a-passo.md` (Tarefa 36) com subtarefas específicas.
> 2. Remoção de `onclick` inline em `inclusaoJogo.jsp`, com submissão nativa de formulário (`type="submit"`) e listener não-inline (`formCadastroJogo`).
> 3. Remoção de `onclick` inline e de `hx-on::after-request` inline em `admin-match-edit-panel.jsp`, substituindo por gatilhos semânticos `data-js="close-drawer"`.
> 4. Ajuste em `webapp/js/ux-helper.js` para:
>    - fechar o drawer via delegação de evento em `[data-js="close-drawer"]`;
>    - fechar o drawer no `htmx:afterRequest` quando o submit da `.admin-edit-form-vertical` for bem-sucedido.

## 3. Validação (Build/Teste)

- Comando: `rg -n "onclick=|hx-on::after-request"` nos arquivos administrativos ajustados.
- Resultado: Sucesso (sem ocorrências nos arquivos alvo).
- Comando: `mvn -q -Dmaven.repo.local=/tmp/.m2 -Dfrontend.skip=true -DskipTests compile`.
- Resultado: Falha por dependência externa (Nexus corporativo com `401 Unauthorized`), sem relação com os patches aplicados.
- Observações:
  - A validação de conformidade CSP para os pontos alterados foi concluída via inspeção estática.
  - Validação funcional completa depende de ambiente com acesso autenticado ao repositório Maven corporativo.

## 4. Análise Humana (Veredito)

- [ ] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** Ajuste de baixo risco e focado em compatibilidade com CSP rígida, sem mudança de regra de negócio.
