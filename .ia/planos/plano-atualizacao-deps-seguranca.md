# Plano de Modernização - Dependências e Segurança

Este plano detalha a atualização das bibliotecas de terceiros (Fase 2, Item 5) e a criação da nova **Fase 5: Segurança Progressiva**.

## Propostas

### 1. Atualização de Dependências (Fase 2, Item 5)
O objetivo é trazer as bibliotecas legadas para versões que minimizem conflitos com a stack Jakarta EE 10 / Spring 6, mantendo a compatibilidade funcional.

- **DWR (Direct Web Remoting):** Usar `org.directwebremoting:dwr:3.0.2-RELEASE` (ou fork compatível se disponível).
- **Cewolf/JFreeChart:** Atualizar `jfreechart` para `1.5.4` e buscar fork `cewolf-jakarta` ou configurar o `eclipse-transformer-maven-plugin` se necessário.
- **Batik:** Integrar via Maven para suporte a SVG no JFreeChart.
- **OWASP Dependency-Check:** Integrar a versão `12.1.0` (ou última estável) para auditoria contínua de CVEs.

### 2. Nova Fase 5: Segurança Progressiva
Foco na proteção da aplicação contra as principais ameaças (OWASP Top 10) em ambiente moderno.

1. **Auditoria de Vulnerabilidades:** Configuração do OWASP Dependency Check.
2. **Reforço de Autenticação:** Transição total para BCrypt e MFA (Opcional).
3. **Proteção de Camada Web:** Configuração de headers de segurança (HSTS, CSP) e proteção CSRF no Struts 6.
4. **Sanitização de Entradas:** Revisão de validadores Struts e proteção contra XSS.

## Mudanças Propostas

### [Phase 2, Item 5] Modernização de Dependências
#### [MODIFY] [pom.xml](file:///home/rosner/projetosgit/sistema-bolao/pom.xml)
- Adicionar dependências: `dwr`, `cewolf`, `batik-all`.

### [Phase 5] Segurança
#### [MODIFY] [pom.xml](file:///home/rosner/projetosgit/sistema-bolao/pom.xml)
- Adicionar plugin `org.owasp:dependency-check-maven`.

#### [MODIFY] [passo-a-passo.md](file:///home/rosner/projetosgit/sistema-bolao/passo-a-passo.md)
- Incluir a nova seção "Fase 5: Segurança".

## Plano de Verificação

### Testes Automatizados
- `mvn verify`: O build deve falhar se houver vulnerabilidades críticas (CVSS > 7) após configuração do plugin.
- `mvn compile`: Garantir que as novas dependências não geram conflitos de classpath.

### Verificação Manual
- Gerar gráfico com Cewolf para validar compatibilidade.
- Validar se requisições DWR (ainda não migradas para HTMX) continuam operacionais.
