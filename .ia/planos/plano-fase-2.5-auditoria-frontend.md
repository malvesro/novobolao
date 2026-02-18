# Plano Detalhado: Fase 2.5 - Auditoria e Ajuste do Frontend

## Visão Geral

Esta fase garante que o frontend está funcional, performático e manutenível após as migrações de backend da Fase 2.

## Tarefas Detalhadas

### 1. Auditoria Visual Completa

**Objetivo:** Validar renderização e funcionalidade de todas as telas principais.

**Telas Prioritárias para Teste:**
- [ ] Login (`login.jsp`)
- [ ] Cadastro (`cadastro.jsp`)
- [ ] Dashboard/Principal (`seguro/principal.jsp`)
- [ ] Classificação/Ranking (`seguro/classificacao.jsp`)
- [ ] Jogos/Palpites (`seguro/jogos.jsp`)
- [ ] Gráfico de Desempenho (`seguro/graficoDesempenho.jsp`)
- [ ] Copa (`seguro/copa.jsp`)
- [ ] Admin - Inclusão de Jogo (`admin/inclusaoJogo.jsp`)
- [ ] Admin - Participantes (`admin/participantes.jsp`)
- [ ] Troca de Senha (`seguro/trocaSenha.jsp`)
- [ ] Bate-papo (`seguro/batePapo.jsp` - desativado, verificar mensagem)

**Checklist de Validação por Tela:**
- [ ] Layout renderiza corretamente
- [ ] Formulários funcionam (submit, validação)
- [ ] Botões respondem a cliques
- [ ] Links navegam corretamente
- [ ] Mensagens de erro aparecem adequadamente
- [ ] Animações/efeitos funcionam (se aplicável)
- [ ] Gráficos renderizam (se aplicável)
- [ ] Tabelas exibem dados corretamente
- [ ] Menu lateral funciona
- [ ] Logout funciona

**Navegadores para Teste:**
- Chrome (última versão)
- Firefox (última versão)
- Edge (última versão)
- Safari (se disponível)

**Resoluções para Teste:**
- Desktop: 1920x1080, 1366x768
- Tablet: 768x1024
- Mobile: 375x667 (iPhone SE), 414x896 (iPhone 11)

**Entregável:** Documento com screenshots e lista de bugs encontrados.

---

### 2. Inventário e Análise de Scripts

**Objetivo:** Mapear todos os arquivos JavaScript e identificar dependências.

**Arquivos a Analisar:**
- [ ] `js/BrowserDetector.js`
- [ ] `js/prototype.js` (LEGADO - remover)
- [ ] `js/scriptaculous.js` (LEGADO - remover)
- [ ] `js/effects.js` (LEGADO - verificar se é do Scriptaculous)
- [ ] `js/engine.js` (CUSTOMIZADO - analisar)
- [ ] `js/util.js` (CUSTOMIZADO - analisar)
- [ ] `js/overlib.js` (LEGADO - tooltips, avaliar necessidade)
- [ ] `js/jquery-4.0.0.min.js` (MODERNO - manter)
- [ ] `js/htmx.min.js` (MODERNO - manter)

**Para Cada Script Customizado:**
1. Ler código completo
2. Identificar dependências (usa Prototype? jQuery? Vanilla?)
3. Documentar funcionalidades
4. Decidir: manter, refatorar ou remover

**Entregável:** Planilha/documento com análise de cada script.

---

### 3. Remoção de Prototype e Scriptaculous

**Objetivo:** Eliminar bibliotecas legadas do projeto.

**Passo 1: Identificar Uso**
- [ ] Buscar por `$()` em todos os JSPs (pode ser Prototype ou jQuery)
- [ ] Buscar por `$$()` (seletor Prototype)
- [ ] Buscar por `Effect.` (Scriptaculous)
- [ ] Buscar por `new Effect.` (Scriptaculous)
- [ ] Buscar por `.observe()` (Prototype event handler)
- [ ] Buscar por `Ajax.Request` (Prototype AJAX)

**Passo 2: Migrar para jQuery**

Conversões comuns:
```javascript
// Prototype → jQuery
$('elementId')              → $j('#elementId')
$$('.className')            → $j('.className')
element.observe('click')    → $j(element).on('click')
Effect.Fade(element)        → $j(element).fadeOut()
Effect.Appear(element)      → $j(element).fadeIn()
new Ajax.Request(url)       → $j.ajax(url)
```

**Passo 3: Remover do cabecalho.jspf**
- [ ] Remover linha `<script src="${base}/js/prototype.js">`
- [ ] Remover linha `<script src="${base}/js/scriptaculous.js">`
- [ ] Remover linha `<script src="${base}/js/effects.js">` (se for do Scriptaculous)

**Passo 4: Testar**
- [ ] Executar build
- [ ] Testar todas as telas novamente
- [ ] Verificar console do navegador para erros JS

**Entregável:** Código migrado e testado.

