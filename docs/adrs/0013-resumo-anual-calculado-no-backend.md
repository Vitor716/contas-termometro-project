# ADR 0013 - Resumo anual calculado no backend

## Status

Proposta.

## Contexto

A visão anual atual consulta os 12 meses no frontend e agrega os resultados. Isso permitiu validar a experiência rapidamente, mas possui problemas:

- até 24 requisições quando são consultados lançamentos e resumos;
- fallback no JavaScript reproduz parte das regras financeiras;
- diferenças futuras entre cálculo mensal e anual;
- dificuldade para definir melhor e pior mês;
- ausência de contrato reutilizável por outros clientes.

## Decisão proposta

O módulo `orcamento` deve fornecer:

```text
GET /api/anos/{ano}/resumo
```

O backend reutiliza a calculadora mensal aprovada pela ADR 0012 para gerar os 12 meses.

Meses sem lançamentos devem aparecer com valores zerados.

## Resposta recomendada

```text
ano
totalEntradas
totalSaidasOperacionais
totalInvestido
saldoAcumulado
percentualInvestidoAnual
mediaMensalEntradas
mediaMensalSaidas
mediaMensalInvestida
melhorMes
piorMes
mesesComMovimentacao
meses[12]
```

## Melhor e pior mês

Não usar apenas maior saldo absoluto.

Primeira regra:

1. comparar `performanceContraMeta`;
2. em empate, comparar `saldoMes`;
3. em novo empate, preferir maior `percentualInvestido`.

Meses sem entrada e sem movimentação não participam do ranking.

## Média mensal

O DTO deve expor duas médias quando útil:

- média calendário: total dividido por 12;
- média ativa: total dividido por meses com movimentação.

Isso evita esconder meses vazios ou distorcer anos incompletos.

## Frontend

Após o endpoint existir:

- remover agregação financeira e fallback do JavaScript;
- manter apenas apresentação, navegação e estado;
- realizar uma requisição por ano;
- apresentar erro se o contrato anual falhar, sem recalcular regra no navegador.

## Testes obrigatórios

- ano vazio;
- ano com um mês;
- doze meses;
- meses sem entrada;
- ranking com empate;
- soma anual igual à soma dos resumos mensais;
- percentuais calculados sobre totais anuais, não média dos percentuais.

## Consequências

- Regra anual fica centralizada.
- O frontend fica menor e previsível.
- O endpoint depende da estabilização do resumo mensal.
