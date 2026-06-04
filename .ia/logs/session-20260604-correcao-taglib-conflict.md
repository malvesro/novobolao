# Sessão de Correção: Conflito de Prefixos Taglib (JasperException)

**Data:** 04 de Junho de 2026
**Agente:** Arquiteto de Software Sênior (Time Mercúrio)
**Tarefa:** Correção de Erro 500 em `validacaoCadastro.jsp`

## 1. Problema Identificado

Ao acessar a tela de validação de cadastro (`validacaoCadastro.jsp`), o servidor retornava **HTTP Status 500 – Internal Server Error** com a seguinte exceção:

`org.apache.jasper.JasperException: /WEB-INF/content/validacaoCadastro.jsp (line: [3], column: [67]) Attempt to redefine the prefix [opendev] to [/WEB-INF/tld/opendev.tld], when it was already defined as [http://www.opendev.com.br/tld] in the current scope.`

## 2. Análise Técnica

O projeto utiliza um **include-prelude** configurado no `web.xml` para injetar o arquivo `/WEB-INF/content/template/cabecalho.jspf` em todas as páginas `*.jsp`.

O arquivo `cabecalho.jspf` já declara as taglibs fundamentais:
- `c` (JSTL Core)
- `fmt` (JSTL I18N)
- `fn` (JSTL Functions)
- `s` (Struts Tags)
- `sec` (Spring Security Tags)
- `opendev` (Taglib customizada do projeto usando a URI `http://www.opendev.com.br/tld`)

### Por que o erro ocorreu apenas em `validacaoCadastro.jsp`?

O compilador JSP (Jasper) permite redefinições de prefixos se a URI for **idêntica** à já definida. Em outras páginas, a declaração redundante usava a URI lógica `http://www.opendev.com.br/tld`, o que o Jasper ignorava por ser redundante.

No entanto, em `validacaoCadastro.jsp`, a declaração era:
`<%@ taglib prefix="opendev" uri="/WEB-INF/tld/opendev.tld" %>`

Embora o `web.xml` mapeie a URI lógica para este arquivo físico, o Jasper trata as strings de URI como identificadores únicos. Ao encontrar `/WEB-INF/tld/opendev.tld` após `http://www.opendev.com.br/tld` ter sido definido para o mesmo prefixo `opendev`, o servidor interpretou como uma tentativa de mudar a definição da taglib no meio do processamento, resultando na `JasperException`.

## 3. Estratégia de Correção

1.  **Eliminação de Redundância:** Remover todas as declarações `<%@ taglib %>` do arquivo `validacaoCadastro.jsp`, confiando no `include-prelude`.
2.  **Auditoria e Limpeza:** Identificar outros arquivos JSP que também continham declarações redundantes e removê-las para evitar problemas futuros e seguir o princípio DRY (Don't Repeat Yourself).
3.  **Conformidade com Diretrizes:** Alinhar com a diretriz de `AGENTS.md` que desencoraja declarações duplicadas em fragmentos e assegura que o host (ou prelude) forneça o contexto necessário.

## 4. Execução

Foram alterados os seguintes arquivos para remover declarações redundantes de taglibs:

- `webapp/WEB-INF/content/validacaoCadastro.jsp` (Correção do erro 500)
- `webapp/WEB-INF/content/admin/participantes.jsp`
- `webapp/WEB-INF/content/recuperacao/recuperar-senha.jsp`
- `webapp/WEB-INF/content/recuperacao/redefinir-senha.jsp`
- `webapp/WEB-INF/content/regras.jsp`
- `webapp/WEB-INF/content/seguro/classificacao.jsp`
- `webapp/WEB-INF/content/seguro/copa.jsp`
- `webapp/WEB-INF/content/seguro/graficoDesempenho.jsp`
- `webapp/WEB-INF/content/seguro/jogos.jsp`
- `webapp/WEB-INF/content/seguro/principal.jsp`
- `webapp/WEB-INF/content/seguro/trocaSenha.jsp`

## 5. Validação

- O arquivo `validacaoCadastro.jsp` agora está limpo e herda corretamente as taglibs do `cabecalho.jspf`.
- Outros JSPs (como `cadastro.jsp`) já seguiam este padrão e funcionavam corretamente.
- A consistência do prefixo `opendev` foi restabelecida usando apenas a URI `http://www.opendev.com.br/tld` conforme definido no `web.xml`.

## 6. Conclusão

A tarefa 14.6 do `passo-a-passo.md` foi concluída com sucesso, estabilizando a tela de validação do fluxo OTP.
