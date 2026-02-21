Ponto de Parada – Fase 6 (Tabela Copa 2026)

  - Consolidamos os arquivos fornecidos e extraímos 89 partidas (fase de grupos + confrontos já definidos) com
    horários no fuso de Brasília, gerando data/copa2026_tabela_brt.csv.
  - O CSV mantém placeholders para seleções vindas de repescagens (ex.: DEN/MKD/CZE/IRL) e inclui apenas as
    fases cuja programação horária estava disponível (grupos + 32-avos e final). Quartas, semis e disputa de 3º
    lugar carecem de data/horário oficiais.
  - Documentação atualizada em .ia/documentacao/README-migracao.md (seção 4.8) e log registrado em .ia/logs/
    session-20260221-fase6-grupos-csv.md.

  Próximos Passos ao Retomar

  1. Aguardar / encontrar fonte oficial com horários definitivos das oitavas em diante para completar o CSV.
  2. Assim que repescagens definirem os classificados, substituir os placeholders por seleções reais.
  3. A partir do CSV finalizado, gerar o script 03-copa-2026-data.sql (ou outro formato) e atualizar as telas/
     documentação correspondentes.
