# Módulo de análise financeira

## Objetivo

O módulo `consultor` transforma os cálculos do orçamento em recomendações reproduzíveis. Ele não oferece aconselhamento de investimento e não tenta prever o mercado. Sua função é responder:

> Esta compra cabe na minha vida financeira sem prejudicar metas, reserva e compromissos futuros?

## Princípios

- Determinístico: os mesmos dados e configurações produzem o mesmo resultado.
- Explicável: cada decisão lista regras, valores e projeções.
- Conservador por padrão: dinheiro investido e reserva não são tratados como saldo livre.
- Configurável: limites pertencem ao usuário, não ficam escondidos no código.
- Simulação não persiste lançamentos reais.
- IA é uma camada de linguagem opcional, nunca a fonte da decisão.

## Entradas

### Situação financeira

- resumo do mês atual;
- lançamentos confirmados;
- parcelas ativas e futuras;
- saídas fixas conhecidas;
- entradas recorrentes confirmadas;
- saldo disponível;
- reserva financeira protegida;
- meta de investimento mensal e anual.

### Compra

- descrição;
- valor total;
- desconto à vista;
- quantidade de parcelas oferecida;
- juros e custo efetivo, quando informados;
- mês da primeira parcela;
- prioridade: essencial, planejada ou discricionária.

### Configuração

- percentual mínimo de investimento;
- valor mínimo de reserva intocável;
- orçamento diário mínimo;
- percentual máximo de renda comprometida;
- margem de segurança;
- quantidade máxima de meses projetados;
- estratégia `CONSERVADORA`, `EQUILIBRADA` ou `FLEXIVEL`.

## Pipeline de decisão

```text
validar entrada
    |
    v
montar cenário-base
    |
    v
gerar alternativas de pagamento
    |
    v
projetar cada mês afetado
    |
    v
aplicar regras obrigatórias
    |
    v
calcular indicadores e riscos
    |
    v
classificar alternativas
    |
    v
selecionar recomendação
    |
    v
gerar explicação auditável
    |
    +--> explicação opcional por IA local
```

## Cenário-base

Antes de simular a compra, construir a situação sem a compra para cada mês projetado:

```text
entradaProjetada
saidasFixasProjetadas
parcelasExistentes
metaInvestimento
gastoDiarioPlanejado
reservaProtegida
saldoProjetado
```

Dados confirmados têm prioridade sobre inferências. Para meses sem entrada conhecida, usar uma política explícita:

- conservadora: menor entrada recorrente dos últimos meses;
- equilibrada: mediana das entradas recorrentes;
- flexível: média, com margem de segurança.

Não projetar renda extra não recorrente como garantida.

## Alternativas avaliadas

- não comprar agora;
- comprar à vista;
- parcelar de `2` até `N` vezes;
- adiar para outro mês;
- reduzir o valor até um limite seguro.

Cada alternativa produz uma lista de `ProjecaoMensal`.

## Indicadores

### Comprometimento de renda

```text
comprometimento =
    (saidasFixas + parcelasExistentes + novaParcela)
    / entradaProjetada
```

Comparar com `percentualMaximoRendaComprometida`.

### Margem depois da meta

```text
margemDepoisDaMeta =
    entradaProjetada
    - saidasFixas
    - parcelas
    - gastoDiarioPlanejado
    - metaInvestimento
```

### Cobertura da reserva

```text
reservaDepoisDaCompra =
    reservaAtual - usoNecessarioDaReserva
```

A reserva protegida é uma restrição obrigatória, salvo simulação explicitamente marcada como emergência.

### Impacto na meta

```text
percentualMetaMantida =
    investimentoPossivelDepoisDaCompra / metaInvestimento
```

### Recuperação

Número de meses necessários para repor:

- valor retirado da reserva;
- investimento adiado;
- margem de segurança consumida.

## Regras obrigatórias

Uma alternativa deve ser `NAO_RECOMENDADA` quando qualquer condição crítica ocorrer:

- saldo projetado negativo;
- reserva abaixo do mínimo;
- orçamento diário abaixo do mínimo;
- comprometimento acima do teto rígido;
- parcela ultrapassa mês sem entrada confiável;
- meta fica inviável além da tolerância configurada.

Uma alternativa pode ser `ATENCAO` quando:

- preserva as restrições, mas reduz a meta;
- deixa margem pequena;
- depende de entrada projetada, não confirmada;
- aumenta significativamente o comprometimento;
- recuperação excede o prazo aceitável.

`OK` exige que todas as restrições sejam mantidas com margem.

## Comparação à vista versus parcelado

À vista recebe preferência quando:

- não toca a reserva;
- mantém meta e orçamento diário;
- o desconto é financeiramente relevante;
- a margem restante permanece segura.

Parcelado recebe preferência quando:

- preserva liquidez sem exceder limites futuros;
- não possui juros ou o custo adicional é aceitável;
- todas as parcelas cabem nas projeções;
- a compra à vista consumiria margem necessária.

O sistema deve explicar o custo da escolha. Parcelar apenas para reduzir o impacto visual do mês não é justificativa suficiente.

## Valor máximo seguro

Encontrar por busca incremental ou binária o maior valor que mantém a alternativa dentro das regras. Calcular separadamente:

- máximo à vista;
- máximo por quantidade de parcelas;
- máximo sem reduzir a meta;
- máximo usando a tolerância configurada.

## Pontuação

A classificação final deve vir primeiro das regras obrigatórias. Uma pontuação serve apenas para ordenar alternativas que passaram pelas mesmas restrições.

Exemplo:

```text
score = 100
score -= penalidadeMeta
score -= penalidadeComprometimento
score -= penalidadeOrcamentoDiario
score -= penalidadeIncerteza
score -= penalidadePrazoRecuperacao
score += beneficioDesconto
```

Pesos devem ficar configurados e cobertos por testes. Não esconder uma reprovação crítica dentro de uma média.

## Saída esperada

```text
decisao
alternativaRecomendada
valorMaximoSeguro
quantidadeMaximaParcelas
impactoMesAtual
projecoesMensais[]
metaAntes
metaDepois
reservaAntes
reservaDepois
comprometimentoAntes
comprometimentoDepois
orcamentoDiarioAntes
orcamentoDiarioDepois
mesesParaRecuperacao
regrasAcionadas[]
alertas[]
explicacaoDeterministica
```

## IA local

Integração opcional via porta/adaptador:

```text
ExplicadorDecisao
  |- ExplicadorPorTemplate
  `- ExplicadorOllama
```

Enviar ao modelo somente o resultado calculado, preferencialmente anonimizado. Exigir resposta estruturada contendo:

- resumo;
- principais riscos;
- comparação das alternativas;
- próximos passos.

Nunca enviar credenciais, CSV bruto, observações pessoais ou banco inteiro.

## Testes mínimos

- compra à vista saudável;
- compra à vista que toca a reserva;
- parcelamento saudável;
- parcela que cabe hoje, mas quebra mês futuro;
- renda zero;
- meta ainda não atingida;
- comprometimento exatamente no limite;
- desconto à vista menor que custo de perda de liquidez;
- recuperação curta e longa;
- indisponibilidade da IA;
- resposta inválida da IA sem alterar a decisão.
