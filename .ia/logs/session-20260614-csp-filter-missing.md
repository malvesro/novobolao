# Relatório de Erro: CSP Nonce Filter ausente no web.xml
**Data:** 14/06/2026
**Agente:** Arquiteto Sênior (Time Mercúrio)

## 1. Descrição do Problema
Apesar da existência da classe `CspNonceFilter`, as violações de CSP persistiam porque o filtro não estava registrado no `web.xml`, impedindo a geração e injeção do `cspNonce` na requisição.

## 2. Diagnóstico
A verificação do arquivo `web.xml` confirmou a ausência da definição e do mapeamento (`filter-mapping`) para `com.opendev.bolao.security.CspNonceFilter`. Sem isso, o atributo `cspNonce` nunca era populado na requisição e o header `Content-Security-Policy` não era aplicado pelo filtro.

## 3. Solução Implementada
- Registro do `CspNonceFilter` no `web.xml`.
- Adição do `<filter-mapping>` apontando para `/*`.

## 4. Conclusão
A política de CSP agora será aplicada a todas as requisições, garantindo que o `nonce` esteja disponível para as JSPs. Isso deve eliminar os erros de "violates CSP directive" observados no console do navegador.

---
Status: Resolvido
