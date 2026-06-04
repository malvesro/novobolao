# ADR 006: Migração do Módulo de E-mail de SMTP para API REST (Brevo)

## Status
Proposto

## Contexto
O sistema Bolão utiliza o protocolo SMTP para o envio de e-mails transacionais (recuperação de senha, notificações). No entanto, ao hospedar a aplicação no Hugging Face Spaces (free tier), identificamos que as portas SMTP padrão (465 e 587) são bloqueadas por padrão para evitar abuso.

Para garantir a confiabilidade das notificações sem depender de infraestrutura SMTP aberta, decidimos utilizar a API REST oficial da Brevo.

## Decisão
Substituir o transporte direto via `jakarta.mail` (SMTP) por chamadas HTTP REST para a API V3 da Brevo (`https://api.brevo.com/v3/smtp/email`).

Implementaremos um padrão de Strategy/Provider para permitir a alternância entre SMTP (para desenvolvimento local/outros provedores) e Brevo REST (para produção no Hugging Face) via variável de ambiente `EMAIL_PROVIDER`.

## Consequências
- **Positivas**:
    - Compatibilidade garantida com Hugging Face Spaces.
    - Melhor entrega e monitoramento via dashboard da Brevo.
    - Independência de bibliotecas de transporte SMTP pesadas em produção.
- **Negativas**:
    - Dependência de um serviço externo (SaaS).
    - Necessidade de gerenciar segredos de API (API Key).

## Referências
- [Brevo API Documentation](https://developers.brevo.com/docs/send-a-transactional-email)
- `passo-a-passo.md` - Fase 9, Item 8.
