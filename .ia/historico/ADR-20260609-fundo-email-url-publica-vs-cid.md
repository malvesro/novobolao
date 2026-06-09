# ADR-20260609: Estratégia de fundo de imagem em e-mails (URL pública vs CID)

## Status
Aprovado (2026-06-09)

## Contexto
Deseja-se aplicar a imagem `brasao-fundo-email.png` como fundo comum de todos os e-mails com escurecimento leve para manter legibilidade.

O pipeline atual de envio utiliza `Email.java` para montagem de HTML e `BrevoEmailSender` para envio REST com `htmlContent`, sem suporte nativo implementado para anexos inline CID.

## Decisão
Adotar **URL pública** para a imagem de fundo no template base de e-mail (`cabecalho.html`), exposta por placeholder `${emailBgUrl}` fornecido pelo backend (`Email.java`).

Escurecimento aplicado com overlay semitransparente no layout (`rgba(10, 18, 30, 0.42)`) e fallback por cor sólida para clientes sem suporte a `background-image`.

## Justificativa
1. Menor acoplamento: evita ampliar o contrato de `EmailMessage`/`BrevoEmailSender` para CID e anexos base64.
2. Menor complexidade operacional: sem manipulação de anexos inline por provedor.
3. Compatibilidade progressiva: clientes que bloqueiam imagem remota ainda exibem layout legível por fallback de cor e card branco.
4. Evolução incremental: mantém possibilidade futura de migrar para CID caso surjam requisitos de renderização offline/cliente específico.

## Consequências
- Positivas:
  - Implementação simples e rastreável.
  - Sem quebra do fluxo atual de envio.
  - Manutenção centralizada no template base.
- Negativas:
  - Dependência de carregamento remoto de imagem por cliente de e-mail.
  - Em clientes com suporte limitado a background image, prevalece fallback sem imagem.

## Implementação
- `src/com/opendev/bolao/email/Email.java`
  - Nova propriedade `${emailBgUrl}` construída a partir de `mail.property.systemurl`.
- `src/main/resources/com/opendev/bolao/email/templates/cabecalho.html`
  - Fundo global com `background-image: url('${emailBgUrl}')` + overlay escurecido.
- `src/main/resources/com/opendev/bolao/email/templates/rodape.html`
  - Ajuste estrutural para fechamento dos wrappers adicionados no cabeçalho.

## Validação
- Checklist de validação cross-client registrado no log de sessão da tarefa 35.
- Verificação de segurança: rota `/img/**` liberada no Spring Security.
