# Sessao: Implementação de OGNL Allowlist e Proteções Cross-Origin

**Data:** 2026-02-19
**Autor:** Assistente Técnico Líder/IA
**Skills Utilizadas:** modernization-java-migration v1.0.0, security-audit v1.0.0

## 1. Prompt Principal (Input)

> Validar OGNL Allowlist no Struts 7 para classes do pacote com.opendev.bolao.model e utilitários de exibição. Implementar recomendações de segurança do Struts 7.

## 2. Resposta da IA (Output Resumido)

Implementação das defesas proativas do Struts 7 para mitigar ataques de RCE (Remote Code Execution) e injeção de expressões.

### O Que foi Configurado e Por Quê:

| Recurso | Configuração | Motivo/Justificativa |
| :--- | :--- | :--- |
| **Allowlist de Pacotes** | `com.opendev.bolao.model`, `com.opendev.bolao.util` | O Struts 7 bloqueia por padrão o acesso a qualquer classe via OGNL. Permitir estes pacotes é essencial para que as JSPs consigam ler dados das entidades e utilitários. |
| **Limite de OGNL** | `expressionMaxLength=200` | Previne ataques de negação de serviço (DoS) que tentam sobrecarregar a CPU com expressões recursivas ou excessivamente longas. |
| **Bloqueio de Nós OGNL** | `excludedNodeTypes` (This, VarRef) | Impede que atacantes utilizem referências internas do Struts para acessar o contexto da aplicação ou realizar escalada de privilégios. |
| **Isolamento COOP/COEP** | Interceptores `coop` e `coep` | Protege a aplicação contra ataques de canal lateral e vazamentos de dados de origem cruzada. |
| **Fetch Metadata** | Interceptor `fetchMetadata` | Utiliza metadados do navegador para validar se a requisição é legítima e esperada (ex: bloqueia navegação indesejada iniciada por sites terceiros). |

### Arquivos Impactados:
- `src/main/resources/struts.xml`: Configuração das constantes e da `bolaoStack`.

## 3. Validacao (Build/Teste)

- **Comando:** `docker compose up --build`
- **Resultado:** Sucesso.
- **Observacoes:** A aplicação inicializou sem erros. O Struts 7 confirmou o uso da allowlist e o registro dos novos interceptores de segurança.

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** A inclusão do pacote `com.opendev.bolao.util` foi necessária para permitir o uso da função `participantesOnline()` nas JSPs.

---
**Auto-Análise:** [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
