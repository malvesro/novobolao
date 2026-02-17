# ADR-20260217-estrategia-arquitetural-frontend

## Contexto e Visão do Arquiteto
Estamos modernizando um sistema de 20 anos. A stack original (Prototype 1.4/Scriptaculous 1.5) é imperativa e baseada em manipulação direta de DOM. A introdução do **Struts 6 / Jakarta EE 10** exige uma decisão sobre como evoluir a interface sem cair na armadilha de uma reescrita SPA (React/Vue) que seria custosa e desconectada da arquitetura atual baseada em JSPs.

## Análise Sênior: jQuery vs HTMX no Ecossistema Struts

### 1. jQuery 4.0.0 (A Camada de Estabilidade)
**Por que usar?**
- **Drop-in replacement:** Existe uma quantidade massiva de código legado que espera um seletor e uma animação. O jQuery 4.0.0 é a evolução natural e segura para substituir o Prototype.
- **Ecossistema:** Qualquer plugin de gráfico, calendário ou máscara de campo de data que precisarmos será trivial de encontrar para jQuery.
- **Veredito:** Indispensável para a **sobrevivência** do código legado e transição suave.

### 2. HTMX (A Mudança de Paradigma)
**Por que usar?**
- **Alinhamento com Struts:** O Struts foi desenhado para retornar HTML/JSP. O DWR (JSON) sempre foi um "puxadinho". O HTMX permite que o Struts volte a fazer o que faz de melhor: renderizar fragmentos de JSP que são injetados diretamente na tela.
- **Menos código, menos bugs:** Substitui centenas de linhas de AJAX manual por atributos como `hx-post` e `hx-target`.
- **Performance:** Evita o custo de serialização/deserialização JSON no cliente e no servidor.
- **Veredito:** É a recomendação estratégica para **novas funcionalidades** e refatorações de AJAX, visando eliminar o DWR a longo prazo.

### 3. Alpine.js (O Toque de Reatividade)
**Por que usar?**
- Excelente para lógica de UI local que não precisa de servidor (ex: fechar menu, abrir dropdown).
- Evita o "jQuery spaghetti" de seletores globais.

## Decisão Arquitetural Final
Adotaremos uma **Arquitetura Híbrida e Progressiva**:

1.  **Fundação (jQuery 4.0.0):** Instalado globalmente em modo `noConflict` ($j). Substituirá o Prototype/Scriptaculous em scripts existentes para garantir estabilidade.
2.  **Motor AJAX (HTMX):** Introduzido como o padrão para novas requisições assíncronas. O objetivo é que 80% do intercâmbio de dados use HTMX, reduzindo a necessidade de escrever JavaScript imperativo.
3.  **Transição Granular:** Não tentaremos remover o DWR de uma vez. Ele coexistirá até que as telas sejam refatoradas para HTMX.

## Justificativa Sênior
Esta abordagem evita a obsolescência (usar APENAS jQuery em 2026 seria um erro estratégico) e evita o risco de sobre-engenharia (usar React/Vue em JSPs monolíticos cria um "Frankenstein" difícil de manter). HTMX + Struts é a combinação mais eficiente para produtividade e modernidade em Java monoliths.

Auto-Analise: [Modernidade: 10/10] | [Custo: 2/10] | [Manutenibilidade: 9/10]
