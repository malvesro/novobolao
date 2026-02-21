# Fase 6 — Atualização de Dados (SQL) - Status em 21/02/2026

## 1. Escopo da Tarefa
Gerar e validar o script `data/sql/03-copa-2026-data.sql` contendo a carga completa da Copa 2026: seleções, fases de grupos (12 grupos A–L), fase eliminatória ampliada e horários em BRT.

## 2. Estado Atual
- **Dataset processado:** `data/copa2026_tabela_brt_final.csv` (gerado pelo script `scripts/atualizar_copa2026_dataset.py`).  
  - 89 partidas registradas (72 grupos + 16 de 32-avos + final).  
  - 82 seleções distintas (41 placeholders/slots de repescagens ainda sem definição).  
- **Script SQL:** `data/sql/03-copa-2026-data.sql` contém inserts `ON DUPLICATE KEY` para 82 seleções e 89 jogos.

## 3. Lacunas Identificadas
1. **Fases posteriores:** Oitavas, quartas, semifinais e 3º lugar ainda não possuem data/horário oficial (FIFA não publicou agenda completa).  
2. **Placeholders:** vagas de repescagem (UEFA e intercontinentais) continuam representadas por códigos (`UEFA Playoff A`, etc.).  
3. **Total de jogos:** A Copa 2026 terá 104 partidas. Atualmente faltam 15 confrontos (oitavas em diante).

## 4. Próximos Passos Planejados
1. **Aguardar agenda oficial das fases finais** para completar o CSV com 32-avos, oitavas, quartas, semifinais e 3º lugar.  
2. **Atualizar `data/copa2026_placeholders.json`** quando as repescagens definirem as seleções reais; executar novamente `scripts/atualizar_copa2026_dataset.py` para gerar o SQL final.  
3. **Reexecutar validações** (diferença de partidas, consistência de fases) antes de aplicar o script no banco.  
4. **Documentar a aplicação**: registrar em `.ia/logs/` o ambiente, data e confirmação após importação.

## 5. Situação
- **Carga completa:** pendente (aguardando dados oficiais).  
- **Script gerações parciais:** disponíveis para revisão (`data/sql/03-copa-2026-data.sql`).  
- **Ação recomendada:** revisitar quando a FIFA publicar o calendário final ou quando os placeholders forem definidos.

