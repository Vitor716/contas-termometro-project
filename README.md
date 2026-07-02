# Contas Termometro Project

API Kotlin para controle financeiro pessoal baseado em temperatura do orcamento diario.

Este repositorio nasce como um molde de projeto real, ainda simples, mas com decisoes explicitas para evoluir sem depender de infraestrutura paga no inicio.

## Decisoes iniciais

- Backend: Kotlin 2.2, Java 21 e Spring Boot 4.
- Arquitetura: monolito modular por pacotes de negocio.
- Primeiro modo de execucao: local-first, sem cloud obrigatoria e sem dados pessoais versionados.
- Persistencia recomendada para o MVP: arquivo local SQLite fora do Git.
- GitHub: usado para versionar codigo, documentacao, migrations e exemplos anonimos. Nao sera usado como banco de dados financeiro.
- Firebase: fica adiado. Pode entrar no futuro para Auth ou frontend, mas nao e dependencia do core.
- Frontend inicial: simples, servido pelo proprio Spring Boot, sem Angular/Vue/React no MVP.

## Modulos previstos

- `identidade`: usuario, autenticacao e integracao futura com Firebase Auth.
- `lancamentos`: entradas, saidas fixas, gastos diarios, investimentos e ajustes de saldo.
- `importacao`: leitura e pre-visualizacao de CSVs antes de gravar lancamentos.
- `configuracao`: categorias, metas e preferencias financeiras.
- `orcamento`: resumo mensal/anual, calculos do termometro e performance.
- `consultor`: respostas para "posso comprar?" e simulacoes.
- `compartilhado`: contratos e tipos compartilhados de baixo acoplamento.

## Uso rapido no Windows

Modo mais simples, para usar como um link:

```powershell
.\instalar-link.bat
```

Esse comando gera a aplicacao, cria o atalho `Contas Termometro` na area de trabalho e configura o Windows para iniciar o servidor local quando voce entrar no sistema.

Depois disso, basta clicar em `Contas Termometro.url`. O link usa o protocolo local:

```text
contas-termometro://abrir
```

Na primeira vez, o navegador pode pedir confirmacao para abrir o app local. Confirme para iniciar o servidor e abrir a interface.

Com o servidor ja ativo, tambem funciona abrir direto:

```text
http://localhost:17321
```

Primeira configuracao:

```powershell
.\configurar.bat
```

Abrir a aplicacao:

```powershell
.\usar.bat
```

Ver se esta rodando e qual banco esta usando:

```powershell
.\status.bat
```

Parar a aplicacao:

```powershell
.\parar.bat
```

Remover o inicio automatico:

```powershell
.\desinstalar-link.bat
```

Enviar alteracoes de codigo para o GitHub:

```powershell
.\subir-codigo.bat -Mensagem "descreva a alteracao"
```

O script de envio roda testes, prepara os arquivos, bloqueia dados locais sensiveis (`.db`, `.ctbackup`, `logs`, `data`, `backups-automaticos`) e faz `commit` + `push` na branch atual.

## Rodando localmente manualmente

Pre-requisitos:

- JDK 21+

Com o Gradle Wrapper:

```powershell
.\gradlew.bat bootRun
```

Use esse modo quando quiser ver os logs da aplicacao no terminal.

Endpoint inicial:

```bash
curl http://localhost:17321/api/sistema/saude
```

Interface local:

```text
http://localhost:17321
```

## Proximos passos

1. Implementar persistencia SQLite local.
2. Criar o modulo `lancamentos` para lancamentos financeiros.
3. Implementar resumo mensal.
4. Implementar simulador de compra no modulo `consultor`.
5. Evoluir metas de investimento depois que os calculos mensais estiverem confiaveis.

## Documentacao principal

- [Visao de produto](docs/produto.md)
- [Regras de calculo](docs/regras-de-calculo.md)
- [Modelo de dados local](docs/modelo-de-dados.md)
- [Roadmap](docs/roadmap.md)
- [Arquitetura](docs/arquitetura.md)
- [Frontend](docs/frontend.md)
- [Motor de análise financeira](docs/analise-financeira.md)
- [Importação inteligente](docs/importacao-inteligente.md)
- [Setup local no Windows](docs/setup-windows.md)
- [Backup, versionamento e sincronização](docs/backup-e-sincronizacao.md)
- [Índice de decisões arquiteturais](docs/adrs/README.md)
