# Backup, versionamento e sincronização

## Três necessidades diferentes

### Backup

Recuperar os dados depois de perda ou corrupção.

### Versionamento

Recuperar estados anteriores e saber quando um snapshot foi criado.

### Sincronização

Editar em mais de um computador e propagar mudanças automaticamente.

Não tratar os três como o mesmo problema.

## Recomendação atual

Usar SQLite local e gerar backups criptografados. Versionar esses backups em um repositório GitHub privado separado é aceitável se a criptografia acontecer antes do `git add`.

Firebase deve entrar apenas quando a sincronização automática justificar autenticação, regras, conflitos e nova persistência.

## GitHub sem vazamento de conteúdo

### O que nunca fazer

```powershell
git add contas-termometro.db
git add exportacao.csv
git add backup.json
```

Mesmo em repositório privado, o histórico pode permanecer após remoção comum.

### Fluxo seguro

1. fechar a aplicação ou criar snapshot consistente;
2. copiar o banco para uma pasta temporária;
3. compactar;
4. criptografar localmente;
5. verificar que o arquivo não pode ser aberto sem chave;
6. adicionar somente o arquivo criptografado;
7. enviar para repositório privado de backups;
8. restaurar um backup de teste periodicamente.

Exemplo conceitual com `age` usando senha:

```powershell
Compress-Archive .\contas-termometro.db .\contas-termometro.zip
age -p -o .\contas-termometro.zip.age .\contas-termometro.zip
Remove-Item .\contas-termometro.zip
```

Preferir chave assimétrica:

```powershell
age-keygen -o "$HOME\.contas-termometro\backup-key.txt"
age-keygen -y "$HOME\.contas-termometro\backup-key.txt"
age -r <CHAVE_PUBLICA> -o backup.db.age contas-termometro.db
```

A chave privada deve ter uma segunda cópia em gerenciador de senhas ou mídia segura. Perder a chave significa perder os backups.

### Repositório de backup

- privado;
- separado do código;
- sem GitHub Actions que processem o conteúdo;
- MFA habilitado;
- branch única;
- retenção mensal/anual;
- arquivos abertos bloqueados por `.gitignore` e hook de pre-commit.

### Limitação

Criptografia gera arquivos binários diferentes em cada execução. O Git não consegue produzir diffs úteis e o repositório cresce. Para um projeto pessoal, manter snapshots mensais e os últimos backups diários é mais adequado que commit a cada alteração.

## Firebase Firestore

### Quando faz sentido

- uso frequente em mais de um computador;
- necessidade de sincronização automática;
- login do usuário;
- possível evolução para mobile;
- dados representados como documentos, não como arquivo SQLite.

### Modelo

Cada entidade recebe:

```text
id global
usuarioId
criadoEm
atualizadoEm
versao
excluidoEm
```

O backend sincroniza mudanças e trata conflitos. Uma estratégia simples é bloquear edição concorrente usando `versao`, em vez de aceitar silenciosamente “última gravação vence”.

### Segurança mínima

- Firebase Authentication;
- documentos sempre sob o UID;
- Security Rules testadas;
- nenhuma credencial administrativa no navegador;
- App Check como proteção adicional, não substituto da autenticação;
- exportação local periódica, porque o free tier não inclui backup/restauração gerenciados.

### Privacidade

Firestore é cloud. Mesmo com regras corretas, os dados deixam o computador. Para um sistema financeiro pessoal, isso precisa ser uma opção explícita.

## Comparação

| Critério | SQLite local | GitHub + arquivo criptografado | Firestore |
|---|---|---|---|
| Custo obrigatório | Não | Não | Não dentro da cota atual |
| Funciona offline | Sim | Uso local sim | Cache possível, sincronização exige internet |
| Histórico | Backup manual | Sim, por snapshot | Precisa ser implementado |
| Sincronização | Não | Manual | Sim |
| Complexidade | Baixa | Média | Alta |
| Dados em cloud | Não | Somente cifrados | Sim |
| Consultas remotas | Não | Não | Sim |
| Recuperação | Copiar banco | Descriptografar snapshot | Exportação própria no free tier |

## Fases sugeridas

1. exportar/importar backup local;
2. criptografar/descriptografar;
3. publicar backup criptografado no GitHub;
4. automatizar retenção e teste de restauração;
5. reavaliar Firebase quando houver segundo dispositivo em uso recorrente.
