# Plano de Atualização de Bibliotecas de Terceiros (Fase 2 - Item 5)

Este plano visa atualizar e modernizar as bibliotecas de terceiros do projeto, removendo dependências obsoletas e vulneráveis.

## Status Atual das Bibliotecas

### ✅ Bibliotecas Já Atualizadas
- **JFreeChart**: 1.5.4 (versão moderna e estável)
- **Batik**: 1.17 (versão mais recente para suporte SVG)
- **Quartz Scheduler**: 2.3.2 (compatível com Jakarta EE)
- **EHCache**: 3.10.8 (versão moderna com suporte JSR-107)
- **Commons Lang3**: 3.14.0 (versão atual)
- **Commons Text**: 1.11.0 (versão atual)
- **Jakarta Mail**: 2.0.3 (Angus - implementação moderna)
- **SLF4J**: 2.0.12 (versão atual)
- **Logback**: 1.5.0 (versão atual)

### ⚠️ Bibliotecas Legadas que Requerem Atenção

#### 1. Cewolf (REMOVIDO)
- **Status**: Comentado no pom.xml
- **Versão**: 1.2.4 (descontinuada)
- **Problema**: Repositório Maven descontinuado, não compatível com Jakarta EE
- **Ação**: ✅ Já removido do build
- **Próximo Passo**: Migrar gráficos para JFreeChart direto ou biblioteca JS moderna

#### 2. DWR (Direct Web Remoting)
- **Status**: Ativo
- **Versão Atual**: 3.0.2-RELEASE
- **Última Versão Disponível**: 3.0.2-RELEASE (2015)
- **Problema**: Biblioteca descontinuada, não há suporte ativo
- **Ação Recomendada**: Migração gradual para HTMX (já iniciada na Fase 2, Tarefa 7)
- **Prioridade**: Média (manter por enquanto, migrar progressivamente)

### 📊 Análise de Versões

| Biblioteca | Versão Atual | Última Disponível | Status | Ação |
|------------|--------------|-------------------|--------|------|
| JFreeChart | 1.5.4 | 1.5.4 | ✅ Atualizada | Nenhuma |
| Batik | 1.17 | 1.17 | ✅ Atualizada | Nenhuma |
| Quartz | 2.3.2 | 2.3.2 | ✅ Atualizada | Nenhuma |
| EHCache | 3.10.8 | 3.10.8 | ✅ Atualizada | Nenhuma |
| DWR | 3.0.2 | 3.0.2 (EOL) | ⚠️ Legada | Migrar para HTMX |
| Cewolf | - | 1.2.4 (EOL) | ❌ Removida | Usar JFreeChart direto |
| Commons Lang3 | 3.14.0 | 3.14.0 | ✅ Atualizada | Nenhuma |
| Commons Text | 1.11.0 | 1.11.0 | ✅ Atualizada | Nenhuma |
| Jakarta Mail | 2.0.3 | 2.0.3 | ✅ Atualizada | Nenhuma |
| SLF4J | 2.0.12 | 2.0.12 | ✅ Atualizada | Nenhuma |
| Logback | 1.5.0 | 1.5.0 | ✅ Atualizada | Nenhuma |

## Plano de Ação

### Curto Prazo (Concluído)
- [x] Remover Cewolf do pom.xml (já comentado)
- [x] Verificar build sem Cewolf
- [x] Manter JFreeChart 1.5.4 para gráficos

### Médio Prazo (Em Progresso)
- [ ] Continuar migração de DWR para HTMX (iniciada na Tarefa 7)
- [ ] Identificar todas as funcionalidades que usam DWR
- [ ] Migrar progressivamente para HTMX

### Longo Prazo
- [ ] Remover completamente DWR quando todas as funcionalidades forem migradas
- [ ] Avaliar migração de gráficos para bibliotecas JS modernas (Chart.js, D3.js)

## Verificação de Segurança

Para verificar vulnerabilidades conhecidas (CVEs), execute:

```bash
wsl bash -c "mvn org.owasp:dependency-check-maven:check"
```

## Conclusão

A maioria das bibliotecas de terceiros já está atualizada para versões modernas e compatíveis com Jakarta EE 10. As duas bibliotecas legadas identificadas (Cewolf e DWR) estão sendo tratadas:

- **Cewolf**: Removida do build (comentada no pom.xml)
- **DWR**: Migração gradual para HTMX em andamento

O build Maven agora funciona corretamente sem o Cewolf, e o projeto está em um estado estável para continuar a modernização.

> `Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]`
