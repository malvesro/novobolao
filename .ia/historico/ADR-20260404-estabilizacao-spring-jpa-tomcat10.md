# ADR-20260404-estabilizacao-spring-jpa-tomcat10

**Data:** 2026-04-04
**Status:** Aprovado

## Contexto

A migração para Spring Data JPA 3.x em um projeto legado baseado em arquivos de configuração Spring XML enfrentou dois impedimentos críticos durante o deploy no Tomcat 10.1:
1.  **Bug do EntityPathResolver:** O namespace `<jpa:repositories>` falhava em injetar o resolver padrão, exigindo dependência de QueryDsl mesmo sem uso do mesmo.
2.  **Incompatibilidade de Versões:** O downgrade para 3.1.x (tentativa inicial) causou quebra no `FactoryBean` com o Spring 6.1.14.

## Decisao

Decidimos abandonar a configuração de repositórios via XML em favor do **Java Config** utilizando a anotação `@EnableJpaRepositories`. 

**Ações tomadas:**
1.  Criação da classe `JpaRepositoriesConfig` para gerenciar o scan de repositórios.
2.  Manutenção do Spring Data JPA na versão **3.2.5** (versão recomendada para Spring 6.1.x).
3.  Substituição do `OpenSessionInViewFilter` pelo `OpenEntityManagerInViewFilter` no `web.xml`.

## Alternativas Consideradas

1.  **Downgrade para Spring Data JPA 3.1.x:** Rejeitado pois causou `Invalid value type for factoryBeanObjectType`.
2.  **Adição de dependência QueryDsl:** Rejeitado para evitar inflar o classpath com bibliotecas desnecessárias.

## Consequencias

- **Positivos:** Bootstrap estável e compatível com as regras de injeção do Spring 6. Projeto alinhado com as especificações Jakarta EE 10.
- **Riscos:** Configuração híbrida (XML + Java Config) exige atenção ao `component-scan`.

## Responsaveis

- Antigravity (IA)
