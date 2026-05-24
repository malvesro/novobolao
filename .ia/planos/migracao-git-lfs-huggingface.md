# Plano [PLN-001]: Migração Estruturada para Git LFS e Reescrita de Histórico

Este plano visa resolver permanentemente a rejeição de arquivos binários pelo Hugging Face, convertendo imagens e recursos pesados em ponteiros LFS, inclusive no histórico da branch de deploy.

## 🎯 Objetivo
Transformar todos os arquivos binários (`.png`, `.jpg`, `.wav`, `.jar`, etc.) em objetos LFS e limpar o histórico do Git local para reduzir o tamanho do upload e cumprir as políticas do Hugging Face Spaces.

## 🛠️ Ferramentas Necessárias
- Git LFS (deve estar instalado no SO: `sudo apt install git-lfs`)

## 📋 Iterações e Subtarefas

### Iteração 1: Preparação do Ambiente e Repositório
- [ ] 1.1. Validar instalação do Git LFS no sistema (`git lfs version`).
- [ ] 1.2. Inicializar o LFS no repositório (`git lfs install`).
- [ ] 1.3. Sincronizar estado da `branch-limpa` com os arquivos restaurados da `nuvem`.

### Iteração 2: Configuração e Rastreamento (Tracking)
- [ ] 2.1. Configurar extensões para LFS no `.gitattributes`.
- [ ] 2.2. Validar que as imagens do sistema (`webapp/img/`) estão marcadas para LFS.

### Iteração 3: Reescrita de Histórico (Migração)
- [ ] 3.1. Executar `git lfs migrate import --everything --include="..."` para converter binários antigos em ponteiros LFS.
- [ ] 3.2. Verificar a criação de novos commits reescritos.

### Iteração 4: Higienização Profunda (Deep Clean)
- [ ] 4.1. Expirar reflogs para desvincular objetos binários antigos (`git reflog expire`).
- [ ] 4.2. Executar Garbage Collection agressiva (`git gc --prune=now --aggressive`).

### Iteração 5: Validação Final e Push
- [ ] 5.1. Listar arquivos rastreados pelo LFS para conferência (`git lfs ls-files`).
- [ ] 5.2. Realizar o push forçado para o Hugging Face (`git push nuvem branch-limpa:main -f`).

## ⚠️ Considerações de Arquitetura
A reescrita de histórico via `migrate import` criará novos hashes de commit. Isso é seguro na `branch-limpa` pois ela foi criada especificamente para este deploy.
