# ADR-20260219-upgrade-struts-7.md

## Status
Aprovado

## Contexto
O projeto original utilizava **WebWork 2.2.2** (baseado em XWork 1.1.3), uma tecnologia descontinuada há mais de uma década. No início da Fase 2 de modernização, tentamos migrar para o **Struts 6.3.0**. 

No entanto, durante a validação em runtime no **Tomcat 10** (que implementa **Jakarta EE 10**), identificamos um erro crítico `java.lang.NoClassDefFoundError: javax/servlet/Filter`. Este erro ocorreu porque o Struts 6.x ainda utiliza o namespace legado `javax.*`, enquanto o Tomcat 10 exige o novo namespace `jakarta.*`.

## Decisão
Decidimos atualizar o framework web para o **Struts 7.0.0**. 

Esta versão é a primeira do Apache Struts a oferecer suporte nativo completo para o **Jakarta EE 10** e **JDK 17+**, eliminando todas as referências ao namespace `javax.*` em seu núcleo e dependências.

### Mudanças Técnicas Necessárias:
1.  **Versão do Maven:** Atualizado `${struts.version}` para `7.0.0`.
2.  **Namespace Java:** Atualização de todos os imports de `com.opensymphony.xwork2.*` para `org.apache.struts2.*` (ex: `ActionSupport`).
3.  **Configuração XML:** Atualização da DTD no `struts.xml` para a versão 2.5/7.0.
4.  **Anotações de Segurança:** Adoção obrigatória de `@StrutsParameter` para injeção de parâmetros em Actions (exigência do Struts 7).

## Consequências

### Positivas:
- **Compatibilidade Nativa:** Integração perfeita com o Tomcat 10 e Jakarta Servlet 6.0.
- **Segurança Proativa:** O Struts 7 habilita por padrão o **OGNL Allowlisting**, mitigando ataques de injeção de expressão.
- **Manutenibilidade:** Uso de uma stack moderna com suporte ativo da comunidade Apache.

### Negativas/Desafios:
- **Refatoração Obrigatória:** Todas as Actions que recebem parâmetros via setters agora exigem a anotação `@StrutsParameter`.
- **Quebra de Retrocompatibilidade:** Removido o suporte ao XWork legado, exigindo atualização de TLDs e configurações XML antigas.

## Referências
- [Apache Struts Security Guide](https://struts.apache.org/security/)
- [Struts 7.0.0 Release Notes](https://struts.apache.org/docs/version-notes-700.html)
- Log de Sessão: `.ia/logs/session-20260219-correcoes-runtime-v2.md`
