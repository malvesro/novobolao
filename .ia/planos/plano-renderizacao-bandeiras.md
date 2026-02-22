# Plano: Renderização das Bandeiras com Assets PNG

**Data:** 2026-02-22  
**Responsável:** Assistente Técnico Líder  
**Contexto:** Após migrar o catálogo de bandeiras para arquivos PNG específicos (`webapp/img/bandeiras/*.png`), é necessário ajustar o front-end para exibir os novos assets, mantendo fallback acessível quando a imagem não estiver disponível.

## Objetivo
Garantir que todas as telas autenticadas e públicas exibam a bandeira correspondente a cada seleção por meio de imagens PNG, preservando fallback em texto/emoji e conformidade com acessibilidade e testes automatizados.

## Premissas
- `flags.properties` contém o mapeamento país → código ISO (incluindo variantes `gb-eng`, `gb-wls`, `gb-sct`, `gb-nir`).
- Os PNGs estão disponíveis em `webapp/img/bandeiras/<codigo>.png`.
- Emojis continuarão como fallback quando não houver asset.

## Etapas
1. **Revisar utilitário de bandeiras**
   - Refatorar `FlagUtils` para carregar `flags.properties` dinamicamente.
   - Expor métodos `getCodigoPais`, `hasAssetDisponivel`, `getEmojiFallback`.
   - Atualizar/expandir `FlagUtilsTest` cobrindo códigos com hífen.

2. **Atualizar domínios e services**
   - Ajustar `Equipe` para delegar ao novo contrato de `FlagUtils`.
   - Garantir serialização/deserialização sem `System.out.println`.

3. **Atualizar JSPs**
   - `webapp/WEB-INF/content/seguro/jogos.jsp`
   - `webapp/WEB-INF/content/seguro/principal.jsp`
   - Incluir `<img src="${pageContext.request.contextPath}/img/bandeiras/${codigo}.png">` com `alt` descritivo, fallback em texto para ausência de asset.

4. **Ajustar CSS**
   - Revisar `.flag-icon` em `webapp/css/estilo.css` para suportar imagens 24x18.
   - Garantir responsividade e alignment cross-browser.

5. **Testes e validação**
   - Executar `mvn test -Dfrontend.skip=true`.
   - Realizar smoke manual via Docker (verificar páginas: principal, jogos, admin/jogos).
   - Atualizar ou criar screenshots em `telas/` se necessário.

## Entregáveis
- Código atualizado (`FlagUtils`, `Equipe`, JSPs, CSS).
- Testes unitários ajustados (`FlagUtilsTest`).
- Entrada de log de sessão descrevendo execução.
- Atualização correspondente no `passo-a-passo.md`.

## Riscos e Mitigações
- **Diferenças de case/códigos não mapeados:** validar dataset antes do deploy; fallback textual.
- **Layout quebrado em resoluções menores:** validar CSS com browsers modernos.
- **Cache de navegador/stale assets:** garantir versionamento adequado durante deploy (hash via Vite ou cabeçalhos).

## Critérios de Conclusão
- Todas as seleções exibem imagem correta nas telas autenticadas e públicas.
- Tests `mvn test -Dfrontend.skip=true` aprovados.
- Verificação manual confirmando ausência de ícones quebrados.
