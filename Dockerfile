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
# Usamos Tomcat 10 (Jakarta EE 10) com JDK 17
FROM tomcat:10.1-jdk17
WORKDIR /usr/local/tomcat/webapps/
# Remover apps padrão do Tomcat para segurança
RUN rm -rf ROOT docs examples host-manager manager
# Instalar curl para health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
# Gerar keystore local para HTTPS (ambiente dev)
RUN keytool -genkeypair -alias tomcat \
    -keyalg RSA -keysize 2048 -storetype PKCS12 \
    -keystore /usr/local/tomcat/conf/keystore.p12 \
    -validity 3650 -storepass changeit -keypass changeit \
    -dname "CN=localhost, OU=Dev, O=Bolao, L=Local, ST=Local, C=BR" \
    -ext SAN=DNS:localhost,IP:127.0.0.1
# Habilitar conector HTTPS no Tomcat (JSSE com SSLHostConfig)
RUN perl -0777 -i -pe 's#</Service>#    <Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol" \
               maxThreads="150" SSLEnabled="true" maxParameterCount="1000" \
               scheme="https" secure="true" clientAuth="false">\\n\
        <SSLHostConfig sslProtocol="TLS" protocols="TLSv1.3,TLSv1.2">\\n\
            <Certificate certificateKeystoreFile="conf/keystore.p12" \
                         certificateKeystorePassword="changeit" \
                         certificateKeystoreType="PKCS12" type="RSA" />\\n\
        </SSLHostConfig>\\n\
    </Connector>\\n\
</Service>#s' /usr/local/tomcat/conf/server.xml
# Copiar o WAR gerado no stage 1
COPY --from=build /app/target/sistema-bolao.war ./ROOT.war

EXPOSE 8080
EXPOSE 8443
CMD ["catalina.sh", "run"]
