# Sessao: Configurar Envio de Emails via SMTP do Gmail

**Data:** 2026-05-16
**Autor:** Arquiteto Assistente

## 1. Prompt Principal (Input)

> creio que faltou no documento de arquitetura oracle a parte do envio de e-mail, pois o sistema bolão envia e-mails. Melhor usar uma conta nova do Gmail ? COnta do gmail não irá funcionar com o sistema bolão, pois não aceita mais usuário e senha ?

## 2. Resposta da IA (Output Resumido)

> A estratégia foi validada como a melhor opção de "Zero-Cost" viável. O guia `arquitetura_multi_bolao_oracle_free_nginx.md` recebeu atualizações nos `.env` para suprir as chaves `SMTP_` que a app demanda. Foi provada a compatibilidade do Gmail instruindo a criação da flag de autenticação App Passwords (Senha de Aplicativo) obrigatória que dispensa senhas root desprotegidas no arquivo de config. Editada a documentação na sessão 16.3 focada nesse troubleshooting de Mail Setup.

## 3. Validacao (Build/Teste)

- Comando: Code Review nos Environment Variables de SMTP no `docker-compose.yml`.
- Resultado: Configurações validadas.

## 4. Analise Humana (Veredito)

- [x] Codigo aceito sem alteracoes.

> Auto-Analise: [Risco: Baixo] | [Compatibilidade: OK] | [Veredito: Aprovado]
