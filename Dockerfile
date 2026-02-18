# Stage 1: Build
FROM maven:3.8-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
# Baixar dependências para cache
RUN mvn dependency:go-offline -B
COPY src ./src
COPY webapp ./webapp
RUN mvn clean package -DskipTests

# Stage 2: Runtime
# Usamos uma imagem minimalista do Tomcat 10 (Jakarta EE 10) baseada em Alpine para manter o tamanho reduzido e segurança
FROM tomcat:10.1-jdk17-alpine
WORKDIR /usr/local/tomcat/webapps/
# Remover apps padrão do Tomcat para segurança
RUN rm -rf ROOT docs examples host-manager manager
# Instalar curl para health check
RUN apk add --no-cache curl
# Copiar o WAR gerado no stage 1
COPY --from=build /app/target/sistema-bolao.war ./ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
