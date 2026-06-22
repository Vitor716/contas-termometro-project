# ADR 0004 - Privacidade e GitHub

## Status

Aceita.

## Contexto

O projeto lida com informacoes financeiras pessoais. O repositorio sera usado no GitHub para versionar codigo e documentacao, mas isso nao pode vazar dados reais.

## Decisao

GitHub não será usado como banco de dados.

Dados reais abertos devem ficar em arquivo local ignorado pelo Git. O repositório de código pode conter somente estrutura, migrations, exemplos anônimos e testes.

A ADR 0010 permite usar um repositório privado separado como destino de snapshots, desde que:

- o conteúdo seja criptografado localmente antes do `git add`;
- a chave nunca seja versionada;
- nenhum arquivo aberto temporário entre no histórico;
- Git continue sendo transporte/versionamento de backup, não banco operacional.

## Consequencias

- O projeto funciona sem cloud.
- O risco de vazamento por commit acidental diminui.
- Abrir o projeto em outro computador exige restaurar um backup local ou criar novos dados.
- Sincronização entre máquinas será tratada como funcionalidade futura, não como requisito do MVP.
