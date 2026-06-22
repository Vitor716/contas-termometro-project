# ADR 0012 - Semântica oficial dos resumos financeiros

## Status

Proposta para substituir ambiguidades da implementação atual.

## Contexto

O resumo mensal existente diverge de `docs/regras-de-calculo.md`:

- `saidaTotal` não inclui investimento;
- `saldoMes` não inclui investimento nem ajuste de saldo;
- porcentagem investida retorna escala `0..100`, enquanto partes da documentação usam `0..1`;
- `performanceContraMeta` é diferença de pontos percentuais, mas a documentação também descreve razão contra a meta;
- gasto esperado até hoje não multiplica pelo dia do mês;
- entrada zero pode causar divisão por zero;
- meta está fixa no código apesar da tabela `metas_mensais`.

Sem uma semântica única, frontend, visão anual e consultor podem interpretar o mesmo campo de maneiras diferentes.

## Decisão proposta

### Escala de percentuais

No domínio e na API, percentuais serão decimais entre `0` e `1`.

Exemplos:

```text
0.15 = 15%
1.00 = 100%
```

O frontend é responsável apenas pela formatação visual.

### Fluxo de caixa

```text
saidaOperacional = saidasFixas + gastoDiario
saidaTotal = saidaOperacional + totalInvestido
saldoMes = entradas - saidaTotal + ajustes
sobraAntesDeInvestir = entradas - saidaOperacional + ajustes
```

Investimento é saída de caixa, mas deve permanecer destacado como formação de patrimônio.

### Meta

```text
metaInvestimentoValor = entradas * percentualMetaInvestimento
percentualInvestido = entradas > 0 ? totalInvestido / entradas : 0
performanceContraMeta = metaInvestimentoValor > 0
    ? totalInvestido / metaInvestimentoValor
    : 0
diferencaMeta = totalInvestido - metaInvestimentoValor
```

Interpretação:

- performance `>= 1`: meta atingida;
- performance entre `0.8` e `1`: próxima da meta;
- performance `< 0.8`: abaixo da meta.

### Orçamento diário

```text
limiteMensalDiario =
    entradas
    - saidasFixas
    - metaInvestimentoValor
    + ajustes

orcamentoDiario = limiteMensalDiario / diasDoMes
esperadoAteHoje = orcamentoDiario * diaAvaliacao
restanteNoMes = limiteMensalDiario - gastoDiario
diferencaAteHoje = esperadoAteHoje - gastoDiario
```

`restanteNoMes` e `diferencaAteHoje` são conceitos distintos e devem possuir campos diferentes.

### Datas

- mês atual usa o dia corrente;
- mês histórico usa o último dia;
- mês futuro usa dia `0` para realizado e pode ter projeção separada.

### Meta persistida

Buscar a meta vigente em `metas_mensais`. Na ausência, usar configuração padrão explícita, inicialmente `20%`, e informar a origem da meta no DTO.

## DTO recomendado

```text
mesReferencia
diaAvaliacao
diasDoMes
totalEntradas
totalSaidasFixas
totalGastoDiario
totalInvestido
ajustes
saidaOperacional
saidaTotal
saldoMes
sobraAntesDeInvestir
percentualInvestido
percentualMetaInvestimento
metaInvestimentoValor
diferencaMeta
performanceContraMeta
orcamentoDiario
esperadoAteHoje
diferencaAteHoje
restanteNoMes
origemMeta
```

## Migração

Alterar nomes ou semântica dos campos atuais de uma vez, antes que existam consumidores externos. Não manter campos ambíguos apenas por compatibilidade interna.

## Testes obrigatórios

- mês normal;
- entrada zero;
- apenas ajuste;
- déficit;
- investimento acima da meta;
- meta zero;
- fevereiro bissexto;
- mês histórico;
- arredondamento monetário;
- lançamentos cancelados ignorados.

## Consequências

- O frontend precisará ajustar nomes e escalas.
- A visão anual poderá agregar campos sem reinterpretá-los.
- O consultor passa a usar valores estáveis e reproduzíveis.
