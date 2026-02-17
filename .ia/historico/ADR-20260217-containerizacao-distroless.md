# ADR-20260217-containerizacao-distroless.md

## Status
Proposto

## Contexto
O projeto está sendo modernizado para a stack Spring 6 / Jakarta EE 10 / Tomcat 10. Para facilitar o deploy, padronizar o ambiente de desenvolvimento e garantir a segurança do runtime, é necessária uma estratégia de containerização.

## Decisão
Adotar o **Docker** e **Docker Compose** para orquestração local, utilizando princípios de **Containers Distroless** para a imagem final da aplicação.

### Detalhes Técnicos:
1.  **Imagens Multi-stage:**
    *   **Stage 1 (Build):** Utilizar uma imagem Maven com OpenJDK 17 para compilar e gerar o WAR.
    *   **Stage 2 (Runtime):** Utilizar uma imagem base **Distroless Java 17** da Google (ou Tomcat compatível com Jakarta EE). 
    *   *Nota:* Como as imagens Distroless puras da Google para Java geralmente não incluem um Tomcat pronto, utilizaremos uma base minimalista ou instalaremos o Tomcat sobre uma base distroless, ou alternativamente, usaremos imagens minimalistas baseadas em Alpine/Wolfi focadas em segurança.
2.  **Segurança (Distroless):** Redução da superfície de ataque ao remover shells, editores e pacotes desnecessários do container de execução.
3.  **Docker Compose:**
    *   Serviço `app`: Tomcat 10 + Jakarta EE.
    *   Serviço `db`: MySQL 8.0 (utilizando volumes para persistência).
    *   Rede interna isolada.

## Consequências
*   **Positivas:** Deploy reprodutível, ambiente de dev idêntico ao de produção, maior segurança, isolamento de dependências de infra (MySQL).
*   **Negativas:** Necessidade de Docker instalado no host, maior tempo de build inicial (download de imagens).

## Alternativas Consideradas
*   **Instalação Local (Bare Metal):** Descartada por dificuldades de reprodutibilidade e conflitos de versão (Java, MySQL).
*   **Imagens Full OS (Ubuntu/Debian):** Descartadas para priorizar segurança e eficiência (tamanho da imagem).
