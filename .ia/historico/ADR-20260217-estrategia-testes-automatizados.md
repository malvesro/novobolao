# ADR-20260217-estrategia-testes-automatizados

## Contexto
O projeto carece de testes automatizados. Com a migração para Spring 6 e Hibernate 6, é fundamental garantir que as regras de negócio permaneçam íntegras durante as próximas fases de modernização e refatoração.

## Decisão
Adotaremos uma pirâmide de testes focada em:
1.  **Testes de Unidade:** Foco em lógica de negócio pura (Services e Utils), utilizando JUnit 5 e Mockito.
2.  **Testes de Integração:** Foco na camada de persistência (DAOs) e integração Spring, utilizando Spring Test e possivelmente H2 em memória para rapidez.

### Stack Tecnológica
- **Framework de Teste:** JUnit 5 (Jupiter).
- **Mocking:** Mockito.
- **Spring Test:** Integração com o contexto do Spring 6.
- **AssertJ:** Para asserções fluídas e legíveis.

## Consequências
- Maior confiança para refatorações futuras.
- Documentação viva das regras de negócio através dos testes.
- Aumento inicial no tempo de desenvolvimento (compensado pela redução de bugs em produção).

## Status: Proposto
Data: 2026-02-17
Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
