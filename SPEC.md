# SPEC - Contas Termometro

## Visao

Sistema de controle financeiro pessoal baseado em temperatura do orcamento diario.

O usuario cadastra renda e contas fixas. O sistema calcula quanto pode gastar por dia, acompanha gastos reais e redistribui saldo nao usado entre os dias restantes do mes.

## MVP 1

Objetivo: permitir cadastrar renda, contas fixas e consultar o orcamento diario basico.

Entregaveis:

- `POST /api/setup/income`
- `POST /api/setup/bills`
- `GET /api/budget/today`
- `GET /api/budget/month`
- Calculadora de orcamento diario sem rollover
- Testes unitarios da calculadora

Criterio de aceite:

- Com renda de R$ 5.000, contas fixas de R$ 2.000 e mes de 30 dias, o budget diario deve ser R$ 100.

## MVP 2

Objetivo: rollover diario.

- Fechamento manual de dia
- Fechamento automatico diario
- Redistribuicao do saldo nao gasto
- Status `HEALTHY`, `WARNING`, `CRITICAL`, `EXCEEDED`

## MVP 3

Objetivo: analytics mensal.

- Resumo por categoria
- Total gasto
- Projecao de sobra
- Comparativo mensal

## MVP 4

Objetivo: advisor financeiro.

- `POST /api/advisor/check`
- Compra a vista
- Compra parcelada
- Tempo estimado para juntar dinheiro

