---
skill_name: arquitetura-sistema-bolao
description: Diretrizes arquiteturais do Sistema Bolao em migracao controlada (Java 1.4/1.6 -> 17/21, WebWork -> Struts 7, Spring antigo -> 6, Hibernate 3 -> 6, Acegi -> Spring Security).
version: 1.1.0
tags: [architecture, webwork, struts, spring, hibernate, java, migration]
---

# Diretrizes de Arquitetura

## 1. Objetivo e Escopo

Estas diretrizes definem padroes obrigatorios para evolucao do monolito do Sistema Bolao, garantindo seguranca, consistencia de camadas e compatibilidade durante a migracao tecnologica em andamento.

## 2. Stack Atual e Alvo (Migração Controlada)

- **Atual (legado):** Java 1.4/1.6, Tomcat 5.5, WebWork/XWork, Spring antigo, Hibernate 3, Acegi Security, DWR, Quartz, JSP, MySQL.
- **Alvo (em migracao):** Java 17 ou 21, Struts 7 (ou Spring MVC), Spring 6, Hibernate 6, Spring Security, Tomcat 9+.

**Regra central:** ate a migracao ser concluida, todo novo codigo deve permanecer compativel com o stack atual. Nao introduza APIs exclusivas de Java 8+ nem recursos de Struts 7 / Spring 6 / Hibernate 6.

## 3. Camadas e Dependências (Regra de Ouro)

Todo fluxo HTTP deve seguir:

```
Action (WebWork) -> Service/Business -> DAO -> Hibernate -> MySQL
```

Regras:

- **Actions (WebWork/XWork)**: orquestram navegacao e delegam regras para Service/Business.  
- **Service/Business**: concentra regras de negocio e coordena transacoes (Spring).  
- **DAO**: acesso a dados via Hibernate 3.  
- **Models/DTOs**: sem dependencias de camada superior.

Quebras nessa cadeia são bloqueadas.

## 4. WebWork com Migracao para Struts 7

- Acoes novas devem atualizar `src/xwork.xml` e apontar JSPs em `webapp/`.  
- OGNL restrito, sem exposicao indevida de campos.  
- Bundles i18n obrigatorios para textos de UI.  
- Formularios mutaveis devem manter protecao equivalente a token quando aplicavel.

**Migracao para Struts 7:**  
Somente quando aprovado em ADR. Ate la, evite APIs/recursos especificos da 7 e mantenha `xwork.xml` como fonte de roteamento.

## 5. Spring com Migracao para 6

- Servicos com DI via XML (padrao atual).  
- Evite dependencias que exijam `jakarta.*` (compativel apenas com Spring 6).  
- Integracoes externas devem ficar encapsuladas em `dao`/`util`.

**Migracao para Spring 6:**  
Exigira Java 17 e namespaces Jakarta. So iniciar apos ADR aprovado e plano de compatibilidade.

## 6. Java com Migração para 17

- Sem `var`, `record`, modulos, `sealed` ou APIs exclusivas 9+.  
- Evite `java.time` enquanto o baseline for 1.6.  
- `Optional` apenas apos migracao e apenas em retornos de servico/repositorio, nunca como argumento.

## 7. Segurança e Validação

- Validação de entradas é obrigatória (OWASP).  
- SQL sempre parametrizado, sem concatenação de dados do usuário.  
- Sem lógica sensível em JSP.  
- Metodos expostos via DWR devem ser restritos por role e validacao.

## 8. Observabilidade e Logs

- Use o logger padrao do projeto (Commons Logging) ate a migracao.  
- Mensagens de log em portugues, claras e rastreaveis.  
- Proibido `System.out.println`.

## 9. Migração: Princípios de Compatibilidade

- Mudanças que dependem do stack alvo devem ser isoladas e guardadas por feature toggle ou camada de compatibilidade.  
- Cada passo significativo de migração deve ter ADR.

## 10. ADR e Registro

- Decisoes arquiteturais relevantes devem gerar ADR em `docs/adr/` no formato `ADR-YYYYMMDD-titulo-curto.md`.  
- Use rascunho em `.ia/historico/ADR-XXX.md` quando aplicavel.

## 11. Checklist de Conformidade

- [ ] Fluxo Action → Service/Business → DAO respeitado.  
- [ ] Inputs validados e sanitizados.  
- [ ] SQL parametrizado.  
- [ ] Sem dependências exclusivas de Java 9+ / Spring 6 / Struts 7.  
- [ ] Logs usando o padrao atual.  
- [ ] Testes atualizados quando existirem.  
- [ ] ADR criado quando houver decisao arquitetural nao trivial.
