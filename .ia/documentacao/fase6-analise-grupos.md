# Fase 6 — Análise das Chaves da Copa do Mundo 2026

## 1. Objetivo
Registrar o panorama atualizado das chaves da Copa 2026 para subsidiar as próximas atividades da Fase 6 (planejamento funcional e atualização visual). O foco é alinhar o dataset interno `data/copa2026_tabela_brt.csv` com as informações oficiais divulgadas após o sorteio final.

## 2. Fontes Consultadas
- Dataset interno consolidado em 21/02/2026: `data/copa2026_tabela_brt.csv` (89 partidas mapeadas em horário de Brasília).
- Comunicado oficial da FIFA com o resultado do sorteio final das 12 chaves (05/12/2025).  
- Coberturas independentes que reiteram o layout das chaves e os seis playoffs pendentes (dezembro/2025 – fevereiro/2026).

## 3. Resumo das Chaves (Situação em 21/02/2026)
`*` indica vaga ainda em aberto aguardando playoffs.

| Grupo | Seleções |
|-------|----------|
| **A** | México (host), Coreia do Sul, África do Sul, UEFA Playoff D* |
| **B** | Canadá (host), Suíça, Catar, UEFA Playoff A* |
| **C** | Brasil, Marrocos, Escócia, Haiti |
| **D** | Estados Unidos (host), Austrália, Paraguai, UEFA Playoff C* |
| **E** | Alemanha, Equador, Costa do Marfim, Curaçao |
| **F** | Holanda, Japão, Tunísia, UEFA Playoff B* |
| **G** | Bélgica, Egito, Irã, Nova Zelândia |
| **H** | Espanha, Cabo Verde, Arábia Saudita, Uruguai |
| **I** | França, Senegal, Noruega, Vaga Playoff 2 (Intercontinental)* |
| **J** | Argentina, Áustria, Argélia, Jordânia |
| **K** | Portugal, Colômbia, Uzbequistão, Vaga Playoff 1 (Intercontinental)* |
| **L** | Inglaterra, Croácia, Gana, Panamá |

**Slots pendentes:**
- UEFA Playoff A: Itália, Irlanda do Norte, País de Gales ou Bósnia e Herzegovina.
- UEFA Playoff B: Ucrânia, Suécia, Polônia ou Albânia.
- UEFA Playoff C: Turquia, Romênia, Eslováquia ou Kosovo.
- UEFA Playoff D: Dinamarca, Macedônia do Norte, Tchéquia ou Irlanda.
- Playoff Intercontinental 1: República Democrática do Congo, Jamaica ou Nova Caledônia.
- Playoff Intercontinental 2: Iraque, Bolívia ou Suriname.

## 4. Diagnóstico do Dataset Interno
- **Cobertura atual:** 72 partidas da fase de grupos + 16 jogos do Round of 32 + final (total 89). Faltam confrontos intermediários (Round of 16, quartas, semifinais e 3º lugar).  
- **Lacunas de participantes:** As 6 vagas restantes (4 da repescagem UEFA, 2 do torneio intercontinental) aparecem como placeholders.  
- **Conferência de datas:** Datas e sedes dos jogos de abertura e grupos A–L estão alinhadas com o calendário divulgado até o momento.  
- **Risco de divergência:** FIFA confirmou a grade inicial de horários e sedes em **06/12/2025**; eventuais revisões posteriores precisam ser monitoradas para ajustar o dataset.

## 5. Recomendações Imediatas
1. **Manter placeholders claros** (`UEFA Playoff A`, `Playoff 1`) até a definição oficial em março/2026.  
2. **Preparar script incremental** para atualizar o CSV assim que os playoffs forem concluídos (reutilizar o pipeline já usado na consolidação inicial).  
3. **Planejar ampliação do dataset** para incluir Round of 16, quartas, semifinais e disputa de 3º lugar assim que a FIFA liberar os horários definitivos.  
4. **Derivar artefatos de negócio** (tabelas e seeds do banco) a partir desta versão para garantir consistência com as telas de classificação e agenda.

## 6. Implicações para o Sistema
- **Domínio:** o bolão cobre todo o torneio masculino da FIFA 2026, público-alvo torcedores brasileiros.  
- **Atualizações futuras:** telas de palpites e classificação precisam aceitar 12 grupos e o novo Round of 32.  
- **Dependências entre tarefas:** Elaborar o plano detalhado da Fase 6 (tarefa 2) deverá usar esse diagnóstico como referência para priorizar alterações de dados e UX.
