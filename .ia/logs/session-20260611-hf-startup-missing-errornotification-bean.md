# Sessao: Correcao de inicializacao no Hugging Face por bean ausente

**Data:** 2026-06-11  
**Autor:** Assistente Tecnico Lider/IA  
**Skills Utilizadas:** architecture-guardian v1.0.0, senior-java-dev-legacy v1.0.0

## 1. Prompt Principal (Input)

> Erro de inicialização no Hugging Face.

## 2. Diagnóstico técnico

- Causa raiz identificada no log de produção:
  - `UnsatisfiedDependencyException` ao criar `ExceptionLoggingInterceptor`.
  - Dependência ausente: `com.opendev.bolao.service.ErrorNotificationService`.
- Contexto:
  - `ExceptionLoggingInterceptor` é construído via Struts `SpringObjectFactory`.
  - O contexto Spring carregado no deploy HF é majoritariamente XML e não registrava explicitamente `ErrorNotificationService`.
  - Resultado: falha de bootstrap do filtro Struts e erro de inicialização do app.

## 3. Alterações realizadas

- Arquivo alterado: `src/main/resources/applicationContext-service.xml`
  - Adicionado bean explícito:
    - `<bean id="errorNotificationService" class="com.opendev.bolao.service.ErrorNotificationService" />`
  - Adicionado comentário explicativo sobre a necessidade em ambientes com configuração XML/Struts.

## 4. Validação executada

- Rebuild da aplicação:
  - `docker compose up -d --build app` com sucesso.
- Verificação de logs:
  - `ExceptionLoggingInterceptor` passou a ser inicializado sem erro de autowire.
  - Ausência de `NoSuchBeanDefinitionException` para `ErrorNotificationService`.
- Smoke runtime:
  - `GET /health.txt` -> HTTP 200.
- Testes automatizados:
  - `mvn -Dfrontend.skip=true test` -> 52 testes, 0 falhas, 0 erros.

## 5. Justificativa da solução

1. **Baixo risco de regressão:** correção pontual de wiring, sem alteração de regra de negócio.
2. **Compatibilidade com arquitetura legada:** respeita o modelo atual de configuração XML já adotado no projeto.
3. **Aderência ao cenário HF:** elimina discrepância entre ambiente local e deploy de produção quando a injeção por scanning não cobre todos os serviços usados por interceptors.

## 6. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** correção aplicada de forma mínima e segura para restabelecer startup no Hugging Face.
