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

# Stage 2: Maven Dependencies (Pre-warm de dependências e PLUGINS)
FROM maven:3.8-openjdk-17-slim AS deps
WORKDIR /app
COPY pom.xml .
# O comando 'go-offline' do Maven é incompleto. 
# Rodamos um 'verify' ignorando erros (pela falta de src) para baixar plugins e deps de build.
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B && \
    mvn verify -DskipTests -Dfrontend.skip=true -B || true

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
# -Dmaven.test.skip=true: Pula compilação e execução de testes (mais rápido que -DskipTests)
# Sem 'clean': Redundante em uma camada de build fresca
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -T 1C -Dmaven.test.skip=true -Dfrontend.skip=true -B

# Stage 4: Runtime
FROM tomcat:10.1-jdk17-temurin
WORKDIR /usr/local/tomcat/webapps/

# Remover apps padrão do Tomcat
RUN rm -rf ROOT docs examples host-manager manager && \
    apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Configuração de Fuso Horário
ENV TZ=America/Sao_Paulo

# Configuração de Memória e JVM
ENV CATALINA_OPTS="-Xmx256m -Xms256m -XX:MaxMetaspaceSize=160m -XX:+UseSerialGC -Xss256k -Djava.awt.headless=true -Duser.timezone=America/Sao_Paulo"

# Alterar porta para 7860
RUN sed -i 's/port="8080"/port="7860"/g' /usr/local/tomcat/conf/server.xml

# Copiar o WAR gerado no stage 3
COPY --from=builder /app/target/sistema-bolao.war ./ROOT.war

EXPOSE 7860

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=60s \
  CMD curl -f http://localhost:7860/health.txt || exit 1

CMD ["catalina.sh", "run"]
