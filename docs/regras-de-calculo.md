# Regras de Calculo

Este documento define as formulas iniciais. Se a planilha atual usar outra formula, a planilha vence ate que a regra seja revisada conscientemente.

## Tipos de lancamento

- `ENTRADA`: entrada de dinheiro.
- `SAIDA_FIXA`: saida fixa.
- `GASTO_DIARIO`: gasto diario/debito.
- `INVESTIMENTO`: economia, reserva ou investimento.
- `AJUSTE_SALDO`: ajuste manual de saldo para reconciliar com banco/carteira.

## Resumo mensal

Para um mes `M`:

```text
totalEntradas = soma(ENTRADA)
totalSaidasFixas = soma(SAIDA_FIXA)
totalGastoDiario = soma(GASTO_DIARIO)
totalInvestido = soma(INVESTIMENTO)
saidaSemInvestimento = totalSaidasFixas + totalGastoDiario
saidaTotal = totalSaidasFixas + totalGastoDiario + totalInvestido
saldoMes = totalEntradas - saidaTotal + soma(AJUSTE_SALDO)
```

Os nomes internos, endpoints e documentos devem usar a linguagem publica em portugues.

## Economia mensal

Existem duas leituras uteis:

```text
percentualInvestidoSobreEntradas = totalInvestido / totalEntradas
sobraDepoisDeGastos = totalEntradas - totalSaidasFixas - totalGastoDiario
```

`percentualInvestidoSobreEntradas` mostra quanto do dinheiro que entrou foi investido.

`sobraDepoisDeGastos` mostra sobra operacional antes de decidir investir, comprar ou deixar parado.

## Performance do mes

Performance inicial recomendada:

```text
metaInvestimento = totalEntradas * percentualMetaInvestimentoMensal
diferencaInvestimento = totalInvestido - metaInvestimento
percentualPerformance = totalInvestido / metaInvestimento
```

Interpretacao:

- `percentualPerformance >= 1`: meta batida.
- `percentualPerformance entre 0.8 e 1`: perto da meta.
- `percentualPerformance < 0.8`: abaixo da meta.

## Diario

O diario representa gasto variavel do mes.

```text
orcamentoDiario = (totalEntradas - totalSaidasFixas - metaInvestimento) / diasDoMes
totalGastoDiario = soma(GASTO_DIARIO)
diarioRestante = orcamentoDiario * diaDoMes - totalGastoDiario
```

Essa regra permite comparar o gasto variavel acumulado com o limite esperado ate o dia atual.

## Resumo anual

Para um ano `Y`:

```text
entradasAnuais = soma(totalEntradas dos meses)
investimentoAnual = soma(totalInvestido dos meses)
percentualInvestidoAnual = investimentoAnual / entradasAnuais
```

## Decisao de compra a vista

Entrada:

- preco da compra;
- mes da compra;
- meta mensal de investimento;
- resumo atual do mes.

Regra inicial:

```text
sobraDepoisDaCompra = sobraDepoisDeGastos - valorCompra
investimentoDepoisDaCompra = totalInvestido
metaInvestimento = totalEntradas * percentualMetaInvestimentoMensal
podeComprarAVista = sobraDepoisDaCompra >= 0 and investimentoDepoisDaCompra >= metaInvestimento
```

Se a compra for paga usando dinheiro que iria para investimento:

```text
investimentoDepoisDaCompra = totalInvestido - max(0, valorCompra - sobraDepoisDeGastos)
podeComprarAVista = investimentoDepoisDaCompra >= metaInvestimento
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
valorParcela = valorCompra / parcelas
saidasFixasFuturas = totalSaidasFixas + valorParcela
orcamentoDiarioFuturo = (entradaEsperada - saidasFixasFuturas - metaInvestimento) / diasDoMes
podeParcelar = orcamentoDiarioFuturo >= orcamentoDiarioMinimo
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
