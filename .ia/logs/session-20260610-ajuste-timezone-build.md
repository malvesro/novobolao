# Log: Ajuste de Fuso Horário no Timestamp de Build (2026-06-10)

## Problema
O timestamp de build gerado automaticamente pelo Maven (`maven.build.timestamp`) utilizava UTC, resultando em um horário desalinhado com o fuso horário de Brasília (UTC-3).

## Resolução
Adição do `build-helper-maven-plugin` para gerar um timestamp customizado configurado explicitamente para `America/São_Paulo`.

1.  **Configuração de Build (`pom.xml`):** Adicionado `build-helper-maven-plugin` gerando a propriedade `build.timestamp.sp`.
2.  **Configuração de Propriedades (`version.properties`):** Atualizado para referenciar `build.timestamp.sp`.

## Verificação
- Após `mvn clean compile`, o arquivo `target/classes/version.properties` mostra `build.timestamp=2026-06-10T16:17:16-03:00`, confirmando o uso correto do fuso horário de São Paulo (-03:00).
