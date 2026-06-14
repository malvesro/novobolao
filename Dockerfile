# syntax=docker/dockerfile:1.4

# Stage 1: Frontend Build (Isolado e Cacheável)
FROM node:20-slim AS frontend-builder
WORKDIR /app
COPY package*.json ./
COPY vite.config.js ./
# Cache mount para npm para acelerar re-builds
RUN --mount=type=cache,target=/root/.npm \
    npm install
COPY webapp ./webapp
COPY src/frontend ./src/frontend
RUN npm run build

# Stage 2: Maven Dependencies (Apenas baixar dependências)
FROM maven:3.8-openjdk-17-slim AS deps
WORKDIR /app
COPY pom.xml .
# Baixa apenas as dependências do projeto. 
# Removido 'mvn verify' para evitar execução lenta do dependency-check no build.
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

# Stage 3: Backend Build & Assembly
FROM deps AS builder
WORKDIR /app
# Copia o código fonte e webapp
COPY src ./src
COPY webapp ./webapp
# Copia os assets buildados no Stage 1 para o local que o Maven espera
COPY --from=frontend-builder /app/webapp/assets ./webapp/assets
# Otimizações:
# -T 1C: Usa 1 thread por core da CPU para build paralelo
# -Dmaven.test.skip=true: Pula compilação e execução de testes
# -Ddependency-check.skip=true: Auditoria deve ser feita manualmente via ./scripts/run-audit.sh
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -T 1C -Dmaven.test.skip=true -Dfrontend.skip=true -Ddependency-check.skip=true -B

# Stage 4: Runtime
FROM tomcat:10.1-jdk17-temurin
WORKDIR /usr/local/tomcat/webapps/

# Remover apps padrão do Tomcat
RUN rm -rf ROOT docs examples host-manager manager && \
    apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Configuração de fuso horário canônico do domínio.
# Observação: no Hugging Face o host pode rodar com outro timezone; por isso
# fixamos tanto o TZ do processo quanto o user.timezone da JVM.
ENV TZ=America/Sao_Paulo

# Configuração de memória/JVM para reduzir pausas e melhorar latência no HF.
# Estratégia de Arquiteto: 
# 1. Heap fixo em 1024m para evitar redimensionamento agressivo em runtime.
# 2. String Deduplication para reduzir footprint de memória de objetos JSTL/Hibernate.
# 3. G1GC tunado para latência agressiva (MaxGCPauseMillis=100).
ENV CATALINA_OPTS="-Xms1024m -Xmx1024m \
    -XX:MaxMetaspaceSize=256m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=100 \
    -XX:InitiatingHeapOccupancyPercent=35 \
    -XX:+UseStringDeduplication \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.awt.headless=true \
    -Duser.timezone=America/Sao_Paulo"

# Otimização do server.xml:
# 1. Alterar porta para 7860.
# 2. Tuning de concorrência (threads e backlog).
# 3. Ativar compressão GZIP para reduzir payload de fragmentos HTMX e assets.
RUN sed -i 's/port="8080"/port="7860"/g' /usr/local/tomcat/conf/server.xml && \
    sed -i 's/protocol="HTTP\/1.1"/protocol="HTTP\/1.1" maxThreads="150" minSpareThreads="25" acceptCount="100" keepAliveTimeout="15000" maxKeepAliveRequests="100" compression="on" compressionMinSize="1024" compressableMimeType="text\/html,text\/xml,text\/plain,text\/css,application\/javascript,application\/json"/g' /usr/local/tomcat/conf/server.xml

# Copiar o WAR gerado no stage 3
COPY --from=builder /app/target/sistema-bolao.war ./ROOT.war

EXPOSE 7860

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=60s \
  CMD curl -f http://localhost:7860/health.txt || exit 1

CMD ["catalina.sh", "run"]
