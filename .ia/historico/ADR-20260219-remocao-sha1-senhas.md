# ADR-20260219-remocao-sha1-senhas

**Data:** 2026-02-19
**Status:** Aprovado

## Contexto

Durante a migração da aplicação para o stack moderno (Spring 6 + Spring Security 6) preservamos um encoder legado (`LegacySha1PasswordEncoder`) para compatibilizar senhas armazenadas em SHA-1. A aplicação, porém, encontra-se desativada desde a retomada do projeto e não há base de usuários ativos a serem preservados. Manter o fallback em SHA-1 aumenta a superfície de ataque e obriga a manter código específico para algoritmos obsoletos.

## Decisao

Eliminar o suporte à verificação de senhas em SHA-1 e padronizar 100% dos usuários em `BCryptPasswordEncoder`, gerando hashes modernos no ato do cadastro ou redefinição de senha. A base de dados deverá ser higienizada para remover hashes legados ou os usuários deverão passar pelo fluxo de redefinição antes de retomarem o acesso.

## Alternativas Consideradas

1. Manter fallback SHA-1 com rehash gradual — rejeitado por prolongar a exposição a algoritmo inseguro.
2. Adotar migração automática com tolerância a SHA-1 apenas por tempo determinado — rejeitado pela ausência de usuários ativos e pelo custo de manter lógica temporária.

## Consequencias

- Hardening imediato das credenciais, eliminando o suporte a algoritmo depreciado.
- Necessidade de redefinir senhas dos usuários importados ou recriar a base de credenciais.
- Simplificação do código de autenticação e testes.

## Responsaveis

- Time Mercúrio (Modernização do Sistema Bolão)

> NOTE: Renomeie o arquivo conforme o padrao e mova para `docs/adr/` quando aprovado.
