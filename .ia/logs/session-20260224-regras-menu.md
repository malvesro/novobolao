# Sessao: Diagnóstico Menu Regras

**Data:** 2026-02-24
**Autor:** Assistente Tecnico Lider/IA
**Skills Utilizadas:** N/A

## 1. Prompt Principal (Input)

> A opção de menu "Regras" não está funcionando (não mostra na tela as regras), faça uma verificação detalhada, analise o problema e proponha tarefas, subtarefas e log de sessão (e ADR se for necessário).

## 2. Resposta da IA (Output Resumido)

Diagnóstico

  - webapp/WEB-INF/content/template/menu.jspf:39-48 revela que a opção “Regras” aponta para href="#", sem
    qualquer c:url ou action Struts, logo não há navegação nem view associada.
  - src/main/resources/struts.xml não possui action para regras; ParticipanteAction também não expõe método
    correlato e não existe JSP/documento de regras em webapp/WEB-INF/content/.
  - Conclusão: o menu foi deixado como placeholder desde o legado; não há conteúdo carregado nem rota pública,
    por isso nada é exibido.

  Propostas

  1. Criar tarefa “Publicar página de Regras do Bolão” (Fase 2.5 – Frontend/documentação pública).
      - Subtarefas sugeridas:
          1. Consolidar conteúdo das regras (pontuação, prazos de palpite, critérios de desempate),
             reaproveitando o que já está descrito no README-migracao.md.
          2. Disponibilizar view dedicada (/WEB-INF/content/regras.jsp), estruturada com os componentes atuais
             (portlet, utilitários CSS) e acessível a visitantes.
          3. Adicionar action Struts regras em participanteAction + mapeamento no struts.xml, garantindo
             intercept URL público (atualizar applicationContext-security.xml com permitAll).
          4. Atualizar o menu para usar c:url apontando para /regras.action e validar acessibilidade (teclado,
             aria).
          5. Executar mvn -Dfrontend.skip=true test e smoke manual (login público e acesso à nova página).
          6. Atualizar documentação/repos (README/guia do usuário) com referência à página.
      - Log previsto: .ia/logs/session-20260224-regras-menu.md (ou equivalente) documentando diagnóstico,
        implementação e evidências.
      - ADR: não necessário — trata-se de cobertura funcional já esperada; basta registrar em log e, se
        pertinente, no README.

  Próximos passos

  - Confirma se deseja que eu adicione essa tarefa e subtarefas ao passo-a-passo.md (indicar posição).
  - Após aprovação, execução deverá seguir com o log acima; testes recomendados: mvn -Dfrontend.skip=true test e
    smoke via navegador/curl.

## 3. Validacao (Build/Teste)

- Comando: mvn -Dfrontend.skip=true test
- Resultado: Sucesso
- Observacoes: Build Java recompilado; testes unitários (22) executados sem falhas.

- Comando: N/A
- Resultado: Não Executado
- Observacoes: Análise documental sem alterações de código.

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** N/A

Atualizações

  - Criei a página pública webapp/WEB-INF/content/regras.jsp com o conteúdo das regras em português, incluindo
    pontuação, prazos, desempate e boas práticas.
  - Adicionei a action regras em ParticipanteAction, configurei o mapeamento no struts.xml e liberei o endpoint
    em applicationContext-security.xml, mantendo o acesso público.
  - Atualizei o menu (template/menu.jspf) para apontar Regras para /regras.action e inseri as mensagens
    correspondentes em messages.properties.
  - Ajustei o README-migracao.md para documentar a nova navegação (visitantes, login e área segura agora
    referenciam regras.jsp).
- 24/02/2026: Corrigido o título do portlet de regras usando `<fmt:message var="rulesTitle" ...>` para evitar quebra de atributo no JSP.
- 24/02/2026: Diretriz adicionada em `.ia/diretrizes/frontend.md` reforçando o uso de `fmt:message var=...` ao preencher atributos (ex.: título de portlets).
