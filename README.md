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

## Rodando localmente

Pre-requisitos:

- JDK 21+

Com o Gradle Wrapper:

```powershell
.\gradlew.bat bootRun
```

Endpoint inicial:

```bash
curl http://localhost:8081/api/sistema/saude
```

Interface local:

```text
http://localhost:8081
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
