# Contas Termometro Project

API Kotlin para controle financeiro pessoal baseado em temperatura do orcamento diario.

Este repositorio nasce como um molde de projeto real, ainda simples, mas com decisoes explicitas para evoluir sem depender de infraestrutura paga no inicio.

## Decisoes iniciais

- Backend: Kotlin 2.2, Java 21 e Spring Boot 4.
- Arquitetura: monolito modular por pacotes de negocio.
- Primeiro modo de execucao: local-first, sem cloud obrigatoria.
- Persistencia: ainda nao escolhida no codigo. A decisao recomendada esta documentada em `docs/adrs/0002-persistencia-local-e-free-tier.md`.
- Firebase: candidato para Auth/hosting/free tier, mas nao deve entrar antes de existir necessidade real de autenticacao ou sincronizacao.

## Modulos previstos

- `identity`: usuario, autenticacao e integracao futura com Firebase Auth.
- `setup`: renda mensal, contas fixas e preferencias financeiras.
- `budget`: calculo do orcamento diario e status do termometro.
- `advisor`: respostas para "posso comprar?" e simulacoes.
- `shared`: contratos e tipos compartilhados de baixo acoplamento.

## Rodando localmente

Pre-requisitos:

- JDK 21+
- Gradle instalado, ou adicionar Gradle Wrapper em um proximo passo

Com Gradle instalado:

```bash
gradle bootRun
```

Endpoint inicial:

```bash
curl http://localhost:8080/api/system/health
```

## Proximos passos

1. Adicionar Gradle Wrapper.
2. Escolher persistencia do MVP: PostgreSQL local com Docker ou SQLite local.
3. Implementar MVP 1: setup de renda/contas fixas e consulta de budget diario.
4. Decidir Firebase Auth somente quando houver frontend ou sincronizacao entre maquinas.

