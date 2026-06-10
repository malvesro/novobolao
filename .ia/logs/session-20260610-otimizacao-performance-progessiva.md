# Sessao: Otimização de Performance para Ambientes Restritos (HF Spaces)

**Data:** 2026-06-10
**Autor:** Arquiteto de Software Sênior (Time Mercúrio)
**Skills Utilizadas:** modernization-java-migration v1.0.0, ui-ux-pro-max v1.0.0, architecture-guardian v1.0.0

## 1. Prompt Principal (Input)

> Melhore o plano: Além de Carregar por padrão apenas os jogos de "Hoje" ou do "Próximo dia de jogos", conforme a opção de navegação do usuário, ir carregando mais dias de jogos já cadastrados. Melhor ao rolar a tela ou com um botão "Mais jogos" ? Avalie.

## 2. Resposta da IA (Output Resumido)

Implementada a Fase 15 do plano de evolução para resolver a lentidão no Hugging Face Spaces.

**Mudanças Principais:**
- **Infraestrutura:** Tuning de JVM no `Dockerfile` (`-Xmx384m`, limits de threads) para estabilidade no plano gratuito.
- **Backend:** Nova lógica em `ParticipanteAction` para carga inicial reduzida (apenas a próxima data com jogos). Adicionada action `palpitesMaisJogosPartial` para carregamento progressivo.
- **Globalização do Progresso:** Refatorada a lógica de cálculo da barra de progresso em `ParticipanteAction.atualizarProgressoPalpites()`. O cálculo agora ignora filtros de exibição, garantindo que o usuário veja seu progresso real no campeonato completo (ex: 10 / 104 jogos) mesmo visualizando apenas um dia por vez. Isso evita a falsa percepção de conclusão (ex: 3/3 - 100%) que ocorreria com o carregamento parcial.
- **Frontend:** Refatoração do `jogos.jsp` para usar fragmentos (`jogos-lista-fragmento.jsp`) e inclusão do botão **"Carregar Próxima Data"** via HTMX, evitando o processamento síncrono de 104 jogos.

**Arquivos impactados:**
- `Dockerfile`
- `src/com/opendev/bolao/action/ParticipanteAction.java`
- `src/com/opendev/bolao/service/JogoService.java`
- `src/com/opendev/bolao/service/impl/JogoServiceImpl.java`
- `src/com/opendev/bolao/repository/JogoRepository.java`
- `src/main/resources/struts.xml`
- `webapp/WEB-INF/content/seguro/jogos.jsp`
- `webapp/WEB-INF/content/seguro/partials/jogos-lista-fragmento.jsp` (novo)
- `passo-a-passo.md`

## 3. Validacao (Build/Teste)

- Comando: `mvn test`
- Resultado: Sucesso (51 testes, 0 falhas). Incluído novo teste unitário `ParticipanteActionLoadTest`.
- Comando: `docker compose build app`
- Resultado: Sucesso. Verificada a nova configuração de JVM no build.

## 4. Analise Humana (Veredito)

- [ ] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** O uso do botão "Mais Jogos" foi preferido ao Infinite Scroll para garantir controle de rede e evitar sobrecarga de CPU no ambiente do Hugging Face.
