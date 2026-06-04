# Sessão: Banco de Produção Aiven sem Dados

**Data:** 2026-06-04
**Autor:** Arquiteto Técnico Líder (@arquiteto)
**Skills Utilizadas:** `docker-expert v1.0.0`

## 1. Problema Identificado

**Ambiente:** Produção (`novobolaodacopa-bolaocopa.hf.space`)

A tela `/seguro/palpites.action` exibe apenas o filtro de busca e a mensagem de ajuda, sem nenhum jogo listado.

## 2. Causa Raiz

O banco Aiven (MySQL remoto) conectou com sucesso (HikariPool confirmado no log), mas não possui os dados da Copa 2026. O script `data/sql/03-copa-2026-data.sql` ainda não foi executado nesse ambiente.

**Evidência no log:**
```
HikariPool-1 - Start completed.
```
Sem erros de conexão — o banco está acessível mas vazio.

## 3. Dados Necessários

| Script | Conteúdo | Obrigatório |
|---|---|---|
| `data/sql/01-schema.sql` (ou equivalente) | Criação das tabelas | Sim (se banco novo) |
| `data/sql/03-copa-2026-data.sql` | 104 jogos + 48 equipes Copa 2026 | Sim |
| Script de seed participantes | Usuários admin/user com BCrypt | Sim |

## 4. Próximos Passos

Ver Tarefa 13 no `passo-a-passo.md`.

> Auto-Análise: [Risco: Alto] | [Compatibilidade: OK] | [Veredito: Executar carga antes de usar em produção]
