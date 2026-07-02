# Backup, versionamento e sincronizacao

## O que existe no MVP 11

A aplicacao possui uma tela `Backups` no menu lateral.

Ela permite:

- ver o caminho do banco SQLite local, tamanho, data de atualizacao e versao do schema;
- exportar um arquivo `.ctbackup` criptografado por senha;
- restaurar um `.ctbackup` validando formato, checksum e versao do banco;
- criar automaticamente um snapshot do banco atual antes de restaurar.

O arquivo `.ctbackup` e o unico formato recomendado para mover dados entre computadores. Ele contem um snapshot SQLite consistente gerado por `VACUUM INTO`, um manifesto tecnico e criptografia local com AES-GCM.

## Exportar

1. Abra `http://localhost:17321`.
2. Entre em `Backups`.
3. Informe uma senha com pelo menos 8 caracteres.
4. Clique em `Baixar backup criptografado`.
5. Guarde o arquivo `.ctbackup` e a senha em locais separados.

Sem a senha, o backup nao pode ser restaurado.

## Restaurar no mesmo computador

1. Entre em `Backups`.
2. Selecione um arquivo `.ctbackup`.
3. Informe a senha usada na exportacao.
4. Clique em `Restaurar backup`.
5. Confirme a acao.

Antes da restauracao, a aplicacao cria uma copia automatica do estado atual em `backups-automaticos`, ao lado do banco SQLite.

## Migrar para outro computador Windows

No computador antigo:

1. Execute a aplicacao normalmente.
2. Exporte um `.ctbackup` pela tela `Backups`.
3. Copie o `.ctbackup` para um pendrive, pasta segura ou repositorio privado de backups.
4. Nao copie `contas-termometro.db` aberto para Git ou nuvem.

No computador novo:

1. Clone o projeto.
2. Instale o JDK 21+.
3. Execute:

```powershell
.\gradlew.bat bootRun
```

4. Abra `http://localhost:17321`.
5. Entre em `Backups`.
6. Restaure o arquivo `.ctbackup` usando a senha.

Se a versao do schema do backup for diferente da versao da aplicacao, a restauracao e bloqueada. Atualize o projeto no computador novo para a mesma versao antes de tentar novamente.

## GitHub privado

GitHub pode ser usado somente como destino de arquivos ja criptografados.

Pode versionar:

```powershell
git add contas-termometro-20260702-130000.ctbackup
```

Nunca versionar:

```powershell
git add contas-termometro.db
git add extrato.csv
git add backup.json
```

Use um repositorio privado separado do codigo, com MFA habilitado. Mesmo privado, o GitHub nao deve receber banco aberto, CSV, JSON financeiro ou chave/senha.

## Retencao sugerida

Para uso pessoal:

- manter os ultimos 7 backups diarios;
- manter 1 backup por mes dos ultimos 12 meses;
- manter 1 backup anual;
- testar restauracao depois de grandes alteracoes.

## Sincronizacao

Este MVP entrega portabilidade e backup. Ele nao implementa edicao simultanea em dois computadores.

Firebase/Firestore continua adiado ate existir necessidade real de sincronizacao automatica entre dispositivos. Se entrar no futuro, deve ser modelado por entidades e regras de seguranca, nao como upload aberto do SQLite.
