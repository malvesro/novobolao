# Sessao: Remocao do Fallback SHA-1

**Data:** 2026-02-19
**Autor:** Assistente Tecnico Lider/IA
**Skills Utilizadas:** modernization-java-migration v1.0.0

## 1. Prompt Principal (Input)

> Remover o suporte a senhas SHA-1 conforme ADR e validar a autenticacao apenas com BCrypt.

## 2. Resposta da IA (Output Resumido)

> Eliminado o encoder legado (`LegacySha1PasswordEncoder`) e o `DelegatingPasswordEncoder`. O `applicationContext-security.xml` agora referencia apenas `BCryptPasswordEncoder`. Removidos os utilitarios obsoletos e recompilado o projeto para garantir consistencia.

Arquivos impactados:
- `src/main/resources/applicationContext-security.xml`
- `src/com/opendev/bolao/util/LegacySha1PasswordEncoder.java` (removido)
- `src/com/opendev/bolao/util/DelegatingPasswordEncoder.java` (removido)

## 3. Validacao (Build/Teste)

- Comando: `mvn test -DskipITs`
- Resultado: Sucesso
- Observacoes: Warnings ja conhecidos de APIs legadas (`Email`, `BatePapo`). Sem regressao nos testes de servico/acao.

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** Necessario preparar cenario de usuarios com senhas BCrypt para validacao manual do fluxo login/logout e RBAC.
