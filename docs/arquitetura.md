# Arquitetura

## Estilo

O projeto comeca como monolito modular. A decisao reduz custo operacional e evita separar servicos antes de haver maturidade de dominio.

## Boundaries

- `identidade`: autenticacao, dono dos dados e integracao futura com Firebase Auth.
- `lancamentos`: livro de lancamentos financeiros. Sera o modulo central do MVP.
- `importacao`: leitura de arquivos externos, pre-visualizacao e confirmacao antes de gravar.
- `configuracao`: preferencias, categorias e metas.
- `orcamento`: calculos diarios, status do termometro e resumos mensais/anuais.
- `consultor`: simulacoes e recomendacoes.
- `compartilhado`: tipos pequenos e estaveis, sem regra de negocio.

## Regra de dependencia

Controllers chamam services do proprio modulo. Modulos nao acessam entidades internas de outros modulos.

Quando um modulo precisar reagir a outro, comece com chamada de aplicacao simples dentro do monolito. Pub/Sub, filas ou eventos externos ficam para quando houver necessidade operacional clara.

## Persistencia

O MVP deve usar SQLite local, com o arquivo real em `data/contas-termometro.db`.

O diretorio `data/` nao deve ser versionado. Migrations, exemplos anonimos e fixtures de teste podem ficar no repositorio.

## Fluxo inicial

```text
POST /api/lancamentos
        |
        v
lancamentos salva lancamento local
        |
        v
GET /api/meses/{yyyy-MM}/resumo
        |
        v
orcamento calcula resumo mensal sob demanda
        |
        v
consultor usa o resumo para simular compras
```

## Decisao importante

Nao criar integracao cloud antes de ter o core funcionando localmente.

O projeto precisa primeiro provar que:

- os lancamentos representam bem a planilha;
- os resumos batem com os calculos atuais;
- o consultor explica decisoes de compra de forma util;
- os dados reais nao vazam para Git.

## Frontend

O frontend inicial deve ficar dentro do mesmo projeto Spring Boot, servido como arquivos estaticos ou templates simples.

Frameworks como Angular, Vue, React ou Next.js ficam fora do MVP. Eles podem ser revisitados se a interface crescer a ponto de exigir estado complexo, roteamento client-side ou componentes muito interativos.
