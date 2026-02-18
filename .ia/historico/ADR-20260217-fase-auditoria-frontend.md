# ADR-20260217-fase-auditoria-frontend

## Contexto

Após a conclusão da Fase 2 (migração de backend para Spring 6, Struts 6, Hibernate 6 e Jakarta EE 10), identificamos a necessidade crítica de uma fase dedicada à auditoria e ajuste do frontend antes de prosseguir com funcionalidades novas.

### Situação Atual do Frontend

**Bibliotecas Carregadas:**
- ✅ jQuery 4.0.0 (moderno)
- ✅ HTMX 1.9.10 (moderno)
- ⚠️ Prototype.js 1.4 (2006 - legado)
- ⚠️ Scriptaculous.js 1.5 (2006 - legado)
- ⚠️ engine.js, util.js, overlib.js (scripts customizados antigos)

**Problemas Identificados:**

1. **Coexistência Perigosa:** Prototype e jQuery carregados simultaneamente, mesmo com `noConflict()`, aumenta risco de bugs
2. **Peso Desnecessário:** ~150KB+ de bibliotecas legadas que não são mais necessárias
3. **Falta de Validação:** Nenhuma tela foi testada visualmente após as migrações de backend
4. **CSS Legado:** Arquivo `estilo.css` provavelmente contém hacks para IE6/7 e não é responsivo
5. **Taglib Cewolf:** Ainda referenciada no `cabecalho.jspf` mas biblioteca removida do pom.xml
6. **Performance:** Sem minificação, sem bundling, sem otimização de assets
7. **Acessibilidade:** Não auditada, provavelmente não conforme WCAG 2.1

## Análise de Risco (Visão Sênior Frontend)

### Risco Alto: Regressão Visual Silenciosa
- Mudanças no Struts 6 podem ter alterado renderização de tags
- Mudanças no Spring Security 6 podem ter quebrado fluxos de autenticação
- Formulários podem não estar validando corretamente
- **Impacto:** Bugs em produção, perda de dados, frustração do usuário

### Risco Médio: Performance Degradada
- Carregamento de 150KB+ de JS legado desnecessário
- CSS não otimizado
- Sem cache adequado de assets
- **Impacto:** Experiência do usuário ruim, especialmente em conexões lentas

### Risco Médio: Manutenibilidade
- Desenvolvedores confusos sobre qual biblioteca usar (Prototype vs jQuery)
- Código duplicado e inconsistente
- **Impacto:** Aumento de tempo de desenvolvimento, bugs

### Risco Baixo: Segurança
- Bibliotecas antigas podem ter vulnerabilidades XSS conhecidas
- **Impacto:** Potencial exploração de vulnerabilidades

## Decisão Arquitetural

Criar **Fase 2.5: Auditoria e Ajuste do Frontend** como fase obrigatória entre a Fase 2 (Backend) e Fase 3 (Funcionalidades Novas).

### Objetivos da Fase 2.5

1. **Validação:** Garantir que todas as telas funcionam corretamente após migrações
2. **Limpeza:** Remover código legado (Prototype, Scriptaculous)
3. **Modernização:** Refatorar CSS, implementar responsividade básica
4. **Performance:** Otimizar carregamento de assets
5. **Qualidade:** Melhorar acessibilidade e compatibilidade cross-browser

### Abordagem Técnica

#### 1. Auditoria Visual (Teste Manual)
- Testar todas as telas principais em navegadores modernos
- Documentar bugs visuais e funcionais
- Priorizar correções críticas

#### 2. Remoção de Bibliotecas Legadas
- Remover Prototype.js e Scriptaculous.js do `cabecalho.jspf`
- Identificar e migrar scripts que ainda usam Prototype
- Testar após cada remoção

#### 3. Refatoração CSS
- Auditar `estilo.css` linha por linha
- Remover hacks para IE6/7 (ex: `* html`, `_propriedade`)
- Implementar media queries para responsividade básica
- Organizar CSS por componentes

#### 4. Otimização de Performance
- Minificar JS e CSS
- Implementar cache de assets (headers HTTP)
- Considerar lazy loading de scripts não críticos

#### 5. Acessibilidade
- Adicionar labels adequados em formulários
- Verificar contraste de cores (WCAG AA)
- Garantir navegação por teclado
- Adicionar atributos ARIA onde necessário

## Consequências

### Positivas
- ✅ Frontend validado e funcional
- ✅ Código mais limpo e manutenível
- ✅ Performance melhorada
- ✅ Base sólida para modernização futura
- ✅ Redução de débito técnico

### Negativas
- ⏱️ Atraso de ~1-2 semanas no cronograma
- 💰 Custo de tempo de desenvolvimento
- 🔄 Possível necessidade de ajustes em múltiplas telas

### Mitigação de Riscos
- Testar incrementalmente (uma biblioteca/tela por vez)
- Manter branch de fallback com código anterior
- Documentar todas as mudanças
- Criar checklist de validação

## Alternativas Consideradas

### Alternativa 1: Pular para Fase 3 (Rejeitada)
**Motivo:** Risco muito alto de bugs em produção. Não é aceitável modernizar backend sem validar frontend.

### Alternativa 2: Fazer Auditoria Apenas em Produção (Rejeitada)
**Motivo:** Antipadrão. Usuários não devem ser cobaias.

### Alternativa 3: Auditoria Mínima (Considerada mas Insuficiente)
**Motivo:** Resolver apenas bugs críticos não resolve débito técnico. Melhor fazer completo agora.

## Plano de Execução

1. Criar Fase 2.5 no `passo-a-passo.md`
2. Criar plano detalhado de auditoria
3. Executar tarefas sequencialmente
4. Documentar descobertas e correções
5. Validar com usuários (se possível)

## Veredito Técnico (Desenvolvedor Sênior Frontend)

Esta fase é **absolutamente necessária**. Ignorá-la seria negligência técnica. Um sistema com backend moderno e frontend quebrado é pior do que um sistema totalmente legado, pois cria expectativas falsas.

A remoção de Prototype/Scriptaculous é especialmente crítica porque:
1. São bibliotecas de 2006 (20 anos!)
2. Não recebem atualizações de segurança
3. Conflitam conceitualmente com jQuery/HTMX
4. Adicionam peso desnecessário
5. Confundem desenvolvedores

> `Auto-Analise: [Risco de não fazer: Alto] | [Prioridade: Crítica] | [Veredito: Obrigatório]`

---

**Data:** 2026-02-17  
**Autor:** Kiro (Arquiteto de Software Sênior / Desenvolvedor Sênior Frontend)  
**Status:** Aprovado
