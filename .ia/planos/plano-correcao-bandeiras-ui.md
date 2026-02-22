# Plano: Correção da Renderização de Bandeiras nas Telas Autenticadas

**Data:** 2026-02-22  
**Responsável:** Assistente Técnico Líder  
**Contexto:** Após migração para emojis e, mais recentemente, PNGs dedicados, as telas autenticadas ainda exibem placeholders (siglas) em vez das bandeiras corretas. A captura `telas/Erros-bandeiras-paises.png` evidencia o problema na tela de atualização de resultados.

## Objetivo
Diagnosticar e corrigir, de ponta a ponta, a exibição das bandeiras em todas as telas autenticadas e administrativas, garantindo que os PNGs gerados sejam servidos corretamente com fallback acessível.

## Etapas Planejadas
1. **Inventário de Telas Impactadas**
   - Mapear as JSPs que exibem bandeiras (`seguro/*.jsp`, `admin/*.jsp`, fragmentos em `partials/`).
   - Registrar componentes Struts/HTMX que reutilizam o markup.

2. **Verificação de Empacotamento e Deploy**
   - Confirmar que `webapp/img/bandeiras/*.png` é copiado para o WAR (estrutura Maven vs. overlay).
   - Checar pipeline do `Dockerfile` e build Maven para garantir inclusão dos assets.

3. **Análise em Runtime (Container)**
   - Validar existência dos PNGs dentro do container (`/usr/local/tomcat/webapps/ROOT/img/bandeiras/`).
   - Simular requisições HTTP para os PNGs e comparar com `bandeiraUrl` gerado.

4. **Validação do Modelo**
   - Instrumentar `Equipe`/`FlagUtils` em testes adicionais para confirmar retorno de `bandeiraUrl` para seleções com hífen (`gb-eng`, etc.).
   - Revisar chamadas nas actions/services que populam `Equipe` garantindo que `nomePais` esteja normalizado.

5. **Ajustes nas Views**
   - Harmonizar includes JSP (partials) reutilizados por telas admin/seguro.
   - Atualizar `c:choose` para evitar curto-circuito, removendo duplicações que impedem carregamento da imagem.

6. **Testes e Evidências**
   - Executar `mvn test -Dfrontend.skip=true`.
   - Smoke test via Docker autenticando e navegando nas telas ajustadas.
   - Atualizar capturas em `telas/` comprovando correção.

## Riscos & Mitigações
- **Erro de Empacotamento:** Ajustar `pom.xml` se os assets não estiverem sendo copiados no WAR.
- **Dados Inconsistentes:** Se `nomePais` no banco não corresponder ao mapeamento, preparar normalização antes do render.
- **Cache de navegador/container:** Planejar limpeza ou versionamento de assets após correção.

## Critério de Conclusão
- Todas as telas listadas exibem a bandeira correta (PNG) com fallback.
- Testes automatizados verdes e smoke manual documentado em log.
- Registro das mudanças no `passo-a-passo.md` e atualização das evidências visuais.
