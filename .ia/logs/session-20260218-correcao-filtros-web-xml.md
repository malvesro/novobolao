# Sessão: Correção de Filtros no web.xml e Dependências javax.servlet

**Data:** 2026-02-18
**Autor:** Kiro (Assistente Técnico Líder)
**Skills Utilizadas:** senior-java-dev-legacy v1.0.0

## 1. Prompt Principal (Input)

> Após correção dos arquivos XML de configuração do Spring para compatibilidade com Spring 6, o rebuild do Docker foi executado. A aplicação falhou ao inicializar com dois erros críticos:
> 1. `No bean named 'securityFilter' available`
> 2. `java.lang.NoClassDefFoundError: javax.servlet.Filter`

**Contexto:** Após migração para Spring 6 e Jakarta EE 10, a aplicação não inicializa devido a:
- Referência incorreta ao bean de segurança no `web.xml`
- Dependências ainda usando `javax.servlet` ao invés de `jakarta.servlet`

## 2. Análise do Problema

### Erro 1: Bean 'securityFilter' não encontrado

**Log do erro:**
```
org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named 'securityFilter' available
    at org.springframework.web.filter.DelegatingFilterProxy.initDelegate(DelegatingFilterProxy.java:332)
```

**Causa Raiz:**
- O `web.xml` define um filtro com nome `securityFilter`
- O `DelegatingFilterProxy` procura um bean Spring com esse nome
- No `applicationContext-security.xml`, o bean correto é `filterChainProxy` (Acegi Security)
- Há um mismatch entre o nome do filtro no `web.xml` e o nome do bean no Spring

**Trecho do web.xml:**
```xml
<filter>
    <filter-name>securityFilter</filter-name>
    <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
    <!-- delegating to bean named 'filterChainProxy' by default or we can specify targetBeanName -->
</filter>
```

**Trecho do applicationContext-security.xml:**
```xml
<bean id="filterChainProxy" class="org.acegisecurity.util.FilterChainProxy">
    ...
</bean>
```

### Erro 2: javax.servlet.Filter não encontrado

**Log do erro:**
```
java.lang.NoClassDefFoundError: javax/servlet/Filter
    at java.base/java.lang.ClassLoader.defineClass1(Native Method)
    ...
    at org.apache.catalina.core.ApplicationFilterConfig.getFilter(ApplicationFilterConfig.java:218)
```

**Causa Raiz:**
- Tomcat 10 usa Jakarta EE 10, que renomeou `javax.servlet` para `jakarta.servlet`
- Alguma dependência ou código ainda referencia `javax.servlet.Filter`
- Possíveis culpados:
  - Acegi Security 1.0.0 (muito antigo, usa javax.servlet)
  - DWR 3.0.2 (pode ter dependências transitivas antigas)
  - Struts 6 (se não configurado corretamente)

## 3. Solução Proposta

### Correção 1: Atualizar web.xml para referenciar o bean correto

**Opção A - Renomear o filtro no web.xml:**
```xml
<filter>
    <filter-name>filterChainProxy</filter-name>
    <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
</filter>
```

**Opção B - Especificar o targetBeanName (Recomendada):**
```xml
<filter>
    <filter-name>securityFilter</filter-name>
    <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
    <init-param>
        <param-name>targetBeanName</param-name>
        <param-value>filterChainProxy</param-value>
    </init-param>
</filter>
```

**Decisão:** Usar Opção B para manter o nome do filtro consistente e explicitamente mapear para o bean correto.

### Correção 2: Identificar e corrigir dependências javax.servlet

**Estratégia:**
1. Verificar o `pom.xml` para identificar dependências problemáticas
2. Adicionar exclusões para dependências transitivas `javax.servlet`
3. Garantir que todas as dependências usem Jakarta EE 10

**Dependências suspeitas:**
- `acegi-security` 1.0.0 - EOL, usa javax.servlet
- `dwr` 3.0.2 - pode ter dependências transitivas antigas
- Qualquer biblioteca que não foi atualizada para Jakarta EE

## 4. Implementação

### Arquivos a serem modificados:

1. **webapp/WEB-INF/web.xml**
   - Adicionar `<init-param>` ao filtro `securityFilter` especificando `targetBeanName=filterChainProxy`

