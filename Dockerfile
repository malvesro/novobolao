# Stage 1: Build
FROM maven:3.8-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY package.json package-lock.json ./
COPY vite.config.js ./
RUN mvn dependency:go-offline -B
COPY src ./src
COPY webapp ./webapp
RUN mvn clean package -DskipTests

# Stage 2: Runtime
# Usamos Tomcat 10 (Jakarta EE 10) com JDK 17 (Temurin)
FROM tomcat:10.1-jdk17-temurin
WORKDIR /usr/local/tomcat/webapps/

# Remover apps padrão do Tomcat para segurança e economia de memória
RUN rm -rf ROOT docs examples host-manager manager

# Instalar curl para health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Configuração de Fuso Horário (Brasília)
ENV TZ=America/Sao_Paulo

# Configuração de Memória e JVM para Koyeb (Limitado a 512MB RAM, 0.1 vCPU)
# -Xmx256m: Heap máximo reduzido para 256MB para dar espaço ao Metaspace
# -Xms256m: Heap inicial fixo
# -XX:MaxMetaspaceSize=160m: Aumentado para 160MB (estava em 96MB, causando OOM)
# -XX:+UseSerialGC: GC mais eficiente para sistemas com pouca CPU e RAM
# -Xss256k: Redução do tamanho da stack de threads
ENV CATALINA_OPTS="-Xmx256m -Xms256m -XX:MaxMetaspaceSize=160m -XX:+UseSerialGC -Xss256k -Djava.awt.headless=true -Duser.timezone=America/Sao_Paulo"

# Alterar porta padrão do Tomcat de 8080 para 7860 (Requisito Hugging Face)
RUN sed -i 's/port="8080"/port="7860"/g' /usr/local/tomcat/conf/server.xml

# Copiar o WAR gerado no stage 1
COPY --from=build /app/target/sistema-bolao.war ./ROOT.war

# Porta obrigatória para Hugging Face Spaces
EXPOSE 7860

# Health check ajustado para a nova porta
HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=60s \
  CMD curl -f http://localhost:7860/health.txt || exit 1

CMD ["catalina.sh", "run"]
