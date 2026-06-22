# ADR 0010 - Backup criptografado e sincronização opcional

## Status

Proposta.

## Contexto

O usuário quer abrir os dados em outro computador e manter histórico sem publicar informações financeiras. GitHub e Firebase atendem problemas diferentes:

- GitHub versiona arquivos;
- Firestore sincroniza registros;
- SQLite mantém o modo local simples.

Um repositório privado não torna dados financeiros adequados para commit. Credenciais comprometidas, colaboradores, logs ou configuração incorreta ainda podem expor o histórico.

## Decisão recomendada

Manter SQLite como fonte principal e implementar primeiro exportação criptografada.

Ordem:

1. banco SQLite local;
2. exportação consistente e versionada do formato de backup;
3. criptografia no computador antes de qualquer upload;
4. destino configurável: pasta, GitHub privado ou outro armazenamento;
5. Firebase somente quando sincronização de registros entre dispositivos se tornar requisito real.

## Alternativa A - GitHub privado com backup criptografado

Permitido somente para arquivos já criptografados.

Fluxo:

```text
SQLite
  |
  v
exportação/backup consistente
  |
  v
compactação
  |
  v
criptografia local
  |
  v
arquivo .age no repositório privado
```

Regras:

- usar ferramenta moderna como `age`;
- chave ou senha fica em gerenciador de senhas e nunca no repositório;
- versionar apenas `.age`, checksum e manifesto sem valores financeiros;
- nunca versionar `.db`, `.json`, `.csv` ou logs abertos;
- usar repositório privado separado do código;
- testar restauração;
- manter política de retenção para o repositório não crescer indefinidamente;
- proteger a conta GitHub com MFA.

O GitHub vê nome, tamanho, data e frequência dos arquivos, mas não o conteúdo criptografado. Uma senha fraca ainda permite ataque offline.

### Formato sugerido

```text
backups/
  2026/
    contas-2026-06-22T180000Z.db.zip.age
manifest.json
```

O manifesto contém versão do formato, checksum do arquivo criptografado e data. Não contém saldo, descrição ou categoria.

## Alternativa B - Firebase Firestore

Firestore pode armazenar cada lançamento como documento e sincronizar dispositivos. O Spark plan possui faixa gratuita adequada para um usuário pessoal, mas exige:

- Firebase Authentication;
- regras de segurança restritas ao UID;
- modelo de sincronização e resolução de conflitos;
- identificadores estáveis;
- exclusões e atualizações idempotentes;
- migração entre SQLite e documentos;
- monitoramento de cotas.

As cotas gratuitas atuais incluem 1 GiB armazenado, 50 mil leituras/dia, 20 mil escritas/dia, 20 mil exclusões/dia e 10 GiB/mês de saída. Backup gerenciado, restauração e PITR não estão incluídos gratuitamente.

Firestore não deve receber uma cópia aberta do arquivo SQLite. Se usado, deve ser uma persistência/sincronização por entidades. Para backup de blob criptografado, GitHub privado ou armazenamento de arquivos é arquiteturalmente mais simples.

## Segurança no Firebase

Estrutura conceitual:

```text
usuarios/{uid}/lancamentos/{id}
usuarios/{uid}/metas/{id}
usuarios/{uid}/parcelamentos/{id}
```

Regra mínima:

```text
request.auth != null
request.auth.uid == uid
```

As regras devem ser testadas no Emulator Suite. Chaves de serviço administrativo nunca entram no frontend ou Git.

Firebase cifra dados em trânsito e em repouso na infraestrutura, mas o projeto e as credenciais autorizadas conseguem ler os documentos. Para reduzir confiança no provedor, seria necessária criptografia de campos no cliente, aumentando bastante a complexidade de consultas.

## Escolha

Para o estágio atual:

- GitHub privado + backup criptografado é recomendado para histórico e portabilidade;
- Firebase é adiado até existir necessidade de sincronização automática entre computadores;
- nenhuma alternativa substitui teste de restauração e uma segunda cópia da chave.

## Referências

- [GitHub - remoção de dados sensíveis](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository)
- [age - criptografia de arquivos](https://age-encryption.org/)
- [Firebase - preços](https://firebase.google.com/pricing)
- [Firestore - cotas gratuitas](https://firebase.google.com/docs/firestore/quotas)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)
