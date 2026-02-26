# Plano de Correção – Bugs na Tela de Cadastro (26/02/2026)

## Contexto e Escopo

- **Bug 1:** Link “Página principal” exibido após o sucesso do cadastro aponta para o contexto raiz e não retorna ao fluxo público esperado.
- **Bug 2:** Submissões duplicadas de cadastro acionam `DataIntegrityViolationException` (violação de chave única) e resultam em HTTP 500, sem mensagem amigável ao usuário.
- Abrange ajustes no frontend (JSP) e backend (Struts/Spring/Hibernate) relacionados ao fluxo de cadastro público.
- Referências iniciais: `webapp/WEB-INF/content/cadastro.jsp`, `ParticipanteAction`, `ParticipanteServiceImpl`, `ParticipanteDao`.

## Premissas

- Manter compatibilidade com as diretrizes vigentes (`.ia/diretrizes/frontend.md`, `.ia/diretrizes/seguranca.md`).
- Garantir logs de sessão e atualização do `passo-a-passo.md` ao concluir cada tarefa.
- Utilizar skills relevantes (`modernization-java-migration v1.0.0`, `security-audit v1.0.0`) conforme necessário.

## Entregáveis

1. Link funcional de retorno após cadastro concluído, com regressão testada.
2. Tratamento adequado para cadastros duplicados, exibindo mensagem orientativa sem erro 500.
3. Testes automatizados (unitários e/ou integração) cobrindo os novos comportamentos.
4. Documentação atualizada (README/guia ou notas relevantes) e logs de sessão.

## Plano Passo a Passo

### Tarefa 1 – Corrigir link de retorno na tela de cadastro (Bug 1)
1. **Reprodução e análise**
   - Reproduzir o fluxo completo até a mensagem de sucesso.
   - Capturar o HTML final e validar o valor gerado por `<c:url var="contextURL" value="/" />`.
   - Registrar observações no log de sessão.
2. **Mapeamento da rota correta**
   - Verificar qual action pública deve ser o destino (`/login.action`, `/index.action` ou outra página definida na navegação).
   - Confirmar com as diretrizes de navegação pública e menu principal.
3. **Ajuste da view**
   - Atualizar `cadastro.jsp` (ou fragmento correspondente) para apontar para a action certa usando `<c:url>`.
   - Garantir consistência com acessibilidade (rótulo amigável, suporte a teclado).
4. **Validação**
   - Executar `mvn -Dfrontend.skip=true test`.
   - Validar manualmente via navegador/Docker e registrar evidência.
5. **Rastreabilidade**
   - Atualizar `passo-a-passo.md` (Fase 2) com conclusão da tarefa.
   - Registrar log em `.ia/logs/session-YYYYMMDD-cadastro-link.md`.

### Tarefa 2 – Prevenir 500 em cadastros duplicados (Bug 2)
1. **Diagnóstico técnico**
   - Reproduzir o erro usando credenciais já cadastradas.
   - Inspecionar `ParticipanteAction.cadastrar` e `ParticipanteServiceImpl.criarNovo`.
   - Levantar validações Struts/Spring existentes para login e e-mail.
2. **Desenho da correção**
   - Decidir entre validação prévia (consulta ao serviço) ou tratamento da exceção (`DataIntegrityViolationException`) com mensagem customizada.
   - Mapear mensagem i18n e feedback na UI (`errosInclusao`).
3. **Implementação**
   - Atualizar camada de serviço/DAO para checar duplicidade (login e, se aplicável, e-mail) antes do `save`.
   - Ajustar `ParticipanteAction` para popular mensagens amigáveis e retornar ao formulário sem interromper a sessão.
   - Garantir sanitização/normalização consistente com utilitários existentes.
4. **Testes**
   - Criar/atualizar testes unitários (`ParticipanteServiceImplTest`, `ParticipanteActionTest`) cobrindo o cenário de duplicidade.
   - Considerar teste de integração com banco em memória simulando o constraint.
   - Reexecutar `mvn -Dfrontend.skip=true test`.
5. **Documentação e Logs**
   - Atualizar README/guia (se necessário) descrevendo o comportamento esperado quando o login já existir.
   - Registrar log de sessão (`.ia/logs/session-YYYYMMDD-cadastro-duplicado.md`).
   - Atualizar `passo-a-passo.md` (Fase 2) com o status concluído.

### Tarefa 3 – Revisão final e evidências
1. **Smoke Testing**
   - Executar fluxo de cadastro bem-sucedido + tentativa duplicada.
   - Registrar capturas de tela, se possível, em `telas/`.
2. **Verificação de Logs**
   - Garantir ausência de stack traces no log do aplicativo para ambos os cenários.
3. **Encerramento**
   - Consolidar conclusões no log de sessão final.
   - Preparar mensagem de commit alinhada às diretrizes.
   - Solicitar revisão/aprovação conforme governança do time Mercúrio.

## Dependências/Observações

- Confirmar se há tarefas correlatas em andamento (ex.: sanitização, seeds) para evitar conflito.
- Se surgir necessidade de ajuste estrutural (ex.: alteração de rotas públicas), propor ADR ou atualização das diretrizes.