---

### 4. Auditoria e Refatoração CSS

**Objetivo:** Modernizar CSS, remover hacks legados, implementar responsividade.

**Passo 1: Análise do estilo.css**
- [ ] Ler arquivo completo
- [ ] Identificar hacks IE6/7 (ex: `* html`, `_propriedade`, `filter:`)
- [ ] Identificar seletores complexos desnecessários
- [ ] Identificar valores hardcoded que deveriam ser variáveis
- [ ] Verificar uso de `!important` (antipadrão)

**Passo 2: Organização**
Reorganizar CSS por seções:
```css
/* 1. Reset/Normalize */
/* 2. Variáveis (se usar CSS custom properties) */
/* 3. Layout Global (body, wrapper, header, footer) */
/* 4. Componentes (botões, formulários, tabelas) */
/* 5. Páginas Específicas */
/* 6. Utilitários */
/* 7. Media Queries (Responsividade) */
```

**Passo 3: Remover Hacks Legados**
- [ ] Remover `* html` (hack IE6)
- [ ] Remover `_propriedade` (hack IE6)
- [ ] Remover `filter:` (hack IE)
- [ ] Remover `-moz-` prefixes desnecessários (Firefox moderno não precisa)
- [ ] Atualizar `-webkit-` para versões padrão quando possível

**Passo 4: Implementar Responsividade Básica**
```css
/* Mobile First Approach */
/* Base styles (mobile) */

@media (min-width: 768px) {
  /* Tablet */
}

@media (min-width: 1024px) {
  /* Desktop */
}
```

Focos:
- [ ] Menu lateral responsivo (hamburger em mobile?)
- [ ] Tabelas responsivas (scroll horizontal ou stack)
- [ ] Formulários adaptáveis
- [ ] Imagens flexíveis (`max-width: 100%`)

**Passo 5: Melhorias de Acessibilidade**
- [ ] Verificar contraste de cores (mínimo 4.5:1 para texto)
- [ ] Adicionar `:focus` visível em elementos interativos
- [ ] Garantir tamanho mínimo de toque (44x44px para mobile)

**Entregável:** CSS refatorado, organizado e responsivo.

---

### 5. Remoção de Referências ao Cewolf

**Objetivo:** Limpar referências à biblioteca removida.

**Ações:**
- [ ] Remover `<%@taglib prefix="cewolf" uri="http://cewolf.sourceforge.net/taglib/cewolf.tld" %>` do `cabecalho.jspf`
- [ ] Buscar por `<cewolf:` em todos os JSPs
- [ ] Se encontrado, substituir por implementação alternativa (JFreeChart direto ou Chart.js)
- [ ] Testar telas de gráficos

**Entregável:** Código sem referências ao Cewolf.

---

### 6. Otimização de Performance

**Objetivo:** Melhorar tempo de carregamento e experiência do usuário.

**Passo 1: Minificação**
- [ ] Minificar `jquery-4.0.0.min.js` (já minificado ✓)
- [ ] Minificar `htmx.min.js` (já minificado ✓)
- [ ] Minificar scripts customizados (`engine.js`, `util.js`)
- [ ] Minificar `estilo.css`

Ferramentas sugeridas:
- UglifyJS ou Terser para JS
- cssnano ou clean-css para CSS

**Passo 2: Cache de Assets**
Configurar headers HTTP no `web.xml` ou servidor:
```xml
<!-- Cache static resources for 1 year -->
<mime-mapping>
    <extension>js</extension>
    <mime-type>application/javascript</mime-type>
</mime-mapping>
```

Ou usar versionamento de assets:
```html
<script src="${base}/js/jquery-4.0.0.min.js?v=1.0"></script>
```

**Passo 3: Lazy Loading (Opcional)**
- [ ] Carregar scripts não críticos de forma assíncrona
- [ ] Usar `defer` ou `async` em scripts que não bloqueiam renderização

**Passo 4: Análise de Performance**
- [ ] Usar Chrome DevTools Lighthouse
- [ ] Medir tempo de carregamento
- [ ] Identificar recursos pesados
- [ ] Otimizar imagens (se necessário)

**Entregável:** Assets otimizados e configuração de cache.

---

### 7. Auditoria de Acessibilidade

**Objetivo:** Garantir conformidade básica com WCAG 2.1 Level AA.

**Checklist WCAG:**

**Perceptível:**
- [ ] Todas as imagens têm atributo `alt` descritivo
- [ ] Contraste de cores adequado (4.5:1 para texto normal, 3:1 para texto grande)
- [ ] Conteúdo não depende apenas de cor para transmitir informação
- [ ] Vídeos têm legendas (se aplicável)

**Operável:**
- [ ] Toda funcionalidade acessível via teclado
- [ ] Ordem de tabulação lógica (`tabindex` se necessário)
- [ ] Links têm texto descritivo (evitar "clique aqui")
- [ ] Foco visível em elementos interativos
- [ ] Sem armadilhas de teclado

