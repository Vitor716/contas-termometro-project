# ADR 0014 - Recorrencia de lancamentos

## Status

Proposta.

## Contexto

Entradas fixas e saidas fixas normalmente se repetem todos os meses. Exemplos:

- salario;
- aluguel;
- internet;
- mensalidade;
- assinatura;
- parcelas recorrentes sem fim definido.

Cadastrar esses valores manualmente em todo mes gera trabalho repetitivo e aumenta o risco de esquecer uma conta importante. Ao mesmo tempo, copiar lancamentos futuros de forma definitiva pode distorcer historico, dificultar edicao e criar duplicidades.

## Decisao proposta

O sistema deve permitir marcar um lancamento como recorrente.

Uma recorrencia representa uma serie. A serie define o padrao que vale para meses futuros ate ser alterada, encerrada ou cancelada.

O lancamento mensal pode ser:

- criado manualmente, sem recorrencia;
- criado a partir de uma recorrencia;
- editado como excecao apenas de um mes;
- editado como nova regra para este mes e meses seguintes.

## Tipos elegiveis

No MVP de recorrencia, aceitar:

- `ENTRADA`;
- `SAIDA_FIXA`.

Nao aceitar inicialmente:

- `GASTO_DIARIO`, porque e variavel;
- `INVESTIMENTO`, porque pode depender da meta do mes;
- `AJUSTE_SALDO`, porque deve representar reconciliacao pontual.

Essa restricao pode mudar no futuro com nova ADR.

## Regra de vigencia

Uma recorrencia deve ter:

```text
data_inicio
mes_inicio
mes_fim opcional
frequencia
dia_preferencial
status
```

Regra inicial:

- frequencia mensal;
- vale a partir de `mes_inicio`;
- se `mes_fim` estiver vazio, continua indefinidamente;
- ao encerrar, nao deve apagar lancamentos ja realizados;
- ao alterar, a mudanca vale apenas para meses futuros, exceto quando o usuario escolher explicitamente alterar tambem o mes atual.

## Materializacao

Para evitar criar anos de dados desnecessarios, o backend pode materializar lancamentos recorrentes sob demanda.

Opcoes aceitas:

1. materializar quando um mes for aberto;
2. materializar por comando explicito;
3. manter como projecao para meses futuros e gravar somente quando o mes for confirmado.

Decisao recomendada para o MVP:

- ao consultar um mes, garantir que as recorrencias vigentes daquele mes existam como lancamentos;
- cada ocorrencia gerada deve guardar referencia para a recorrencia;
- a geracao deve ser idempotente, usando chave unica por recorrencia e mes.

## Edicao

Ao editar lancamento recorrente, a interface deve perguntar o escopo:

- `Somente este mes`: altera apenas a ocorrencia selecionada e marca como excecao;
- `Este e proximos meses`: cria uma nova versao da recorrencia a partir do mes selecionado;
- `Toda a serie`: permitido apenas se nao alterar historico ja fechado ou se o usuario confirmar impacto retroativo.

Regra padrao: edicoes nao devem alterar meses anteriores silenciosamente.

## Exclusao e encerramento

Ao excluir uma ocorrencia recorrente, a interface deve perguntar:

- `Remover somente este mes`;
- `Encerrar recorrencia a partir deste mes`;
- `Excluir toda a serie`, apenas se for seguro e confirmado.

Encerrar uma recorrencia deve preencher `mes_fim` ou mudar o status, preservando lancamentos ja existentes.

## Duplicidade

A recorrencia nao pode gerar duas ocorrencias iguais no mesmo mes.

Chave sugerida:

```text
recorrencia_id + mes_referencia
```

Importacoes e lancamentos manuais devem alertar quando forem similares a uma ocorrencia recorrente ja prevista.

## Consequencias

- O usuario cadastra contas fixas uma vez e o sistema carrega os meses futuros.
- Projecoes ficam mais confiaveis para o consultor financeiro.
- O modelo precisa diferenciar regra recorrente, ocorrencia mensal e excecao.
- Exclusao fisica de lancamentos fica ainda menos adequada; a ADR 0011 deve ser considerada antes da implementacao.

## Testes obrigatorios

- cria recorrencia mensal sem fim;
- abre mes futuro e gera uma unica ocorrencia;
- reabrir o mesmo mes nao duplica lancamento;
- editar somente um mes cria excecao;
- editar este e proximos meses preserva meses anteriores;
- encerrar recorrencia impede geracao posterior;
- recorrencia de `GASTO_DIARIO`, `INVESTIMENTO` e `AJUSTE_SALDO` e rejeitada no MVP.
