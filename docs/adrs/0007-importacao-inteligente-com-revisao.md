# ADR 0007 - Importação inteligente com revisão humana

## Status

Aceita como melhoria futura.

## Contexto

A importação básica de CSV reduz digitação, mas o valor real está em reconhecer parcelas, recorrências e duplicidades sem corromper o livro financeiro. Classificações automáticas erradas afetam resumos e decisões de compra.

## Decisão

A evolução da importação será um MVP próprio e seguirá duas fases:

1. pré-visualização e geração de sugestões;
2. confirmação explícita antes de alterar lançamentos existentes ou persistir classificações aprendidas.

O sistema poderá:

- extrair parcela atual e total de parcelas quando o CSV fornecer essa informação;
- agrupar parcelas pertencentes à mesma compra;
- projetar parcelas futuras;
- detectar recorrências e sugerir `SAIDA_FIXA`;
- sugerir categoria com base no histórico confirmado;
- detectar duplicidades dentro do mês e entre importações;
- aprender regras locais somente depois da confirmação do usuário.

## Duplicidade

Usar mais de uma camada:

### Identidade da origem

- `provedor`;
- identificador da mensagem ou arquivo;
- hash SHA-256 dos bytes do arquivo;
- número ou hash da linha normalizada.

O mesmo arquivo não pode ser processado duas vezes, salvo operação explícita de reprocessamento.

### Impressão digital do lançamento

Criar uma chave derivada de:

```text
data_contabil
descricao_normalizada
valor_centavos
conta_origem
```

Essa impressão detecta a mesma transação em arquivos diferentes. Descrição normalizada significa caixa uniforme, espaços compactados e remoção apenas de ruídos conhecidos. Não remover informação de parcela antes da análise.

### Similaridade

Itens próximos, mas não idênticos, devem ser marcados como `POSSIVEL_DUPLICIDADE`, nunca bloqueados automaticamente. Exemplo: mesma descrição e valor com diferença de um dia.

## Recorrências

Uma recorrência é uma hipótese, não uma classificação imediata.

Pontuação inicial sugerida:

- descrição normalizada compatível;
- valor igual ou com variação dentro de tolerância configurável;
- ocorrência em pelo menos três meses distintos;
- intervalo mensal consistente;
- categoria anteriormente confirmada.

Faixas:

- baixa confiança: apenas registrar evidência;
- média confiança: mostrar sugestão;
- alta confiança: pré-selecionar a sugestão, ainda exigindo confirmação.

Após confirmações repetidas, criar uma `regra_classificacao` local. A regra pode aplicar a classificação automaticamente em novas pré-visualizações, mas deve continuar visível e reversível.

## Parcelas

Quando a descrição trouxer padrões como `2/10`, `PARC 02/10` ou equivalente do provedor:

- preservar a descrição original;
- extrair `parcela_atual` e `total_parcelas`;
- criar ou localizar um `grupo_parcelamento`;
- estimar valor total como `valor_parcela * total_parcelas`;
- projetar meses futuros sem criar lançamentos realizados;
- permitir correção manual.

Inputs manuais também devem aceitar compra parcelada e criar o mesmo modelo de grupo.

## Consequências

- A importação se torna auditável e segura.
- O modelo de dados ganha sugestões, regras e grupos de parcelamento.
- O fluxo exige uma tela de revisão.
- A automação de e-mail reutiliza exatamente o mesmo pipeline.
