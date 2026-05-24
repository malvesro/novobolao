# ADR-20260217-seguranca-inicial

**Data:** 2026-02-17
**Status:** Rascunho

## Contexto

As primeiras tarefas do plano de modernização do "Sistema Bolão" focaram em endereçar as vulnerabilidades de segurança mais críticas identificadas na "Análise Inicial Obrigatória" (`analise-inicial.md`). As decisões abaixo foram tomadas para mitigar riscos imediatos e estabelecer uma base mais segura para futuras evoluções, mantendo a compatibilidade com a arquitetura existente.

## Decisao

Foram implementadas as seguintes medidas de segurança:

1.  **Forçar HTTPS para toda a Aplicação:**
    *   **Motivação:** A ausência de HTTPS no formulário de login e em outras partes da aplicação expunha credenciais e dados do usuário a interceptação (`man-in-the-middle`).
    *   **Implementação:** Adição de um `security-constraint` no `webapp/WEB-INF/web.xml` com `transport-guarantee` definido como `CONFIDENTIAL` para `/*`, garantindo que todas as requisições sejam redirecionadas para HTTPS.
    *   **Ferramenta/Skill:** `senior-java-dev-legacy v1.0.0` (implica conhecimento do `web.xml` e configuração de segurança Java EE).

2.  **Desativar o Modo Debug do DWR em Produção:**
    *   **Motivação:** O modo debug do DWR (`Direct Web Remoting`) expunha detalhes internos da aplicação e de serviços, representando um risco de divulgação de informações sensíveis e potenciais ataques.
    *   **Implementação:** Alteração do parâmetro `debug` para `false` no servlet `dwr-invoker` em `webapp/WEB-INF/web.xml`.
    *   **Ferramenta/Skill:** `senior-java-dev-legacy v1.0.0` (implica conhecimento do `web.xml` e configuração de servlets).

3.  **Auditoria e Melhoria do Hashing de Senhas (Transição de SHA-1 para BCrypt):**
    *   **Motivação:** O uso de SHA-1 sem salt para hashing de senhas é uma prática insegura, tornando as senhas vulneráveis a ataques de força bruta e tabelas arco-íris.
    *   **Implementação:**
        *   Adição da dependência `spring-security-crypto` (versão 5.7.7) ao `pom.xml`.
        *   Criação da classe `com.opendev.bolao.util.DelegatingPasswordEncoder` que implementa `org.acegisecurity.providers.encoding.PasswordEncoder` e gerencia a transição gradual. Ela verifica hashes existentes (sem prefixo = SHA-1) e codifica novas senhas com BCrypt (prefixado com `{bcrypt}`).
        *   Atualização de `src/applicationContext-security.xml` para configurar o `passwordEncoder` como uma instância de `DelegatingPasswordEncoder`, injetando encoders para SHA-1 e BCrypt.
        *   Modificação de `ParticipanteServiceImpl.java` para utilizar o `passwordEncoder` injetado ao criar novos participantes.
    *   **Ferramenta/Skill:** `senior-java-dev-legacy v1.0.0`, `security-audit v1.0.0` (análise de risco e sugestão de melhoria).

## Alternativas Consideradas

*   **Não implementar as medidas:** Manter a aplicação em estado de alta vulnerabilidade, expondo dados de usuários e a infraestrutura a ataques. Considerado inaceitável devido aos riscos críticos.
*   **Reescrever imediatamente todo o sistema de segurança:** Abordagem ideal em termos de segurança, mas inviável para a fase atual de curto prazo devido ao alto custo e impacto na estabilidade da aplicação legada. A estratégia de `DelegatingPasswordEncoder` permite uma transição suave.

## Consequencias

*   **Impactos Positivos:**
    *   Melhora significativa na segurança da comunicação (HTTPS) e do armazenamento de senhas.
    *   Redução da superfície de ataque da aplicação ao desativar o debug do DWR.
    *   Estabelecimento de uma base mais robusta para futuras modernizações de segurança.
    *   Possibilidade de coexistência de senhas antigas (SHA-1) com novas (BCrypt) durante a fase de transição.
*   **Riscos ou Custos:**
    *   Aumento da complexidade inicial da configuração do `PasswordEncoder`.
    *   Necessidade de re-testar as funcionalidades de login e cadastro.
    *   Configuração do ambiente de deployment pode precisar ser ajustada para lidar com HTTPS (certificado, porta).

## Responsaveis

- Equipe de desenvolvimento.

## Auto-Análise Final:
Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
As alterações são focadas em segurança, com mínimo impacto na lógica de negócio e na compatibilidade para a transição.
