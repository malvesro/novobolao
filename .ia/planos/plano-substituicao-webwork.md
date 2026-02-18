# Plano de Substituição do WebWork (Fase 2 - Item 4)

Este plano visa a eliminação total das referências ao framework WebWork para consolidar a transição para o Struts 6.

## Mudanças Propostas

### 1. Web Configuration
- [ ] Remover a declaração do servlet `webwork` (`com.opensymphony.webwork.dispatcher.ServletDispatcher`) do `web.xml`.
- [ ] Remover o `servlet-mapping` para `*.action` associado ao servlet `webwork`.
- [ ] Garantir que o `StrutsPrepareAndExecuteFilter` do Struts 2 é o único responsável pela interceptação de requests.

### 2. JSP Taglibs
- [ ] Verificar novamente todos os arquivos `.jsp` e `.jspf` em busca da taglib uri `/webwork` ou `prefix="ww"`.
- [ ] Substituir remanescentes por `uri="/struts-tags"` e `prefix="s"`.

### 3. Limpeza de Configuração
- [ ] Remover o arquivo `xwork.xml` se ele ainda existir fisicamente (foi substituído por `struts.xml`).

## Plano de Verificação

### Build
- `mvn clean compile` para garantir que não há erros de dependência.

### Manual
- Acessar a aplicação e validar se as telas `.action` continuam funcionando sob o filtro do Struts 2.
