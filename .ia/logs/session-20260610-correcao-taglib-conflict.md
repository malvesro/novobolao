# Log: Correção de Conflito de Taglib (2026-06-10)

## Problema
Ocorre uma `org.apache.jasper.JasperException` ao renderizar a tela de palpites: "Attempt to redefine the prefix [opendev] to [/opendev-tags], when it was already defined as [http://www.opendev.com.br/tld] in the current scope."

O compilador JSP (Jasper) detectou que o mesmo prefixo `opendev` estava sendo associado a duas Uris diferentes (`http://www.opendev.com.br/tld` e `/opendev-tags`) no mesmo escopo de processamento (devido ao uso de `<%@include%>`).

## Resolução
Padronização da declaração da taglib `opendev` em `webapp/WEB-INF/content/seguro/partials/jogos-lista-fragmento.jsp` para utilizar a URI canônica (`http://www.opendev.com.br/tld`), que é a URI definida no `web.xml` e utilizada nos outros arquivos JSP (via `cabecalho.jspf`).

## Verificação
- A renderização da tela de palpites não deve mais apresentar erro de compilação JSP.
