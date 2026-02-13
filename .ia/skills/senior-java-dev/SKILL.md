---
skill_name: senior-java-dev
description: Orientações de desenvolvimento Java + Struts 7.
version: 2.0.0
tags: [java, struts 7, spring, coding-standards]
---

# Guia do Desenvolvedor Java

## 1. Auto-Análise Obrigatória

Antes de finalizar qualquer entrega, responda explicitamente:

> **Auto-Análise:**  
> 
> - **Escopo:** qual funcionalidade/regra foi tocada?  
> - **Camadas:** quais componentes (Action, Service, DAO, JSP) foram alterados?  
> - **Validação:** como os dados de entrada foram saneados/validados?  
> - **Testes:** quais testes automatizados/manuals cobrem a mudança?

## 2. Regras de Sintaxe e Estilo

1. **Tratamento de Nulos**  
   
   - Evite retornar `null`. Quando inevitável (por legado), documente o comportamento.  
   - Só use `Optional` em retornos de serviço/repositorio; nunca como argumento de método.

2. **Injeção de Dependência**  
   
   - Actions Struts recebem dependências via setters/`with*`; 
   - serviços Spring utilizam construtores ou setters declarados em XML.  
   - Não introduza Lombok; mantenha código explícito.

3. **Logging e Mensagens**  
   
   - Use SLF4J (`LOGGER`) já presente no projeto.  
   - Mensagens de log em português claro; nada de `System.out.println`.

4. **Tratamento de Exceções**  
   
   - Propague exceções checadas relevantes (`SQLException`, `AplicacaoException`).  
   - Use `try-with-resources` para conexões/streams.  
   - Nunca capture exceções genéricas para silenciar erros.

## 3. Padrões por Camada

### Actions (https://struts.apache.org/)

- Sem lógica de negócio ou acesso direto a DAOs.  
- Configure mensagens, sessões e navegação (result codes).  
- Utilize `withSession`, `withServletRequest`, `withServletResponse`; mantenha métodos `set*` apenas como adaptadores.
- Empregue interceptors de segurança (`tokenSession`, `defaultStack`) e garanta `<s:token/>` em formulários que alteram estado.  
- Nunca exponha entidades diretamente em JSP; converta para objetos de exibição apropriados.

### Business / Service

- Implementam regras e orquestram transações.  
- Não manipulam `HttpServletRequest`/`HttpSession`.  
- Preferir métodos expressivos (ex.: `carregarTempoExpiracaoChaveAcesso()`), reutilizando utilitários existentes.

### DAO / Persistence

- Localização: `br.jus.tse.administrativa.online.dao`, `br.jus.tse.spring.dao`.  
- Utilize `DAO.novaConexao()` ou `JdbcTemplate` conforme o padrão local.  
- Mantenha SQL parametrizado; sem concatenação direta de dados do usuário.

### JSP / WebContent

- Estritamente para view. Use JSTL/Tags Struts, evitando scriptlets.  
- Centralize strings em bundles quando necessário (i18n).

## 4. Testes

- **Unitários:** JUnit 5 (Jupiter). Evite Mockito “solto”; configure stubs claros.  
- **Integração:** quando acessar banco, prefira testes controlados com dados de homologação ou mocks.  
- Atualize testes em sempre que uma regra mudar.

## 5. Checklist Final

- [ ] Actions, Services e DAOs respeitam o fluxo Action → Service → DAO?  
- [ ] Inputs vindos de formulários foram validados/normalizados?  
- [ ] Não há chamadas diretas a APIs proibidas (System.out, Lombok, libs não autorizadas)?  
- [ ] Testes existentes foram atualizados ou novos testes foram criados?  
- [ ] Documentação (JavaDoc, markdown) atualizada quando necessário?
- [ ] A mensagem de commit criada pelo desenvolvedor está clara, segue o padrão do time (prefixo + contexto) e explica “o quê/por quê”? Caso contrário, redija uma sugestão profissional e ofereça automatizar o `git commit --amend`.
- [ ] Se houve nova decisão técnica (ex.: adotar interceptador, alterar fluxo), foi aberto um ADR em `01_docs/adr/` seguindo o padrão `ADR-YYYYMMDD-titulo-curto.md` com data, motivação, alternativas e resultado?

Falhando em qualquer item acima, reavalie antes de entregar. A consistência do monolito depende dessas boas práticas.

## 6. A cada progresso com sucesso, fazer um commit local.

- Quando o build funcionar após uma mudança ou tarefa exeutada.
- Quando algum código novo for criado e funcionar.
- Quando algum código for migrado com sucesso (após testes). 
- Cada tarefa com sucesso. 

## 7. Validação de Mensagens de Commit

- Analise sempre a mensagem mais recente (`git log -1 --pretty=%B`).  
- Verifique se contém prefixo do time (`feat:`, `fix:`, `refactor:`, `docs:`, `chore:` etc.), módulo entre parênteses quando caber e descrição objetiva.  
- Inclua “por quê” (motivo ou contexto) e, se possível, destaque impactos/arquivos críticos.  
- Caso a mensagem esteja confusa, proponha uma nova sugestão clara e didática.  
- Informe ao usuário como aplicar a correção (por exemplo: `git commit --amend -m "nova mensagem"`).  
- Somente realize a alteração automática quando o usuário autorizar explicitamente.
- Ao sugerir uma nova mensagem, use o padrão `tipo(escopo): resumo` seguido de bullets explicando as alterações (arquivos/impactos).
