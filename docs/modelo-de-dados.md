# Modelo de Dados Local

## Decisao inicial

Usar SQLite local para os dados reais do usuario no MVP.

Motivo:

- roda sem Docker;
- nao exige cloud;
- facilita backup manual;
- reduz risco de expor dados financeiros no GitHub;
- atende bem um usuario local no inicio.

## Arquivos locais

Sugestao:

```text
data/
  contas-termometro.db
  backups/
```

`data/` deve ficar no `.gitignore`.

## O que pode ir para o GitHub

- Codigo.
- Documentacao.
- Migrations.
- Arquivos `.example`.
- Seeds anonimos.
- Tests fixtures sem dados reais.

## O que nao pode ir para o GitHub

- Banco SQLite real.
- Exportacao da planilha real.
- Prints com valores reais.
- `.env`.
- Backups.
- Logs com payload financeiro.

## Tabelas iniciais sugeridas

### `ledger_entries`

Livro principal de lancamentos.

```text
id
type
description
amount
occurred_on
reference_month
category
notes
created_at
updated_at
```

Tipos:

- `INCOME`
- `FIXED_EXPENSE`
- `DAILY_EXPENSE`
- `INVESTMENT`
- `BALANCE_ADJUSTMENT`

### `monthly_targets`

Metas configuraveis por mes.

```text
id
reference_month
investment_target_rate
minimum_daily_budget
created_at
updated_at
```

### `purchase_simulations`

Historico opcional de simulacoes.

```text
id
reference_month
description
price
payment_mode
installments
decision
reason
created_at
```

## Sobre usar Git como banco

Nao usar GitHub como banco dos dados financeiros reais.

Git e bom para versionar texto e codigo. Para dados financeiros pessoais, ele cria riscos:

- historico e dificil de apagar completamente;
- um commit errado pode expor anos de informacao;
- conflitos de merge em dados sao ruins;
- repositorios remotos mudam o modelo de privacidade.

Se quiser versionar dados futuramente, considerar uma exportacao criptografada local e manual, nunca dados abertos.