**Compreensível:**
- [ ] Labels associados a inputs (`<label for="inputId">`)
- [ ] Mensagens de erro claras e específicas
- [ ] Linguagem consistente
- [ ] Navegação consistente entre páginas

**Robusto:**
- [ ] HTML válido (sem erros críticos)
- [ ] Atributos ARIA onde necessário
- [ ] Compatível com leitores de tela

**Ferramentas:**
- [ ] axe DevTools (extensão Chrome/Firefox)
- [ ] WAVE (Web Accessibility Evaluation Tool)
- [ ] Lighthouse Accessibility Score

**Entregável:** Relatório de acessibilidade e correções implementadas.

---

### 8. Testes de Compatibilidade Cross-Browser

**Objetivo:** Garantir funcionamento em navegadores modernos.

**Matriz de Testes:**

| Tela | Chrome | Firefox | Edge | Safari | Status |
|------|--------|---------|------|--------|--------|
| Login | [ ] | [ ] | [ ] | [ ] | |
| Dashboard | [ ] | [ ] | [ ] | [ ] | |
| Jogos | [ ] | [ ] | [ ] | [ ] | |
| Admin | [ ] | [ ] | [ ] | [ ] | |

**Aspectos a Testar:**
- [ ] Layout consistente
- [ ] Funcionalidades JavaScript
- [ ] Animações/transições CSS
- [ ] Formulários (validação, submit)
- [ ] AJAX (HTMX e DWR)

**Ferramentas:**
- BrowserStack (teste em múltiplos navegadores/dispositivos)
- Sauce Labs
- Ou VMs locais

**Entregável:** Matriz de compatibilidade preenchida e bugs corrigidos.

---

### 9. Documentação Frontend

**Objetivo:** Documentar arquitetura e padrões frontend para futuros desenvolvedores.

**Criar Documento:** `.ia/diretrizes/frontend.md`

**Conteúdo:**
- Bibliotecas utilizadas (jQuery 4.0.0, HTMX 1.9.10)
- Padrões de código JavaScript
- Estrutura CSS
- Convenções de nomenclatura
- Como adicionar novas funcionalidades AJAX
- Guia de acessibilidade
- Checklist de PR para mudanças frontend

**Entregável:** Documentação completa do frontend.

---

### 10. Validação Final e Sign-off

**Objetivo:** Garantir que todas as tarefas foram concluídas com qualidade.

**Checklist Final:**
- [ ] Todas as telas testadas e funcionais
- [ ] Prototype e Scriptaculous removidos
- [ ] CSS refatorado e responsivo
- [ ] Performance otimizada (Lighthouse score > 80)
- [ ] Acessibilidade auditada (axe score > 90)
- [ ] Compatibilidade cross-browser validada
- [ ] Documentação criada
- [ ] Build Maven passa sem erros
- [ ] Nenhum erro no console do navegador
- [ ] Commits realizados com mensagens claras
- [ ] ADR e logs de sessão criados

**Entregável:** Sistema frontend validado e pronto para Fase 3.

---

## Estimativa de Esforço

| Tarefa | Esforço Estimado |
|--------|------------------|
| 1. Auditoria Visual | 4-6 horas |
| 2. Inventário Scripts | 2-3 horas |
| 3. Remoção Prototype | 6-8 horas |
| 4. Refatoração CSS | 8-12 horas |
| 5. Remoção Cewolf | 1-2 horas |
| 6. Otimização Performance | 3-4 horas |
| 7. Auditoria Acessibilidade | 4-6 horas |
| 8. Testes Cross-Browser | 4-6 horas |
| 9. Documentação | 2-3 horas |
| 10. Validação Final | 2-3 horas |
| **TOTAL** | **36-53 horas (~1-1.5 semanas)** |

## Riscos e Mitigações

| Risco | Probabilidade | Impacto | Mitigação |
|-------|---------------|---------|-----------|
| Bugs ao remover Prototype | Alta | Alto | Testar incrementalmente, manter backup |
| CSS quebra layout | Média | Alto | Testar em múltiplas resoluções, usar DevTools |
| Performance piora | Baixa | Médio | Medir antes/depois com Lighthouse |
| Incompatibilidade browser | Média | Médio | Testar em matriz completa |
| Prazo estoura | Média | Baixo | Priorizar tarefas críticas primeiro |

## Critérios de Sucesso

✅ Todas as telas funcionam corretamente  
✅ Nenhuma biblioteca legada carregada  
✅ CSS organizado e responsivo  
✅ Lighthouse Performance > 80  
✅ Lighthouse Accessibility > 90  
✅ Zero erros no console do navegador  
✅ Compatível com Chrome, Firefox, Edge, Safari  
✅ Documentação completa  

---

**Autor:** Kiro (Desenvolvedor Sênior Frontend)  
**Data:** 2026-02-17  
**Versão:** 1.0
