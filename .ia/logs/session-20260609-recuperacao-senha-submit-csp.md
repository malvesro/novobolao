# Sessao: Correção de Submissão na Recuperação de Senha (CSP x onclick inline)

**Data:** 2026-06-09
**Autor:** Arquiteto de Software Sênior (Time Mercúrio)
**Skills Utilizadas:** security-audit v1.0.0, modernization-java-migration v1.0.0

## 1. Prompt Principal (Input)

> Na tela de recuperação de senha, ao informar o e-mail e clicar no botão "Enviar código" nada acontece.

## 2. Resposta da IA (Output Resumido)

> Análise dos logs e do código identificou que o POST não era disparado ao clicar no botão da tela de recuperação.
>
> Causa raiz:
> - O botão usava `onclick` inline para submeter o formulário (`document.getElementById('formRecuperacao').submit()`).
> - A política CSP atual (`CspNonceFilter`) utiliza `script-src` com nonce/strict-dynamic e não permite event handlers inline.
>
> Correção aplicada:
> - Substituição dos botões para submit HTML nativo via atributo `form="..."`, removendo dependência de JS inline.
> - Arquivos ajustados:
>   - `webapp/WEB-INF/content/recuperacao/recuperar-senha.jsp`
>   - `webapp/WEB-INF/content/recuperacao/redefinir-senha.jsp`

## 3. Validação (Build/Teste)

- Comando: varredura estática dos JSPs para confirmar remoção de `onclick` e presença de `form="formRecuperacao"` / `form="formRedefinir"`.
- Resultado: Sucesso.
- Observações:
  - O submit passa a ocorrer sem JavaScript inline, compatível com CSP.
  - Não executado teste integrado com envio real nesta sessão (dependente de ambiente externo e credenciais SMTP/Brevo).

## 4. Análise Humana (Veredito)

- [ ] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** Correção de baixo risco funcional, focada em conformidade de segurança e comportamento esperado da UI.
