# Plano: Modernização do Envio de E-mails (2026-02-23)

**Objetivo:** Habilitar o sistema a enviar notificações via SMTP moderno com suporte a TLS/STARTTLS, autenticação e configuração externa segura.

## Escopo
- Revisar `Email.java` para suportar propriedades avançadas (porta, STARTTLS, SMTPS, timeouts, trust store) e usar `Session.getInstance` isolado.
- Externalizar parâmetros sensíveis, permitindo sobreposição por variáveis de ambiente/arquivos externos no deploy.
- Documentar e testar integração com servidores SMTP modernos (MailHog/GreenMail) garantindo compatibilidade.

## Etapas
1. **Diagnóstico e refatoração do cliente SMTP**
   - Mapear propriedades necessárias (`mail.smtp.port`, `mail.smtp.auth`, `mail.smtp.starttls.enable`, `mail.smtp.ssl.enable`, `mail.smtp.ssl.trust`, `mail.smtp.connectiontimeout`, `mail.smtp.timeout`).
   - Atualizar `Email.java` para aplicar todas as propriedades dinamicamente, escolher `Session.getInstance` e evitar APIs depreciadas.
   - Implementar fallback seguro para remetente (`from`) e tratar erros com mensagens claras.
2. **Configuração externa e segurança**
   - Definir hierarquia de carregamento (variáveis de ambiente > arquivo externo montado no container > bundle padrão).
   - Ajustar `docker-compose.yml`, `Dockerfile` e documentação para aceitar secrets (`SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `SMTP_TLS`).
   - Sanitizar logs para não expor credenciais; adicionar validação pré-envio.
3. **Testes e documentação**
   - Adicionar testes com servidor SMTP embutido (ex.: GreenMail ou Wiser) cobrindo autenticação obrigatória, TLS e falhas de conexão.
   - Atualizar `README-migracao.md` e diretrizes de segurança com o procedimento de configuração.
   - Registrar resultado em log e, se necessário, ADR resumindo as novas práticas de envio de e-mail.

## Entregáveis
- Código refatorado suportando SMTP moderno e configuração externa segura.
- Arquivos de configuração/documentação atualizados para ambientes Docker e on-premise.
- Testes automatizados garantindo cobertura de envio e autenticação.
- Log de sessão descrevendo validações e evidências.
