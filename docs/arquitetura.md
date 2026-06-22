# Arquitetura

## Estilo

O projeto comeca como monolito modular. A decisao reduz custo operacional e evita separar servicos antes de haver maturidade de dominio.

## Boundaries

- `identidade`: autenticacao, dono dos dados e integracao futura com Firebase Auth.
- `lancamentos`: livro de lancamentos financeiros. Sera o modulo central do MVP.
- `importacao`: captura manual ou por e-mail, parsing, pré-visualização, deduplicação e confirmação.
- `configuracao`: preferencias, categorias e metas.
- `orcamento`: cálculos diários, status do termômetro, projeções e resumos mensais/anuais.
- `consultor`: motor determinístico de simulações e recomendações.
- `parcelas`: grupos de parcelamento e compromissos futuros, podendo começar dentro de `lancamentos` até o domínio justificar módulo próprio.
- `integracoes`: adaptadores opcionais para Gmail e Ollama, sem regra financeira.
- `compartilhado`: tipos pequenos e estaveis, sem regra de negocio.

## Regra de dependencia

Controllers chamam services do proprio modulo. Modulos nao acessam entidades internas de outros modulos.

Quando um modulo precisar reagir a outro, comece com chamada de aplicacao simples dentro do monolito. Pub/Sub, filas ou eventos externos ficam para quando houver necessidade operacional clara.

Integrações externas devem depender de portas definidas pelo núcleo. O domínio não conhece Gmail, Ollama ou formato CSV Nubank.

## Persistencia

O MVP deve usar SQLite local, com o arquivo real em `data/contas-termometro.db`.

O diretorio `data/` nao deve ser versionado. Migrations, exemplos anonimos e fixtures de teste podem ficar no repositorio.

## Fluxo inicial

```text
POST /api/lancamentos
        |
        v
lancamentos salva lancamento local
        |
        v
GET /api/meses/{yyyy-MM}/resumo
        |
        v
orcamento calcula resumo mensal sob demanda
        |
        v
consultor usa o resumo para simular compras
```

## Fluxo futuro de importação

```text
upload manual ou conector de e-mail
        |
        v
importacao valida origem e hash
        |
        v
parser do provedor normaliza linhas
        |
        v
detectores geram parcelas, duplicidades e sugestões
        |
        v
usuario revisa
        |
        v
confirmacao grava lancamentos e regras aprendidas
```

## Motor de decisão

O `consultor` recebe dados consolidados de orçamento, metas e parcelas. Ele gera cenários determinísticos e explicáveis. Um adaptador de IA local pode explicar o resultado, mas não participa do cálculo nem altera a decisão.

O adaptador também pode usar uma API remota com faixa gratuita, desde que receba somente DTO anonimizado e possua fallback. O domínio não depende do provedor.

## Backup e sincronização

- SQLite permanece a fonte operacional.
- Backups podem ser enviados ao GitHub somente após criptografia local.
- Firestore não será tratado como backup de arquivo.
- Firestore pode ser adotado futuramente como sincronização por entidades, com autenticação, regras e resolução de conflitos.

## Decisao importante

Nao criar integracao cloud antes de ter o core funcionando localmente.

O projeto precisa primeiro provar que:

- os lancamentos representam bem a planilha;
- os resumos batem com os calculos atuais;
- o consultor explica decisoes de compra de forma util;
- os dados reais nao vazam para Git.

## Frontend

O frontend inicial deve ficar dentro do mesmo projeto Spring Boot, usando HTML, CSS e Alpine.js servido localmente.

Frameworks como Angular, Vue, React ou Next.js ficam fora do MVP. Eles podem ser revisitados se a interface crescer a ponto de exigir estado complexo, roteamento client-side ou componentes muito interativos.
