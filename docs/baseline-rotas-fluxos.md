# Baseline de Rotas e Fluxos (Sistema Bolao)

Data: 2026-02-13
Fonte: `src/xwork.xml`, `webapp/WEB-INF/dwr.xml`, `webapp/`

## 1. Actions (WebWork/XWork)

### Pacote `default`
- `logout` -> `participanteAction.logout` -> `/login.jsp`
- `cadastro` -> `participanteAction.cadastrar` -> `/cadastro.jsp` (success/input)

### Pacote `seguro` (namespace `/seguro`)
- `ranking` -> `participanteAction.buscarParticipantes` -> `/seguro/classificacao.jsp`
- `palpites` -> `participanteAction.prepararInfoPalpites` -> `/seguro/jogos.jsp`
- `graficoDesempenho` -> `participanteAction.gerarGraficoDesempenho` -> `/seguro/graficoDesempenho.jsp`
- `copa` -> `participanteAction.gerarClassificacaoDaCopa` -> `/seguro/copa.jsp`
- `principal` -> `participanteAction.obterDadosPaginaPrincipal` -> `/seguro/principal.jsp`

### Pacote `admin` (namespace `/admin`)
- `infoEquipes` -> `adminAction.carregarInfoEquipes` -> `/admin/inclusaoJogo.jsp`
- `jogos` -> `adminAction.carregarJogos` -> `/seguro/jogos.jsp`
- `participantes` -> `adminAction.carregarParticipantes` -> `/admin/participantes.jsp`

## 2. Endpoints DWR

### `AdminAction` (Spring bean `adminAction`)
- `criarNovoJogo`
- `atualizarResultadoDoJogo`
- `autorizarParticipante`
- `atualizarPapelParticipante`
- `apagarParticipante`

### `ParticipanteAction` (Spring bean `participanteAction`)
- `buscarPalpiteDoJogo`
- `atualizarPalpite`
- `buscarPalpitesDoJogo`
- `buscarMeusPalpites`

### `BatePapo` (Spring bean `batePapo`)
- `enviarMensagem`
- `buscarInformacoesDoParticipante`
- `entrar`
- `sair`
- `buscarTodosParticipantes`
- `buscarMensagens`
- `buscarIdUltimaMensagem`
- `alterarApelido`

## 3. JSPs (Views)

### Raiz
- `index.jsp`
- `login.jsp`
- `cadastro.jsp`

### `admin/`
- `admin/inclusaoJogo.jsp`
- `admin/participantes.jsp`

### `seguro/`
- `seguro/principal.jsp`
- `seguro/jogos.jsp`
- `seguro/classificacao.jsp`
- `seguro/graficoDesempenho.jsp`
- `seguro/copa.jsp`
- `seguro/batePapo.jsp`
- `seguro/trocaSenha.jsp`

## 4. Observacoes
- `index.jsp` redireciona para `/seguro/principal.jsp` se autenticado, senao `/login.jsp`.
- O mapeamento de Actions usa `*.action` conforme `web.xml`.
- O namespace `/seguro` e `/admin` devem ser preservados na migracao.
