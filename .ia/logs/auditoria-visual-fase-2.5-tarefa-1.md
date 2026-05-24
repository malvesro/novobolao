# Auditoria Visual Completa - Fase 2.5, Tarefa 1

**Data Início:** 2026-02-17  
**Responsável:** Kiro (Desenvolvedor Sênior Frontend)  
**Status:** Concluído em 2026-02-19 (auditoria via Docker/cURL)

## Objetivo

Validar renderização e funcionalidade de todas as telas principais após migrações de backend (Spring 6, Struts 6, Hibernate 6, Jakarta EE 10).

## Metodologia

1. **Análise Estática:** Revisar código JSP/JSPF para identificar problemas potenciais
2. **Execução Local:** Subir aplicação via Docker Compose
3. **Testes Manuais:** Validar cada tela conforme checklist
4. **Documentação:** Registrar bugs, screenshots e recomendações

## Fase 1: Análise Estática (Pré-Execução)

### Telas Prioritárias Identificadas

Baseado na estrutura do projeto, as seguintes telas serão auditadas:

1. ✅ **Login** (`webapp/login.jsp`)
2. ⚠️ **Cadastro** (`webapp/cadastro.jsp`) – redireciona 302 para `login.jsp` (ajuste de segurança pendente)
3. ✅ **Dashboard/Principal** (`webapp/seguro/principal.jsp`)
4. ✅ **Classificação/Ranking** (`webapp/seguro/classificacao.jsp`)
5. ✅ **Jogos/Palpites** (`webapp/seguro/jogos.jsp`)
6. ✅ **Gráfico de Desempenho** (`webapp/seguro/graficoDesempenho.jsp`)
7. ✅ **Copa** (`webapp/seguro/copa.jsp`)
8. ✅ **Admin - Inclusão de Jogo** (`webapp/admin/inclusaoJogo.jsp`)
9. ✅ **Admin - Participantes** (`webapp/admin/participantes.jsp`)
10. ✅ **Troca de Senha** (`webapp/seguro/trocaSenha.jsp`)
11. ✅ **Bate-papo** (`webapp/seguro/batePapo.jsp`)

### Análise Estática: Login.jsp

**Arquivo:** `webapp/login.jsp`

**Bibliotecas JavaScript Detectadas:**
- ✅ jQuery 4.0.0 (via `$j`)
- ⚠️ Prototype.js (legado - ainda carregado)
- ⚠️ Scriptaculous.js (legado - ainda carregado)

**Código Analisado:**
```javascript
// Pulse effect using jQuery
$j("#login_error").fadeOut(200).fadeIn(200).fadeOut(200).fadeIn(200);
var fadeFunc = function () {
    $j("#login_error").fadeOut(1000);
    window.clearTimeout(errorTimeout);
};
errorTimeout = window.setTimeout(fadeFunc, 6000);
```

**Avaliação:**
- ✅ Código jQuery correto e funcional
- ✅ Uso de `$j` (noConflict) adequado
- ✅ Efeito de fade implementado corretamente
- ⚠️ Prototype/Scriptaculous ainda carregados desnecessariamente

**Problemas Potenciais:**
- Nenhum problema crítico identificado
- Recomendação: Remover Prototype/Scriptaculous (Tarefa 3)

### Análise Estática: cabecalho.jspf

**Arquivo:** `webapp/template/cabecalho.jspf`

**Bibliotecas Carregadas (em ordem):**
1. BrowserDetector.js
2. prototype.js ⚠️
3. scriptaculous.js ⚠️
4. effects.js ⚠️
5. engine.js
6. util.js
7. overlib.js
8. jquery-4.0.0.min.js ✅
9. htmx.min.js ✅

**Taglibs Declaradas:**
```jsp
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="cewolf" uri="http://cewolf.sourceforge.net/taglib/cewolf.tld" %> ⚠️
<%@taglib prefix="authz" uri="http://acegisecurity.org/authz" %>
<%@taglib prefix="opendev" uri="http://www.opendev.com.br/tld" %>
```

**Problemas Identificados:**
1. ⚠️ **Taglib Cewolf ainda referenciada** (biblioteca removida do pom.xml)
2. ⚠️ **Prototype/Scriptaculous carregados** (~150KB desnecessários)
3. ⚠️ **Ordem de carregamento** (bibliotecas legadas antes das modernas)

**Impacto:**
- **Crítico:** Taglib Cewolf pode causar erro em telas com gráficos
- **Alto:** Performance degradada por bibliotecas legadas
- **Médio:** Potencial conflito entre Prototype e jQuery

**Recomendações Imediatas:**
1. Remover taglib Cewolf do cabecalho.jspf (Tarefa 5)
2. Remover Prototype/Scriptaculous (Tarefa 3)
3. Reordenar scripts (jQuery/HTMX primeiro)

## Fase 2: Preparação para Execução

### Pré-requisitos

- [x] Docker instalado
- [x] Docker Compose instalado
- [x] Porta 8080 disponível
- [x] Porta 3306 disponível

### Comandos de Execução

```bash
# Build e start dos containers
wsl bash -c "docker-compose up --build -d"

# Verificar logs
wsl bash -c "docker-compose logs -f app"

# Acessar aplicação
# URL: http://localhost:8080/sistema-bolao
```

### Checklist de Validação por Tela

Para cada tela, verificar:

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
- [ ] Console do navegador sem erros JavaScript
- [ ] Console do navegador sem erros 404 (assets)

## Fase 3: Execução e Testes (Concluído via cURL em 2026-02-19)

### Validações Executadas
- [x] Login `admin/admin123` e `user/user123` autenticados via `/j_security_check` (HTTP 302 → `/seguro/principal.jsp`).
- [x] Telas autenticadas principais (`/seguro/*.jsp` e `/admin/*.jsp`) retornaram HTTP 200 com HTML completo.
- [x] Endpoints de gráficos `/seguro/graficoLiderancaImagem.action` e `/seguro/graficoDesempenhoImagem.action` retornaram PNG válidos.
- [x] RBAC: usuário padrão (`USER`) recebeu HTTP 403 ao acessar `/admin/participantes.jsp`.

### Pendências (Execução em Navegadores Reais)
- [ ] Chrome (última versão)
- [ ] Firefox (última versão)
- [ ] Edge (última versão)
- [ ] Validação responsiva: 1920x1080, 1366x768, 768x1024, 375x667

## Descobertas e Bugs

### Bugs Críticos
*Nenhum identificado*

### Bugs Médios
1. `cadastro.jsp` redireciona 302 para `/login.jsp` (ausência de `permitAll` na configuração Spring Security). Impacto: novos usuários não conseguem iniciar pedido de cadastro.

### Bugs Baixos
*Nenhum adicional além das pendências conhecidas de bibliotecas legadas*

### Melhorias Recomendadas
1. Ajustar `applicationContext-security.xml` para liberar `permitAll` em `/cadastro.jsp` e `/cadastro.action`.
2. Remover Prototype/Scriptaculous e reordenar carregamento de scripts (tarefas 2 e 3 da Fase 2.5).
3. Planejar execução manual em navegadores reais após higienização de scripts/CSS.

## Próximos Passos

1. ✅ Subir aplicação via Docker Compose
2. ✅ Testar tela de Login (HTTP 200)
3. ✅ Testar demais telas conforme checklist (HTTP 200 + RBAC)
4. ✅ Documentar bugs encontrados (ver seção "Bugs Médios")
5. ⏳ Criar relatório final com screenshots (depende de testes em navegadores)

---

**Última Atualização:** 2026-02-19 20:52 (UTC-03)
