# Regras de Calculo

Este documento define as formulas iniciais. Se a planilha atual usar outra formula, a planilha vence ate que a regra seja revisada conscientemente.

## Tipos de lancamento

- `INCOME`: entrada de dinheiro.
- `FIXED_EXPENSE`: saida fixa.
- `DAILY_EXPENSE`: gasto diario/debito.
- `INVESTMENT`: economia, reserva ou investimento.
- `BALANCE_ADJUSTMENT`: ajuste manual de saldo para reconciliar com banco/carteira.

## Resumo mensal

Para um mes `M`:

```text
totalIncome = soma(INCOME)
fixedExpenseTotal = soma(FIXED_EXPENSE)
dailyExpenseTotal = soma(DAILY_EXPENSE)
investmentTotal = soma(INVESTMENT)
cashExpenseTotal = fixedExpenseTotal + dailyExpenseTotal
totalOutflow = fixedExpenseTotal + dailyExpenseTotal + investmentTotal
monthBalance = totalIncome - totalOutflow + soma(BALANCE_ADJUSTMENT)
```

## Economia mensal

Existem duas leituras uteis:

```text
investmentRateOnIncome = investmentTotal / totalIncome
freeCashAfterExpenses = totalIncome - fixedExpenseTotal - dailyExpenseTotal
```

`investmentRateOnIncome` mostra quanto do dinheiro que entrou foi investido.

`freeCashAfterExpenses` mostra sobra operacional antes de decidir investir, comprar ou deixar parado.

## Performance do mes

Performance inicial recomendada:

```text
targetInvestment = totalIncome * monthlyInvestmentTargetRate
investmentGap = investmentTotal - targetInvestment
performanceRate = investmentTotal / targetInvestment
```

Interpretacao:

- `performanceRate >= 1`: meta batida.
- `performanceRate entre 0.8 e 1`: perto da meta.
- `performanceRate < 0.8`: abaixo da meta.

## Diario

O diario representa gasto variavel do mes.

```text
dailyBudget = (totalIncome - fixedExpenseTotal - targetInvestment) / daysInMonth
dailyExpenseTotal = soma(DAILY_EXPENSE)
dailyRemaining = dailyBudget * dayOfMonth - dailyExpenseTotal
```

Essa regra permite comparar o gasto variavel acumulado com o limite esperado ate o dia atual.

## Resumo anual

Para um ano `Y`:

```text
annualIncome = soma(totalIncome dos meses)
annualInvestment = soma(investmentTotal dos meses)
annualInvestmentRate = annualInvestment / annualIncome
```

## Decisao de compra a vista

Entrada:

- preco da compra;
- mes da compra;
- meta mensal de investimento;
- resumo atual do mes.

Regra inicial:

```text
cashAfterPurchase = freeCashAfterExpenses - price
investmentAfterPurchase = investmentTotal
targetInvestment = totalIncome * monthlyInvestmentTargetRate
canBuyCash = cashAfterPurchase >= 0 and investmentAfterPurchase >= targetInvestment
```

Se a compra for paga usando dinheiro que iria para investimento:

```text
investmentAfterPurchase = investmentTotal - max(0, price - freeCashAfterExpenses)
canBuyCash = investmentAfterPurchase >= targetInvestment
```

A resposta deve explicar qual interpretacao foi usada.

## Decisao de parcelamento

Entrada:

- preco total;
- numero de parcelas;
- mes inicial;
- gastos fixos futuros conhecidos;
- meta mensal de investimento.

Regra inicial:

```text
installmentValue = price / installments
futureFixedExpenseTotal = fixedExpenseTotal + installmentValue
futureDailyBudget = (expectedIncome - futureFixedExpenseTotal - targetInvestment) / daysInMonth
canInstall = futureDailyBudget >= minimumDailyBudget
```

O sistema deve mostrar:

- valor da parcela;
- meses afetados;
- reducao no diario mensal;
- se a meta de investimento continua possivel.

## Regras de arredondamento

- Valores monetarios devem ter 2 casas decimais.
- Arredondamento padrao: `HALF_UP`.
- Percentuais podem ter 2 casas decimais.

