# ADR 0004 - Privacidade e GitHub

## Status

Aceita.

## Contexto

O projeto lida com informacoes financeiras pessoais. O repositorio sera usado no GitHub para versionar codigo e documentacao, mas isso nao pode vazar dados reais.

## Decisao

GitHub nao sera usado como banco de dados.

Dados reais devem ficar em arquivo local ignorado pelo Git. O repositorio pode conter somente estrutura, migrations, exemplos anonimos e testes.

## Consequencias

- O projeto funciona sem cloud.
- O risco de vazamento por commit acidental diminui.
- Abrir o projeto em outro computador exige restaurar um backup local ou criar novos dados.
- Sincronizacao entre maquinas sera tratada como funcionalidade futura, nao como requisito do MVP.

