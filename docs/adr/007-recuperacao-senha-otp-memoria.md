# ADR-007: Recuperação de Senha com OTP em Memória

**Data:** 2026-04-08
**Status:** Aprovado

## Contexto

O sistema precisa implementar o fluxo publico de recuperacao de senha com **codigo numerico temporario (OTP)** e expiração curta (30 minutos). O cenário e recreativo, com baixo risco operacional, e foi aceito que o armazenamento em memoria volatil e suficiente, mesmo com perda de OTP em caso de restart. Ainda assim, a solucao deve garantir:
- Uso unico do codigo e invalidacao imediata apos a troca de senha.
- Rejeicao de codigos expirados.
- Mensagem neutra para evitar enumeracao de usuarios.
- Registro apenas da **data da ultima troca de senha** no participante.

O projeto adota arquitetura monolitica (Action -> Service -> Persistencia) e usa MySQL, logo a estrategia deve ser compativel com esse stack, **sem exigir nova tabela**.

## Decisao

Adotar **OTP numerico em memoria (volatil)**, com hash do codigo, expiracao de 30 minutos, uso unico e limite de tentativas. O armazenamento sera local (cache em memoria) e sera considerado aceitavel perder codigos em reinicios do sistema. Para persistencia minima, sera registrado **somente** o timestamp da ultima troca de senha no registro do participante.

## Alternativas Consideradas

1. **Token stateless (JWT/assinatura) sem persistencia:**
   - **Prós:** sem nova tabela, menos escrita no banco.
   - **Contras:** revogacao dificil, reutilizacao possivel até expirar, nenhum registro para auditoria/abuso.

2. **Tabela dedicada para tokens (RST_RESET_TOKEN):**
   - **Prós:** auditoria basica, revogacao, multi-instancias e limpeza previsivel.
   - **Contras:** aumenta complexidade, exige migration e manutencao.

3. **Reutilizar tabela existente (ex.: PAR_PARTICIPANTE):**
   - **Prós:** evita nova tabela.
   - **Contras:** acoplamento de estado transitório com dados do usuario, dificulta historico e limpeza, risco de concorrencia.

4. **OTP numerico em memoria (cache local) - escolha atual:**
   - **Prós:** simples, rapido, sem nova tabela, menor esforco de implementacao.
   - **Contras:** perde codigos em restart, nao funciona bem em multi-instancias, auditoria limitada.

## Consequencias

- **Positivas:**
  - Implementacao simples e coerente com o contexto de baixo risco.
  - Menor esforco de manutencao e nenhuma migration.
  - Registro minimo da ultima troca de senha permite rastrear mudancas relevantes.

- **Riscos/Custos:**
  - OTPs serao perdidos em reinicio do servidor.
  - Nao ha rastreio historico de tentativas (apenas logs operacionais).
  - Em ambiente multi-instancia, a validacao pode falhar sem sincronizacao.

- **Seguranca (mitigacoes obrigatorias):**
  - Limite de tentativas por email/IP e expiracao curta (30 minutos).
  - Hash do OTP em memoria e invalidacao imediata apos uso.
  - Respostas neutras para evitar enumeracao de usuarios.

## Responsaveis

- Time Mercurio / Arquiteto de Software Senior
