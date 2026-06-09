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

- `identity`: usuario, autenticacao e integracao futura com Firebase Auth.
- `setup`: renda mensal, contas fixas e preferencias financeiras.
- `budget`: calculo do orcamento diario e status do termometro.
- `advisor`: respostas para "posso comprar?" e simulacoes.
- `shared`: contratos e tipos compartilhados de baixo acoplamento.

## Rodando localmente

Pre-requisitos:

- JDK 21+

Com o Gradle Wrapper:

```powershell
.\gradlew.bat bootRun
```

Endpoint inicial:

```bash
curl http://localhost:8080/api/system/health
```

## Proximos passos

1. Implementar persistencia SQLite local.
2. Criar o modulo `ledger` para lancamentos financeiros.
3. Implementar resumo mensal.
4. Implementar simulador de compra no modulo `advisor`.
5. Evoluir metas de investimento depois que os calculos mensais estiverem confiaveis.

## Documentacao principal

- [Visao de produto](docs/produto.md)
- [Regras de calculo](docs/regras-de-calculo.md)
- [Modelo de dados local](docs/modelo-de-dados.md)
- [Roadmap](docs/roadmap.md)
- [Arquitetura](docs/arquitetura.md)
- [Frontend](docs/frontend.md)
