# Plano Técnico: Adoção de Bundler Frontend (Vite/ESBuild)

**Data:** 2026-02-20  
**Responsável:** Assistente Técnico Líder (Time Mercúrio)  
**Relacionamento:** Fase 2.5 – Tarefa 2 (Inventário de Scripts), Subtarefa 6  
**Contexto:** Projeto WAR Struts 6 + Spring 6 (Tomcat 10/Jakarta EE 10)

---

## 1. Motivação e Objetivos

- Consolidar os módulos JavaScript (HTMX helpers, tooltips, scripts de administração) evitando múltiplos `<script>` soltos.
- Preparar política CSP mais restritiva (sem `unsafe-inline`), gerando bundles com hashes.
- Garantir minificação e versionamento de assets (hash no nome do arquivo) para otimizar caching.
- Permitir modularização ES Modules e testes unitários de scripts.

---

## 2. Requisitos e Restrições

- **Runtime:** Deve continuar gerando artefatos compatíveis com empacotamento WAR padrão (`src/main/webapp`).  
- **Servidor:** Tomcat 10 (Jakarta EE 10). Não há Node em produção; build deve ocorrer na esteira CI/CD.  
- **Dependências:** Evitar substituição do Maven; o bundler será executado via `npm`/`pnpm` ou `yarn` somente em build (`frontend-maven-plugin` ou estágio separado).  
- **Compatibilidade:** Scripts finais precisam funcionar com navegadores evergreen (Chrome/Edge/Firefox/Safari ≥ 2022).  
- **CSP:** Preparar diretiva `script-src 'self' 'sha256-...'` opcional (hashes gerados automaticamente).  
- **Internacionalização:** Bundler deve tratar arquivos estáticos (JS/CSS/IMG) sem quebrar recursos JSP.

---

## 3. Opções Avaliadas

### 3.1 Vite
- ✅ Dev server rápido, suporte a ES Modules, integração fácil com PostCSS.  
- ✅ Gera bundles minificados com hash (`dist/assets/*.js`).  
- ⚠️ Requer Node ≥ 18 na pipeline.  
- ⚠️ Precisa de configuração `base` para publicar em subpath (contexto Tomcat).

### 3.2 ESBuild (CLI ou `esbuild-node`)
- ✅ Build extremamente rápido, configuração simples via script JS.  
- ✅ Menor dependência de eco-sistema; perfeito para bundles pequenos.  
- ⚠️ Não possui dev server integrado sofisticado (mas não é prioridade).  
- ⚠️ Necessita orquestração manual para copiar assets e gerar manifest.

### 3.3 Rollup
- ✅ Configuração detalhada, suporte a libraries.  
- ⚠️ Overkill para caso atual; preferir Vite (usa Rollup internamente).

**Decisão preliminar:** Adotar **Vite** para reduzir esforço de configuração e habilitar hot reload futuro, com fallback para ESBuild caso CI restrinja dependências.

---

## 4. Arquitetura de Pastas Proposta

```
project-root/
├─ package.json
├─ vite.config.js
├─ src/frontend/
│  ├─ main.js              # ponto de entrada principal
│  ├─ admin/participantes.js
│  ├─ seguro/jogos.js
│  ├─ components/tooltips.js
│  └─ styles/main.css
├─ target/...
└─ src/main/webapp/
   ├─ js/                   # conterá bundles gerados (ex.: app.[hash].js)
   └─ css/                  # CSS minificado (se aplicável)
```

- O build Vite gera saída em `src/main/webapp/js` (ou pasta intermediária `src/main/webapp/assets`).
- JSPs passam a importar `app.[hash].js` via `<c:url>` ou `<fmt:message>` com versionamento (manifest JSON).

---

## 5. Fluxo de Build Proposto

1. **Instalar Dependências:**  
   ```bash
   npm install --save-dev vite sass postcss autoprefixer
   ```

2. **Comando de Build:**  
   - `npm run build` → gera `dist/`.
   - Copiar `dist/assets/*.js` e `*.css` para `src/main/webapp/js` e `src/main/webapp/css`.

3. **Integração Maven (opcional):**
   ```xml
   <plugin>
     <groupId>com.github.eirslett</groupId>
     <artifactId>frontend-maven-plugin</artifactId>
     <executions>
       <execution>
         <id>install node and npm</id>
         <goals><goal>install-node-and-npm</goal></goals>
         <configuration>
           <nodeVersion>v20.11.1</nodeVersion>
         </configuration>
       </execution>
       <execution>
         <id>npm install</id>
         <goals><goal>npm</goal></goals>
         <configuration>
           <arguments>ci</arguments>
         </configuration>
       </execution>
       <execution>
         <id>npm build</id>
         <goals><goal>npm</goal></goals>
         <configuration>
           <arguments>run build</arguments>
         </configuration>
       </execution>
     </executions>
   </plugin>
   ```

4. **Manifest:** Utilizar `manifest.json` do Vite para mapear `app.js` → `app.[hash].js`. JSP carrega via tag custom (`<c:set>` + EL).

---

## 6. Tarefas Derivadas

1. Criar `package.json` e configurar scripts (`dev`, `build`, `preview`).  
2. Definir `vite.config.js` com:
   - `build.outDir = 'src/main/webapp/assets'`
   - `build.manifest = true`
   - `base = '/${contextPath}/'` (ou var env)
3. Migrar scripts atuais para módulos ES:
   - `tooltips.js` → `src/frontend/components/tooltips.js`
   - utilitários HTMX (palpites/admin) → entradas dedicadas.
4. Atualizar JSPs para carregar bundle avançado (ex.: `app` + específicos via data-attribute).
5. Configurar pipeline (local/CI) para rodar `npm run build` antes do `mvn package`.
6. Revisar CSP: adicionar diretiva `'self'` + SRI/hashes.
7. Escrever documentação (`.ia/diretrizes/frontend.md` subseção bundler) e atualizar `README.md`.

---

## 7. Métricas de Sucesso

- Redução de requisições JS (≥ 5 → 1 ou 2 bundles).  
- Tamanho total minificado < 150 KB (meta inicial).  
- CSP `script-src` sem `unsafe-inline` viável.  
- `npm run build` integrado ao `mvn package` sem aumentar tempo total > 30s.  
- Documentação para novos devs + instruções de hot reload local.

---

## 8. Riscos e Mitigações

| Risco | Impacto | Mitigação |
|-------|---------|-----------|
| Pipeline CI sem Node | Médio | Incluir instalação via frontend-maven-plugin ou container builder. |
| Conflitos de path em contexto Tomcat | Baixo | Configurar `base` dinamicamente (env var) e validar no Docker. |
| Regressão JS ao modularizar | Médio | Implementar testes unitários simples (Vitest) e smoke tests HTMX. |
| Erro ao carregar bundle hash (cache outdated) | Médio | Usar manifest + fallback e invalidar caches via versionamento. |

---

## 9. Próximos Passos

1. Aprovação do plano pelo time/produto.  
2. Criar branch dedicada (`feature/frontend-bundler`).  
3. Implementar `package.json` + Vite config + reorganização dos scripts.  
4. Atualizar documentação e plano `passo-a-passo.md` com execução das subtarefas.  
5. Rodar `mvn package` + `npm run build` em CI para validar integração.

---

**Auto-Analise:** [Risco: Médio] | [Compatibilidade: Atenção] | [Veredito: Revisar após aprovação]  
**Logs relacionados:** `.ia/logs/session-20260219-inventario-scripts-fase-2-5.md`, `.ia/logs/session-20260220-remocao-dwr-finalizacao.md`
