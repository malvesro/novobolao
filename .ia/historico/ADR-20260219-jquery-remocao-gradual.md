# ADR 2026-02-19 – Estratégia para Remoção do jQuery

## Contexto

- O projeto carrega **jQuery 4.0.0 (build alfa)** em `cabecalho.jspf` apenas para pequenos efeitos de UI (ex.: animação de mensagem de erro no `login.jsp`).
- A aplicação já utiliza **HTMX 1.9.10** e está migrando fluxos AJAX para essa abordagem, mantendo temporariamente Prototype/Scriptaculous enquanto DWR é descontinuado.
- Usar uma versão alfa traz riscos de regressão, dificulta suporte e impede adoção de uma Content-Security-Policy (CSP) estrita.
- A Fase 2.5 busca modernizar o frontend, reduzir dependências legadas e aumentar a segurança.

## Decisão

Remover a dependência do jQuery da aplicação. Caso seja necessário manter alguma funcionalidade temporariamente, usar jQuery 3.7.1 (estável) como fallback mínimo; entretanto, na implementação de 19/02/2026, todos os usos foram reescritos com JavaScript nativo e o script foi eliminado imediatamente.

## Alternativas Consideradas

1. **Manter jQuery 4.0.0 alfa**  
   - Rejeitada por instabilidade, ausência de suporte e potencial conflito com bibliotecas legadas.

2. **Remover jQuery diretamente (sem etapa intermediária)**  
   - Possível, porém implica refatoração imediata; preferimos manter a transição controlada enquanto Prototype/Scriptaculous ainda estão presentes.

3. **Substituir por biblioteca similar (Cash.js, Zepto)**  
   - Não agrega valor: continuaria introduzindo dependência extra para poucos efeitos.

## Consequências

### Positivas
- Reduz risco operacional ao usar versão estável.
- Facilita futura remoção de Prototype/Scriptaculous e adoção de CSP restritiva.
- Diminui peso de scripts e simplifica bundler.

### Negativas
- Exige trabalho para reescrever os trechos que ainda usam `$j()` (embora sejam poucos).
- Necessário validar regressões visuais após cada etapa.

## Plano de Implementação

1. Reescrever efeitos/trechos que utilizavam `$j` para JavaScript nativo ou HTMX.
2. Remover o bloco `noConflict` e a tag `<script>` de jQuery em `cabecalho.jspf`.
3. Excluir o arquivo `webapp/js/jquery-4.0.0.min.js` (ou qualquer variante anterior).
4. Caso algum módulo ainda precise de jQuery durante a transição, adicionar `jquery-3.7.1.min.js` localmente; retirar assim que o código for refatorado.
5. Registrar logs e atualizar `passo-a-passo.md` conforme as tarefas forem concluídas.

## Data
19 de fevereiro de 2026

## Responsável
Assistente Técnico Líder (Time Mercúrio)
