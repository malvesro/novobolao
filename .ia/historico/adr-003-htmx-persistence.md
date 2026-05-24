# ADR-003: Persistência Segura de Palpites via HTMX

## Status
Proposto em 2026-04-03 (Aceito)

## Contexto
O sistema utiliza HTMX para atualizações assíncronas dos palpites dos usuários na tela `jogos.jsp`. O comportamento anterior sincronizava o estado do banco de dados a cada evento de `blur` ou `change` nos inputs de gols. No entanto, se o usuário preenchesse apenas um dos campos (ex: Gols Equipe 1), a requisição era enviada, falhava na validação do servidor (que exigia ambos os campos) e o fragmento HTML retornado reconstruía a linha a partir do banco de dados, limpando o campo recém-preenchido pelo usuário.

## Decisão
Implementar um estado de "Palpite Transiente" na `ParticipanteAction`. 
1.  A ação `atualizarPalpiteHtmx` agora aceita envios parciais.
2.  Caso um dos campos esteja ausente, o sistema não salva no banco de dados, mas cria um objeto `Palpite` temporário com os valores recebidos.
3.  Este objeto temporário é preservado no contexto da requisição para que o fragmento JSP renderize os inputs com os valores que o usuário acabou de digitar.
4.  O salvamento definitivo no banco de dados continua condicionado à presença de ambos os valores numéricos.

## Consequências
- **Positivas:** Melhoria significativa na experiência do usuário (fim da limpeza indesejada de campos). Redução de erros de validação "falso-positivos" no frontend.
- **Negativas:** Leve aumento da complexidade na camada de Action para gerenciar o estado transiente.
