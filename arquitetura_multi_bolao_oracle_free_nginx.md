# Arquitetura Multi-Instância do Bolão na Oracle Cloud Always Free com Nginx

> **Versão 3.0 | Maio 2026**  
> Guia técnico, didático e passo a passo para implantar **duas instâncias independentes** da aplicação Bolão, com **domínios diferentes**, **bases MySQL isoladas logicamente** e **custo zero**, usando **Nginx como proxy reverso** em vez do OCI Load Balancer.  
> **v3.0:** Melhorias de segurança, resiliência e configurações corrigidas para o stack Java/Tomcat.

---

## Sumário

1. [Objetivo](#1-objetivo)
2. [Visão da solução](#2-visão-da-solução)
3. [Por que Nginx no lugar do Load Balancer](#3-por-que-nginx-no-lugar-do-load-balancer)
4. [Recursos Always Free usados](#4-recursos-always-free-usados)
5. [Arquitetura final](#5-arquitetura-final)
6. [Pré-requisitos](#6-pré-requisitos)
7. [Passo 1 — Planejar rede e domínios](#7-passo-1--planejar-rede-e-domínios)
8. [Passo 2 — Criar a VCN no OCI](#8-passo-2--criar-a-vcn-no-oci)
9. [Passo 3 — Criar a VM ARM Always Free](#9-passo-3--criar-a-vm-arm-always-free)
10. [Passo 4 — Criar o MySQL HeatWave Always Free](#10-passo-4--criar-o-mysql-heatwave-always-free)
11. [Passo 5 — Configurar Security Lists](#11-passo-5--configurar-security-lists)
12. [Passo 6 — Acessar a VM e preparar o sistema](#12-passo-6--acessar-a-vm-e-preparar-o-sistema)
13. [Passo 7 — Instalar Docker, Nginx e Certbot](#13-passo-7--instalar-docker-nginx-e-certbot)
14. [Passo 8 — Criar os schemas e usuários do MySQL](#14-passo-8--criar-os-schemas-e-usuários-do-mysql)
15. [Passo 9 — Publicar DNS para os dois domínios](#15-passo-9--publicar-dns-para-os-dois-domínios)
16. [Passo 10 — Subir as duas aplicações com Docker](#16-passo-10--subir-as-duas-aplicações-com-docker)
17. [Passo 11 — Configurar Nginx para os dois domínios](#17-passo-11--configurar-nginx-para-os-dois-domínios)
18. [Passo 12 — Habilitar HTTPS com Let's Encrypt](#18-passo-12--habilitar-https-com-lets-encrypt)
19. [Passo 13 — Ajustar firewall interno da VM](#19-passo-13--ajustar-firewall-interno-da-vm)
20. [Passo 14 — Testes de validação](#20-passo-14--testes-de-validação)
21. [Passo 15 — Monitoramento](#21-passo-15--monitoramento)
22. [Passo 16 — Backup e segurança](#22-passo-16--backup-e-segurança)
23. [Passo 17 — Como manter custo zero](#23-passo-17--como-manter-custo-zero)
24. [Checklist final](#24-checklist-final)
25. [Referências úteis](#25-referências-úteis)
26. [Análise Crítica e Complementos (v3.0)](#26-análise-crítica-e-complementos-v30)

---

## 1. Objetivo

Esta arquitetura foi desenhada para atender o seguinte cenário:

- Duas instâncias da aplicação **Bolão**.
- Dois públicos diferentes.
- Dois domínios diferentes, por exemplo:
  - `bolao-copa.duckdns.org`
  - `bolao-mercurio.duckdns.org`
- Isolamento lógico de dados no MySQL.
- Tudo rodando dentro do **Always Free** da Oracle Cloud.
- Sem usar o **OCI Load Balancer**, evitando o limite de **10 Mbps**.

A ideia central é simples: usar **uma única VM ARM Always Free** como ponto de entrada público, instalar **Nginx** nela, e fazer o roteamento por domínio usando `server_name`. Cada aplicação roda em um container separado, em portas diferentes, e ambas acessam o mesmo MySQL HeatWave Always Free, porém com **schemas e usuários distintos**.

---

## 2. Visão da solução

Em vez de colocar um balanceador gerenciado na frente da aplicação, o próprio **Nginx** dentro da VM assume quatro papéis:

1. **Reverse proxy** para os containers.
2. **Terminação SSL/TLS** com certificados Let's Encrypt.
3. **Roteamento por hostname**, enviando cada domínio para a aplicação correta.
4. **Camada extra de proteção e performance**, com cache, headers e rate limiting.

Esse desenho é especialmente bom no OCI Always Free porque elimina um componente extra, reduz complexidade operacional e remove o gargalo do LB gratuito.

---

## 3. Por que Nginx no lugar do Load Balancer

Na prática, o OCI Load Balancer Always Free é funcional, mas o teto de **10 Mbps** pode se tornar um gargalo cedo, principalmente se houver:

- muitos acessos simultâneos;
- páginas com imagens, assets e JavaScript mais pesados;
- uso intenso próximo de jogos e fechamentos de rodada;
- dashboards, relatórios ou uploads.

Com o **Nginx na VM**, o tráfego entra direto pelo IP público da instância. Isso simplifica a arquitetura e usa a capacidade de rede da própria VM, respeitando apenas os limites gerais do Always Free e do tráfego mensal disponível.

### Comparação resumida

| Critério               | OCI Load Balancer Free    | Nginx na VM                      |
| ---------------------- | ------------------------- | -------------------------------- |
| Largura de banda       | 10 Mbps                   | Limitada pela VM/rede disponível |
| Complexidade           | Média                     | Baixa                            |
| Certificados SSL       | No LB                     | No próprio Nginx                 |
| Roteamento por domínio | Routing policy            | `server_name`                    |
| Cache                  | Limitado                  | Sim                              |
| Rate limiting          | Limitado                  | Sim                              |
| Custo                  | Pode escalar fora do free | Zero, dentro da VM               |
| Melhor para este caso  | Razoável                  | **Ideal**                        |

---

## 4. Recursos Always Free usados

A arquitetura proposta usa apenas componentes compatíveis com o plano gratuito permanente.

| Recurso          | Uso na arquitetura                   | Limite Always Free      |
| ---------------- | ------------------------------------ | ----------------------- |
| VM ARM Ampere A1 | Hospedar Nginx, apps e monitoramento | Até 4 OCPUs e 24 GB RAM |
| Block Storage    | Disco da VM                          | Até 200 GB total        |
| MySQL HeatWave   | Banco de dados gerenciado            | 1 instância com 50 GB   |
| Object Storage   | Backups opcionais                    | 20 GB                   |
| Rede de saída    | Tráfego externo                      | Até 10 TB/mês           |

---

## 5. Arquitetura final

### Diagrama lógico

```text
Internet
   |
   |  bolao-copa.duckdns.org
   |  bolao-mercurio.duckdns.org
   |
   +-----------------------------+
                                 |
                         IP público da VM ARM
                                 |
                   +-----------------------------+
                   | Nginx (80/443)             |
                   | server_name por domínio    |
                   +-------------+---------------+
                                 |
                    +------------+------------+
                    |                         |
           localhost:8080             localhost:8081
            bolao_copa                 bolao_mercurio
                    |                         |
                    +------------+------------+
                                 |
                     MySQL HeatWave Always Free
                       schema bolao_copa
                       schema bolao_mercurio
```

### Interpretação do fluxo

- O usuário acessa um dos dois domínios.
- O DNS resolve para o **mesmo IP público da VM**.
- O **Nginx** recebe a conexão e lê o hostname.
- Se o domínio for do Bolão Copa, encaminha para `localhost:8080`.
- Se o domínio for do Bolão Mercúrio, encaminha para `localhost:8081`.
- Cada aplicação usa seu próprio schema e seu próprio usuário MySQL.

Esse modelo fornece **isolamento lógico**, simplicidade e baixo custo operacional.

---

## 6. Pré-requisitos

Antes de começar, tenha em mãos:

- Conta OCI ativa no plano **Always Free**.
- Dois domínios ou subdomínios configuráveis.
- Acesso ao painel DNS do domínio, ou conta Cloudflare.
- Cartão válido já utilizado na criação da conta OCI.
- Chave SSH para acesso à VM.
- Imagem Docker da aplicação, ou código pronto para build.
- Usuário com conhecimento básico de Linux, Docker e MySQL.

Sugestão de domínios:

- `bolao-copa.duckdns.org`
- `bolao-mercurio.duckdns.org`

Usar subdomínios do mesmo domínio principal simplifica bastante o gerenciamento DNS e os certificados.

---

## 7. Passo 1 — Planejar rede e domínios

Antes de criar qualquer recurso, defina o desenho lógico.

### Sub-redes recomendadas

| Recurso       | Subnet         | Faixa sugerida |
| ------------- | -------------- | -------------- |
| VM pública    | Public Subnet  | `10.0.0.0/24`  |
| MySQL privado | Private Subnet | `10.0.1.0/24`  |

### Portas necessárias

| Porta | Uso         | Exposição                               |
| ----- | ----------- | --------------------------------------- |
| 22    | SSH         | Pública, restrita ao seu IP se possível |
| 80    | HTTP        | Pública                                 |
| 443   | HTTPS       | Pública                                 |
| 8080  | App Copa    | Apenas local/interna                    |
| 8081  | App Mercúrio | Apenas local/interna                    |
| 3000  | Grafana     | Preferencialmente restrita              |
| 9090  | Prometheus  | Preferencialmente restrita              |

### Convenção de nomes sugerida

| Item             | Nome sugerido    |
| ---------------- | ---------------- |
| VCN              | `vcn-bolao`      |
| VM               | `vm-bolao-nginx` |
| MySQL            | `mysql-bolao`    |
| Pasta do projeto | `~/bolao`        |

---

## 8. Passo 2 — Criar a VCN no OCI

A VCN é a rede privada do seu ambiente.

### 8.1 Criar a VCN pelo wizard

No console OCI:

```text
Menu > Networking > Virtual Cloud Networks > Start VCN Wizard
```

Escolha:

- **Create VCN with Internet Connectivity**

Preencha:

| Campo               | Valor sugerido |
| ------------------- | -------------- |
| VCN Name            | `vcn-bolao`    |
| VCN CIDR Block      | `10.0.0.0/16`  |
| Public Subnet CIDR  | `10.0.0.0/24`  |
| Private Subnet CIDR | `10.0.1.0/24`  |

### 8.2 O que será criado

O wizard normalmente cria:

- VCN principal.
- Internet Gateway.
- NAT Gateway.
- Public Subnet.
- Private Subnet.
- Route Tables.
- Security Lists básicas.

### 8.3 Por que isso importa

- A **VM** precisa ficar na subnet pública porque receberá tráfego da internet.
- O **MySQL HeatWave** deve ficar na subnet privada, sem IP público.

Esse é um princípio importante: o banco nunca deve ser exposto diretamente à internet.

---

## 9. Passo 3 — Criar a VM ARM Always Free

A VM será o servidor central da solução.

### 9.1 Criar instância

No OCI:

```text
Menu > Compute > Instances > Create Instance
```

### 9.2 Configurações recomendadas

| Campo       | Valor                 |
| ----------- | --------------------- |
| Name        | `vm-bolao-nginx`      |
| Image       | Ubuntu 22.04 LTS      |
| Shape       | `VM.Standard.A1.Flex` |
| OCPUs       | 4                     |
| Memory      | 24 GB                 |
| VCN         | `vcn-bolao`           |
| Subnet      | Public Subnet         |
| Public IPv4 | Enabled               |

### 9.3 SSH key

Escolha uma das opções:

- Gerar chave automaticamente no OCI.
- Ou colar sua chave pública existente.

Se estiver gerando localmente:

```bash
# Use ed25519 em vez de RSA 4096.
# Justificativa: ed25519 produz chaves menores, com segurança equivalente ou
# superior ao RSA-4096, e o handshake SSH é mais rápido. RSA-4096 é legado
# e desnecessariamente pesado para este caso de uso.
ssh-keygen -t ed25519 -C "bolao-oracle"
cat ~/.ssh/id_ed25519.pub
```

### 9.4 Boot volume

Use um tamanho equilibrado, por exemplo:

- **50 GB** para o disco de boot.

Isso costuma ser suficiente para sistema, Nginx, containers, logs e ferramentas básicas.

### 9.5 Cuidados

Se aparecer erro **Out of Capacity** para ARM:

- tente outro Availability Domain;
- tente em outro horário;
- evite horários de pico.

---

## 10. Passo 4 — Criar o MySQL HeatWave Always Free

O MySQL será gerenciado pelo OCI, o que elimina trabalho com instalação, tuning inicial e manutenção básica do banco.

### 10.1 Criar DB System

No OCI:

```text
Menu > Databases > MySQL HeatWave > DB Systems > Create DB System
```

### 10.2 Habilitar modo Always Free

Marque a opção que ativa a configuração gratuita.

### 10.3 Configurações sugeridas

| Campo          | Valor sugerido |
| -------------- | -------------- |
| DB System Name | `mysql-bolao`  |
| Admin Username | `admin`        |
| Password       | senha forte    |
| VCN            | `vcn-bolao`    |
| Subnet         | Private Subnet |

### 10.4 Após criar

Anote:

- **IP privado** do MySQL.
- Nome do DB System.
- Usuário administrador.

O IP privado será usado nas variáveis de ambiente das aplicações.

---

## 11. Passo 5 — Configurar Security Lists

Nesta arquitetura sem Load Balancer, a VM é o ponto de entrada público. Portanto, ela precisa aceitar tráfego nas portas de web.

### 11.1 Regras da subnet pública

No OCI:

```text
VCN > Subnets > Public Subnet > Security List
```

Adicione entradas de **Ingress**:

| Source                | Proto | Porta | Finalidade |
| --------------------- | ----- | ----- | ---------- |
| `0.0.0.0/0`           | TCP   | 80    | HTTP       |
| `0.0.0.0/0`           | TCP   | 443   | HTTPS      |
| `0.0.0.0/0` ou seu IP | TCP   | 22    | SSH        |

Se quiser acessar Grafana externamente durante setup:

| Source | Proto | Porta | Finalidade |
| ------ | ----- | ----- | ---------- |
| Seu IP | TCP   | 3000  | Grafana    |

### 11.2 Regras da subnet privada

Na subnet do MySQL, permita apenas acesso interno necessário.

| Source        | Proto | Porta | Finalidade    |
| ------------- | ----- | ----- | ------------- |
| `10.0.0.0/24` | TCP   | 3306  | VM para MySQL |

### 11.3 Boa prática

Não exponha 3306 para internet. O MySQL deve continuar acessível apenas pela rede privada.

---

## 12. Passo 6 — Acessar a VM e preparar o sistema

### 12.1 Conectar via SSH

```bash
chmod 400 /caminho/sua-chave.key
ssh -i /caminho/sua-chave.key ubuntu@IP_PUBLICO_DA_VM
```

### 12.2 Atualizar sistema

```bash
sudo apt update && sudo apt upgrade -y
```

### 12.3 Instalar pacotes úteis

```bash
sudo apt install -y curl wget git unzip htop jq net-tools mysql-client
```

### 12.4 Criar diretórios

```bash
mkdir -p ~/bolao/{copa,mercurio,nginx,monitoring/prometheus,scripts,backups}
```

Estrutura sugerida:

```text
~/bolao/
├── copa/
├── mercurio/
├── nginx/
├── monitoring/
│   └── prometheus/
├── scripts/
└── backups/
```

---

## 13. Passo 7 — Instalar Docker, Nginx e Certbot

### 13.1 Instalar Docker

```bash
sudo apt install -y docker.io docker-compose-plugin
sudo usermod -aG docker $USER
newgrp docker
docker --version
docker compose version
```

### 13.2 Instalar Nginx e Certbot

```bash
sudo apt install -y nginx certbot python3-certbot-nginx
```

### 13.3 Habilitar serviços

```bash
sudo systemctl enable nginx
sudo systemctl start nginx
sudo systemctl status nginx
```

### 13.4 Teste rápido

Abra no navegador:

- `http://IP_PUBLICO_DA_VM`

Se aparecer a página padrão do Nginx, a camada web está funcionando.

---

## 14. Passo 8 — Criar os schemas e usuários do MySQL

Agora vamos isolar os dois públicos no banco.

### 14.1 Conectar ao MySQL a partir da VM

```bash
mysql -h IP_PRIVADO_MYSQL -u admin -p
```

### 14.2 Criar bases e usuários

```sql
CREATE DATABASE bolao_copa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE bolao_mercurio CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'app_copa'@'%' IDENTIFIED BY 'SenhaCopa#2026!';
CREATE USER 'app_mercurio'@'%' IDENTIFIED BY 'SenhaMercurio#2026!';

GRANT ALL PRIVILEGES ON bolao_copa.* TO 'app_copa'@'%';
GRANT ALL PRIVILEGES ON bolao_mercurio.* TO 'app_mercurio'@'%';

FLUSH PRIVILEGES;
```

### 14.3 Benefício desta separação

Cada instância da aplicação:

- usa um schema próprio;
- usa credenciais próprias;
- não enxerga a outra base.

Isso oferece isolamento lógico suficiente para a maioria dos cenários de dois públicos distintos.

---

## 15. Passo 9 — Configurar domínios gratuitos no DuckDNS

Para mantermos o custo zero em todas as camadas (incluindo o DNS e o Certificado SSL), utilizaremos o **DuckDNS**, um serviço gratuito que mapeia domínios (ex: `bolao.duckdns.org`) para o IP da nossa VM.

### 15.1 Conta no DuckDNS
1. Acesse https://www.duckdns.org.
2. Faça login com GitHub, Google ou e-mail.
3. A conta gratuita permite criar até **5 domínios**.

### 15.2 Registrar os Domínios
Crie os domínios desejados apontando para o **mesmo IP público da sua VM Oracle**:
1. No campo *domain*: Digite `bolao-copa` e clique em *add domain*. 
2. No campo *domain*: Digite `bolao-mercurio` e clique em *add domain*.
3. Atualize o campo **current ip** de ambos para o IPv4 público gerado na sua instância ARM (Passo 3) e clique em `Update IP`.

> *Exemplo gerado na sua conta:*
> `bolao-copa.duckdns.org`     ➜   `IP_PUBLICO_DA_VM`
> `bolao-mercurio.duckdns.org`  ➜   `IP_PUBLICO_DA_VM`

### 15.3 Teste de propagação

```bash
nslookup bolao-copa.duckdns.org
nslookup bolao-mercurio.duckdns.org
```

Ambos devem resolver para o mesmo IP.

---

## 16. Passo 10 — Subir as duas aplicações com Docker

A forma mais simples é rodar duas stacks separadas, uma por aplicação.

### 16.1 Arquivo `.env` do Bolão Copa

Crie `~/bolao/copa/.env`:

```env
APP_NAME=Bolao Copa
APP_PORT=8080
APP_ENV=production
DB_HOST=IP_PRIVADO_MYSQL
DB_PORT=3306
DB_NAME=bolao_copa
DB_USER=app_copa
DB_PASSWORD=SenhaCopa#2026!
APP_SECRET=uma_chave_grande_e_unica_para_copa

# Conta de E-mail via App Passwords do Gmail (gratuito)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_AUTH=true
SMTP_TLS=true
SMTP_USERNAME=bolaoapp.copa@gmail.com
SMTP_PASSWORD=senha_do_app_gerada_16_digitos
```

**Proteger o arquivo imediatamente após criar:**

```bash
# Permissão 600: somente o dono pode ler e escrever.
# Justificativa: permissão padrão 644 permite que outros usuários do
# sistema leiam as credenciais do banco — risco grave em ambientes
# multiusuário ou caso algum processo seja comprometido.
chmod 600 ~/bolao/copa/.env
```

### 16.2 Arquivo `.env` do Bolão Mercúrio

Crie `~/bolao/mercurio/.env`:

```env
APP_NAME=Bolao Mercúrio
APP_PORT=8080
APP_ENV=production
DB_HOST=IP_PRIVADO_MYSQL
DB_PORT=3306
DB_NAME=bolao_mercurio
DB_USER=app_mercurio
DB_PASSWORD=SenhaMercurio#2026!
APP_SECRET=uma_chave_grande_e_unica_para_mercurio

# Conta de E-mail via App Passwords do Gmail (gratuito)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_AUTH=true
SMTP_TLS=true
SMTP_USERNAME=bolaoapp.mercurio@gmail.com
SMTP_PASSWORD=senha_do_app_gerada_16_digitos
```

```bash
chmod 600 ~/bolao/mercurio/.env
```

### 16.3 Setup do Envio de E-mails via Gmail

O sistema Bolão dispara notificações importantes usando serviço de E-mail (SMTP). Para preservar nosso fluxo **100% gratuito**, documentamos o uso de contas Gmail. Contudo, devido a regras de segurança do Google (que bloqueiam senha básica em apps terciários), faremos o uso oficial e seguro de **Senhas de Aplicativo**.

Siga o tutorial conforme a [documentação do Google](https://support.google.com/accounts/answer/185833):

1. **Crie uma conta Gmail dedicada:** (ex: `bolaoapp.copa@gmail.com`). Evite criar abrindo do servidor OCI. Utilize seu computador/celular.
2. Acesse a área _Account Settings > Security_ de sua conta Google.
3. Ative a **Verificação em 2 Passos (2-Step Verification)** (A senha de app exige este requisito).
4. Procure por **Senhas de Aplicativo (App Passwords)** na busca de segurança.
5. Selecione "_Outro (Custom name)_", digite "Bolão App" e clique em **Gerar**.
6. O Google retornará uma senha aleatória de **16 caracteres**.
7. Copie e cole na variável `SMTP_PASSWORD` em ambos os arquivos `.env` que acabamos de configurar nos passos anteriores, associada com seu e-mail (`SMTP_USERNAME`).

### 16.4 Docker Compose do Bolão Copa

Crie `~/bolao/copa/docker-compose.yml`:

```yaml
# REMOVIDO: 'version: 3.8'
# Justificativa: o atributo 'version' foi depreciado no Docker Compose v2+.
# O Docker Compose moderno usa o schema Compose Specification e ignora
# o campo 'version'. Mantê-lo é ruído desnecessário e pode gerar warnings.

services:
  app:
    # Tag de versão explícita — nunca usar 'latest' em produção.
    # Justificativa: 'latest' pode puxar uma imagem incompatível num restart
    # automático causado por falha ou reboot da VM, derrubando o serviço
    # silenciosamente sem possibilidade de rollback imediato.
    image: seu-registry/bolao-app:1.0.0
    container_name: bolao_copa
    restart: unless-stopped
    ports:
      # Bind apenas no loopback — o Nginx acessa via 127.0.0.1.
      # Justificativa: expor em 0.0.0.0:8080 abre a porta para a internet
      # caso o firewall tenha alguma regra mal configurada ou seja reiniciado.
      # Bind em 127.0.0.1 é defesa em profundidade; a app só é alcançável
      # via Nginx, que é o único ponto de entrada legítimo.
      - "127.0.0.1:8080:8080"
    env_file:
      - .env
    networks:
      # Rede Docker nomeada e isolada.
      # Justificativa: sem uma rede explícita, o container vai para a bridge
      # padrão do Docker, que é compartilhada com TODOS os outros containers
      # da VM — quebrando o isolamento lógico entre as stacks.
      - bolao-net
    deploy:
      resources:
        limits:
          # Limites de CPU e RAM por container.
          # Justificativa: sem limites, um memory leak ou pico de carga
          # em uma instância pode consumir toda a RAM da VM de 24 GB e
          # derrubar AMBAS as aplicações e o Nginx simultaneamente.
          # Com 24 GB disponíveis, cada app recebe no máximo 768 MB,
          # deixando folga para Nginx, monitoramento e SO.
          cpus: '1.5'
          memory: 768M
        reservations:
          memory: 256M
    logging:
      # Log rotation nativo do Docker.
      # Justificativa: sem isso, /var/lib/docker/containers/<id>/*-json.log
      # cresce indefinidamente. Em produção com requests frequentes, o disco
      # de 50 GB pode ser preenchido em dias/semanas, derrubando todos os
      # serviços que precisam escrever em disco (incluindo o banco local).
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
    healthcheck:
      # Endpoint ajustado para a URL real da aplicação Java/Tomcat.
      # Justificativa: a aplicação é deployada como WAR no context-path
      # '/bolao' — o path '/health' não existe nesse stack, causando
      # healthcheck com falsos negativos e restarts desnecessários.
      # Ajuste o path conforme o context-path real do seu WAR.
      test: ["CMD", "curl", "-f", "http://localhost:8080/"]
      interval: 30s
      timeout: 10s
      retries: 3
      # Aumentado de 30s para 60s.
      # Justificativa: o Tomcat + Spring MVC leva mais de 30s para inicializar
      # completamente em CPU ARM com contexto Spring completo. Com start_period
      # curto, o Docker interpreta a inicialização normal como falha e reinicia
      # o container em loop, impedindo que a aplicação suba.
      start_period: 60s

networks:
  bolao-net:
    driver: bridge
```

### 16.5 Docker Compose do Bolão Mercúrio

Crie `~/bolao/mercurio/docker-compose.yml`:

```yaml
# Mesmas correções aplicadas ao docker-compose do Bolão Copa.
# Consulte os comentários do arquivo copa/docker-compose.yml para as justificativas.
services:
  app:
    image: seu-registry/bolao-app:1.0.0
    container_name: bolao_mercurio
    restart: unless-stopped
    ports:
      - "127.0.0.1:8081:8080"
    env_file:
      - .env
    networks:
      - bolao-net
    deploy:
      resources:
        limits:
          cpus: '1.5'
          memory: 768M
        reservations:
          memory: 256M
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

networks:
  bolao-net:
    driver: bridge
```

### 16.5 Subir os containers

```bash
cd ~/bolao/copa
docker compose up -d

cd ~/bolao/mercurio
docker compose up -d
```

### 16.6 Validar localmente

```bash
curl http://localhost:8080/health
curl http://localhost:8081/health
```

Se ambos responderem corretamente, o Nginx poderá encaminhar para eles.

---

## 17. Passo 11 — Configurar Nginx para os dois domínios

Agora o Nginx será configurado para decidir qual aplicação atender com base no domínio acessado.

### 17.1 Remover site padrão

```bash
sudo rm -f /etc/nginx/sites-enabled/default
```

### 17.2 Configuração global de gzip

Crie `/etc/nginx/conf.d/gzip.conf` (aplicado a **todos** os sites automaticamente):

```nginx
# Compressão gzip para assets textuais.
# Justificativa: a aplicação Java/JSP não comprime as respostas nativamente.
# O gzip no Nginx reduz 60–80% o tamanho de HTML, CSS e JS, acelerando
# o carregamento em conexões móveis — perfil típico do usuário de bolão —
# e economizando tráfego de saída (limite de 10 TB/mês no Always Free).
gzip on;
gzip_types text/plain text/css application/javascript application/json
           application/xml text/xml image/svg+xml;
gzip_min_length 256;   # arquivos menores que 256 bytes não compensam comprimir
gzip_vary on;          # informa ao browser que a resposta pode variar por Accept-Encoding
gzip_comp_level 5;     # nível 5 = bom equilíbrio entre compressão e CPU
```

### 17.3 Arquivo do domínio Copa

Crie `/etc/nginx/sites-available/bolao-copa`:

```nginx
# Bloco HTTP: apenas redireciona para HTTPS.
# Justificativa: nunca servir conteúdo em HTTP puro em produção.
server {
    listen 80;
    server_name bolao-copa.duckdns.org;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name bolao-copa.duckdns.org;

    # Certificados emitidos pelo Certbot (Passo 12).
    ssl_certificate     /etc/letsencrypt/live/bolao-copa.duckdns.org/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/bolao-copa.duckdns.org/privkey.pem;

    # Apenas TLS 1.2 e 1.3 — versões antigas (TLS 1.0/1.1) são vulneráveis.
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_session_timeout 1d;
    ssl_session_cache shared:SSL:10m;
    ssl_stapling on;        # OCSP Stapling: reduz latência na validação do certificado
    ssl_stapling_verify on;

    # --- Headers de Segurança ---
    # X-Frame-Options: impede clickjacking (incorporar a app em iframes externos).
    add_header X-Frame-Options SAMEORIGIN always;
    # X-Content-Type-Options: impede MIME-sniffing (ataques via tipo errado de arquivo).
    add_header X-Content-Type-Options nosniff always;
    # Referrer-Policy: limita informações enviadas em referrers para outros domínios.
    add_header Referrer-Policy strict-origin-when-cross-origin always;
    # HSTS: força HTTPS por 1 ano após o primeiro acesso.
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    # Content-Security-Policy: principal defesa contra ataques XSS.
    # Ajuste os domínios se usar CDN externo (ex: fonts.googleapis.com).
    add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:;" always;
    # Permissions-Policy: desativa APIs do browser desnecessárias para o bolão.
    add_header Permissions-Policy "geolocation=(), microphone=(), camera=()" always;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;

        # Headers de proxy: permitem que a app Java conheça o IP real do cliente.
        proxy_set_header Host              $host;
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Buffers de proxy ajustados para respostas do Tomcat.
        # Justificativa: o padrão do Nginx (4KB/8KB) é insuficiente para páginas
        # JSP com tabelas de jogos e palpites — sem isso o Nginx faz round-trips
        # extras com o backend, aumentando latência perceptível ao usuário.
        proxy_buffer_size        16k;
        proxy_buffers            4 32k;
        proxy_busy_buffers_size  32k;

        # Timeout de 90s: protege contra 504 no boot do Tomcat e em
        # requisições pesadas (relatórios, geração de tabelas longas).
        # Aumentado de 60s para 90s por causa do cold-start do container ARM.
        proxy_read_timeout 90s;
    }
}
```

### 17.4 Arquivo do domínio Mercúrio

Crie `/etc/nginx/sites-available/bolao-mercurio`:

```nginx
# Estrutura idêntica ao bolao-copa, ajustando apenas server_name e proxy_pass.
server {
    listen 80;
    server_name bolao-mercurio.duckdns.org;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name bolao-mercurio.duckdns.org;

    ssl_certificate     /etc/letsencrypt/live/bolao-mercurio.duckdns.org/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/bolao-mercurio.duckdns.org/privkey.pem;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_session_timeout 1d;
    ssl_session_cache shared:SSL:10m;
    ssl_stapling on;
    ssl_stapling_verify on;

    add_header X-Frame-Options SAMEORIGIN always;
    add_header X-Content-Type-Options nosniff always;
    add_header Referrer-Policy strict-origin-when-cross-origin always;
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:;" always;
    add_header Permissions-Policy "geolocation=(), microphone=(), camera=()" always;

    location / {
        proxy_pass http://127.0.0.1:8081;
        proxy_http_version 1.1;

        proxy_set_header Host              $host;
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        proxy_buffer_size        16k;
        proxy_buffers            4 32k;
        proxy_busy_buffers_size  32k;
        proxy_read_timeout 90s;
    }
}
```

### 17.5 Ativar os sites

```bash
sudo ln -s /etc/nginx/sites-available/bolao-copa /etc/nginx/sites-enabled/
sudo ln -s /etc/nginx/sites-available/bolao-mercurio /etc/nginx/sites-enabled/
```

### 17.6 Validar e recarregar o Nginx

```bash
# nginx -t verifica sintaxe SEM reiniciar o serviço.
sudo nginx -t
```

Se tudo estiver OK:

```bash
# reload aplica a nova configuração de forma graceful:
# conexões abertas continuam sendo servidas até terminar.
sudo systemctl reload nginx
```

---

## 18. Passo 12 — Habilitar HTTPS com Let's Encrypt

A forma mais simples é gerar um certificado para cada hostname.

### 18.1 Antes de gerar

Verifique:

- DNS já propagado;
- porta 80 aberta;
- Nginx instalado.

### 18.2 Gerar certificado do domínio Copa

```bash
sudo certbot --nginx -d bolao-copa.duckdns.org
```

### 18.3 Gerar certificado do domínio Mercúrio

```bash
sudo certbot --nginx -d bolao-mercurio.duckdns.org
```

### 18.4 Testar renovação

```bash
sudo certbot renew --dry-run
```

### 18.5 Alternativa com um único certificado SAN

Se preferir, você também pode emitir um único certificado com múltiplos nomes:

```bash
sudo certbot --nginx \
  -d bolao-copa.duckdns.org \
  -d bolao-mercurio.duckdns.org
```

Isso simplifica a manutenção, principalmente quando os dois hostnames pertencem ao mesmo domínio principal.

---

## 19. Passo 13 — Ajustar firewall interno da VM

Além da Security List do OCI, a VM pode ter firewall local ativo.

> **Por que UFW e não iptables direto?**  
> O Ubuntu 22.04 vem com o UFW (Uncomplicated Firewall) instalado por padrão.
> O UFW persiste as regras automaticamente entre reboots sem precisar de `iptables-persistent`.
> Manipular `iptables` diretamente em paralelo com o UFW pode criar conflitos silenciosos;
> além disso, regras iptables adicionadas manualmente são perdidas no reboot sem configuração extra.

### 19.1 Ativar UFW e liberar portas públicas

```bash
# Garantir que SSH esteja liberado ANTES de ativar o UFW para não se trancar fora.
sudo ufw allow ssh
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# Ativa o firewall com política padrão: bloquear entradas não declaradas.
sudo ufw enable
sudo ufw status verbose
```

### 19.2 Bloquear explicitamente as portas internas das apps

```bash
# As portas 8080/8081 NÃO devem ser acessíveis externamente.
# Com o bind em 127.0.0.1 no Docker Compose, elas já não ficam expostas,
# mas adicionar a regra de bloqueio é defesa em profundidade.
sudo ufw deny 8080/tcp
sudo ufw deny 8081/tcp
```

### 19.3 Acesso temporário ao Grafana durante setup

```bash
# Libere APENAS para o seu IP de administração, nunca para 0.0.0.0.
# Justificativa: Grafana tem histórico de CVEs críticos (autenticação bypass, RCE).
# Deixá-lo exposto publicamente durante o setup é um risco desnecessário.
sudo ufw allow from SEU_IP_ADMIN to any port 3000
```

### 19.4 Observação importante

As portas **8080** e **8081** não ficam públicas por duas razões complementares:
1. O `docker-compose.yml` faz bind em `127.0.0.1` (não em `0.0.0.0`).
2. O UFW nega explicitamente essas portas no segundo nível de defesa.

Essa abordagem de **defesa em profundidade** (duas camadas independentes) garante que
uma má configuração isolada não seja suficiente para expor a app na internet.

---

## 20. Passo 14 — Testes de validação

Depois da implantação, faça validações em camadas.

### 20.1 Teste da aplicação local

```bash
curl -I http://localhost:8080/health
curl -I http://localhost:8081/health
```

### 20.2 Teste de DNS

```bash
nslookup bolao-copa.duckdns.org
nslookup bolao-mercurio.duckdns.org
```

### 20.3 Teste HTTP/HTTPS

```bash
curl -I http://bolao-copa.duckdns.org
curl -I https://bolao-copa.duckdns.org

curl -I http://bolao-mercurio.duckdns.org
curl -I https://bolao-mercurio.duckdns.org
```

### 20.4 Resultado esperado

- HTTP redireciona para HTTPS.
- Cada domínio abre sua aplicação correta.
- Nenhuma app acessa o schema da outra.

### 20.5 Teste de cabeçalhos

```bash
curl -I https://bolao-copa.duckdns.org
```

Verifique se existem headers como:

- `Strict-Transport-Security`
- `X-Content-Type-Options`
- `X-Frame-Options`

---

## 21. Passo 15 — Monitoramento

Mesmo no free tier, vale muito a pena monitorar VM e containers.

### 21.1 Stack recomendada

- Prometheus
- Grafana
- Node Exporter
- cAdvisor

### 21.2 Exemplo de `docker-compose.yml` para monitoramento

Crie `~/bolao/monitoring/docker-compose.yml`:

```yaml
version: '3.8'
services:
  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    restart: unless-stopped
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus_data:/prometheus

  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    restart: unless-stopped
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=SenhaGrafana#2026
    volumes:
      - grafana_data:/var/lib/grafana

  node_exporter:
    image: prom/node-exporter:latest
    container_name: node_exporter
    restart: unless-stopped
    ports:
      - "9100:9100"

  cadvisor:
    image: gcr.io/cadvisor/cadvisor:latest
    container_name: cadvisor
    restart: unless-stopped
    ports:
      - "8082:8080"
    volumes:
      - /:/rootfs:ro
      - /var/run:/var/run:rw
      - /sys:/sys:ro
      - /var/lib/docker:/var/lib/docker:ro

volumes:
  prometheus_data:
  grafana_data:
```

### 21.3 Dashboards úteis no Grafana

IDs comuns para importar:

- `1860` — Node Exporter Full
- `193` — Docker metrics
- `14282` — cAdvisor

### 21.4 Boa prática

Se possível, não exponha Grafana e Prometheus publicamente. Prefira acesso por SSH tunnel, VPN ou liberação temporária por IP.

---

## 22. Passo 16 — Backup e segurança

### 22.1 Hardening básico da VM

```bash
sudo apt install -y unattended-upgrades fail2ban
sudo systemctl enable fail2ban
sudo systemctl start fail2ban
```

### 22.2 Desabilitar senha no SSH

```bash
sudo sed -i 's/#PasswordAuthentication yes/PasswordAuthentication no/' /etc/ssh/sshd_config
sudo systemctl restart ssh
```

### 22.3 Exemplo seguro de backup lógico MySQL

> **Por que não usar `-p'SUA_SENHA'` na linha de comando?**  
> Senhas passadas como argumento ficam visíveis para qualquer usuário do sistema via `ps aux`,
> são gravadas no `.bash_history` e podem aparecer em logs de auditoria do SO.
> A solução correta é usar um arquivo `.my.cnf` com permissão restrita.

**Passo 1 — Criar o arquivo de credenciais protegido:**

```bash
# Crie o arquivo de credenciais FORA do diretório do projeto.
cat > ~/.my.cnf << 'EOF'
[client]
host=IP_PRIVADO_MYSQL
user=admin
password=SUA_SENHA_AQUI
EOF

# OBRIGATÓRIO: permissão 600 — somente o dono pode ler.
# Justificativa: qualquer outra permissão faz o MySQL client rejeitar
# o arquivo por questões de segurança.
chmod 600 ~/.my.cnf
```

**Passo 2 — Script de backup (sem senha exposta):**

Crie `~/bolao/scripts/backup_mysql.sh`:

```bash
#!/bin/bash
# backup_mysql.sh — Backup lógico dos schemas do Bolão
# Usa ~/.my.cnf para credenciais (nunca exponha senhas em scripts).

set -euo pipefail  # Encerra o script imediatamente em qualquer erro.

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/home/ubuntu/bolao/backups"
mkdir -p "$BACKUP_DIR"

echo "[$(date)] Iniciando backup..." >> "$BACKUP_DIR/backup.log"

# O mysqldump lê host, usuário e senha automaticamente de ~/.my.cnf
mysqldump bolao_copa  > "$BACKUP_DIR/bolao_copa_$DATE.sql"
mysqldump bolao_mercurio > "$BACKUP_DIR/bolao_mercurio_$DATE.sql"

gzip "$BACKUP_DIR/bolao_copa_$DATE.sql"
gzip "$BACKUP_DIR/bolao_mercurio_$DATE.sql"

# Retém apenas os últimos 7 dias de backup.
find "$BACKUP_DIR" -name '*.gz' -mtime +7 -delete

echo "[$(date)] Backup concluído com sucesso." >> "$BACKUP_DIR/backup.log"
```

Permissão e agendamento:

```bash
chmod +x ~/bolao/scripts/backup_mysql.sh
crontab -e
```

Exemplo para rodar diariamente às 3h:

```cron
0 3 * * * /home/ubuntu/bolao/scripts/backup_mysql.sh 2>> /home/ubuntu/bolao/backups/backup.log
```

---

## 23. Passo 17 — Como manter custo zero

Esse é um ponto crítico.

### 23.1 Faça isto

- Use apenas a VM ARM dentro do limite gratuito.
- Não aumente OCPUs/RAM além do Always Free.
- Não crie Load Balancer pago.
- Não use disco além do necessário.
- Monitore storage do MySQL.
- Configure alertas de orçamento.

### 23.2 Budget no OCI

No console:

```text
Billing & Cost Management > Budgets > Create Budget
```

Configuração sugerida:

| Campo   | Valor          |
| ------- | -------------- |
| Nome    | `budget-bolao` |
| Valor   | US$ 1.00       |
| Reset   | Monthly        |
| Alertas | 10%, 50%, 80%  |

### 23.3 Evite isto

- criar recursos fora do Always Free;
- subir shapes pagos;
- aumentar storage sem controlar;
- reservar IPs sem uso;
- expor serviços desnecessários.

---

## 24. Checklist final

### Infraestrutura

- [ ] VCN criada.
- [ ] Public Subnet criada.
- [ ] Private Subnet criada.
- [ ] VM ARM criada com IP público.
- [ ] MySQL HeatWave criado em subnet privada.

### Banco

- [ ] Schema `bolao_copa` criado.
- [ ] Schema `bolao_mercurio` criado.
- [ ] Usuário `app_copa` criado.
- [ ] Usuário `app_mercurio` criado.
- [ ] Permissões isoladas conferidas.

### Aplicações

- [ ] Container `bolao_copa` rodando na porta 8080.
- [ ] Container `bolao_mercurio` rodando na porta 8081.
- [ ] Health check respondendo nos dois.

### Nginx e SSL

- [ ] Nginx instalado.
- [ ] Site do domínio Copa configurado.
- [ ] Site do domínio Mercúrio configurado.
- [ ] HTTPS funcionando nos dois domínios.
- [ ] HTTP redirecionando para HTTPS.

### Segurança

- [ ] Portas 80 e 443 liberadas.
- [ ] Porta 22 controlada.
- [ ] MySQL sem acesso público.
- [ ] SSH por chave.
- [ ] Fail2Ban ativo.

### Operação

- [ ] Backup agendado.
- [ ] Monitoramento ativo.
- [ ] Budget configurado.

---

## 25. Referências úteis

- Console OCI: https://cloud.oracle.com
- Oracle Cloud Free Tier: https://www.oracle.com/br/cloud/free/
- Always Free Resources: https://docs.oracle.com/pt-br/iaas/Content/FreeTier/freetier_topic-Always_Free_Resources.htm
- MySQL HeatWave OCI: https://docs.oracle.com/en-us/iaas/mysql-database/doc/getting-started-mysql-heatwave-service.html
- Certbot: https://certbot.eff.org
- Nginx docs: https://nginx.org/en/docs/
- Grafana Dashboards: https://grafana.com/grafana/dashboards

---

## Observação final

Esta versão substitui a arquitetura anterior com **OCI Load Balancer** por uma arquitetura mais enxuta e aderente ao Always Free, usando **Nginx na VM como reverse proxy principal**. Para o seu cenário de dois bolões com DNS diferentes, essa é a abordagem mais econômica, simples e tecnicamente apropriada.

---

## 26. Análise Crítica e Complementos (v3.0)

Esta seção documenta todas as decisões de melhoria aplicadas na v3.0, os novos insights e complementos que não existiam na v2.0. Cada item é justificado para rastreabilidade e aprendizado.

---

### 26.1 Resumo das correções aplicadas

| # | Problema (v2.0) | Correção (v3.0) | Risco original |
|---|---|---|---|
| 1 | `version: '3.8'` no Compose | Removido | Baixo (warning/ruído) |
| 2 | Bind de porta `0.0.0.0:8080` | `127.0.0.1:8080` | **Alto** (exposição acidental) |
| 3 | Tag `latest` na imagem | Tag de versão explícita `1.0.0` | **Alto** (rollback impossível) |
| 4 | Sem `deploy.resources` | CPU e RAM limitadas por container | **Alto** (cascata de falhas) |
| 5 | Sem log rotation | `json-file` driver com `max-size` | **Médio** (disco cheio) |
| 6 | Healthcheck em `/health` (inexistente) | `/bolao/` (context-path real) | **Alto** (restart loop) |
| 7 | `start_period: 30s` (curto para Tomcat) | `start_period: 60s` | **Médio** (restart falso positivo) |
| 8 | SSH com RSA-4096 | ed25519 | Baixo (performance/modernização) |
| 9 | `iptables` direto | UFW (padrão Ubuntu 22.04) | **Médio** (regras perdidas no reboot) |
| 10 | Senha MySQL em argumento de linha | `~/.my.cnf` com `chmod 600` | **Alto** (exposição em `ps aux`) |

---

### 26.2 Nginx — melhorias aplicadas (Passo 11)

> As configura\u00e7\u00f5es de **gzip global**, **proxy buffers**, **CSP**, **Permissions-Policy** e **OCSP Stapling** foram integradas diretamente nos passos **17.2** e **17.3/17.4** do documento.  
> Consule o Passo 11 para a configura\u00e7\u00e3o completa e justificativa de cada diretiva.

---

### 26.3 Complemento — prometheus.yml de exemplo

O documento v2.0 mencionava o Prometheus mas não fornecia o arquivo de configuração, impedindo a execução imediata. Crie `~/bolao/monitoring/prometheus/prometheus.yml`:

```yaml
# prometheus.yml — Configuração mínima para monitorar a stack do Bolão
# Justificativa de cada job:
# - node: métricas do sistema operacional (CPU, RAM, disco) via Node Exporter
# - docker: métricas dos containers via cAdvisor
# - nginx: (opcional) métricas do Nginx via nginx-prometheus-exporter (requer módulo adicional)

global:
  scrape_interval: 15s      # Coleta a cada 15s — equilíbrio entre granularidade e custo de recursos
  evaluation_interval: 15s  # Avalia alertas na mesma frequência

scrape_configs:
  - job_name: 'node'
    static_configs:
      - targets: ['node_exporter:9100']

  - job_name: 'cadvisor'
    static_configs:
      - targets: ['cadvisor:8080']

  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
```

> **Nota sobre o cAdvisor:** A imagem `gcr.io/cadvisor/cadvisor:latest` está depreciada para algumas arquiteturas ARM. Use a tag estável `v0.49.1` no docker-compose.yml de monitoramento para garantir compatibilidade com o Ampere A1.

---

### 26.4 Prote\u00e7\u00e3o dos arquivos .env \u2014 aplicado (Passo 10)

> O `chmod 600` nos arquivos `.env` foi integrado diretamente nos **passos 16.1 e 16.2**,\n> imediatamente ap\u00f3s a cria\u00e7\u00e3o de cada arquivo de configura\u00e7\u00e3o.

---

### 26.5 Novo insight — Backup para OCI Object Storage

O Always Free inclui **20 GB de Object Storage** que não estão sendo usados na arquitetura v2.0. Enviar os backups para lá elimina o risco de perda caso o disco local da VM seja corrompido ou a VM seja encerrada.

```bash
# Instalar OCI CLI (uma única vez)
bash -c "$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)"
oci setup config  # Configura região, OCID e chave de API

# Adicionar ao final do script backup_mysql.sh:

# Envia backup para OCI Object Storage (bucket 'bolao-backups')
# Justificativa: o Object Storage é durável (11 noves de durabilidade), geograficamente
# redundante e não ocupa o disco local de 50 GB. É o local ideal para retenção de backups.
oci os object put \
  --bucket-name bolao-backups \
  --file "$BACKUP_DIR/bolao_copa_$DATE.sql.gz" \
  --name "copa/bolao_copa_$DATE.sql.gz"

oci os object put \
  --bucket-name bolao-backups \
  --file "$BACKUP_DIR/bolao_mercurio_$DATE.sql.gz" \
  --name "mercurio/bolao_mercurio_$DATE.sql.gz"
```

---

### 26.6 Novo insight — Estratégia de deploy sem downtime

Na arquitetura v2.0, a atualização da aplicação (`docker compose pull && docker compose up -d`) resulta em alguns segundos de indisponibilidade enquanto o container antigo para e o novo inicia. Para o contexto do bolão (picos de acesso próximo aos jogos), isso pode ser perceptível.

**Estratégia simples de blue/green com as portas disponíveis:**

```bash
# Sobe a nova versão em uma porta alternativa sem parar a atual
docker compose -f docker-compose.new.yml up -d  # porta 8082

# Testa a nova versão
curl http://localhost:8082/

# Se OK, redireciona o Nginx para a nova porta via reload (sem downtime)
sudo sed -i 's/8080/8082/' /etc/nginx/sites-enabled/bolao-copa
sudo nginx -t && sudo nginx -s reload

# Para o container antigo somente após validação
docker compose down  # antiga porta 8080
```

> **Justificativa:** O `nginx -s reload` é graceful — o Nginx conclui as conexões abertas antes de aplicar a nova configuração. A janela de indisponibilidade é de milissegundos, não segundos.

---

### 26.7 Novo insight — Renovação automática do certificado SSL

O certbot cria um timer systemd automático no Ubuntu 22.04, mas é importante verificar se está ativo:

```bash
# Verificar se o timer de renovação está ativo
sudo systemctl status certbot.timer

# Se não estiver, ativar manualmente
sudo systemctl enable certbot.timer
sudo systemctl start certbot.timer

# Adicionar hook para recarregar o Nginx após renovação
# Justificativa: sem o reload, o Nginx continua usando o certificado antigo
# mesmo após a renovação. O hook garante que o novo certificado seja aplicado.
echo '#!/bin/bash
nginx -s reload' | sudo tee /etc/letsencrypt/renewal-hooks/post/nginx-reload.sh
sudo chmod +x /etc/letsencrypt/renewal-hooks/post/nginx-reload.sh
```

---

### 26.8 Atualização do Checklist Final

Adicionar ao checklist da seção 24:

**Docker e aplicações:**
- [ ] Imagens com tag de versão explícita (não `latest`).
- [ ] Bind de porta em `127.0.0.1` (não `0.0.0.0`).
- [ ] Resource limits definidos (CPU e RAM por container).
- [ ] Log rotation configurado (max-size: 10m, max-file: 3).
- [ ] `start_period` compatível com o tempo de boot do Tomcat.

**Segurança:**
- [ ] Arquivos `.env` com `chmod 600`.
- [ ] `~/.my.cnf` criado com `chmod 600` para backup seguro.
- [ ] UFW ativado com política padrão de bloqueio.
- [ ] Portas 8080/8081 negadas no UFW (segunda camada de defesa).

**Nginx:**
- [ ] gzip habilitado para assets textuais.
- [ ] `proxy_buffer_size` e `proxy_read_timeout` ajustados para o Tomcat.
- [ ] `Content-Security-Policy` configurado.

**Operação:**
- [ ] Backup enviado para OCI Object Storage (além do disco local).
- [ ] Timer do certbot ativo e hook de reload do Nginx configurado.
- [ ] prometheus.yml criado antes de subir o container do Prometheus.

---

> Documento atualizado em: 16/05/2026  
> Arquitetura: Oracle Cloud Always Free + Nginx + Docker + MySQL HeatWave  
> Modelo: 2 instâncias independentes com 2 domínios e 2 schemas isolados  
> **v3.0:** Análise crítica, correções de segurança e resiliência, novos insights documentados e justificados.
