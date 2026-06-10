# Log: Correção de JPA Exception (2026-06-10)

## Problema
O acesso à tela de palpites resultava em `InvalidDataAccessApiUsageException` devido a parâmetros nomeados não serem corretamente mapeados pelo Spring Data JPA 3.x, exigindo a preservação de nomes de parâmetros ou anotações explícitas.

## Resolução
1.  **Configuração de Compilação:** Adicionado `<parameters>true</parameters>` ao `maven-compiler-plugin` no `pom.xml`.
2.  **Refatoração de Código:** Renomeado o parâmetro na consulta de `JogoRepository.findFirstDateWithGamesOnOrAfter` de `:data` para `:dataReferencia` e garantido o uso de `@Param("dataReferencia")`.

## Verificação
- `mvn clean compile` executado com sucesso.
- Testes `JogoRepositoryTest`, `JogoServiceImplTest` e `ParticipanteActionTest` executados e passando.
