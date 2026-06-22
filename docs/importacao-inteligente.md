# Importação inteligente

## Objetivo

Transformar CSVs em lançamentos confiáveis, mantendo o usuário no controle. A importação automática de arquivo e a classificação inteligente são entregas diferentes.

## Pipeline

```text
arquivo manual ou anexo de e-mail
    |
    v
validar origem, extensão, tamanho e hash
    |
    v
parser específico do provedor
    |
    v
normalizar sem perder o dado bruto
    |
    v
detectar parcelas
    |
    v
detectar duplicidades
    |
    v
buscar recorrências e regras aprendidas
    |
    v
gerar sugestões com evidências
    |
    v
pré-visualização
    |
    v
confirmação do usuário
    |
    v
persistência transacional
```

## Estados de uma linha

- `NOVA`;
- `SUGESTAO_DISPONIVEL`;
- `DUPLICADA`;
- `POSSIVEL_DUPLICIDADE`;
- `INVALIDA`;
- `CONFIRMADA`;
- `IGNORADA`.

## Sugestões

Uma sugestão deve informar:

- tipo e categoria sugeridos;
- confiança de `0` a `1`;
- evidências usadas;
- regra que originou a sugestão;
- valor anterior e valor sugerido;
- se será criada uma regra para próximas importações.

Exemplo:

```json
{
  "tipoSugerido": "SAIDA_FIXA",
  "categoriaSugerida": "Assinaturas",
  "confianca": 0.91,
  "evidencias": [
    "descrição semelhante em 4 meses",
    "variação de valor inferior a 2%",
    "classificação confirmada 3 vezes"
  ]
}
```

## Aprendizado local

Não é necessário machine learning no primeiro corte. Regras determinísticas confirmadas resolvem grande parte do problema:

```text
se descricao_normalizada corresponde a "NETFLIX*"
então tipo = SAIDA_FIXA
e categoria = ASSINATURAS
```

Cada regra deve possuir:

- origem manual ou aprendida;
- padrão;
- classificação resultante;
- número de confirmações;
- última utilização;
- ativo/inativo;
- prioridade.

Conflitos entre regras devem gerar revisão.

## Parcelas

Criar uma entidade conceitual `GrupoParcelamento`:

```text
id
descricao_base
valor_parcela
total_parcelas
primeira_parcela
ultima_parcela
origem
status
```

Cada lançamento vinculado informa:

```text
grupo_parcelamento_id
parcela_atual
total_parcelas
```

Projeções futuras não são lançamentos realizados. Elas ficam separadas para evitar inflar o resumo do mês.

### Aba de parcelas

Mostrar:

- compras parceladas ativas;
- parcela atual e total;
- valor mensal;
- saldo restante estimado;
- mês da última parcela;
- parcelas futuras por mês;
- total da renda já comprometida;
- alerta quando novas parcelas ultrapassarem limites do consultor.

## Recorrências

Agrupar candidatos por descrição normalizada e comparar meses distintos.

Tolerâncias iniciais configuráveis:

- pelo menos três ocorrências;
- intervalo aproximado de um mês;
- diferença de valor percentual;
- diferença máxima de dias no vencimento.

Não considerar compras parceladas como recorrências fixas apenas porque aparecem em meses sucessivos.

## Duplicidades

Bloqueios fortes:

- hash de arquivo já processado;
- mesma mensagem e anexo;
- mesmo hash de linha dentro da mesma origem;
- impressão digital exata do lançamento no mesmo mês.

Alertas:

- data próxima, descrição semelhante e mesmo valor;
- lançamento manual equivalente a uma linha importada;
- reprocessamento após edição do arquivo.

Toda duplicidade ignorada ou aceita deve ficar auditável.

## Confirmação

A API ideal separa:

```text
POST /api/importacoes/pre-visualizacoes
PUT  /api/importacoes/pre-visualizacoes/{id}/linhas/{linhaId}
POST /api/importacoes/pre-visualizacoes/{id}/confirmar
GET  /api/importacoes/{id}
```

A confirmação deve ser transacional e idempotente.

## Captura por e-mail

O conector de e-mail apenas entrega arquivos ao pipeline. Ele não conhece regras Nubank.

Metadados mínimos:

```text
provedor_email
message_id
attachment_id
remetente
assunto
recebido_em
nome_arquivo
hash_arquivo
```

No Gmail, consultar mensagens usando filtros restritos e baixar o attachment pela API. Registrar o `message_id` antes de processar para impedir repetição.
