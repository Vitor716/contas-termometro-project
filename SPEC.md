# SPEC - Contas Termometro

## Visao

Sistema de controle financeiro pessoal baseado em temperatura do orcamento diario.

O usuario registra entradas, saidas fixas, gastos diarios, investimentos e saldo. O sistema calcula a performance do mes e ajuda a responder perguntas praticas como:

- Posso comprar algo agora sem prejudicar minha economia?
- Faz mais sentido pagar a vista ou parcelar?
- Minha porcentagem investida no mes esta saudavel?
- Como esta meu desempenho anual de entradas e economia?

O projeto nao deve depender de cloud para funcionar. Dados financeiros reais devem ficar locais e fora do Git.

## MVP 1

Objetivo: substituir a planilha atual com um lancador local e resumo mensal confiavel.

Entregaveis:

- `POST /api/lancamentos`
- `GET /api/meses/{yyyy-MM}/resumo`
- Tipos de lancamento: `ENTRADA`, `SAIDA_FIXA`, `GASTO_DIARIO`, `INVESTIMENTO`, `AJUSTE_SALDO`
- Calculo de entradas, saidas fixas, gasto diario, saida total, saldo e performance do mes
- Calculo de economia/investimento mensal
- Testes unitarios dos calculos
- Importacao CSV fica para MVP proprio, depois do fluxo manual estar confiavel

Criterio de aceite:

- Dado um conjunto de lancamentos de um mes, o resumo deve bater com a planilha atual.

## MVP 2

Objetivo: simulador de decisao de compra.

- Compra a vista
- Compra parcelada
- Impacto na economia do mes
- Impacto nos meses futuros
- Recomendacao explicavel, sem "magica"

## MVP 3

Objetivo: visao anual.

- Total de entradas do ano
- Total economizado/investido no ano
- Porcentagem investida anual
- Evolucao mensal

## MVP 4

Objetivo: metas de investimento.

- Metas mensais
- Metas anuais
- Projecao de recuperacao apos compras maiores
- Historico de performance contra meta
