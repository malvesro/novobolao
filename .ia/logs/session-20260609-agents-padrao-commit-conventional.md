# Sessao: Padronizacao de instrucoes de commit no AGENTS.md

**Data:** 2026-06-09
**Autor:** Arquiteto de Software Sênior (Time Mercúrio)
**Skills Utilizadas:** architecture-guardian v1.0.0

## 1. Prompt Principal (Input)

> Faça uma análise do AGENTS.md e verifique a melhor forma e lugar no documento para intruir como fazer o commit. Melhore o texto a ser colocado no documento como um engenheiro de contexto e prompt sênior com técnicas avançadas: faça o commit das alterações no padrão conventional commits com emoji e texto do commit em português do Brasil com explicações didáticas, claras e concisas de forma profissional: Cada alteração deve ser fácil de entender.

## 2. Resposta da IA (Output Resumido)

> Foi realizada análise estrutural do AGENTS.md e identificado o melhor ponto para a instrução de commits na seção já existente `### 6. Realização de commits frequentes`.
>
> Alteração aplicada:
> - Inclusão da subseção `#### 6.1 Padrão obrigatório de mensagem de commit (Conventional Commits + Emoji)` com:
>   - formato obrigatório da linha de assunto;
>   - regras objetivas de tipo, emoji, idioma e qualidade textual;
>   - template recomendado (contexto, alterações, impacto);
>   - exemplo concreto em pt-BR com foco em segurança.
>
> Resultado esperado:
> - redução de ambiguidade nas mensagens;
> - melhora na rastreabilidade para revisão e auditoria;
> - padronização consistente entre tarefas técnicas e documentação.

## 3. Validacao (Build/Teste)

- Comando: revisão estática do diff do `AGENTS.md`.
- Resultado: Sucesso.
- Observacoes: alteração documental, sem impacto de build.

## 4. Analise Humana (Veredito)

- [ ] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** Texto posicionado no ponto de maior afinidade semântica do documento (governança de commits), evitando dispersão de regras.
