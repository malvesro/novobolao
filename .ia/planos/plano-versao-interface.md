# Plano: Exibição da Versão do Sistema no Frontend

**Data:** 2026-02-22  (antes do rebuild do container)
**Responsável:** Assistente Técnico Líder
**Contexto:** Após correções de bandeiras e codificação, o usuário solicitou registrar planejamento para expor a versão atual do sistema (com data/hora) diretamente na interface antes de prosseguir com novos deploys.

## Objetivos
1. Definir fonte única para número da versão (informação já existente no `pom.xml` e `build.properties`).
2. Exibir a versão e timestamp de build nas telas autenticadas (rodapé ou cabeçalho) sem poluir a UI.
3. Garantir rastreabilidade da informação tanto no WAR quanto no container.
4. Manter compatibilidade com CSP/HTMX (evitar inline scripts) e internacionalização.

## Premissas
- O `pom.xml` define `<version>` e possui `build.properties` gerado no processo Maven (`build.number`, `build.timestamp`).
- Conteúdo JSP utiliza fragments reutilizáveis (`webapp/WEB-INF/content/template/cabecalho.jspf`, `rodape.jspf`).
- Preferência por leitura da versão via recurso carregado no classpath (`/version.properties`) para não depender de scripts JS.

## Etapas Planejadas
1. **Inventário e Definição da Fonte**
   - Confirmar versão em `pom.xml` e `build.properties`.
   - Avaliar se já existe utilitário Java para expor informações de build.
   - Caso ausente, gerar `src/main/resources/version.properties` durante o build (`maven-resources-plugin`, `git-commit-id-plugin`, etc.).

2. **Serviço/Utilitário Backend**
   - Criar classe `BuildInfoProvider` (singleton Spring) que lê `version.properties` e expõe `getVersion()`/`getBuildTime()`.
   - Disponibilizar atributos em `ApplicationContext` ou via interceptor Struts para que JSPs possam acessar.

3. **Atualização das Views**
   - Escolher posição (ex.: rodapé comum `webapp/WEB-INF/content/template/rodape.jspf`).
   - Adicionar markup acessível (`<small>` ou `aria-hidden` quando apropriado) exibindo `Versão X.Y.Z - build em DD/MM/YYYY HH:MM`.
   - Garantir responsividade (CSS utilitário `.system-version`).

4. **Internacionalização e Configuração**
   - Adicionar chave i18n (`app.version.label`) nos bundles para permitir tradução futura.
   - Validar que o formato de data/hora respeita locale padrão (pt-BR) e timezone configurado.

5. **Testes e Deploy**
   - Ajustar testes unitários/integração (se necessário) para validar presença do atributo.
   - Rodar `mvn test` e `docker compose build app && docker compose up -d app` após implementação.
   - Capturar evidências (`telas/versao-ui.png`).

## Riscos e Mitigações
- **Desalinhamento da versão exibida vs. pacote implantado:** automatizar a geração de `version.properties` durante o build, evitando edição manual.
- **Layout poluído:** validar espaçamento e contraste no rodapé, garantindo acessibilidade.
- **Timezone incorreto:** usar `ZoneId.of("America/Sao_Paulo")` para o timestamp do build quando formatado.

## Entregáveis
- Arquivo de configuração (plugin Maven ou script) gerando `version.properties`.
- Classe utilitária/backing bean para expor os dados às JSPs.
- Ajustes nos fragments JSP/CSS exibindo versão e data.
- Log de sessão documentando a execução e atualização do `passo-a-passo.md`.
