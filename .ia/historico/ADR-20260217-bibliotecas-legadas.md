# ADR-20260217-atualizacao-bibliotecas-legadas

## Contexto
O projeto possui dependências de tecnologias legadas (DWR 1.x e Cewolf) que não possuem suporte oficial nativo para a namespace `jakarta.*` (Jakarta EE 10). A remoção imediata dessas bibliotecas causaria quebra de funcionalidades críticas (gráficos e interações AJAX ainda não migradas).

## Opções Consideradas

### 1. Remoção Imediata e Reescrita
Reescrever todos os gráficos (Cewolf -> JFreeChart puro ou JS) e AJAX (DWR -> HTMX).
- **Prós:** Elimina dívida técnica.
- **Contras:** Custo e risco altíssimos a curto prazo.

### 2. Uso de Forks ou Versões Estáveis com Pontes
Utilizar as versões mais recentes estáveis e tentar a compatibilidade via pacotes de "bridge" ou dependências atualizadas.
- **Prós:** Mantém o sistema rodando.
- **Contras:** Risco de conflitos em tempo de execução.

### 3. Migração Gradual com HTMX (Recomendada)
Manter as bibliotecas no `pom.xml` para evitar erros de compilação, mas priorizar a migração da interface para HTMX (eliminando DWR) e JFreeChart direto (eliminando Cewolf conforme possível).

## Decisão
Adotaremos a **Opção 3**. Atualizaremos o `pom.xml` com as versões mais estáveis conhecidas (DWR 3.0.2 / Cewolf 1.2.x) e integraremos o **JFreeChart 1.5.4** (mais moderno). 

> [!WARNING]
> O Cewolf pode exigir o plugin `maven-transformer` ou ajustes manuais devido à mudança de `javax.servlet` para `jakarta.servlet`. Monitoraremos o deploy no Tomcat 10 rigorosamente.

## Consequências
- O build passará a gerenciar essas libs via Maven (eliminando arquivos JAR soltos se existirem).
- O projeto terá uma fundação para remover essas libs progressivamente.

Auto-Analise: [Risco: Médio] | [Compatibilidade: Atenção] | [Veredito: Aprovado]
