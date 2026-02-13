# Substituicao de Dependencias Bloqueadas no Nexus

Data: 2026-02-13
Origem do erro: build Maven (dependencias nao resolvidas no Nexus corporativo)

## 1. Dependencias bloqueadas (confirmadas no build)

- org.apache.xmlgraphics:batik-all:1.7
- gnujaxp:gnujaxp:1.0.0
- org.hibernate:hibernate3:3.2.6.ga
- org.jfree:jcommon:1.0.0
- org.jfree:jfreechart:1.0.0
- javax.mail:mail:1.3.1
- opensymphony:oscore:2.2.5
- org.quartz-scheduler:quartz:1.5.1
- com.uwyn.rife:rife-continuations:1.0
- com.opensymphony:webwork:2.2.2
- com.opensymphony:xwork:1.1.3

## 2. Estrategia de substituicao (curto prazo)

Objetivo: manter o stack `javax` enquanto o corte Jakarta nao ocorre, mas usar **versoes aprovadas no Nexus**.

### 2.1 Caminhos possiveis

A) **Ajuste de versoes (preferencial)**
- Trocar para versoes mais recentes **ainda javax** que estejam aprovadas no Nexus.
- Exige lista de versoes permitidas pela seguranca.

B) **Substituicao funcional antecipada**
- Para bibliotecas legadas sem versao aprovada (ex: WebWork/XWork), antecipar a migracao para Struts 7 ou outra stack suportada.
- Impacto maior; requer ADR especifico.

C) **Repositorio legado isolado**
- Publicar artefatos legados num repositorio interno isolado (apenas para build de transicao).
- Deve ter aprovacao de seguranca e prazo de expurgo.

## 3. Acoes necessarias (para destravar o build)

1. Solicitar ao time de Nexus/Seguranca a **lista de versoes aprovadas** para cada dependencia bloqueada.
2. Atualizar o `pom.xml` substituindo as versoes bloqueadas pelas aprovadas (sem `systemPath`).
3. Executar build e validar resultados.

## 4. Observacoes

- Sem a lista de versoes aprovadas, a troca no `pom.xml` fica incompleta.
- Dependencias WebWork/XWork sao candidatas a substituicao antecipada por Struts 7, mas isso deve ser decidido em ADR.
