# ADR 0005 - SQLite como persistencia inicial

## Status

Aceita para o MVP.

## Contexto

O projeto precisa ser simples de rodar localmente, sem custo e sem depender de Docker ou cloud para o uso principal.

## Decisao

Usar SQLite como banco local inicial.

O arquivo real do banco deve ficar em `data/contas-termometro.db`, fora do Git.

## Alternativas consideradas

- PostgreSQL local: mais parecido com backend web, mas exige Docker ou instalacao local.
- Firebase Firestore: free tier, mas cria dependencia cloud e muda o modelo de dominio.
- Arquivos JSON/CSV: simples, mas pior para consultas, consistencia e evolucao.

## Consequencias

- O MVP fica facil de executar em qualquer maquina.
- Backups sao simples.
- O projeto continua podendo migrar para PostgreSQL no futuro se virar multiusuario.
- Sera necessario escolher uma dependencia JDBC/Flyway compativel com SQLite quando a implementacao comecar.

