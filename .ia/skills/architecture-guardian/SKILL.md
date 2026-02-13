---
skill_name: architecture-guardian
description: Define as regras arquiteturais do Sistema Bolao (WebWork/XWork + Spring + Hibernate 3 + Acegi) para manter a coerencia do legado e reduzir risco de regressao.
version: 2.1.0
tags: [architecture, layering, webwork, spring, hibernate, acegi, dwr]
---

# Guia Arquitetural do Sistema Bolao (antes da migração de tecnologia)

## 1. Panorama Geral

- **Tipo de solução:** Monolito Java EE legado, empacotado em WAR e rodando em Tomcat.
- **Stack atual:** WebWork/XWork (Actions), Spring (IoC + transacoes), Hibernate 3 (ORM), Acegi Security, DWR, Quartz, JSP, MySQL.
- **Objetivo da skill:** Garantir que novas alteracoes respeitem camadas e configuracoes do legado, evitando acoplamentos incorretos, regressao funcional e quebra de seguranca.
- **Dominios principais:** `com.opendev.bolao` (acoes, servicos, DAOs, modelos, utilitarios, chat, email, grafico).

## 2. Estrutura de Pacotes (Visão Simplificada)

```
src/com/opendev/bolao/
├── action/                # Actions WebWork (entrada HTTP)
├── service/               # Interfaces de servico
├── service/impl/          # Regras de negocio
├── dao/                   # Interfaces DAO
├── dao/hibernate/         # Implementacoes Hibernate 3
├── model/                 # Entidades e regras de dominio
├── util/                  # Utilitarios (validacao, conversao, seguranca)
├── chat/                  # Chat em memoria (DWR)
├── email/                 # Envio de emails e templates
└── grafico/               # Dataset producers (Cewolf/JFreeChart)
```

Outras pastas relevantes:

- `webapp/` → JSPs, JS, CSS, imagens.
- `webapp/WEB-INF/web.xml` → filtros/servlets/listeners.
- `src/applicationContext-*.xml` → configuracoes Spring.
- `src/xwork.xml` → mapeamento WebWork/XWork.
- `webapp/WEB-INF/dwr.xml` → mapeamento DWR.
- `src/com/opendev/bolao/model/*.hbm.xml` → mapeamentos Hibernate.

## 3. Regras de Dependência (Obrigatórias)

1. **Actions (WebWork/XWork)**  
   
   - Orquestram chamadas para `service`.  
   - Nao acessam DAOs diretamente.  
   - Nao implementam regra de negocio ou persistencia.  
   - Podem usar `RequestUtils` para obter request/login, mas sem logica pesada.

2. **Services (service/impl)**  
   
   - Centralizam regras de negocio e transacoes (via `TransactionProxyFactoryBean`).  
   - Podem depender de `dao`, `model`, `util`, `email`, `grafico`.  
   - Nao dependem de Servlet API ou classes WebWork.

3. **DAO / Persistence (dao/hibernate)**  
   
   - Limitados a acesso a dados via Hibernate 3.  
   - Nao dependem de Actions nem da camada web.  
   - Podem depender de `model` e utilitarios de persistencia.

4. **Model / Dominio**  
   
   - Entidades representam regras de dominio e podem conter logica de negocio local.  
   - Nao dependem de Action, Servlet API, DWR ou JSP.

5. **JSP / Webapp**  
   
   - Sem regra de negocio.  
   - Apenas exibem dados preparados pelas Actions.  
   - JavaScript deve ficar em `webapp/js/`.

## 4. Diretrizes Específicas

- **WebWork/XWork:**  
  - Registre novas Actions e resultados em `src/xwork.xml`.  
  - Preserve namespaces existentes (`/seguro`, `/admin`) e mapeamentos `*.action`.
- **Seguranca (Acegi):**  
  - Atualize regras em `src/applicationContext-security.xml` ao criar novas URLs.  
  - Mantenha controle por roles: `admin`, `geral`, `restrito`.  
  - Nao exponha metodos sensiveis via DWR sem validacao.
- **DWR:**  
  - Se expor novos metodos, registrar em `webapp/WEB-INF/dwr.xml` e avaliar acesso via Acegi.  
  - Metodos DWR devem ser pequenos e delegar para `service`.
- **Hibernate:**  
  - Prefira ajustes em `*.hbm.xml` a mudancas no runtime.  
  - Cuidado com chave composta em `Palpite.hbm.xml`.
- **Scheduler (Quartz):**  
  - Jobs e triggers ficam em `src/applicationContext-scheduler.xml`.  
  - Evite logica pesada no job; delegue ao `service`.
- **Configuracoes duplicadas:**  
  - `webapp/WEB-INF/classes/` nao e fonte. Considere `src/` como origem.

## 5. Checklist de Conformidade

Antes de finalizar qualquer alteração:

- [ ] A Action delega tudo para o `service` correspondente?  
- [ ] Nenhuma Action acessa DAO diretamente?  
- [ ] Services nao dependem de Servlet API/WebWork?  
- [ ] DWR foi atualizado e protegido quando necessario?  
- [ ] `xwork.xml` e `applicationContext-*.xml` foram atualizados?  
- [ ] Nenhuma configuracao sensivel foi hardcoded (DB/SMTP)?  
- [ ] Mudancas criticas registradas em ADR (criar `docs/adr/` se nao existir)?

## 6. Violação e Auto-Análise

Se durante a geração for detectado um desvio (ex.: Action acessando diretamente um DAO), **pare** e ajuste a proposta. A auto-análise final deve citar explicitamente:

- Camadas tocadas.
- Como a dependência seguiu o fluxo Action → Service → DAO.
- Quais arquivos de configuracao (WebWork/Spring/Acegi/DWR) foram afetados.

> **Princípio-chave:** Todo fluxo HTTP deve permanecer: **Action → Service → DAO → Hibernate → MySQL**.  
> Quebras nesse caminho são rejeitadas.

## 7. Registro de Decisões (ADR)

- Crie ADRs em `docs/adr/` (crie a pasta se necessario) seguindo o padrão `ADR-YYYYMMDD-titulo-curto.md`.  
- Cada ADR deve conter: Contexto, Decisão, Alternativas consideradas, Consequências e Responsáveis.  
- Se uma alteração impactar camadas ou contratos, registre o ADR antes do merge e referencie-o na mensagem de commit.
