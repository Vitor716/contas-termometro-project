# Modelo de Dados Local

## Decisao inicial

Usar SQLite local para os dados reais do usuario no MVP.

Motivo:

- roda sem Docker;
- nao exige cloud;
- facilita backup manual;
- reduz risco de expor dados financeiros no GitHub;
- atende bem um usuario local no inicio.

## Arquivos locais

Sugestao:

```text
data/
  contas-termometro.db
  backups/
```

`data/` deve ficar no `.gitignore`.

## O que pode ir para o GitHub

- Codigo.
- Documentacao.
- Migrations.
- Arquivos `.example`.
- Seeds anonimos.
- Tests fixtures sem dados reais.

## O que nao pode ir para o GitHub

- Banco SQLite real.
- Exportacao da planilha real.
- Prints com valores reais.
- `.env`.
- Backups.
- Logs com payload financeiro.

## Tabelas iniciais sugeridas

### `lancamentos`

Livro principal de lancamentos.

```text
id
tipo
descricao
valor
data
mes_referencia
categoria
observacao
recorrencia_id
recorrencia_excecao
grupo_parcelamento_id
parcela_atual
total_parcelas
origem
fingerprint
criado_em
atualizado_em
```

Tipos:

- `ENTRADA`
- `SAIDA_FIXA`
- `GASTO_DIARIO`
- `INVESTIMENTO`
- `AJUSTE_SALDO`

Campos de recorrencia:

- `recorrencia_id`: referencia opcional para a serie que gerou o lancamento.
- `recorrencia_excecao`: indica que a ocorrencia foi alterada apenas naquele mes e nao deve ser sobrescrita por atualizacoes futuras da serie.

### `recorrencias_lancamento`

Representa uma regra recorrente de entrada ou saida fixa.

```text
id
tipo
descricao
valor
categoria
observacao
mes_inicio
mes_fim
dia_preferencial
frequencia
status
origem
criado_em
atualizado_em
```

Tipos permitidos no MVP:

- `ENTRADA`
- `SAIDA_FIXA`

Frequencias permitidas no MVP:

- `MENSAL`

Status sugeridos:

- `ATIVA`
- `ENCERRADA`
- `CANCELADA`

Regras:

- uma recorrencia ativa sem `mes_fim` vale indefinidamente;
- `mes_fim` encerra a geracao a partir dos meses posteriores;
- alteracoes em meses futuros devem criar nova versao ou atualizar a serie preservando historico anterior;
- uma ocorrencia mensal deve ser unica por `recorrencia_id + mes_referencia`.

### `metas_mensais`

Metas configuraveis por mes.

```text
id
mes_referencia
percentual_meta_investimento
orcamento_diario_minimo
criado_em
atualizado_em
```

### `simulacoes_compra`

Historico opcional de simulacoes.

```text
id
mes_referencia
descricao
valor
forma_pagamento
parcelas
decisao
motivo
criado_em
```

### `grupos_parcelamento`

Representa uma compra parcelada e suas projeções.

```text
id
descricao_base
valor_parcela_centavos
total_parcelas
primeira_parcela
ultima_parcela
origem
status
criado_em
atualizado_em
```

### `pre_visualizacoes_importacao`

```text
id
provedor
origem_arquivo
hash_arquivo
message_id
attachment_id
status
criado_em
confirmado_em
```

### `linhas_importacao`

Mantém dado bruto, normalizado e sugestões até a confirmação.

```text
id
pre_visualizacao_id
numero_linha
conteudo_bruto
descricao_normalizada
valor_centavos
data
hash_linha
fingerprint_lancamento
status
tipo_sugerido
categoria_sugerida
confianca
evidencias_json
```

### `regras_classificacao`

```text
id
padrao
tipo_resultante
categoria_resultante
origem
confirmacoes
prioridade
ativo
ultima_utilizacao
```

### `configuracoes_financeiras`

```text
id
percentual_meta_investimento
reserva_minima_centavos
orcamento_diario_minimo_centavos
percentual_maximo_comprometimento
margem_seguranca_percentual
estrategia
```

## Índices e unicidade futuros

- índice único em `hash_arquivo` por provedor;
- índice único em `pre_visualizacao_id + hash_linha`;
- índice de busca em `mes_referencia + fingerprint`;
- índice em `grupo_parcelamento_id`;
- indice unico em `recorrencia_id + mes_referencia` quando `recorrencia_id` nao for nulo;
- indice em `mes_inicio + mes_fim + status` para localizar recorrencias vigentes;
- regras não devem sobrescrever histórico sem auditoria.

## Sobre usar Git como banco

Nao usar GitHub como banco dos dados financeiros reais.

Git e bom para versionar texto e codigo. Para dados financeiros pessoais, ele cria riscos:

- historico e dificil de apagar completamente;
- um commit errado pode expor anos de informacao;
- conflitos de merge em dados sao ruins;
- repositorios remotos mudam o modelo de privacidade.

Se quiser versionar dados futuramente, considerar uma exportacao criptografada local e manual, nunca dados abertos.
