# ADR 0001 - Monolito modular local-first

## Status

Aceita.

## Contexto

O projeto deve sair do formato de estudo sem criar custo de infraestrutura ou complexidade de microsservicos.

## Decisao

Comecar com uma API Kotlin/Spring Boot em monolito modular.

## Consequencias

- Deploy e execucao local continuam simples.
- Os modulos preservam fronteiras de negocio.
- Uma extracao futura para servicos externos continua possivel, mas nao e objetivo do MVP.

