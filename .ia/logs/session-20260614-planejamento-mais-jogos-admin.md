# Sessao: Planejamento do Botão "Mais Jogos" na Tela Administrativa

**Data:** 2026-06-14
**Autor:** Antigravity (Arquiteto Sênior)
**Skills Utilizadas:** [modernization-java-migration v1.0.0, architecture-guardian v1.0.0, ui-ux-pro-max v1.0.0]

## 1. Prompt Principal (Input)

> Leia o AGENTS.md e siga suas diretivas. Depois crie um plano para adicionar um botão no final da tela de Atualizar Resultados (perfil administrador) para carregar a próxima data como foi feito na tela de palpites e resultados e crie uma nova tarefa com suas subtarefas no arquivo passo-a-passo.md que possam serem executadas em pequenas iterações.

## 2. Resposta da IA (Output Resumido)

Planejamento e criação da Tarefa 48 no `passo-a-passo.md` para implementar o carregamento incremental de jogos na tela administrativa de resultados.

**Arquivos Impactados (Planejado):**
- `src/com/opendev/bolao/action/AdminAction.java`: Adição do método `buscarMaisJogosHtmx`.
- `src/main/resources/struts.xml`: Novo mapeamento de action no namespace `/admin`.
- `webapp/WEB-INF/content/seguro/partials/jogos-lista-fragmento.jsp`: Ajuste na lógica do botão HTMX para suportar o contexto admin.

**Decisões Arquiteturais:**
- Manter a paridade funcional com a tela de palpites para uma experiência de usuário (UX) coesa.
- Utilizar HTMX para garantir performance em ambientes com recursos limitados (como o Hugging Face Spaces).
- Preservar o isolamento de contextos (`adminResultadoView`) na JSP compartilhada.

## 3. Validacao (Build/Teste)

- Comando: N/A (Apenas planejamento nesta etapa).
- Resultado: Sucesso.
- Observacoes: A tarefa 48 foi inserida no `passo-a-passo.md` seguindo a sequência lógica atual do projeto.

## 4. Analise Humana (Veredito)

- [ ] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** N/A
