# Setup local no Windows

## Objetivo

Rodar o projeto e visualizar os dados com o mínimo de configuração. O modo principal usa Spring Boot e SQLite local, sem Docker e sem conta em cloud.

## Pré-requisitos

- Windows 10 ou 11;
- Git;
- JDK 21;
- PowerShell;
- navegador moderno.

Não é necessário instalar Gradle: o repositório inclui Gradle Wrapper.

## Primeiro uso

No PowerShell:

```powershell
git clone <url-do-repositorio>
cd contas-termometro-project
.\gradlew.bat bootRun
```

Abrir:

```text
http://localhost:17321
```

Verificar a API:

```powershell
Invoke-RestMethod http://localhost:17321/api/sistema/saude
```

## Banco de dados

### Estado atual

A configuração atual aponta para:

```text
contas-termometro.db
```

na raiz do projeto. O `.gitignore` já exclui arquivos `.db`.

### Estado-alvo

Alinhar a implementação com a ADR 0005:

```text
data/
  contas-termometro.db
  backups/
```

Depois dessa adequação, na primeira execução:

1. a aplicação cria `data/` se necessário;
2. o driver SQLite cria o arquivo;
3. Flyway executa migrations pendentes;
4. a aplicação sobe somente se o schema estiver válido.

O caminho deve poder ser alterado por variável:

```powershell
$env:CONTAS_DB_PATH="D:\ContasTermometro\data\contas-termometro.db"
.\gradlew.bat bootRun
```

O valor padrão deve funcionar sem configuração. Enquanto essa tarefa não for concluída, usar o arquivo criado na raiz.

## Visualizar o banco

Opção recomendada para desenvolvimento: SQLiteStudio ou extensão SQLite do editor.

Com SQLite CLI:

```powershell
sqlite3 .\contas-termometro.db
```

Comandos úteis:

```sql
.tables
.schema lancamentos
SELECT * FROM lancamentos ORDER BY id DESC LIMIT 20;
```

Não editar o banco manualmente durante uso normal. A interface e as migrations são as fontes de alteração.

## Backup

Com a aplicação fechada no estado atual:

```powershell
New-Item -ItemType Directory -Force .\backups | Out-Null
Copy-Item .\contas-termometro.db .\backups\contas-termometro-$(Get-Date -Format yyyyMMdd-HHmmss).db
```

Melhoria recomendada: comando de backup da própria aplicação usando a API de backup do SQLite, para permitir cópia consistente mesmo com o sistema aberto.

## Abrir em outro computador

1. instalar Git e JDK 21;
2. clonar o repositório;
3. copiar o backup para `contas-termometro.db` enquanto o estado-alvo não estiver implementado;
4. executar `.\gradlew.bat bootRun`;
5. conferir o endpoint de saúde e o mês mais recente.

O Git transfere o código. O arquivo de backup transfere os dados.

## Portabilidade futura

Criar no frontend:

- `Exportar backup`;
- `Importar backup`;
- exibição do local atual do banco;
- data do último backup;
- validação de versão antes da restauração.

Uma restauração deve criar backup de segurança do banco atual antes de substituí-lo.

## Configuração opcional de IA

O sistema deve funcionar sem IA. Quando ativada, a integração pode ser local ou remota.

### Ollama local

Instalar Ollama no Windows e baixar um modelo compatível com a máquina.

Verificar:

```powershell
Invoke-RestMethod http://localhost:11434/api/tags
```

Configuração sugerida:

```powershell
$env:CONTAS_IA_ENABLED="true"
$env:CONTAS_IA_BASE_URL="http://localhost:11434"
$env:CONTAS_IA_MODEL="<modelo-local>"
.\gradlew.bat bootRun
```

Se o Ollama não estiver instalado, o consultor deve continuar usando explicações determinísticas.

### Gemini free tier

Pode ser usado em computadores sem recursos para modelos locais:

```powershell
$env:CONTAS_IA_ENABLED="true"
$env:CONTAS_IA_PROVIDER="gemini"
$env:GEMINI_API_KEY="<chave-fora-do-git>"
$env:CONTAS_IA_MODEL="<modelo-com-free-tier>"
```

Enviar somente DTO anonimizado. Os limites e modelos gratuitos podem mudar e devem ser verificados antes da implementação.

### GitHub Models

Útil para desenvolvimento e teste de prompts:

```powershell
$env:CONTAS_IA_PROVIDER="github-models"
$env:GITHUB_MODELS_TOKEN="<token-com-models-read>"
```

O uso gratuito possui limites e não deve ser dependência obrigatória.

## Configuração opcional do Gmail

Requisitos:

1. criar projeto no Google Cloud;
2. habilitar Gmail API;
3. criar cliente OAuth do tipo aplicativo desktop;
4. salvar credenciais fora do Git;
5. autorizar o escopo mínimo de leitura;
6. armazenar refresh token fora do repositório.

Variáveis conceituais:

```powershell
$env:CONTAS_EMAIL_ENABLED="true"
$env:CONTAS_EMAIL_PROVIDER="gmail"
$env:CONTAS_EMAIL_CREDENTIALS_PATH="$HOME\.contas-termometro\gmail-credentials.json"
$env:CONTAS_EMAIL_TOKEN_PATH="$HOME\.contas-termometro\gmail-token.json"
```

O primeiro acesso abre o navegador para consentimento. As execuções seguintes usam o token salvo.

## Problemas comuns

### Porta ocupada

```powershell
$env:SERVER_PORT="8090"
.\gradlew.bat bootRun
```

### Java incorreto

```powershell
java -version
.\gradlew.bat --version
```

Ambos devem apontar para Java 21.

### Banco bloqueado

SQLite atende ao uso local, mas deve trabalhar com pool pequeno. Fechar ferramentas externas que mantêm transação aberta.

### Banco não deve aparecer no Git

```powershell
git status
```

Arquivos `.db`, `.sqlite`, tokens, CSVs reais e backups devem continuar ignorados.

Backups destinados ao GitHub devem ser criptografados antes de entrar em qualquer repositório. Consulte `docs/backup-e-sincronizacao.md`.
