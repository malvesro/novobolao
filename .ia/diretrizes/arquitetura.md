---
doc_name: diretrizes-arquitetura
description: Diretrizes arquiteturais genericas para evolucao segura e consistente.
version: 1.0.0
tags: [architecture, layering, security, testing, migration]
---

# Diretrizes de Arquitetura

## 1. Objetivo e Escopo

Estas diretrizes definem padroes obrigatorios para evolucao do sistema, garantindo seguranca, consistencia de camadas e compatibilidade.

> NOTE: Descreva o tipo de sistema, dominio de negocio e metas de evolucao.

## 2. Stack Atual

- **Backend:** Java 17+, Spring Framework 6.1.x, Hibernate 6.4.x (JPA 3.1), Struts 7.1.x (Jakarta EE 10 compatible).
- **Frontend:** JSP (Jakarta), HTMX, Vite (para empacotamento de assets modernos), Vanilla CSS.
- **Segurança:** Spring Security 6.2.x (BCrypt, CSRF protection, CSP).
- **Build/Infra:** Maven 3.9+, Docker (Distroless), GitHub Actions/Hugging Face Spaces.

**Regra central:** Todo novo código deve seguir os padrões de Jakarta EE 10 e as melhores práticas de Spring 6/Struts 7. O uso de HTMX é mandatório para novas interações dinâmicas, evitando o retorno ao DWR ou Prototype.js legados.

> NOTE: Preencha com versoes reais e restricoes do ambiente.

### 2.1 Simplicidade Tecnológica

- Cada nova biblioteca, framework ou camada deve ser avaliada criticamente quanto a complexidade que adiciona, impacto na operação e risco de segurança.
- Prefira soluções simples e eficientes que atendam o requisito com menor custo de manutenção.
- Justifique formalmente adoções tecnológicas relevantes (ADR ou log dedicado), demonstrando benefícios concretos versus os custos de integração, suporte e treinamento.

## 3. Camadas e Dependencias (Regra de Ouro)

Fluxo recomendado (ajuste conforme o projeto):

```
Entrada (API/UI) -> Service/Business -> Persistencia -> Banco
```

Regras:
- A camada de entrada nao acessa persistencia diretamente.
- Services concentram regras de negocio e transacoes.
- Persistencia nao depende da camada web.
- Modelos de dominio nao dependem de frameworks web.

## 4. Regras de Compatibilidade

- Evite APIs exclusivas de versoes alvo antes da migracao.
- Mudancas que exigem quebra de compatibilidade devem ser isoladas e aprovadas por ADR.

## 5. Seguranca e Validacao

- Validacao de entradas e sanitizacao sao obrigatorias.
- SQL sempre parametrizado. Sem concatenacao de dados do usuario.
- Dados sensiveis nunca em logs em texto puro.

## 6. Observabilidade e Logs

- Padronize logging, niveis e formato.
- Proibido `System.out.println` (ou equivalente na linguagem alvo).

## 7. Testes e Qualidade

- Atualize testes ao alterar regras de negocio.
- Defina smoke tests para fluxos criticos.

## 8. ADR e Registro

- Decisoes arquiteturais nao triviais devem gerar ADR em `docs/adr/`.
- Use rascunho em `.ia/historico/` quando necessario.

## 9. Checklist de Conformidade

- [ ] Fluxo de camadas respeitado.
- [ ] Inputs validados e sanitizados.
- [ ] Sem dependencias exclusivas do stack alvo.
- [ ] Logs padronizados e seguros.
- [ ] Testes atualizados.
- [ ] ADR criado quando aplicavel.

> NOTE: Ajuste o checklist com criterios do seu time.
