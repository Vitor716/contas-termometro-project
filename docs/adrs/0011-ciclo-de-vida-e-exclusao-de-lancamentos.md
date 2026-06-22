# ADR 0011 - Ciclo de vida e exclusão de lançamentos

## Status

Proposta.

## Contexto

A tabela `lancamentos` possui o campo `status` com `ATIVO` e `CANCELADO`, mas o fluxo atual:

- lista registros sem filtrar o status;
- exclui fisicamente com `deleteById`;
- exclui fisicamente lançamentos de um lote;
- não registra motivo, data ou origem do cancelamento.

Exclusão física simplifica o CRUD, porém reduz auditabilidade e dificulta entender alterações em resumos históricos. Manter registros cancelados exige que todas as consultas financeiras filtrem corretamente.

## Decisão proposta

Usar cancelamento lógico como operação normal para lançamentos confirmados.

### Estados

- `ATIVO`: participa dos cálculos;
- `CANCELADO`: preservado para auditoria e não participa dos cálculos.

### Exclusão física

Permitida apenas para:

- dados ainda não confirmados em pré-visualização;
- fixtures e testes;
- manutenção administrativa explícita;
- rollback transacional de uma operação que não foi concluída.

Lançamentos confirmados por importação também devem ser cancelados, não apagados silenciosamente.

## Campos necessários

```text
status
cancelado_em
motivo_cancelamento
cancelado_por
```

Para o usuário local, `cancelado_por` pode inicialmente ser `USUARIO_LOCAL`, `IMPORTACAO` ou `SISTEMA`.

## Contrato da API

Opção recomendada:

```text
DELETE /api/lancamentos/{id}
```

mantém o contrato externo, mas internamente altera o status. A resposta continua `204`.

Reativação futura:

```text
POST /api/lancamentos/{id}/reativar
```

## Consultas

- listagens e cálculos usam somente `ATIVO` por padrão;
- histórico pode aceitar `status=TODOS`;
- consultas por lote devem declarar se incluem cancelados;
- índices devem considerar `mes_referencia + status`.

## Consequências

- Resumos históricos ficam auditáveis.
- Todas as consultas precisam filtrar status.
- O banco cresce, mas o volume pessoal é pequeno.
- A interface pode oferecer lixeira ou histórico futuramente.
