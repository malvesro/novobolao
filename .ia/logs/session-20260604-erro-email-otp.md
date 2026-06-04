# Sessão: Erro IllegalArgumentException no Envio de E-mail OTP

**Data:** 2026-06-04
**Autor:** Arquiteto Técnico Líder (@arquiteto)
**Skills Utilizadas:** `modernization-java-migration v1.0.0`, `security-audit v1.0.0`

## 1. Problema Identificado

**Ambiente:** Produção (`novobolaodacopa-bolaocopa.hf.space`)
**Horário:** 16:45:46 (BRT)

```
ERROR com.opendev.bolao.action.ParticipanteAction -- [CADASTRO] Erro ao iniciar fluxo de validacao OTP
java.lang.IllegalArgumentException: Illegal group reference
    at java.base/java.util.regex.Matcher.appendExpandedReplacement(Matcher.java:1067)
    at com.opendev.bolao.email.Email.populateData(Email.java:175)
    at com.opendev.bolao.email.Email.enviar(Email.java:94)
    at com.opendev.bolao.action.ParticipanteAction.cadastrar(ParticipanteAction.java:744)
```

## 2. Causa Raiz

O método `Email.populateData()` utiliza `String.replaceAll(regex, replacement)`:

```java
this.conteudo = this.conteudo.replaceAll("\\$\\{" + chave + "\\}", valor);
```

O segundo argumento do `replaceAll` é interpretado pelo `Matcher` como uma string de substituição onde:
- `$0`, `$1`... referenciam grupos de captura
- `\` é um caractere de escape

Se o **valor** do placeholder contiver `$` seguido de dígitos (ex: URL com parâmetros `?token=$abc`), `\` ou outros caracteres especiais, o `Matcher` lança `IllegalArgumentException: Illegal group reference`.

O OTP ou o link de validação enviado ao e-mail `malvesro@gmail.com` provavelmente contém tais caracteres.

## 3. Solução Planejada

**Opção 1 (recomendada):** Usar `Matcher.quoteReplacement(valor)` para escapar o valor antes de passar ao `replaceAll`:

```java
this.conteudo = this.conteudo.replaceAll(
    "\\$\\{" + chave + "\\}",
    java.util.regex.Matcher.quoteReplacement(valor)
);
```

**Opção 2:** Trocar `replaceAll` por `replace` (que faz substituição literal, sem interpretar regex):

```java
this.conteudo = this.conteudo.replace("${" + chave + "}", valor);
```

A opção 2 é mais simples e igualmente correta aqui, pois o padrão `${chave}` é literal — não há necessidade de regex para essa substituição.

## 4. Impacto

- Fluxo de cadastro com OTP falha completamente
- Usuário não recebe e-mail de verificação
- Cadastro não é concluído

## 5. Próximos Passos

Ver Tarefa 12 no `passo-a-passo.md`.

> Auto-Análise: [Risco: Alto] | [Compatibilidade: OK] | [Veredito: Corrigir imediatamente]
