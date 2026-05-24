# Sessao: Validacao de Login com HTTPS no Docker (Tarefa 2.6.2)

**Data:** 2026-02-19
**Autor:** Assistente Tecnico Lider/IA
**Skills Utilizadas:** N/A

## 1. Prompt Principal (Input)

> "Registre o plano sendo executado, os problemas encontrados e as soluções adotadas com justificativa em uma sessão de log. Continue." + "Siga a opcao 1. Habilitar HTTPS no Docker."

## 2. Resposta da IA (Output Resumido)

- Plano em execucao para validar login via HTTPS, com iteracoes pequenas para evitar desconexao de stream.
- Problemas encontrados:
  - Redirecionamento forcado para HTTPS (via `security-constraint` no `web.xml`) sem conector SSL ativo.
  - Primeira tentativa de conector HTTPS falhou por ausencia de `SSLHostConfig` e conflito de bind.
  - Apos ajuste do conector, HTTPS passou a responder, mas `GET /login.jsp` retornou `HTTP 500` por **taglib Cewolf** ainda referenciada (biblioteca removida do build).
  - Login com `admin/admin123` falhou porque o seed usa SHA-1 **hex** de `admin` (nao base64), gerando credencial incorreta para o teste.
- Solucoes adotadas e justificativa:
  - **Geracao de keystore local** no `Dockerfile` para habilitar TLS no ambiente dev, mantendo a diretriz de forcar HTTPS.
  - **Habilitacao do conector HTTPS** no `server.xml` via `SSLHostConfig` (JSSE), corrigindo erro de inicializacao.
  - **Exposicao da porta 8443** no `docker-compose.yml` para permitir testes locais.
  - **Compatibilidade de hash SHA-1**: ajustado `LegacySha1PasswordEncoder` para aceitar SHA-1 em **base64** e **hex**, garantindo suporte a dados legados existentes.

Arquivos impactados:
- `Dockerfile`
- `docker-compose.yml`
- `src/com/opendev/bolao/util/LegacySha1PasswordEncoder.java`

## 3. Validacao (Build/Teste)

- Comando: `docker compose build --quiet app`
- Resultado: Sucesso
- Observacoes: Imagem reconstruida com keystore e conector HTTPS.

- Comando: `docker compose up -d app`
- Resultado: Sucesso
- Observacoes: Container reiniciado com HTTPS habilitado.

- Comando: `curl -k https://localhost:8443/login.jsp` (executado no container)
- Resultado: Falha (HTTP 500)
- Observacoes: TLS estabelecido com sucesso; erro 500 causado por taglib Cewolf ausente.

- Comando: `curl -k -X POST https://localhost:8443/j_security_check -d 'j_username=admin&j_password=admin'`
- Resultado: Sucesso (302 para `/seguro/principal.jsp`)
- Observacoes: Credencial funcional com SHA-1 hex legado.

- Comando: `curl -k -X POST https://localhost:8443/j_security_check -d 'j_username=admin&j_password=admin123'`
- Resultado: Falha (302 para `/login.jsp?status=invalido`)
- Observacoes: Seed do banco usa SHA-1 hex de `admin` (nao `admin123`).

- Comando: `curl -k https://localhost:8443/seguro/principal.jsp` (com cookie de sessao)
- Resultado: Falha (HTTP 500)
- Observacoes: Erro de JSP por taglib Cewolf ainda referenciada.

## 4. Analise Humana (Veredito)

- [ ] Codigo aceito sem alteracoes.
- [ ] Codigo ajustado manualmente (detalhar abaixo).
- [ ] Alucinacao detectada (prompt refinado).

**Observacoes:** N/A
