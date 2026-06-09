# Arquitetura

## Estilo

O projeto comeca como monolito modular. A decisao reduz custo operacional e evita separar servicos antes de haver maturidade de dominio.

## Boundaries

- `identity`: autenticacao, dono dos dados e integracao futura com Firebase Auth.
- `ledger`: livro de lancamentos financeiros. Sera o modulo central do MVP.
- `setup`: preferencias, categorias e metas.
- `budget`: calculos diarios, status do termometro e resumos mensais/anuais.
- `advisor`: simulacoes e recomendacoes.
- `shared`: tipos pequenos e estaveis, sem regra de negocio.

## Regra de dependencia

Controllers chamam services do proprio modulo. Modulos nao acessam entidades internas de outros modulos.

Quando um modulo precisar reagir a outro, comece com chamada de aplicacao simples dentro do monolito. Pub/Sub, filas ou eventos externos ficam para quando houver necessidade operacional clara.

## Persistencia

O MVP deve usar SQLite local, com o arquivo real em `data/contas-termometro.db`.

O diretorio `data/` nao deve ser versionado. Migrations, exemplos anonimos e fixtures de teste podem ficar no repositorio.

## Fluxo inicial

```text
POST /api/ledger/entries
        |
        v
ledger salva lancamento local
        |
        v
GET /api/months/{yyyy-MM}/summary
        |
        v
budget calcula resumo mensal sob demanda
        |
        v
advisor usa o resumo para simular compras
```

## Decisao importante

Nao criar integracao cloud antes de ter o core funcionando localmente.

O projeto precisa primeiro provar que:

- os lancamentos representam bem a planilha;
- os resumos batem com os calculos atuais;
- o advisor explica decisoes de compra de forma util;
- os dados reais nao vazam para Git.
