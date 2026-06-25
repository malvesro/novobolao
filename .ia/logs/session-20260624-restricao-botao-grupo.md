# Sessao: Restricao do botao "Ver palpites do grupo" a janela de palpites encerrada

**Data:** 2026-06-24
**Autor:** Cline (Arquiteto de Software Senior)
**Skills Utilizadas:** architecture-guardian v1.0.0, java17-struts7 v1.0.0, jsp-jspf v1.0.0, security-audit v1.0.0

## 1. Prompt Principal (Input)

> Ajustar regra: O botao de palpites que permite ver os palpites de todos os participantes so pode ser habilitado quando faltar 1 h para cada jogo, quando nenhum participante nao pode mais fazer palpites ou modificar seu palpite para aquele jogo. Assim, um participante so pode ver os palpites dos demais quando nao for mais possivel realizar palpites para aquele jogo, evitando copiar o palpite de outro participante.

## 2. Resposta da IA (Output Resumido)

### Analise do Codigo Existente

O metodo `getPodeVerPalpitesGrupo()` ja existia em `Jogo.java` (linhas 212-214) com a logica correta `return !getPodeDarPalpite();`, que retorna `true` quando a janela de palpites esta encerrada (falta <=1h para o jogo comecar).

### Arquivos Alterados

1. **webapp/WEB-INF/content/seguro/partials/match-row.jspf**
   - Substituido o botao fixo por um bloco `c:choose` condicional:
     - Quando `jogo.podeVerPalpitesGrupo` e `true`: botao habilitado com `hx-get`, `aria-expanded`, `aria-controls` e tooltip i18n `match.tip.group.view`.
     - Quando `false`: botao desabilitado (`disabled`, `aria-disabled="true"`) com classes CSS `btn-grupo-toggle--disabled` e tooltip explicativo via i18n `match.tip.group.disabled.tooltip`.

2. **src/main/resources/messages.properties**
   - Adicionadas 3 novas chaves i18n:
     - `match.tip.group.view=Ver palpites do grupo`
     - `match.tip.group.disabled.label=Palpites do grupo indisponiveis`
     - `match.tip.group.disabled.tooltip=Os palpites do grupo ficarao visiveis quando a janela de palpites for encerrada (1 hora antes do jogo).`

3. **src/com/opendev/bolao/action/ParticipanteAction.java**
   - Adicionada validacao server-side (defesa em profundidade) no metodo `listarPalpitesDoJogoHtmx()`:
     - Busca o `Jogo` pelo `jogoId`.
     - Valida `jogo.getPodeVerPalpitesGrupo()` antes de retornar os palpites.
     - Se a janela ainda estiver aberta, retorna lista vazia com log `WARN [HTMX][GRUPO]`.

4. **tests/com/opendev/bolao/model/JogoTest.java**
   - Adicionados 3 novos testes unitarios:
     - `devePermitirVisualizarPalpitesDoGrupoQuandoJanelaEncerrada`: valida que `getPodeVerPalpitesGrupo()` retorna `true` quando falta <1h.
     - `deveBloquearVisualizacaoDePalpitesDoGrupoQuandoJanelaAberta`: valida que retorna `false` quando falta >1h.
     - `deveRetornarRelacaoInversaEntrePodeDarPalpiteEPodeVerPalpitesGrupo`: valida a relacao inversa entre os dois metodos.

### Decisoes Tecnicas

- **Nao foi necessario alterar Jogo.java** — o metodo `getPodeVerPalpitesGrupo()` ja existia com a logica correta.
- **Tooltip i18n** em vez de title fixo para manter internacionalizacao consistente.
- **Defesa em profundidade** no backend: mesmo que o frontend seja burlado, o servidor nao retorna palpites se a janela estiver aberta.
- **Testes:** 3 novos cenarios para garantir que a regra nao regrida.

## 3. Validacao (Build/Teste)

- Comando: `mvn test -Dtest=JogoTest -Dfrontend.skip=true`
- Resultado: Sucesso
- Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
- Observacoes: Os 3 novos testes + 6 existentes passaram. Nao ha quebra de regressao.

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** implementacao completa seguindo o plano. Atualizado passo-a-passo.md com task 79, registrado neste log de sessao.