2. **pom.xml** (se necessário)
   - Adicionar exclusões para dependências transitivas `javax.servlet`
   - Verificar versões de todas as dependências

### Passos de Execução:

1. ✅ Identificar problema através dos logs do Docker
2. ✅ Analisar causa raiz (bean name mismatch + javax.servlet)
3. ⏳ Corrigir `web.xml` com `targetBeanName`
4. ⏳ Verificar e corrigir `pom.xml` se necessário
5. ⏳ Rebuild do Docker
6. ⏳ Verificar logs de inicialização
7. ⏳ Testar acesso à aplicação

## 5. Validação (Build/Teste)

### Comandos a serem executados:

1. **Rebuild da aplicação:**
   ```bash
   wsl bash -c "cd ~/projetosgit/sistema-bolao && docker compose down"
   wsl bash -c "cd ~/projetosgit/sistema-bolao && docker compose up --build -d"
   ```

2. **Verificação de logs:**
   ```bash
   wsl bash -c "cd ~/projetosgit/sistema-bolao && docker compose logs -f app"
   ```

3. **Teste de acesso:**
   - URL: http://localhost:8080
   - Login: admin / admin123

### Resultado Esperado:
- ✅ Aplicação inicializa sem erros
- ✅ Spring Context carrega todos os beans
- ✅ Filtros de segurança funcionam corretamente
- ✅ Página de login acessível

## 6. Análise Técnica (Auto-Análise)

### Problema Raiz

A migração para Spring 6 e Jakarta EE 10 foi parcialmente concluída:
- ✅ Código Java migrado (jakarta.*)
- ✅ Arquivos XML de configuração atualizados (XSD)
- ❌ Configuração do `web.xml` não atualizada para referenciar beans corretos
- ❌ Dependências antigas (Acegi Security) ainda usam javax.servlet

### Impacto

- **Risco:** Médio - Aplicação não inicializa, bloqueando testes
- **Compatibilidade:** Atenção - Dependências legadas incompatíveis com Jakarta EE 10
- **Urgência:** Alta - Bloqueia progresso da Fase 2.5 (Auditoria Frontend)

### Decisões Técnicas

1. **Usar `targetBeanName` no web.xml**: Mantém clareza e explicitação do mapeamento
2. **Investigar Acegi Security**: Biblioteca EOL incompatível com Jakarta EE, pode requerer substituição futura
3. **Exclusões no pom.xml**: Adicionar exclusões cirúrgicas para evitar conflitos de dependências

### Lições Aprendidas

- Migração para Jakarta EE requer verificação completa de TODAS as dependências
- Bibliotecas EOL (Acegi Security 1.0.0) são incompatíveis com Jakarta EE 10
- `DelegatingFilterProxy` requer mapeamento explícito quando nome do filtro ≠ nome do bean

### Referências

- [Spring Framework 6.0 Migration Guide](https://github.com/spring-projects/spring-framework/wiki/Upgrading-to-Spring-Framework-6.x)
- [Jakarta EE 10 Migration Guide](https://jakarta.ee/specifications/platform/10/)
- [DelegatingFilterProxy Documentation](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/filter/DelegatingFilterProxy.html)
- ADR relacionado: `.ia/historico/ADR-20260217-upgrade-spring-framework.md`

## 7. Próximos Passos

1. Aplicar correções no `web.xml`
2. Verificar `pom.xml` para dependências javax.servlet
3. Rebuild e teste
4. Se Acegi Security continuar problemático, considerar migração para Spring Security 6 (já planejada)

## 8. Veredito

**Auto-Análise:** [Risco: Médio] | [Compatibilidade: Atenção] | [Veredito: Correção Necessária]

### Justificativa

As correções são essenciais para a aplicação inicializar. O problema do `javax.servlet` pode indicar necessidade de acelerar a migração do Acegi Security para Spring Security 6 (Tarefa 2 da Fase 2, já marcada como concluída mas pode ter issues residuais).

---

**Skill Aplicada:** senior-java-dev-legacy v1.0.0
**Documentação Relacionada:** 
- `passo-a-passo.md` (Fase 2, Tarefa 1 - Upgrade Spring Framework)
- `.ia/historico/ADR-20260217-upgrade-spring-framework.md`
- `.ia/logs/session-20260218-correcao-xml-spring6.md`
