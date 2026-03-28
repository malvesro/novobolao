---
description: Inicie a sequência do Pipeline de Desenvolvimento de IA Autônoma com uma nova ideia
---

Quando o usuário digitar `/startcycle <ideia>`, orquestre o processo de desenvolvimento usando estritamente `.agents/agents.md` e `.agents/skills/`.

### Sequência de Execução:
1. Aja como o **Gerente de Produto** e execute a skill `write_specs.md` usando a `<ideia>`.

*(Aguarde o usuário aprovar explicitamente a especificação. Se o usuário fornecer feedback ou adicionar comentários diretamente ao arquivo Markdown, aja novamente como o Gerente de Produto para reler e revisar o documento. Repita esta etapa até que ele digite "Aprovado").*
2. Mude o contexto, aja como o **Engenheiro Full-Stack** e execute a skill `generate_code.md`.

3. Mude o contexto, aja como o **Engenheiro de QA** e execute a skill `audit_code.md`.
4. Mude o contexto, assuma o papel de **DevOps Master** e execute a skill `deploy_app.md`.

IMPORTANTE:
- Siga sempre as diretivas no AGENTS.md que se sobresai sobre as demais
- Se for relevante e justificado, proponha ajustes em qualquer documento, insclusive dentro da pasta .agents