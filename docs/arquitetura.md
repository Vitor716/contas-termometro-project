# Arquitetura

## Estilo

O projeto comeca como monolito modular. A decisao reduz custo operacional e evita separar servicos antes de haver maturidade de dominio.

## Boundaries

- `identity`: autenticacao, dono dos dados e integracao futura com Firebase Auth.
- `setup`: renda, contas fixas, preferencias e metas.
- `budget`: calculos diarios, status do termometro e fechamento de dia.
- `advisor`: simulacoes e recomendacoes.
- `shared`: tipos pequenos e estaveis, sem regra de negocio.

## Regra de dependencia

Controllers chamam services do proprio modulo. Modulos nao acessam entidades internas de outros modulos.

Quando um modulo precisar reagir a outro, comece com chamada de aplicacao simples dentro do monolito. Pub/Sub, filas ou eventos externos ficam para quando houver necessidade operacional clara.

